package it.shottydeveloper.superclans.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.database.migration.SchemaInitializer;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final SuperClans plugin;
    private HikariDataSource dataSource;

    private String tablePrefix;

    public DatabaseManager(SuperClans plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        var cfg = plugin.getConfigManager();

        this.tablePrefix = cfg.getTablePrefix();

        HikariConfig config = new HikariConfig();

        String host = cfg.getDatabaseHost();
        int port = cfg.getDatabasePort();
        String database = cfg.getDatabaseName();
        String username = cfg.getDatabaseUsername();
        String password = cfg.getDatabasePassword();
        int poolSize = cfg.getPoolSize();
        long connectionTimeout = cfg.getConnectionTimeout();
        String dbType = cfg.getDatabaseType();

        String jdbcPrefix = "mysql".equals(dbType) ? "jdbc:mysql" : "jdbc:mariadb";
        String jdbcUrl = String.format(
                "%s://%s:%d/%s?useSSL=false&characterEncoding=UTF-8&autoReconnect=true",
                jdbcPrefix, host, port, database
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setPoolName("SuperClans-Pool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        try {
            dataSource = new HikariDataSource(config);

            try (Connection testConn = getConnection()) {
                if (testConn != null && !testConn.isClosed()) {
                    String dbLabel = "mysql".equals(dbType) ? "MySQL" : "MariaDB";
                    plugin.getLogger().info("Connessione a " + dbLabel + " riuscita!");
                }
            }

            SchemaInitializer schemaInitializer = new SchemaInitializer(this, tablePrefix);
            schemaInitializer.initialize();

            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Errore nella connessione al database: " + e.getMessage());
            if (plugin.getSettingsConfig().isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Il pool di connessioni non è disponibile!");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Pool di connessioni chiuso correttamente.");
        }
    }

    public boolean isConnected() {
        if (dataSource == null || dataSource.isClosed()) return false;
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public SuperClans getPlugin() {
        return plugin;
    }
}