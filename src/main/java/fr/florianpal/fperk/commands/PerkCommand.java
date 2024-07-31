
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

package fr.florianpal.fperk.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.gui.subGui.MainGui;
import fr.florianpal.fperk.languages.MessageKeys;
import fr.florianpal.fperk.managers.commandManagers.CommandManager;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.Perk;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("perk")
public class PerkCommand extends BaseCommand {

    private final CommandManager commandManager;

    private final PlayerPerkCommandManager playerPerkCommandManager;

    private final PerkConfig perkConfig;

    private final FPerk plugin;

    public PerkCommand(FPerk plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.playerPerkCommandManager = plugin.getPlayerPerkCommandManager();
        this.perkConfig = plugin.getConfigurationManager().getPerkConfig();
    }

    @Default
    @CommandPermission("fperk.show")
    @Description("{@@fperk.show_help_description}")
    public void onShowPerk(Player playerSender) {
        TaskChain<Perk> chain = FPerk.newChain();
        chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(playerSender)).sync(playerPerks -> {
            MainGui mainGui = new MainGui(plugin, perkConfig.getPerks().values().stream().toList(), playerPerks, playerSender, playerSender,1);
            mainGui.refreshGui();

            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.SHOW_PERK);
            return null;
        }).execute();
    }


    @Subcommand("admin toggle")
    @CommandPermission("fperk.admin.toggle")
    @Description("{@@fperk.reload_help_description}")
    @CommandCompletion("@players")
    public void onAdminToggle(Player playerSender, String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if(offlinePlayer != null && offlinePlayer.isOnline()) {
            TaskChain<Perk> chain = FPerk.newChain();
            chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(offlinePlayer)).sync(playerPerks -> {
                MainGui mainGui = new MainGui(plugin, perkConfig.getPerks().values().stream().toList(), playerPerks, offlinePlayer.getPlayer(), playerSender, 1);
                mainGui.refreshGui();

                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.SHOW_PERK);
                return null;
            }).execute();
        }
    }

    @Subcommand("admin reload")
    @CommandPermission("fperk.admin.reload")
    @Description("{@@fperk.reload_help_description}")
    public void onReload(Player playerSender) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        plugin.reloadConfig();
        issuerTarget.sendInfo(MessageKeys.RELOAD);
    }

    @HelpCommand
    @Description("{@@fperk.help_description}")
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }


    public List<Perk> getPerkPerPermission(Player player, List<Perk> perks)  {

        return perks.stream().filter(p -> player.hasPermission(p.getPermission())).toList();
    }
}