package it.shottydeveloper.superclans.database.migration;

import it.shottydeveloper.superclans.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    private final DatabaseManager databaseManager;
    private final String prefix;

    public SchemaInitializer(DatabaseManager databaseManager, String prefix) {
        this.databaseManager = databaseManager;
        this.prefix = prefix;
    }

    public void initialize() {
        try (Connection conn = databaseManager.getConnection()) {
            createClansTable(conn);
            createMembersTable(conn);
            createTerritoriesTable(conn);

            databaseManager.getPlugin().getLogger().info("Schema del database inizializzato con successo!");
        } catch (SQLException e) {
            databaseManager.getPlugin().getLogger().severe("Errore nell'inizializzazione dello schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createClansTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS `%s` (
                    `id` VARCHAR(36) NOT NULL,
                    `name` VARCHAR(20) NOT NULL,
                    `tag` VARCHAR(5) NOT NULL,
                    `leader_uuid` VARCHAR(36) NOT NULL,
                    `home_world` VARCHAR(64) DEFAULT NULL,
                    `home_x` DOUBLE DEFAULT NULL,
                    `home_y` DOUBLE DEFAULT NULL,
                    `home_z` DOUBLE DEFAULT NULL,
                    `home_yaw` FLOAT DEFAULT NULL,
                    `home_pitch` FLOAT DEFAULT NULL,
                    `created_at` BIGINT NOT NULL,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_clan_name` (`name`),
                    UNIQUE KEY `uk_clan_tag` (`tag`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """.formatted(prefix + "clans");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createMembersTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS `%s` (
                    `player_uuid` VARCHAR(36) NOT NULL,
                    `player_name` VARCHAR(16) NOT NULL,
                    `clan_id` VARCHAR(36) NOT NULL,
                    `role` ENUM('MEMBER', 'OFFICER', 'LEADER') NOT NULL DEFAULT 'MEMBER',
                    `joined_at` BIGINT NOT NULL,
                    PRIMARY KEY (`player_uuid`),
                    KEY `idx_clan_id` (`clan_id`),
                    CONSTRAINT `fk_member_clan`
                        FOREIGN KEY (`clan_id`)
                        REFERENCES `%s` (`id`)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """.formatted(prefix + "members", prefix + "clans");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createTerritoriesTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS `%s` (
                    `id` VARCHAR(36) NOT NULL,
                    `clan_id` VARCHAR(36) NOT NULL,
                    `world_name` VARCHAR(64) NOT NULL,
                    `chunk_x` INT NOT NULL,
                    `chunk_z` INT NOT NULL,
                    `worldguard_region` VARCHAR(128) DEFAULT NULL,
                    `claimed_at` BIGINT NOT NULL,
                    `claimed_by` VARCHAR(36) NOT NULL,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_chunk` (`world_name`, `chunk_x`, `chunk_z`),
                    KEY `idx_territory_clan` (`clan_id`),
                    CONSTRAINT `fk_territory_clan`
                        FOREIGN KEY (`clan_id`)
                        REFERENCES `%s` (`id`)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """.formatted(prefix + "territories", prefix + "clans");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}