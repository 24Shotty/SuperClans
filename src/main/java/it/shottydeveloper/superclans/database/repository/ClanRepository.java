package it.shottydeveloper.superclans.database.repository;

import it.shottydeveloper.superclans.database.DatabaseManager;
import it.shottydeveloper.superclans.model.Clan;
import it.shottydeveloper.superclans.util.LocationSerializer;
import org.bukkit.Location;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class ClanRepository {

    private final DatabaseManager databaseManager;
    private final String clansTable;
    private final Logger logger;

    public ClanRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.clansTable = databaseManager.getTablePrefix() + "clans";
        this.logger = databaseManager.getPlugin().getLogger();
    }

    public boolean saveClan(Clan clan) {
        String sql = "INSERT INTO `" + clansTable + "` " +
                "(id, name, tag, leader_uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clan.getClanId().toString());
            ps.setString(2, clan.getName());
            ps.setString(3, clan.getTag());
            ps.setString(4, clan.getLeaderUuid().toString());
            setHomeParameters(ps, clan.getHome(), 5);
            ps.setLong(11, clan.getCreatedAt());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nel salvataggio del clan " + clan.getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateClan(Clan clan) {
        String sql = "UPDATE `" + clansTable + "` SET " +
                "name=?, tag=?, leader_uuid=?, home_world=?, home_x=?, home_y=?, home_z=?, home_yaw=?, home_pitch=? " +
                "WHERE id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clan.getName());
            ps.setString(2, clan.getTag());
            ps.setString(3, clan.getLeaderUuid().toString());
            setHomeParameters(ps, clan.getHome(), 4);
            ps.setString(10, clan.getClanId().toString());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento del clan " + clan.getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClan(UUID clanId) {
        String sql = "DELETE FROM `" + clansTable + "` WHERE id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clanId.toString());
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione del clan " + clanId + ": " + e.getMessage());
            return false;
        }
    }

    public Optional<Clan> findById(UUID clanId) {
        String sql = "SELECT * FROM `" + clansTable + "` WHERE id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clanId.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClan(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento del clan " + clanId + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<Clan> findByName(String name) {
        String sql = "SELECT * FROM `" + clansTable + "` WHERE LOWER(name)=LOWER(?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClan(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca del clan per nome " + name + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<Clan> findByTag(String tag) {
        String sql = "SELECT * FROM `" + clansTable + "` WHERE LOWER(tag)=LOWER(?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tag);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClan(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nella ricerca del clan per tag " + tag + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Clan> findAll() {
        List<Clan> clans = new ArrayList<>();
        String sql = "SELECT * FROM `" + clansTable + "`";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clans.add(mapResultSetToClan(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento di tutti i clan: " + e.getMessage());
        }

        return clans;
    }

    public boolean updateHome(UUID clanId, Location home) {
        String sql = "UPDATE `" + clansTable + "` SET " +
                "home_world=?, home_x=?, home_y=?, home_z=?, home_yaw=?, home_pitch=? " +
                "WHERE id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setHomeParameters(ps, home, 1);
            ps.setString(7, clanId.toString());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento della home: " + e.getMessage());
            return false;
        }
    }

    private void setHomeParameters(PreparedStatement ps, Location home, int startIndex) throws SQLException {
        if (home != null) {
            ps.setString(startIndex, home.getWorld().getName());
            ps.setDouble(startIndex + 1, home.getX());
            ps.setDouble(startIndex + 2, home.getY());
            ps.setDouble(startIndex + 3, home.getZ());
            ps.setFloat(startIndex + 4, home.getYaw());
            ps.setFloat(startIndex + 5, home.getPitch());
        } else {
            for (int i = 0; i < 6; i++) {
                ps.setNull(startIndex + i, Types.NULL);
            }
        }
    }

    private Clan mapResultSetToClan(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String name = rs.getString("name");
        String tag = rs.getString("tag");
        UUID leaderUuid = UUID.fromString(rs.getString("leader_uuid"));
        long createdAt = rs.getLong("created_at");

        Location home = LocationSerializer.fromResultSet(rs);

        return new Clan(id, name, tag, leaderUuid, home, createdAt);
    }
}