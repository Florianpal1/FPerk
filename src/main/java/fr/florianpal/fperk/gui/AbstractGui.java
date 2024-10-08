
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

package fr.florianpal.fperk.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.AbstractGuiConfiguration;
import fr.florianpal.fperk.configurations.GlobalConfig;
import fr.florianpal.fperk.managers.commandManagers.CommandManager;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.objects.gui.Barrier;
import fr.florianpal.fperk.utils.FormatUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractGui implements InventoryHolder, Listener, GuiInterface {
    protected Inventory inv;

    protected final FPerk plugin;
    protected Player player;

    protected Player showPlayer;
    protected int page;
    protected final GlobalConfig globalConfig;
    protected final CommandManager commandManager;

    protected final AbstractGuiConfiguration abstractGuiConfiguration;

    protected final List<Perk> perks;

    protected final List<PlayerPerk> playerPerks;

    private int referenceItem = 0;

    private int referenceBarrier = 0;

    protected AbstractGui(FPerk plugin, AbstractGuiConfiguration abstractGuiConfiguration, Player player, Player showPlayer, int page, List<Perk> perks, List<PlayerPerk> playerPerks) {
        this.perks = perks;
        this.playerPerks = playerPerks;
        this.plugin = plugin;
        this.player = player;
        this.showPlayer = showPlayer;
        this.page = page;
        this.commandManager = plugin.getCommandManager();
        inv = null;
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();
        this.abstractGuiConfiguration = abstractGuiConfiguration;

        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    protected void initGui(String title, int size, int referenceItem, int referenceBarrier) {
        inv = Bukkit.createInventory(this, size, title);
        this.referenceBarrier = referenceBarrier;
        this.referenceItem = referenceItem;
        refreshGui();
    }

    public void refreshGui() {
        initBarrier();
        initClose();
        initPrevious();
        initAction();
        initNext(referenceItem, referenceBarrier);
        initCustomObject();
        openInventory();
    }

    protected void initBarrier() {
        for (Barrier barrier : abstractGuiConfiguration.getBarrierBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription(), barrier.getTexture()));
        }
    }

    protected void initNext(int referenceItem, int referenceBarrier) {
        this.referenceItem = referenceItem;
        this.referenceBarrier = referenceBarrier;
        for (Barrier next : abstractGuiConfiguration.getNextBlocks()) {
            if ((this.referenceBarrier * this.page) - this.referenceBarrier < this.referenceItem - this.referenceBarrier) {
                inv.setItem(next.getIndex(), createGuiItem(next.getMaterial(), next.getTitle(), next.getDescription(), next.getTexture()));
            } else {
                inv.setItem(next.getRemplacement().getIndex(), createGuiItem(next.getRemplacement().getMaterial(), next.getRemplacement().getTitle(), next.getRemplacement().getDescription(), next.getRemplacement().getTexture()));
            }
        }
    }

    protected void initPrevious() {
        for (Barrier previous : abstractGuiConfiguration.getPreviousBlocks()) {
            if (page > 1) {
                inv.setItem(previous.getIndex(), createGuiItem(previous.getMaterial(), previous.getTitle(), previous.getDescription(), previous.getTexture()));
            } else {
                inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(previous.getRemplacement().getMaterial(), previous.getRemplacement().getTitle(), previous.getRemplacement().getDescription(), previous.getRemplacement().getTexture()));
            }
        }
    }

    protected void initClose() {
        for (Barrier close : abstractGuiConfiguration.getCloseBlocks()) {
            inv.setItem(close.getIndex(), createGuiItem(close.getMaterial(), close.getTitle(), close.getDescription(), close.getTexture()));
        }
    }

    protected void initAction() {
        for (Action action  : abstractGuiConfiguration.getActionBlocks()) {
            inv.setItem(action.getIndex(), createGuiItem(action.getMaterial(), action.getTitle(), action.getDescription(), action.getTexture()));
        }
    }

    public ItemStack createGuiItem(Material material, String name, List<String> description, String texture) {
        ItemStack itemStack = new ItemStack(material, 1);

        if (material == Material.PLAYER_HEAD) {
            if (PaperLib.isPaper()) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", texture));
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                skullMeta.setPlayerProfile(profile);
                itemStack.setItemMeta(skullMeta);
            }

            itemStack.setAmount(1);
        }

        ItemMeta meta = itemStack.getItemMeta();
        name = name.replace("{ActivatedPerk}", String.valueOf(playerPerks.stream().filter(PlayerPerk::isEnabled).count()));
        name = FormatUtils.format(name);

        List<String> descriptions = new ArrayList<>();
        for (String desc : description) {

            desc = desc.replace("{ActivatedPerk}", String.valueOf(playerPerks.stream().filter(PlayerPerk::isEnabled).count()));
            desc = FormatUtils.format(desc);
            descriptions.add(desc);
        }

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(descriptions);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (inv.getHolder() != this || e.getInventory() != inv) {
            return;
        }

        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;


        for (Barrier next : abstractGuiConfiguration.getNextBlocks()) {
            if (e.getRawSlot() == next.getIndex() && ((this.referenceBarrier * this.page) - this.referenceBarrier < perks.size() - this.referenceBarrier) && next.getMaterial() != next.getRemplacement().getMaterial()) {
                this.page = this.page + 1;
                refreshGui();
                return;
            }
        }

        for (Barrier previous : abstractGuiConfiguration.getPreviousBlocks()) {
            if (e.getRawSlot() == previous.getIndex() && this.page > 1) {
                this.page = page - 1;
                refreshGui();
                return;
            }
        }

        for (Barrier close : abstractGuiConfiguration.getCloseBlocks()) {
            if (e.getRawSlot() == close.getIndex()) {
                inv.close();
                return;
            }
        }

        onInventoryClickCustom(e);
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    protected void openInventory() {
        showPlayer.openInventory(inv);
    }
}
