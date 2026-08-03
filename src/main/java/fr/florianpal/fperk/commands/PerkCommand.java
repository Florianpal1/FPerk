
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
import fr.florianpal.fperk.objects.PlayerPerk;
import it.unimi.dsi.fastutil.Pair;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
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
        chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(playerSender)).syncLast(playerPerks -> {
            MainGui mainGui = new MainGui(plugin, perkConfig.getPerks().values().stream().toList(), playerPerks, playerSender, playerSender,1);
            mainGui.refreshGui();

            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.SHOW_PERK);
        }).execute();
    }

    @Subcommand("modify")
    @CommandPermission("fperk.modify")
    @Description("{@@fperk.modify_help_description}")
    public void onEnablePerk(Player playerSender, String perkName, boolean status) {
        TaskChain<Perk> chain = FPerk.newChain();
        chain.asyncFirst(() -> Pair.of(playerPerkCommandManager.getPlayerPerk(playerSender, perkName), playerPerkCommandManager.getPlayerPerk(playerSender))).syncLast(pair -> {

            var perks = perkConfig.getPerks().values().stream().toList();
            var optionalPerk = perks.stream().filter(p -> p.getId().equals(perkName)).findFirst();

            var playerPerks = pair.second();
            long count = playerPerks.stream().filter(PlayerPerk::isEnabled).count();

            User user = plugin.getLuckPerms().getUserManager().getUser(playerSender.getUniqueId());
            String meta = user.getCachedData().getMetaData().getMetaValue("fperk.maxperk");
            int result = -1;
            if (meta != null) {
                result = Integer.parseInt(meta);
            }

            CommandIssuer issuer = commandManager.getCommandIssuer(playerSender);

            if (optionalPerk.isEmpty()) {
                issuer.sendInfo(MessageKeys.NOT_FOUND, "{PerkName}", perkName);
                return;
            }

            var perk = optionalPerk.get();

            if (!playerSender.hasPermission(perk.getPermission())) {
                issuer.sendInfo(MessageKeys.NO_PERMISSION, "{PerkName}", perk.getDisplayName());
                return;
            }

            var optionalPlayerPerk = pair.first();
            // The requested state, not a toggle : asking for the state a perk is already in is a
            // no-op rather than an inversion.
            boolean enabled = optionalPlayerPerk.map(PlayerPerk::isEnabled).orElse(false);

            if (status != enabled) {
                if (status) {
                    var playerPerk = optionalPlayerPerk.orElse(null);

                    if (playerPerk != null && !perk.isIgnoreDelais()
                            && perk.getDelais() > new Date().getTime() - playerPerk.getLastEnabled().getTime()) {
                        issuer.sendInfo(MessageKeys.DELAIS, "{PerkName}", perk.getDisplayName());
                        return;
                    }

                    if (result <= count && (perk.getPermissionBypass() == null || !playerSender.hasPermission(perk.getPermissionBypass()))) {
                        issuer.sendInfo(MessageKeys.MAX_PERK);
                        return;
                    }

                    if (playerPerk == null) {
                        playerPerk = new PlayerPerk(-1, playerSender.getUniqueId(), perk.getId(), new Date().getTime(), true);
                        playerPerk.setId(playerPerkCommandManager.addPlayerPerk(playerPerk));
                        plugin.getSkillService().enable(playerSender, playerPerk, perk);
                    } else {
                        plugin.getSkillService().enable(playerSender, playerPerk, perk);
                        playerPerk.setEnabled(true);
                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                    }
                } else {
                    var playerPerk = optionalPlayerPerk.get();

                    plugin.getSkillService().disable(playerSender, playerPerk, perk);
                    playerPerk.setEnabled(false);
                    playerPerkCommandManager.updatePlayerPerk(playerPerk);
                }
            }

            issuer.sendInfo(MessageKeys.MODIFY_PERK, "{PerkName}", perkName, "{NewStatus}", String.valueOf(status));
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
            chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(offlinePlayer)).syncLast(playerPerks -> {
                MainGui mainGui = new MainGui(plugin, perkConfig.getPerks().values().stream().toList(), playerPerks, offlinePlayer.getPlayer(), playerSender, 1);
                mainGui.refreshGui();

                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.SHOW_PERK);
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
}