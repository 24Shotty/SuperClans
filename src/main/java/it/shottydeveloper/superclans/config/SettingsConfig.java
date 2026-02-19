package it.shottydeveloper.superclans.config;

import it.shottydeveloper.superclans.SuperClans;


public class SettingsConfig {

    private final SuperClans plugin;
    private final ConfigManager configManager;

    public SettingsConfig(SuperClans plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public boolean isDebug() {
        return configManager.isDebug();
    }

    public boolean shouldUseWorldGuard() {
        return configManager.isUseWorldGuard() && plugin.isWorldGuardEnabled();
    }

    public int getMaxMembers() {
        return configManager.getMaxMembers();
    }

    public int getMaxClaims() {
        return configManager.getMaxClaims();
    }

    public int getTeleportDelay() {
        return configManager.getTeleportDelay();
    }

    public boolean isCancelOnMove() {
        return configManager.isCancelOnMove();
    }

    public int getInviteExpireSeconds() {
        return configManager.getInviteExpireSeconds();
    }
}