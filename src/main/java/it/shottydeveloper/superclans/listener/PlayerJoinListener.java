package it.shottydeveloper.superclans.listener;

import it.shottydeveloper.superclans.SuperClans;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final SuperClans plugin;

    public PlayerJoinListener(SuperClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getClanManager().updatePlayerName(event.getPlayer())
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getChatManager().disableClanChat(event.getPlayer().getUniqueId());
    }
}