package it.shottydeveloper.superclans.command.subcommands;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.util.CooldownUtil;
import it.shottydeveloper.superclans.util.PermissionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeCommand implements ClanCommand.SubCommand {

    private final SuperClans plugin;

    private final CooldownUtil cooldown;

    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();

    private final Map<UUID, Location> initialLocations = new HashMap<>();

    public HomeCommand(SuperClans plugin) {
        this.plugin = plugin;
        this.cooldown = new CooldownUtil(10);
    }

    @Override
    public void execute(Player player, String subCommand, String[] args) {
        if (!PermissionUtil.canHome(player)) {
            player.sendMessage(plugin.getMessagesConfig().get("general.no-permission"));
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.must-be-in-clan"));
            return;
        }

        if (!clan.hasHome()) {
            player.sendMessage(plugin.getMessagesConfig().get("home.no-home"));
            return;
        }

        if (cooldown.isOnCooldown(player.getUniqueId())) {
            long remaining = cooldown.getRemainingSeconds(player.getUniqueId());
            player.sendMessage(plugin.getMessagesConfig().getRaw(
                    "&cDevi aspettare ancora &e" + remaining + " secondi &cprima di usare /clan home!"));
            return;
        }

        int delay = plugin.getSettingsConfig().getTeleportDelay();

        if (delay <= 0) {
            performTeleport(player, clan.getHome());
            return;
        }

        player.sendMessage(plugin.getMessagesConfig().get("home.teleporting",
                "seconds", String.valueOf(delay)));

        initialLocations.put(player.getUniqueId(), player.getLocation().clone());

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(player.getUniqueId());
            Location initialLoc = initialLocations.remove(player.getUniqueId());

            if (!player.isOnline()) return;

            if (plugin.getSettingsConfig().isCancelOnMove() && initialLoc != null) {
                Location currentLoc = player.getLocation();
                if (initialLoc.distanceSquared(currentLoc) > 0.25) {
                    player.sendMessage(plugin.getMessagesConfig().get("home.cancelled"));
                    return;
                }
            }

            performTeleport(player, clan.getHome());
        }, delay * 20L);

        pendingTeleports.put(player.getUniqueId(), task);
    }

    private void performTeleport(Player player, Location destination) {
        player.teleport(destination);
        player.sendMessage(plugin.getMessagesConfig().get("home.teleported"));
        cooldown.setCooldown(player.getUniqueId());
    }

    public void cancelPendingTeleport(UUID playerUuid) {
        BukkitTask task = pendingTeleports.remove(playerUuid);
        if (task != null) {
            task.cancel();
        }
        initialLocations.remove(playerUuid);
    }

    @Override
    public List<String> tabComplete(Player player, String subCommand, String[] args) {
        return Collections.emptyList();
    }
}