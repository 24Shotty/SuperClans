package it.shottydeveloper.superclans.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;


public class WorldGuardHook {

    private final SuperClans plugin;
    private final RegionContainer regionContainer;

    public WorldGuardHook(SuperClans plugin) {
        this.plugin = plugin;
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        plugin.getLogger().info("WorldGuardHook inizializzato!");
    }

    public boolean createRegionForChunk(Chunk chunk, Clan clan, String regionName) {
        World world = chunk.getWorld();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            plugin.getLogger().warning("RegionManager null per il mondo " + world.getName());
            return false;
        }

        try {
            int minX = chunk.getX() * 16;
            int minZ = chunk.getZ() * 16;
            int maxX = minX + 15;
            int maxZ = minZ + 15;

            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight();

            BlockVector3 min = BlockVector3.at(minX, minY, minZ);
            BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, min, max);
            region.setPriority(10);

            if (plugin.getConfigManager().isProtectBlockBreak() || plugin.getConfigManager().isProtectBlockPlace()) {
                region.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
                region.setFlag(Flags.BUILD, StateFlag.State.DENY);
            }

            if (plugin.getConfigManager().isProtectPvp()) {
                region.setFlag(Flags.PVP.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
                region.setFlag(Flags.PVP, StateFlag.State.DENY);
            }

            if (plugin.getConfigManager().isProtectExplosions()) {
                region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
                region.setFlag(Flags.TNT, StateFlag.State.DENY);
                region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
            }

            if (plugin.getConfigManager().isProtectFireSpread()) {
                region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
            }

            if (plugin.getConfigManager().isProtectMobSpawning()) {
                region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
            }

            for (ClanMember member : clan.getAllMembers()) {
                try {
                    com.sk89q.worldguard.LocalPlayer localPlayer =
                            WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(member.getPlayerUuid()));
                    region.getMembers().addPlayer(localPlayer);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossibile aggiungere " + member.getPlayerName() + " alla regione: " + e.getMessage());
                }
            }

            regionManager.addRegion(region);

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Errore nella creazione della regione WG '" + regionName + "': " + e.getMessage());
            if (plugin.getSettingsConfig().isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean removeRegion(String worldName, String regionName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Impossibile trovare il mondo '" + worldName + "' per rimuovere la regione!");
            return false;
        }

        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return false;

        try {
            regionManager.removeRegion(regionName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Errore nella rimozione della regione WG '" + regionName + "': " + e.getMessage());
            return false;
        }
    }

    public boolean regionExists(String worldName, String regionName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return false;

        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return false;

        return regionManager.hasRegion(regionName);
    }

    public boolean addMemberToRegion(String worldName, String regionName, java.util.UUID playerUuid) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return false;

        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return false;

        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) return false;

        try {
            com.sk89q.worldguard.LocalPlayer localPlayer =
                    WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(playerUuid));
            region.getMembers().addPlayer(localPlayer);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}