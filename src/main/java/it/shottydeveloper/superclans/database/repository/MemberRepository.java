package it.shottydeveloper.superclans.database.repository;

import it.shottydeveloper.superclans.database.DatabaseManager;
import it.shottydeveloper.superclans.model.ClanMember;
import it.shottydeveloper.superclans.model.ClanRole;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;


public class MemberRepository {

    private final DatabaseManager databaseManager;
    private final String membersTable;
    private final Logger logger;

    public MemberRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.membersTable = databaseManager.getTablePrefix() + "members";
        this.logger = databaseManager.getPlugin().getLogger();
    }

    public boolean saveMember(ClanMember member) {
        String sql = "INSERT INTO `" + membersTable + "` " +
                "(player_uuid, player_name, clan_id, role, joined_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getPlayerUuid().toString());
            ps.setString(2, member.getPlayerName());
            ps.setString(3, member.getClanId().toString());
            ps.setString(4, member.getRole().name());
            ps.setLong(5, member.getJoinedAt());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nel salvataggio del membro " + member.getPlayerName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateMemberRole(UUID playerUuid, ClanRole newRole) {
        String sql = "UPDATE `" + membersTable + "` SET role=? WHERE player_uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newRole.name());
            ps.setString(2, playerUuid.toString());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento del ruolo per " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updatePlayerName(UUID playerUuid, String newName) {
        String sql = "UPDATE `" + membersTable + "` SET player_name=? WHERE player_uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setString(2, playerUuid.toString());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.severe("Errore nell'aggiornamento del nome per " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMember(UUID playerUuid) {
        String sql = "DELETE FROM `" + membersTable + "` WHERE player_uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUuid.toString());
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            logger.severe("Errore nell'eliminazione del membro " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public Optional<ClanMember> findByPlayerUuid(UUID playerUuid) {
        String sql = "SELECT * FROM `" + membersTable + "` WHERE player_uuid=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMember(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento del membro " + playerUuid + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<ClanMember> findByClanId(UUID clanId) {
        List<ClanMember> members = new ArrayList<>();
        String sql = "SELECT * FROM `" + membersTable + "` WHERE clan_id=?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clanId.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento dei membri del clan " + clanId + ": " + e.getMessage());
        }

        return members;
    }

    public List<ClanMember> findAll() {
        List<ClanMember> members = new ArrayList<>();
        String sql = "SELECT * FROM `" + membersTable + "`";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }

        } catch (SQLException e) {
            logger.severe("Errore nel caricamento di tutti i membri: " + e.getMessage());
        }

        return members;
    }

    private ClanMember mapResultSetToMember(ResultSet rs) throws SQLException {
        UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
        String playerName = rs.getString("player_name");
        UUID clanId = UUID.fromString(rs.getString("clan_id"));
        ClanRole role = ClanRole.valueOf(rs.getString("role"));
        long joinedAt = rs.getLong("joined_at");

        return new ClanMember(playerUuid, playerName, clanId, role, joinedAt);
    }
}