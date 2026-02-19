package it.shottydeveloper.superclans.model;

import org.bukkit.Chunk;

import java.util.UUID;

public class ClanTerritory {

    private final UUID territoryId;

    private final UUID clanId;

    private final String worldName;

    private final int chunkX;
    private final int chunkZ;

    private String worldGuardRegionName;

    private final long claimedAt;

    private final UUID claimedBy;

    public ClanTerritory(UUID clanId, Chunk chunk, UUID claimedBy) {
        this.territoryId = UUID.randomUUID();
        this.clanId = clanId;
        this.worldName = chunk.getWorld().getName();
        this.chunkX = chunk.getX();
        this.chunkZ = chunk.getZ();
        this.claimedAt = System.currentTimeMillis();
        this.claimedBy = claimedBy;
        this.worldGuardRegionName = null;
    }

    public ClanTerritory(UUID territoryId, UUID clanId, String worldName,
                         int chunkX, int chunkZ, String worldGuardRegionName,
                         long claimedAt, UUID claimedBy) {
        this.territoryId = territoryId;
        this.clanId = clanId;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldGuardRegionName = worldGuardRegionName;
        this.claimedAt = claimedAt;
        this.claimedBy = claimedBy;
    }

    public UUID getTerritoryId() {
        return territoryId;
    }

    public UUID getClanId() {
        return clanId;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public String getWorldGuardRegionName() {
        return worldGuardRegionName;
    }

    public void setWorldGuardRegionName(String worldGuardRegionName) {
        this.worldGuardRegionName = worldGuardRegionName;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public UUID getClaimedBy() {
        return claimedBy;
    }

    public boolean isChunk(Chunk chunk) {
        return worldName.equals(chunk.getWorld().getName())
                && chunkX == chunk.getX()
                && chunkZ == chunk.getZ();
    }

    public String getChunkKey() {
        return worldName + ":" + chunkX + ":" + chunkZ;
    }

    public static String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    @Override
    public String toString() {
        return "ClanTerritory{" +
                "world='" + worldName + '\'' +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                ", clanId=" + clanId +
                '}';
    }
}