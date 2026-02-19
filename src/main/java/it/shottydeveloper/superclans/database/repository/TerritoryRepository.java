package it.shottydeveloper.superclans.database.repository;

import it.shottydeveloper.superclans.database.DatabaseManager;
import it.shottydeveloper.superclans.model.ClanTerritory;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class TerritoryRepository {

    private final DatabaseManager databaseManager;
    private final String territoriesTable;
    private final Logger logger;

    public TerritoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.territoriesTable = databaseManager.getTablePrefix() + "territories";
        this.logger = databaseManager.getPlugin().getLogger();
    }

    public boolean saveTerritory(ClanTerritory territory) {
        String sql = "INSERT INTO `" + territoriesTable + "` " +
                "(id, clan_id, world_name, chunk_x, chunk_z, worldguard_region, claimed_at, claimed_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, territory.getTerritoryId().toString());
            ps.setString(2, territory.getClanId().toString());
            ps.setString(3, territory.getWorldName());
            ps.setInt(4, territory.getChunkX());
            ps.setInt(5, territory.getChunkZ());
            ps.setString(6, territory.getWorldGuardRegionName());
            ps.setLong(7, territory.getClaimedAt());
            ps.setString(8, territory.getClaimedBy().toString());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nel salvataggio del territorio: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteByChunk(String worldName, int chunkX, int chunkZ) {
        String sql = "DELETE FROM `" + territoriesTable + "` WHERE world_name=? AND chunk_x=? AND chunk_z=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, worldName);
            ps.setInt(2, chunkX);
            ps.setInt(3, chunkZ);
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione del territorio: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteByClanId(UUID clanId) {
        String sql = "DELETE FROM `" + territoriesTable + "` WHERE clan_id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clanId.toString());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione dei territori del clan: " + e.getMessage());
            return false;
        }
    }

    public Optional<ClanTerritory> findByChunk(String worldName, int chunkX, int chunkZ) {
        String sql = "SELECT * FROM `" + territoriesTable + "` WHERE world_name=? AND chunk_x=? AND chunk_z=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, worldName);
            ps.setInt(2, chunkX);
            ps.setInt(3, chunkZ);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToTerritory(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca del territorio: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<ClanTerritory> findByClanId(UUID clanId) {
        List<ClanTerritory> territories = new ArrayList<>();
        String sql = "SELECT * FROM `" + territoriesTable + "` WHERE clan_id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clanId.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                territories.add(mapResultSetToTerritory(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento dei territori del clan: " + e.getMessage());
        }

        return territories;
    }

    public List<ClanTerritory> findAll() {
        List<ClanTerritory> territories = new ArrayList<>();
        String sql = "SELECT * FROM `" + territoriesTable + "`";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                territories.add(mapResultSetToTerritory(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento di tutti i territori: " + e.getMessage());
        }

        return territories;
    }

    public boolean updateWorldGuardRegion(UUID territoryId, String regionName) {
        String sql = "UPDATE `" + territoriesTable + "` SET worldguard_region=? WHERE id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, regionName);
            ps.setString(2, territoryId.toString());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento della regione WG: " + e.getMessage());
            return false;
        }
    }

    private ClanTerritory mapResultSetToTerritory(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID clanId = UUID.fromString(rs.getString("clan_id"));
        String worldName = rs.getString("world_name");
        int chunkX = rs.getInt("chunk_x");
        int chunkZ = rs.getInt("chunk_z");
        String wgRegion = rs.getString("worldguard_region");
        long claimedAt = rs.getLong("claimed_at");
        UUID claimedBy = UUID.fromString(rs.getString("claimed_by"));

        return new ClanTerritory(id, clanId, worldName, chunkX, chunkZ, wgRegion, claimedAt, claimedBy);
    }
}