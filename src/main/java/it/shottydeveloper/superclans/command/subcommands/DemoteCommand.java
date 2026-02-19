package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.model.ClanRole;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DemoteCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public DemoteCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canDemote(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        ClanMember demoterMember = clan.getMember(player.getUniqueId());
        if (demoterMember == null || !demoterMember.isLeader()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-leader"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesConfig().get("demote.usage"));
            return;
        }

        String targetName = args[0];

        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(plugin.getMessagesConfig().get("demote.cannot-demote-self"));
            return;
        }

        ClanMember targetMember = findMemberByName(clan, targetName);
        if (targetMember == null) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.player-not-in-clan",
                    "player", targetName));
            return;
        }

        ClanRole prevRole = targetMember.getRole().getPreviousRole();
        if (prevRole == null) {
            player.sendMessage(plugin.getMessagesConfig().get("demote.already-min",
                    "player", targetName));
            return;
        }

        if (targetMember.isLeader()) {
            player.sendMessage(plugin.getMessagesConfig().get("demote.already-min",
                    "player", targetName));
            return;
        }

        if (!plugin.getClanManager().setMemberRole(targetMember, prevRole)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("demote.broadcast",
                        "player", targetMember.getPlayerName(),
                        "role", prevRole.getDisplayName(),
                        "demoter", player.getName()));

        player.sendMessage(plugin.getMessagesConfig().get("demote.success",
                "player", targetMember.getPlayerName(),
                "role", prevRole.getDisplayName()));
    }

    private ClanMember findMemberByName(Clan clan, String name) {
        for (ClanMember member : clan.getAllMembers()) {
            if (member.getPlayerName().equalsIgnoreCase(name)) {
                return member;
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        if (args.length == 1) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan == null) return Collections.emptyList();

            return clan.getAllMembers().stream()
                    .filter(m -> !m.getPlayerUuid().equals(player.getUniqueId())
                            && m.getRole() == ClanRole.OFFICER)
                    .map(ClanMember::getPlayerName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}