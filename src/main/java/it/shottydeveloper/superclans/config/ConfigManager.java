package it.shottydeveloper.superclans.config;

import it.shottydeveloper.superclans.SuperClans;
import org.bukkit.configuration.file.FileConfiguration;


public class ConfigManager {

    private final SuperClans plugin;
    private FileConfiguration config;

    public ConfigManager(SuperClans plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getDatabaseType() {
        return config.getString("database.type", "mariadb").toLowerCase();
    }

    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }

    public String getDatabaseName() {
        return config.getString("database.name", "superclans");
    }

    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }

    public String getDatabasePassword() {
        return config.getString("database.password", "password");
    }

    public String getTablePrefix() {
        return config.getString("database.table-prefix", "sc_");
    }

    public int getPoolSize() {
        return config.getInt("database.pool-size", 10);
    }

    public long getConnectionTimeout() {
        return config.getLong("database.connection-timeout", 30000);
    }

    public int getMinNameLength() {
        return config.getInt("clan.min-name-length", 3);
    }

    public int getMaxNameLength() {
        return config.getInt("clan.max-name-length", 20);
    }

    public int getMinTagLength() {
        return config.getInt("clan.min-tag-length", 2);
    }

    public int getMaxTagLength() {
        return config.getInt("clan.max-tag-length", 5);
    }

    public int getMaxMembers() {
        return config.getInt("clan.max-members", 20);
    }

    public int getMaxClaims() {
        return config.getInt("clan.max-claims", 10);
    }

    public String getAllowedNameChars() {
        return config.getString("clan.allowed-name-chars", "[a-zA-Z0-9_]");
    }

    public String getAllowedTagChars() {
        return config.getString("clan.allowed-tag-chars", "[a-zA-Z0-9]");
    }

    public int getInviteExpireSeconds() {
        return config.getInt("invites.expire-seconds", 60);
    }

    public int getTeleportDelay() {
        return config.getInt("teleport.delay", 3);
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("teleport.cancel-on-move", true);
    }

    public boolean isUseWorldGuard() {
        return config.getBoolean("territory.use-worldguard", true);
    }

    public int getClaimRadiusChunks() {
        return config.getInt("territory.claim-radius-chunks", 0);
    }

    public boolean isClaimOutlineEnabled() {
        return config.getBoolean("territory.outline.enabled", true);
    }

    public String getClaimOutlineMaterial() {
        return config.getString("territory.outline.material", "GREEN_CONCRETE");
    }

    public int getClaimOutlineDurationSeconds() {
        return config.getInt("territory.outline.duration-seconds", 15);
    }

    public boolean isAdminBypassEnabled() {
        return config.getBoolean("territory.admin-bypass", true);
    }

    public boolean isProtectBlockBreak() {
        return config.getBoolean("territory.protections.block-break", true);
    }

    public boolean isProtectBlockPlace() {
        return config.getBoolean("territory.protections.block-place", true);
    }

    public boolean isProtectPvp() {
        return config.getBoolean("territory.protections.pvp", false);
    }

    public boolean isProtectExplosions() {
        return config.getBoolean("territory.protections.explosions", true);
    }

    public boolean isProtectFireSpread() {
        return config.getBoolean("territory.protections.fire-spread", true);
    }

    public boolean isProtectMobSpawning() {
        return config.getBoolean("territory.protections.mob-spawning", false);
    }

    public String getChatFormat() {
        return config.getString("chat.format", "&8[&6{tag}&8] &7[{role}] &f{player}&8: &a{message}");
    }

    public boolean isGlobalPrefix() {
        return config.getBoolean("chat.global-prefix", true);
    }

    public String getGlobalChatFormat() {
        return config.getString("chat.global-format", "&8[&6{tag}&8] &f{player}&7: &f{message}");
    }

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }
}