
/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:07
 *
 *  @author Florianpal.
 */

package fr.florianpal.fperk.gui.subGui;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.gui.MainGuiConfig;
import fr.florianpal.fperk.enums.ActionType;
import fr.florianpal.fperk.enums.StatusType;
import fr.florianpal.fperk.gui.AbstractGui;
import fr.florianpal.fperk.gui.GuiInterface;
import fr.florianpal.fperk.languages.MessageKeys;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.objects.gui.Barrier;
import fr.florianpal.fperk.utils.FormatUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.*;

public class MainGui extends AbstractGui implements GuiInterface {

    private final List<Perk> perks;

    private final List<PlayerPerk> playerPerks;

    private final MainGuiConfig mainGuiConfig;

    private final PlayerPerkCommandManager playerPerkCommandManager;

    public MainGui(FPerk plugin, List<Perk> perks, List<PlayerPerk> playerPerks, Player player, int page) {
        super(plugin, plugin.getConfigurationManager().getMainGuiConfig(), player, page);
        this.mainGuiConfig = plugin.getConfigurationManager().getMainGuiConfig();
        this.playerPerkCommandManager = plugin.getPlayerPerkCommandManager();
        this.perks = perks;
        this.playerPerks = playerPerks;

        String titleInv = mainGuiConfig.getNameGui();
        titleInv = titleInv.replace("{Page}", String.valueOf(this.page)).replace("{TotalPage}", String.valueOf(((this.perks.size() - 1) / mainGuiConfig.getPerkBlocks().size()) + 1));
        initGui(titleInv, mainGuiConfig.getSize(), perks.size(), mainGuiConfig.getPerkBlocks().size());
    }

    public void initializeItems() {
        inv.clear();

        if(!this.perks.isEmpty()) {
            int id = (this.mainGuiConfig.getPerkBlocks().size() * this.page) - this.mainGuiConfig.getPerkBlocks().size();
            for (int index : mainGuiConfig.getPerkBlocks()) {
                int finalId = id;
                inv.setItem(index, createGuiItem(perks.get(id), playerPerks.stream().filter(p -> p.getPerk().equals(perks.get(finalId).getId())).findFirst()));
                id++;
                if (id >= (perks.size())) break;
            }
        }
        initPrevious();
        initNext(perks.size(), this.mainGuiConfig.getPerkBlocks().size());
        initAction();
        initBarrier();
        openInventory();
    }

