package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public InfoCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canInfo(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan;

        if (args.length == 0) {
            clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan == null) {
                player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
                return;
            }
        } else {
            String search = String.join(" ", args);
            clan = plugin.getClanManager().getClanByName(search);
            if (clan == null) {
                clan = plugin.getClanManager().getClanByTag(search);
            }
            if (clan == null) {
                player.sendMessage(plugin.getMessagesConfig().get("info.clan-not-found", "search", search));
                return;
            }
        }

        List<String> lines = new ArrayList<>();
        lines.add(plugin.getMessagesConfig().getRaw("info.header", "clan", clan.getName()));
        lines.add(plugin.getMessagesConfig().getRaw("info.tag", "tag", clan.getTag()));
        lines.add(plugin.getMessagesConfig().getRaw("info.leader", "leader",
                clan.getLeaderMember() != null ? clan.getLeaderMember().getPlayerName() : "?"));
        lines.add(plugin.getMessagesConfig().getRaw("info.members", "count", String.valueOf(clan.getMemberCount())));
        lines.add(plugin.getMessagesConfig().getRaw("info.territories", "count", String.valueOf(clan.getTerritoryCount())));
        lines.add(clan.hasHome()
                ? plugin.getMessagesConfig().getRaw("info.home-set")
                : plugin.getMessagesConfig().getRaw("info.home-not-set"));

        String membersList = clan.getAllMembers().stream()
                .map(m -> m.getPlayerName() + " (" + m.getRole().getDisplayName() + ")")
                .collect(Collectors.joining(", "));
        lines.add(plugin.getMessagesConfig().getRaw("info.members-list", "list", membersList));

        for (String line : lines) {
            player.sendMessage(plugin.getMessagesConfig().color(line));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return plugin.getClanManager().getAllClans().stream()
                    .map(Clan::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
