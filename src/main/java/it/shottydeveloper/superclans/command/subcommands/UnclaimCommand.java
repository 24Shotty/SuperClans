package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class UnclaimCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public UnclaimCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canUnclaim(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member == null || !member.isOfficerOrHigher()) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-officer-or-higher"));
            return;
        }

        boolean success = plugin.getTerritoryManager().unclaimChunk(player, clan);

        if (!success) {
            player.sendMessage(plugin.getMessagesConfig().get("unclaim.not-claimed"));
            return;
        }

        player.sendMessage(plugin.getMessagesConfig().get("unclaim.success"));
        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("unclaim.broadcast", "player", player.getName()));
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        return Collections.emptyList();
    }
}