package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class KickCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;
    private final Map<UUID, PendingKick> awaitingConfirmation = new HashMap<>();
    private static final long CONFIRM_EXPIRE_MS = 30_000L;

    public KickCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    private static class PendingKick {
        final String targetName;
        final long timestamp;

        PendingKick(String targetName, long timestamp) {
            this.targetName = targetName;
            this.timestamp = timestamp;
        }
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canKick(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        ClanMember kickerMember = clan.getMember(player.getUniqueId());
        if (kickerMember == null || !kickerMember.isOfficerOrHigher()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-officer-or-higher"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.usage"));
            return;
        }

        String targetName = args[0];

        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.cannot-kick-self"));
            return;
        }

        ClanMember targetMember = findMemberByName(clan, targetName);
        if (targetMember == null) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.player-not-in-clan",
                    "player", targetName));
            return;
        }

        if (targetMember.isLeader()) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.cannot-kick-leader"));
            return;
        }

        if (!kickerMember.isLeader() && targetMember.getRole().isAtLeast(kickerMember.getRole())) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.cannot-kick-higher-rank"));
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            handleConfirmation(player, clan, targetName);
            return;
        }

        awaitingConfirmation.put(player.getUniqueId(), new PendingKick(targetName, System.currentTimeMillis()));
        player.sendMessage(plugin.getMessagesConfig().get("kick.confirm",
                "player", targetName));
    }

    private void handleConfirmation(Player player, Clan clan, String targetName) {
        PendingKick pending = awaitingConfirmation.get(player.getUniqueId());
        if (pending == null || !pending.targetName.equalsIgnoreCase(targetName)) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.confirm",
                    "player", targetName));
            awaitingConfirmation.put(player.getUniqueId(), new PendingKick(targetName, System.currentTimeMillis()));
            return;
        }

        if (System.currentTimeMillis() - pending.timestamp > CONFIRM_EXPIRE_MS) {
            awaitingConfirmation.remove(player.getUniqueId());
            player.sendMessage(plugin.getMessagesConfig().get("kick.confirm-expired"));
            return;
        }

        awaitingConfirmation.remove(player.getUniqueId());

        ClanMember targetMember = findMemberByName(clan, targetName);
        if (targetMember == null) {
            player.sendMessage(plugin.getMessagesConfig().get("kick.player-not-in-clan",
                    "player", targetName));
            return;
        }

        String kickedName = targetMember.getPlayerName();
        plugin.getClanManager().removeMember(targetMember.getPlayerUuid(), clan);

        plugin.getChatManager().disableClanChat(targetMember.getPlayerUuid());

        Player kickedPlayer = Bukkit.getPlayer(targetMember.getPlayerUuid());
        if (kickedPlayer != null && kickedPlayer.isOnline()) {
            kickedPlayer.sendMessage(plugin.getMessagesConfig().get("kick.success-kicked",
                    "clan", clan.getName()));
        }

        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("kick.broadcast",
                        "player", kickedName,
                        "kicker", player.getName()));

        player.sendMessage(plugin.getMessagesConfig().get("kick.success-kicker",
                "player", kickedName));
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
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) return Collections.emptyList();

        if (args.length == 1) {
            return clan.getAllMembers().stream()
                    .filter(m -> !m.getPlayerUuid().equals(player.getUniqueId()))
                    .map(ClanMember::getPlayerName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            ClanMember target = findMemberByName(clan, args[0]);
            if (target != null && "confirm".startsWith(args[1].toLowerCase())) {
                return Collections.singletonList("confirm");
            }
        }

        return Collections.emptyList();
    }
}