    private ItemStack createGuiItem(Perk perk, Optional<PlayerPerk> playerPerk) {
        ItemStack item = perk.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        String title = mainGuiConfig.getPerkTitle();
        title = title.replace("{Name}", perk.getDisplayName());
        title = FormatUtil.format(title);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        List<String> listDescription = new ArrayList<>();

        for (String desc : mainGuiConfig.getPerkDescription()) {
            desc = desc.replace("{Name}", perk.getDisplayName());

            if (desc.contains("competences")) {
                if (perk.getCompetences().isEmpty()) {
                    listDescription.add(desc.replace("{competences}", ""));
                } else {
                    for(var line : perk.getCompetences().entrySet()) {
                        var format = mainGuiConfig.getCompetenceFormat();
                        format = format.replace("{DisplayName}", line.getValue().getDisplayName());
                        listDescription.add(FormatUtil.format(format));
                    }
                }
            } else {
                if(playerPerk.isPresent()) {
                    desc = desc.replace("{IsEnabled}", globalConfig.getStatus().get(playerPerk.get().isEnabled() ? StatusType.ACTIVATED : StatusType.DESACTIVED));
                } else {
                    desc = desc.replace("{IsEnabled}", globalConfig.getStatus().get(StatusType.DESACTIVED));
                }
                listDescription.add(FormatUtil.format(desc));
            }

        }
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(listDescription);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (inv.getHolder() != this || e.getInventory() != inv) {
            return;
        }

        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;


        for (Barrier next : mainGuiConfig.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.mainGuiConfig.getPerkBlocks().size() * this.page) - this.mainGuiConfig.getPerkBlocks().size() < perks.size() - this.mainGuiConfig.getPerkBlocks().size()) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                this.page = this.page + 1;
                initializeItems();
                return;
            }
        }

        for (Barrier previous : mainGuiConfig.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                this.page = page - 1;
                initializeItems();
                return;
            }
        }

        for (Barrier close : mainGuiConfig.getCloseBlocks()) {
            if (e.getRawSlot() == close.getIndex()) {
                inv.close();
                return;
            }
        }

        for (Integer perkBlock : mainGuiConfig.getPerkBlocks()) {
            if (e.getRawSlot() == perkBlock) {
                int nb0 = mainGuiConfig.getPerkBlocks().get(0);
                int nb = ((e.getRawSlot() - nb0)) / 9;
                Perk perk = perks.get((e.getRawSlot() - nb0) + ((this.mainGuiConfig.getPerkBlocks().size() * this.page) - this.mainGuiConfig.getPerkBlocks().size()) - nb * 2);

                TaskChain<List<PlayerPerk>> chain = FPerk.newChain();
                chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(player)).sync(playerPerks -> {
                    var optionalPlayerPerk = playerPerks.stream().filter(p -> p.getPerk().equals(perk.getId())).findFirst();
                    if(optionalPlayerPerk.isPresent()) {
                        var playerPerk = optionalPlayerPerk.get();
                        if(playerPerk.isEnabled()) {
                            playerPerk.setEnabled(false);
                            playerPerks.stream().filter(p -> p.getId() == playerPerk.getId()).forEach(p -> p.setEnabled(false));
                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                            for(var competence : perk.getCompetences().entrySet()) {
                                switch (competence.getValue().getType()) {
                                    case EFFECT -> {
                                        var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                        if (potionEffectType != null) {
                                            player.removePotionEffect(potionEffectType);
                                        }
                                    }
                                    case FLY -> {
                                        player.setAllowFlight(false);
                                        player.setFlying(false);
                                    }
                                    case KEEP_INVENTORY -> {
                                        plugin.removeKeepInventory(playerPerk.getPlayerUUID());
                                    }
                                    case KEEP_EXPERIENCE -> {
                                        plugin.removeKeepExperience(playerPerk.getPlayerUUID());
                                    }
                                }
                            }
                        } else {
                            playerPerk.setEnabled(true);
                            playerPerks.stream().filter(p -> p.getId() == playerPerk.getId()).forEach(p -> p.setEnabled(true));
                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                            for(var competence : perk.getCompetences().entrySet()) {
                                switch (competence.getValue().getType()) {
                                    case EFFECT -> {
                                        var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                        if(potionEffectType != null) {
                                            var potionEffect = potionEffectType.createEffect(Integer.MAX_VALUE, competence.getValue().getLevel());
                                            player.addPotionEffect(potionEffect);
                                        }
                                    }
                                    case FLY -> {
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                    }
                                    case KEEP_INVENTORY -> {
                                        plugin.addKeepInventory(player.getUniqueId());
                                    }
                                    case KEEP_EXPERIENCE -> {
                                        plugin.addKeepExperience(playerPerk.getPlayerUUID());
                                    }
                                }
                            }
                        }
                    } else {
                        PlayerPerk playerPerk = new PlayerPerk(-1, player.getUniqueId(), perk.getId(), new Date().getTime(), true);
                        int id = playerPerkCommandManager.addPlayerPerk(playerPerk);
                        playerPerk.setId(id);
                        playerPerks.add(playerPerk);
                        for(var competence : perk.getCompetences().entrySet()) {
                            switch (competence.getValue().getType()) {
                                case EFFECT -> {
                                    var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                    if(potionEffectType != null) {
                                        var potionEffect = potionEffectType.createEffect(Integer.MAX_VALUE, competence.getValue().getLevel());
                                        player.addPotionEffect(potionEffect);
                                    }
                                }
                                case FLY -> {
                                    player.setAllowFlight(true);
                                    player.setFlying(true);
                                }
                                case KEEP_INVENTORY -> {
                                    plugin.addKeepInventory(player.getUniqueId());
                                }
                                case KEEP_EXPERIENCE -> {
                                    plugin.addKeepExperience(player.getUniqueId());
                                }
                            }
                        }
                    }
                    initializeItems();
                    return false;
                }).execute();

                return;
            }
        }

        for(Action action : mainGuiConfig.getActionBlocks()) {
            if (e.getRawSlot() == action.getIndex()) {
                if(action.getType().equals(ActionType.RESET_ALL)) {
                    playerPerkCommandManager.disableAllPerk(player);
                    inv.close();
                    for(PotionEffect potionEffect : player.getActivePotionEffects()) {
                        player.removePotionEffect(potionEffect.getType());
                    }
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(player);
                    issuerTarget.sendInfo(MessageKeys.DISABLE_ALL_PERK);
                }
            }
        }
    }
}