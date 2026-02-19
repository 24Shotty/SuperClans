package it.shottydeveloper.superclans.core;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.model.ClanTerritory;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TerritoryManager {

    private final SuperClans plugin;

    private final Map<String, ClanTerritory> territoriesByChunk = new HashMap<>();

    /** Blocchi del perimetro da ripristinare su unclaim, chiave = chunkKey */
    private final Map<String, List<RestoreEntry>> outlineBlocksByChunk = new HashMap<>();

    public TerritoryManager(SuperClans plugin) {
        this.plugin = plugin;
        rebuildCache();
    }

    public enum ClaimAreaFailure {
        NONE,
        CLAIMED_BY_OTHER,
        MAX_CLAIMS,
        ERROR
    }

    public record ClaimAreaResult(ClaimAreaFailure failure, int claimed, UUID conflictClanId) {
        public static ClaimAreaResult success(int claimed) {
            return new ClaimAreaResult(ClaimAreaFailure.NONE, claimed, null);
        }

        public static ClaimAreaResult failure(ClaimAreaFailure failure, int claimed, UUID conflictClanId) {
            return new ClaimAreaResult(failure, claimed, conflictClanId);
        }
    }

    public void rebuildCache() {
        territoriesByChunk.clear();
        for (Clan clan : plugin.getClanManager().getAllClans()) {
            for (ClanTerritory territory : clan.getAllTerritories()) {
                territoriesByChunk.put(territory.getChunkKey(), territory);
            }
        }
        plugin.getLogger().info("Cache territori ricostruita: " + territoriesByChunk.size() + " chunk.");
    }

    public ClaimAreaResult claimArea(Player player, Clan clan) {
        Chunk center = player.getLocation().getChunk();

        int radius = plugin.getConfigManager().getClaimRadiusChunks();
        if (radius < 0) radius = 0;
        if (radius > 10) radius = 10;

        int cx = center.getX();
        int cz = center.getZ();
        World world = center.getWorld();

        List<Chunk> toClaim = new ArrayList<>();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                String key = ClanTerritory.getChunkKey(chunk);

                ClanTerritory existing = territoriesByChunk.get(key);
                if (existing != null) {
                    if (!existing.getClanId().equals(clan.getClanId())) {
                        return ClaimAreaResult.failure(ClaimAreaFailure.CLAIMED_BY_OTHER, 0, existing.getClanId());
                    }
                    continue;
                }

                toClaim.add(chunk);
            }
        }

        int maxClaims = plugin.getSettingsConfig().getMaxClaims();
        if (maxClaims > 0 && clan.getTerritoryCount() + toClaim.size() > maxClaims) {
            return ClaimAreaResult.failure(ClaimAreaFailure.MAX_CLAIMS, 0, null);
        }

        int claimed = 0;
        for (Chunk chunk : toClaim) {
            ClanTerritory territory = claimSingleChunk(player, clan, chunk);
            if (territory == null) {
                return ClaimAreaResult.failure(ClaimAreaFailure.ERROR, claimed, null);
            }
            claimed++;
        }

        if (claimed > 0 && plugin.getConfigManager().isClaimOutlineEnabled()) {
            outlinePerimeter(world, cx, cz, radius, player.getLocation());
        }

        return ClaimAreaResult.success(claimed);
    }

    private ClanTerritory claimSingleChunk(Player player, Clan clan, Chunk chunk) {
        String chunkKey = ClanTerritory.getChunkKey(chunk);
        if (territoriesByChunk.containsKey(chunkKey)) return null;

        ClanTerritory territory = new ClanTerritory(clan.getClanId(), chunk, player.getUniqueId());

        if (plugin.getSettingsConfig().shouldUseWorldGuard() && plugin.getWorldGuardHook() != null) {
            String regionName = "clan_" + clan.getTag().toLowerCase() + "_" +
                    chunk.getWorld().getName() + "_" + chunk.getX() + "_" + chunk.getZ();
            regionName = regionName.replaceAll("[^a-zA-Z0-9_-]", "_");

            boolean wgSuccess = plugin.getWorldGuardHook().createRegionForChunk(chunk, clan, regionName);
            if (wgSuccess) {
                territory.setWorldGuardRegionName(regionName);
            } else {
                plugin.getLogger().warning("Impossibile creare regione WorldGuard per il chunk " + chunkKey);
            }
        }

        if (!plugin.getClanManager().getTerritoryRepository().saveTerritory(territory)) {
            return null;
        }

        clan.addTerritory(territory);
        territoriesByChunk.put(chunkKey, territory);
        return territory;
    }

    private record RestoreEntry(Location location, BlockData originalData) {}

    private void outlinePerimeter(World world, int centerChunkX, int centerChunkZ, int radiusChunks, Location playerLocation) {
        String materialName = plugin.getConfigManager().getClaimOutlineMaterial();
        Material outlineMaterial = Material.matchMaterial(materialName);
        if (outlineMaterial == null) {
            outlineMaterial = Material.GREEN_CONCRETE;
        }

        int durationSeconds = plugin.getConfigManager().getClaimOutlineDurationSeconds();
        if (durationSeconds < 0) durationSeconds = 0;

        int outlineY = (int) Math.floor(playerLocation.getY()) - 1;
        if (outlineY < world.getMinHeight() || outlineY >= world.getMaxHeight()) {
            outlineY = world.getHighestBlockYAt((int) playerLocation.getX(), (int) playerLocation.getZ());
        }

        int minX = (centerChunkX - radiusChunks) * 16;
        int maxX = (centerChunkX + radiusChunks) * 16 + 15;
        int minZ = (centerChunkZ - radiusChunks) * 16;
        int maxZ = (centerChunkZ + radiusChunks) * 16 + 15;

        List<String> chunkKeysModified = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            placeOutlineBlock(world, x, minZ, outlineY, outlineMaterial, chunkKeysModified);
            placeOutlineBlock(world, x, maxZ, outlineY, outlineMaterial, chunkKeysModified);
        }

        for (int z = minZ + 1; z <= maxZ - 1; z++) {
            placeOutlineBlock(world, minX, z, outlineY, outlineMaterial, chunkKeysModified);
            placeOutlineBlock(world, maxX, z, outlineY, outlineMaterial, chunkKeysModified);
        }

        if (durationSeconds > 0 && !chunkKeysModified.isEmpty()) {
            long ticks = durationSeconds * 20L;
            List<String> chunksToRestore = new ArrayList<>(chunkKeysModified);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (String ck : chunksToRestore) {
                    restoreOutlineForChunk(ck);
                }
            }, ticks);
        }
    }

    private void placeOutlineBlock(World world, int x, int z, int y, Material outlineMaterial, List<String> chunkKeysModified) {
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) return;

        Block block = world.getBlockAt(x, y, z);
        Location blockLoc = block.getLocation();
        String chunkKey = world.getName() + ":" + (x >> 4) + ":" + (z >> 4);

        List<RestoreEntry> existingEntries = outlineBlocksByChunk.get(chunkKey);
        if (existingEntries != null) {
            RestoreEntry existingEntry = existingEntries.stream()
                    .filter(entry -> {
                        Location entryLoc = entry.location();
                        return entryLoc.getBlockX() == x && entryLoc.getBlockY() == y && entryLoc.getBlockZ() == z
                                && entryLoc.getWorld().equals(world);
                    })
                    .findFirst()
                    .orElse(null);

            if (existingEntry != null) {
                int cx = x >> 4;
                int cz = z >> 4;
                if (world.isChunkLoaded(cx, cz)) {
                    block.setBlockData(existingEntry.originalData(), false);
                }
                existingEntries.remove(existingEntry);
            }
        }

        RestoreEntry entry = new RestoreEntry(blockLoc.clone(), block.getBlockData().clone());
        outlineBlocksByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(entry);
        if (!chunkKeysModified.contains(chunkKey)) chunkKeysModified.add(chunkKey);

        block.setType(outlineMaterial, false);
    }

    private void restoreOutlineForChunk(String chunkKey) {
        List<RestoreEntry> entries = outlineBlocksByChunk.remove(chunkKey);
        if (entries == null) return;
        for (RestoreEntry entry : entries) {
            Location loc = entry.location();
            World w = loc.getWorld();
            if (w == null) continue;
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (!w.isChunkLoaded(cx, cz)) continue;
            Block b = w.getBlockAt(loc);
            b.setBlockData(entry.originalData(), false);
        }
    }

    public boolean unclaimChunk(Player player, Clan clan) {
        Chunk chunk = player.getLocation().getChunk();
        String chunkKey = ClanTerritory.getChunkKey(chunk);

        ClanTerritory territory = territoriesByChunk.get(chunkKey);
        if (territory == null) return false;

        if (!territory.getClanId().equals(clan.getClanId())) return false;

        restoreOutlineForChunk(chunkKey);

        if (territory.getWorldGuardRegionName() != null && plugin.isWorldGuardEnabled()) {
            plugin.getWorldGuardHook().removeRegion(
                    territory.getWorldName(),
                    territory.getWorldGuardRegionName()
            );
        }

        plugin.getClanManager().getTerritoryRepository().deleteByChunk(
                chunk.getWorld().getName(), chunk.getX(), chunk.getZ()
        );

        clan.removeTerritory(chunkKey);
        territoriesByChunk.remove(chunkKey);

        return true;
    }

    public ClanTerritory getTerritoryAt(Player player) {
        return getTerritoryAtChunk(player.getLocation().getChunk());
    }

    public ClanTerritory getTerritoryAtChunk(Chunk chunk) {
        return territoriesByChunk.get(ClanTerritory.getChunkKey(chunk));
    }

    public boolean isChunkClaimed(Chunk chunk) {
        return territoriesByChunk.containsKey(ClanTerritory.getChunkKey(chunk));
    }

    public boolean isChunkOwnedBy(Chunk chunk, UUID clanId) {
        ClanTerritory territory = getTerritoryAtChunk(chunk);
        return territory != null && territory.getClanId().equals(clanId);
    }

    public Clan getClanAtChunk(Chunk chunk) {
        ClanTerritory territory = getTerritoryAtChunk(chunk);
        if (territory == null) return null;
        return plugin.getClanManager().getClanById(territory.getClanId());
    }
}