package it.shottydeveloper.superclans.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownUtil {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private final long cooldownMs;

    public CooldownUtil(long cooldownSeconds) {
        this.cooldownMs = cooldownSeconds * 1000L;
    }

    public boolean isOnCooldown(UUID playerUuid) {
        Long lastUse = cooldowns.get(playerUuid);
        if (lastUse == null) return false;
        return (System.currentTimeMillis() - lastUse) < cooldownMs;
    }

    public long getRemainingSeconds(UUID playerUuid) {
        Long lastUse = cooldowns.get(playerUuid);
        if (lastUse == null) return 0;

        long elapsed = System.currentTimeMillis() - lastUse;
        long remaining = cooldownMs - elapsed;

        return remaining > 0 ? (remaining / 1000) + 1 : 0;
    }

    public void setCooldown(UUID playerUuid) {
        cooldowns.put(playerUuid, System.currentTimeMillis());
    }

    public void clearCooldown(UUID playerUuid) {
        cooldowns.remove(playerUuid);
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry ->
                (now - entry.getValue()) >= cooldownMs
        );
    }
}