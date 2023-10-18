
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

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.AbstractGuiConfiguration;
import fr.florianpal.fperk.configurations.GlobalConfig;
import fr.florianpal.fperk.managers.commandManagers.CommandManager;
import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.objects.gui.Barrier;
import fr.florianpal.fperk.utils.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGui implements InventoryHolder, Listener {
    protected Inventory inv;

    protected final FPerk plugin;
    protected Player player;
    protected int page;
    protected final GlobalConfig globalConfig;
    protected final CommandManager commandManager;

    protected final AbstractGuiConfiguration abstractGuiConfiguration;

    private int referenceItem = 0;

    private int referenceBarrier = 0;

    protected AbstractGui(FPerk plugin, AbstractGuiConfiguration abstractGuiConfiguration, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.commandManager = plugin.getCommandManager();
        inv = null;
        this.globalConfig = plugin.getConfigurationManager().getGlobalConfig();
        this.abstractGuiConfiguration = abstractGuiConfiguration;

        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]);
    }

    protected void initGui(String title, int size, int referenceItem, int referenceBarrier) {
        inv = Bukkit.createInventory(this, size, title);
        initBarrier();
        initClose();
        initPrevious();
        initAction();
        initNext(referenceItem, referenceBarrier);
    }

    protected void initBarrier() {
        for (Barrier barrier : abstractGuiConfiguration.getBarrierBlocks()) {
            inv.setItem(barrier.getIndex(), createGuiItem(barrier.getMaterial(), barrier.getTitle(), barrier.getDescription()));
        }
    }

    protected void initNext(int referenceItem, int referenceBarrier) {
        this.referenceItem = referenceItem;
        this.referenceBarrier = referenceBarrier;
        for (Barrier next : abstractGuiConfiguration.getNextBlocks()) {
            if ((this.referenceBarrier * this.page) - this.referenceBarrier < this.referenceItem - this.referenceBarrier) {
                inv.setItem(next.getIndex(), createGuiItem(next.getMaterial(), next.getTitle(), next.getDescription()));
            } else {
                inv.setItem(next.getRemplacement().getIndex(), createGuiItem(next.getRemplacement().getMaterial(), next.getRemplacement().getTitle(), next.getRemplacement().getDescription()));
            }
        }
    }

    protected void initPrevious() {
        for (Barrier previous : abstractGuiConfiguration.getPreviousBlocks()) {
            if (page > 1) {
                inv.setItem(previous.getIndex(), createGuiItem(previous.getMaterial(), previous.getTitle(), previous.getDescription()));
            } else {
                inv.setItem(previous.getRemplacement().getIndex(), createGuiItem(previous.getRemplacement().getMaterial(), previous.getRemplacement().getTitle(), previous.getRemplacement().getDescription()));
            }
        }
    }

    protected void initClose() {
        for (Barrier close : abstractGuiConfiguration.getCloseBlocks()) {
            inv.setItem(close.getIndex(), createGuiItem(close.getMaterial(), close.getTitle(), close.getDescription()));
        }
    }

    protected void initAction() {
        for (Action action  : abstractGuiConfiguration.getActionBlocks()) {
            inv.setItem(action.getIndex(), createGuiItem(action.getMaterial(), action.getTitle(), action.getDescription()));
        }
    }

    public ItemStack createGuiItem(Material material, String name, List<String> description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        name = FormatUtil.format(name);
        List<String> descriptions = new ArrayList<>();
        for (String desc : description) {

            desc = FormatUtil.format(desc);
            descriptions.add(desc);
        }
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(descriptions);
            item.setItemMeta(meta);
        }
        return item;
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    protected void openInventory() {
        player.openInventory(inv);
    }
}
