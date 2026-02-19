package it.shottydeveloper.superclans.core;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatManager {

    private final SuperClans plugin;
    private final Set<UUID> clanChatToggled = new HashSet<>();

    public ChatManager(SuperClans plugin) {
        this.plugin = plugin;
    }

    public void sendClanMessage(Clan clan, Player sender, String message) {
        ClanMember senderMember = clan.getMember(sender.getUniqueId());
        if (senderMember == null) return;

        String formatted = plugin.getConfigManager().getChatFormat()
                .replace("{tag}", clan.getTag())
                .replace("{clan}", clan.getName())
                .replace("{role}", senderMember.getRole().getDisplayName())
                .replace("{player}", sender.getName())
                .replace("{message}", message);
        formatted = plugin.getMessagesConfig().color(formatted);

        int onlineCount = 0;

        for (ClanMember member : clan.getAllMembers()) {
            Player memberPlayer = Bukkit.getPlayer(member.getPlayerUuid());
            if (memberPlayer != null && memberPlayer.isOnline()) {
                memberPlayer.sendMessage(formatted);
                onlineCount++;
            }
        }

        if (onlineCount <= 1) {
            sender.sendMessage(plugin.getMessagesConfig().get("chat.no-members-online"));
        }
    }

    public boolean toggleClanChat(UUID playerUuid) {
        if (clanChatToggled.contains(playerUuid)) {
            clanChatToggled.remove(playerUuid);
            return false;
        } else {
            clanChatToggled.add(playerUuid);
            return true;
        }
    }

    public boolean hasClanChatEnabled(UUID playerUuid) {
        return clanChatToggled.contains(playerUuid);
    }

    public void disableClanChat(UUID playerUuid) {
        clanChatToggled.remove(playerUuid);
    }

    public void broadcastToClan(Clan clan, String message) {
        for (ClanMember member : clan.getAllMembers()) {
            Player player = Bukkit.getPlayer(member.getPlayerUuid());
            if (player != null && player.isOnline()) {
                player.sendMessage(plugin.getMessagesConfig().color(message));
            }
        }
    }
}