package it.shottydeveloper.superclans.listener;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final SuperClans plugin;

    public ChatListener(SuperClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getChatManager().hasClanChatEnabled(event.getPlayer().getUniqueId())) {
            if (plugin.getConfigManager().isGlobalPrefix()) {
                handleGlobalChatPrefix(event);
            }
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        if (clan == null) {
            plugin.getChatManager().disableClanChat(event.getPlayer().getUniqueId());
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();
        plugin.getChatManager().sendClanMessage(clan, event.getPlayer(), message);
    }

    private void handleGlobalChatPrefix(AsyncPlayerChatEvent event) {
        Clan clan = plugin.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        if (clan == null) return;

        String globalFormat = plugin.getConfigManager().getGlobalChatFormat()
                .replace("{tag}", clan.getTag())
                .replace("{clan}", clan.getName())
                .replace("{player}", "%1$s")
                .replace("{message}", "%2$s");
        globalFormat = plugin.getMessagesConfig().color(globalFormat);

        event.setFormat(globalFormat);
    }
}