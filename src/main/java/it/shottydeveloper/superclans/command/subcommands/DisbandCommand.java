package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisbandCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;
    private final Map<UUID, Long> awaitingConfirmation = new HashMap<>();
    private static final long CONFIRM_EXPIRE_MS = 30_000L;

    public DisbandCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canDisband(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member == null || !member.isLeader()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-leader"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            handleConfirmation(player, clan);
            return;
        }

        awaitingConfirmation.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(plugin.getMessagesConfig().get("disband.confirm",
                "clan", clan.getName()));
    }

    private void handleConfirmation(Player player, Clan clan) {
        Long confirmTime = awaitingConfirmation.get(player.getUniqueId());
        if (confirmTime == null) {
            player.sendMessage(plugin.getMessagesConfig().get("disband.confirm",
                    "clan", clan.getName()));
            awaitingConfirmation.put(player.getUniqueId(), System.currentTimeMillis());
            return;
        }

        if (System.currentTimeMillis() - confirmTime > CONFIRM_EXPIRE_MS) {
            awaitingConfirmation.remove(player.getUniqueId());
            player.sendMessage(plugin.getMessagesConfig().get("disband.confirm",
                    "clan", clan.getName()));
            awaitingConfirmation.put(player.getUniqueId(), System.currentTimeMillis());
            return;
        }

        awaitingConfirmation.remove(player.getUniqueId());
        String clanName = clan.getName();

        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("disband.broadcast", "clan", clanName));

        for (var member : clan.getAllMembers()) {
            plugin.getChatManager().disableClanChat(member.getPlayerUuid());
        }

        plugin.getInviteManager().removeInvitesForClan(clan.getClanId());

        plugin.getClanManager().disbandClan(clan);

        player.sendMessage(plugin.getMessagesConfig().get("disband.success", "clan", clanName));
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        if (args.length == 1) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                ClanMember member = clan.getMember(player.getUniqueId());
                if (member != null && member.isLeader() && "confirm".startsWith(args[0].toLowerCase())) {
                    return Collections.singletonList("confirm");
                }
            }
        }
        return Collections.emptyList();
    }
}