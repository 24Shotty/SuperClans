package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.util.ChatUtil;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CreateCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    public CreateCommand(SuperClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canCreate(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessagesConfig().get("create.usage"));
            return;
        }

        String clanName = args[0];
        String clanTag = args[1];

        if (plugin.getClanManager().isInClan(player.getUniqueId())) {
            player.sendMessage(plugin.getMessagesConfig().get("create.already-in-clan"));
            return;
        }

        int minName = plugin.getConfigManager().getMinNameLength();
        int maxName = plugin.getConfigManager().getMaxNameLength();

        if (clanName.length() < minName) {
            player.sendMessage(plugin.getMessagesConfig().get("create.name-too-short",
                    "min", String.valueOf(minName)));
            return;
        }

        if (clanName.length() > maxName) {
            player.sendMessage(plugin.getMessagesConfig().get("create.name-too-long",
                    "max", String.valueOf(maxName)));
            return;
        }

        String nameRegex = plugin.getConfigManager().getAllowedNameChars();
        if (!ChatUtil.isValidString(clanName, nameRegex)) {
            player.sendMessage(plugin.getMessagesConfig().get("create.name-invalid-chars"));
            return;
        }

        int minTag = plugin.getConfigManager().getMinTagLength();
        int maxTag = plugin.getConfigManager().getMaxTagLength();

        if (clanTag.length() < minTag) {
            player.sendMessage(plugin.getMessagesConfig().get("create.tag-too-short",
                    "min", String.valueOf(minTag)));
            return;
        }

        if (clanTag.length() > maxTag) {
            player.sendMessage(plugin.getMessagesConfig().get("create.tag-too-long",
                    "max", String.valueOf(maxTag)));
            return;
        }

        String tagRegex = plugin.getConfigManager().getAllowedTagChars();
        if (!ChatUtil.isValidString(clanTag, tagRegex)) {
            player.sendMessage(plugin.getMessagesConfig().get("create.tag-invalid-chars"));
            return;
        }

        if (plugin.getClanManager().isClanNameTaken(clanName)) {
            player.sendMessage(plugin.getMessagesConfig().get("create.name-taken",
                    "clan", clanName));
            return;
        }

        if (plugin.getClanManager().isClanTagTaken(clanTag)) {
            player.sendMessage(plugin.getMessagesConfig().get("create.tag-taken",
                    "tag", clanTag));
            return;
        }

        Clan newClan = plugin.getClanManager().createClan(player, clanName, clanTag);

        if (newClan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.error-occurred"));
            return;
        }

        player.sendMessage(plugin.getMessagesConfig().get("create.success",
                "clan", clanName,
                "tag", clanTag));
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        return Collections.emptyList();
    }
}