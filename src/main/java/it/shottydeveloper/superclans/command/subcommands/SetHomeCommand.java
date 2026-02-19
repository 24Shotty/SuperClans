package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetHomeCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public SetHomeCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canSetHome(player)) {
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

        Location homeLocation = player.getLocation().clone();

        if (!plugin.getClanManager().getClanRepository().updateHome(clan.getClanId(), homeLocation)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        clan.setHome(homeLocation);

        player.sendMessage(plugin.getMessagesConfig().get("sethome.success"));
        plugin.getChatManager().broadcastToClan(clan,
                plugin.getMessagesConfig().getRaw("sethome.broadcast", "player", player.getName()));
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        return Collections.emptyList();
    }
}
