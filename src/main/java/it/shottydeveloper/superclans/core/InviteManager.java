package it.shottydeveloper.superclans.core;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {

    private final SuperClans plugin;

    private final Map<UUID, PendingInvite> pendingInvites = new HashMap<>();

    public InviteManager(SuperClans plugin) {
        this.plugin = plugin;
    }

    public void createInvite(UUID invitedUuid, Clan clan, UUID inviterUuid) {
        cancelPendingTask(invitedUuid);

        int expireSeconds = plugin.getSettingsConfig().getInviteExpireSeconds();

        BukkitTask expiryTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PendingInvite invite = pendingInvites.remove(invitedUuid);
            if (invite != null) {
                Player invited = Bukkit.getPlayer(invitedUuid);
                if (invited != null && invited.isOnline()) {
                    invited.sendMessage(plugin.getMessagesConfig().get("invite.expired",
                            "clan", clan.getName()));
                }
            }
        }, expireSeconds * 20L);

        pendingInvites.put(invitedUuid, new PendingInvite(invitedUuid, clan, inviterUuid, expiryTask));
    }

    public boolean hasPendingInvite(UUID playerUuid) {
        return pendingInvites.containsKey(playerUuid);
    }

    public PendingInvite getPendingInvite(UUID playerUuid) {
        return pendingInvites.get(playerUuid);
    }

    public Clan acceptInvite(UUID playerUuid) {
        PendingInvite invite = pendingInvites.remove(playerUuid);
        if (invite == null) return null;

        invite.getExpiryTask().cancel();
        return invite.getClan();
    }

    public Clan denyInvite(UUID playerUuid) {
        PendingInvite invite = pendingInvites.remove(playerUuid);
        if (invite == null) return null;

        invite.getExpiryTask().cancel();
        return invite.getClan();
    }

    public void removeInvitesForClan(UUID clanId) {
        pendingInvites.entrySet().removeIf(entry -> {
            if (entry.getValue().getClan().getClanId().equals(clanId)) {
                entry.getValue().getExpiryTask().cancel();
                return true;
            }
            return false;
        });
    }

    private void cancelPendingTask(UUID playerUuid) {
        PendingInvite existing = pendingInvites.get(playerUuid);
        if (existing != null) {
            existing.getExpiryTask().cancel();
        }
    }

    public boolean hasClanInvitedPlayer(UUID clanId, UUID playerUuid) {
        PendingInvite invite = pendingInvites.get(playerUuid);
        if (invite == null) return false;
        return invite.getClan().getClanId().equals(clanId);
    }

    public static class PendingInvite {
        private final UUID invitedUuid;
        private final Clan clan;
        private final UUID inviterUuid;
        private final BukkitTask expiryTask;

        public PendingInvite(UUID invitedUuid, Clan clan, UUID inviterUuid, BukkitTask expiryTask) {
            this.invitedUuid = invitedUuid;
            this.clan = clan;
            this.inviterUuid = inviterUuid;
            this.expiryTask = expiryTask;
        }

        public UUID getInvitedUuid() { return invitedUuid; }
        public Clan getClan() { return clan; }
        public UUID getInviterUuid() { return inviterUuid; }
        public BukkitTask getExpiryTask() { return expiryTask; }
    }
}