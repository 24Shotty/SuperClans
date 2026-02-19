package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InviteCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public InviteCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        switch (subCommand.toLowerCase()) {
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (!PermissionUtil.canInvite(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        ClanMember inviterMember = clan.getMember(player.getUniqueId());
        if (inviterMember == null || !inviterMember.isOfficerOrHigher()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-officer-or-higher"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesConfig().getRaw("&cUso: /clan invite <giocatore>"));
            return;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(plugin.getMessagesConfig().get("invite.cannot-invite-self"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.player-not-found",
                    "player", targetName));
            return;
        }

        if (plugin.getClanManager().isInClan(target.getUniqueId())) {
            player.sendMessage(plugin.getMessagesConfig().get("invite.already-in-clan",
                    "player", target.getName()));
            return;
        }

        int maxMembers = plugin.getSettingsConfig().getMaxMembers();
        if (maxMembers > 0 && clan.getMemberCount() >= maxMembers) {
            player.sendMessage(plugin.getMessagesConfig().get("invite.clan-full"));
            return;
        }

        if (plugin.getInviteManager().hasClanInvitedPlayer(clan.getClanId(), target.getUniqueId())) {
            player.sendMessage(plugin.getMessagesConfig().get("invite.already-invited",
                    "player", target.getName()));
            return;
        }

        int expireSeconds = plugin.getSettingsConfig().getInviteExpireSeconds();
        plugin.getInviteManager().createInvite(target.getUniqueId(), clan, player.getUniqueId());

        player.sendMessage(plugin.getMessagesConfig().get("invite.sent", "player", target.getName()));
        target.sendMessage(plugin.getMessagesConfig().get("invite.received",
                "clan", clan.getName(),
                "seconds", String.valueOf(expireSeconds)));
    }

    private void handleAccept(Player player) {
        if (!plugin.getInviteManager().hasPendingInvite(player.getUniqueId())) {
            player.sendMessage(plugin.getMessagesConfig().get("invite.no-pending"));
            return;
        }

        Clan clan = plugin.getInviteManager().acceptInvite(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        if (!plugin.getClanManager().addMember(player, clan)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        player.sendMessage(plugin.getMessagesConfig().get("invite.accepted-receiver",
                "clan", clan.getName()));

        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("invite.broadcast-join", "player", player.getName()));
    }

    private void handleDeny(Player player) {
        if (!plugin.getInviteManager().hasPendingInvite(player.getUniqueId())) {
            player.sendMessage(plugin.getMessagesConfig().get("invite.no-pending"));
            return;
        }

        Clan clan = plugin.getInviteManager().denyInvite(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        player.sendMessage(plugin.getMessagesConfig().get("invite.denied-receiver",
                "clan", clan.getName()));
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        if (subCommand.equalsIgnoreCase("invite") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !plugin.getClanManager().isInClan(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}