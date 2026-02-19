package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ChatCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public ChatCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canChat(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        if (args.length == 0) {
            boolean enabled = plugin.getChatManager().toggleClanChat(player.getUniqueId());
            if (enabled) {
                player.sendMessage(plugin.getMessagesConfig().get("chat.toggle-on"));
            } else {
                player.sendMessage(plugin.getMessagesConfig().get("chat.toggle-off"));
            }
        } else {
            String message = String.join(" ", args);
            plugin.getChatManager().sendClanMessage(clan, player, message);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        return Collections.emptyList();
    }
}