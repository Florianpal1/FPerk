
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
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.gui.MainGuiConfig;
import fr.florianpal.fperk.enums.ActionType;
import fr.florianpal.fperk.enums.StatusType;
import fr.florianpal.fperk.gui.AbstractGui;
import fr.florianpal.fperk.gui.GuiInterface;
import fr.florianpal.fperk.languages.MessageKeys;
import fr.florianpal.fperk.managers.VaultIntegrationManager;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.utils.EffectUtils;
import fr.florianpal.fperk.utils.FormatUtils;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MainGui extends AbstractGui implements GuiInterface {

    private final MainGuiConfig mainGuiConfig;

    private final PlayerPerkCommandManager playerPerkCommandManager;

    private final VaultIntegrationManager vaultIntegrationManager;

    public MainGui(FPerk plugin, List<Perk> perks, List<PlayerPerk> playerPerks, Player player, Player showPlayer,int page) {
        super(plugin, plugin.getConfigurationManager().getMainGuiConfig(), player, showPlayer, page, perks, playerPerks);
        this.mainGuiConfig = plugin.getConfigurationManager().getMainGuiConfig();
        this.playerPerkCommandManager = plugin.getPlayerPerkCommandManager();
        this.vaultIntegrationManager = plugin.getVaultIntegrationManager();

        String titleInv = mainGuiConfig.getNameGui();
        titleInv = titleInv.replace("{Page}", String.valueOf(this.page)).replace("{TotalPage}", String.valueOf(((this.perks.size() - 1) / mainGuiConfig.getPerkBlocks().size()) + 1));
        initGui(titleInv, mainGuiConfig.getSize(), perks.size(), mainGuiConfig.getPerkBlocks().size());
    }

    @Override
    public void initCustomObject() {
        if (!this.perks.isEmpty()) {
            int id = (this.mainGuiConfig.getPerkBlocks().size() * this.page) - this.mainGuiConfig.getPerkBlocks().size();
            for (int index : mainGuiConfig.getPerkBlocks()) {
                int finalId = id;
                inv.setItem(index, createGuiItem(perks.get(id), playerPerks.stream().filter(p -> p.getPerk().equals(perks.get(finalId).getId())).findFirst()));
                id++;
                if (id >= (perks.size())) break;
            }
        }
    }

    private ItemStack createGuiItem(Perk perk, Optional<PlayerPerk> playerPerk) {
        ItemStack item = perk.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        String title = mainGuiConfig.getPerkTitle();
        title = FormatUtils.format(title.replace("{Name}", perk.getDisplayName()));
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        List<String> listDescription = new ArrayList<>();

        for (String desc : mainGuiConfig.getPerkDescription()) {
            desc = desc.replace("{Name}", perk.getDisplayName());

            if (desc.contains("competences")) {
                if (perk.getCompetences().isEmpty()) {
                    listDescription.add(desc.replace("{competences}", ""));
                } else {
                    for (var line : perk.getCompetences().entrySet()) {

                        for (String displayName : line.getValue().getDisplayName()) {
                            listDescription.add(FormatUtils.format(displayName));
                        }
                    }
                }
            } else {
                if (playerPerk.isPresent()) {
                    desc = desc.replace("{IsEnabled}", globalConfig.getStatus().get(playerPerk.get().isEnabled() ? StatusType.ACTIVATED : StatusType.DESACTIVED));
                } else {
                    desc = desc.replace("{IsEnabled}", globalConfig.getStatus().get(StatusType.DESACTIVED));
                }
                listDescription.add(FormatUtils.format(desc));
            }

        }
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(listDescription);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onInventoryClickCustom(InventoryClickEvent e) {

        for (Integer perkBlock : mainGuiConfig.getPerkBlocks()) {
            if (e.getRawSlot() == perkBlock) {
                int block = e.getRawSlot();
                int before = 0;
                for (int i : this.mainGuiConfig.getPerkBlocks()) {
                    if (i < block) {
                        before = before + 1;
                    }
                }

                int index = before + ((this.mainGuiConfig.getPerkBlocks().size() * this.page) - this.mainGuiConfig.getPerkBlocks().size());
                Perk perk = perks.get(index);


                boolean havePermission = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId()).getCachedData().getPermissionData().checkPermission(perk.getPermission()).asBoolean();
                if(!havePermission) {
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(showPlayer);
                    issuerTarget.sendInfo(MessageKeys.NO_PERMISSION, "{PerkName}", perk.getDisplayName());
                    return;
                }

                var optionalPlayerPerk = playerPerks.stream().filter(p -> p.getPerk().equals(perk.getId())).findFirst();
                long count = this.playerPerks.stream().filter(PlayerPerk::isEnabled).count();

                User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
                String meta = user.getCachedData().getMetaData().getMetaValue("fperk.maxperk");
                int result = -1;
                if (meta != null) {
                    result = Integer.parseInt(meta);
                }

                if (optionalPlayerPerk.isPresent()) {
                    var playerPerk = optionalPlayerPerk.get();

                    if((!playerPerk.isEnabled()) && (!perk.isIgnoreDelais()) && perk.getDelais() > new Date().getTime() - playerPerk.getLastEnabled().getTime()) {
                        CommandIssuer issuerTarget = commandManager.getCommandIssuer(showPlayer);
                        issuerTarget.sendInfo(MessageKeys.DELAIS, "{PerkName}", perk.getDisplayName());
                        return;
                    }

                    if (playerPerk.isEnabled()) {

                        EffectUtils.disabledPerk(plugin, player, perk);
                        this.playerPerks.stream().filter(p -> p.getId() == playerPerk.getId()).forEach(p -> p.setEnabled(false));


                        playerPerk.setEnabled(false);
                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                    } else {
                        if (result <= count && (perk.getPermissionBypass() == null || !player.hasPermission(perk.getPermissionBypass()))) {
                            CommandIssuer issuerTarget = commandManager.getCommandIssuer(showPlayer);
                            issuerTarget.sendInfo(MessageKeys.MAX_PERK);
                            return;
                        }

                        EffectUtils.enabledPerk(plugin, player, playerPerk, perk);
                        this.playerPerks.stream().filter(p -> p.getId() == playerPerk.getId()).forEach(p -> p.setEnabled(true));

                        playerPerk.setEnabled(true);
                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                    }
                } else {
                    if (result <= count && (perk.getPermissionBypass() == null || !player.hasPermission(perk.getPermissionBypass()))) {
                        CommandIssuer issuerTarget = commandManager.getCommandIssuer(showPlayer);
                        issuerTarget.sendInfo(MessageKeys.MAX_PERK);
                        return;
                    }

                    PlayerPerk playerPerk = new PlayerPerk(-1, player.getUniqueId(), perk.getId(), new Date().getTime(), true);
                    playerPerk.setId(playerPerkCommandManager.addPlayerPerk(playerPerk));

                    this.playerPerks.add(playerPerk);
                    EffectUtils.enabledPerk(plugin, player, playerPerk, perk);

                }
                refreshGui();
                return;
            }
        }

        for (Action action : mainGuiConfig.getActionBlocks()) {
            if (e.getRawSlot() == action.getIndex()) {
                if (action.getType().equals(ActionType.RESET_ALL)) {
                    playerPerkCommandManager.disableAllPerk(player);
                    inv.close();
                    for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                        player.removePotionEffect(potionEffect.getType());
                    }
                    EffectUtils.enabledFly(player, false);
                    EffectUtils.resetFlySpeed(player);

                    plugin.removeAllPerkActive(player.getUniqueId());

                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(showPlayer);
                    issuerTarget.sendInfo(MessageKeys.DISABLE_ALL_PERK);
                }
            }
        }
    }
}