package de.loggx.lobbysystem.database;

import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.loggx.lobbysystem.LobbySystem;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class MySQLManager {

    private final LobbySystem plugin;
    private HikariDataSource dataSource;
    private boolean sqliteMode = true;

    public MySQLManager(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        JsonObject root = plugin.getConfigManager().getConfig();
        String storageType = root.has("storage_type")
                ? root.get("storage_type").getAsString().trim().toLowerCase()
                : "sqlite";
        this.sqliteMode = !storageType.equals("mysql");

        try {
            HikariConfig config = new HikariConfig();
            if (sqliteMode) {
                Class.forName("org.sqlite.JDBC");
                String fileName = root.has("sqlite_file") ? root.get("sqlite_file").getAsString() : "database.db";
                File dbFile = new File(plugin.getDataFolder(), fileName);
                config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
                config.setMaximumPoolSize(1);
                config.setConnectionTestQuery("SELECT 1");
            } else {
                JsonObject mysql = root.getAsJsonObject("mysql");
                config.setJdbcUrl("jdbc:mysql://" + mysql.get("host").getAsString() + ":" +
                        mysql.get("port").getAsInt() + "/" + mysql.get("database").getAsString() + "?useSSL=false&autoReconnect=true");
                config.setUsername(mysql.get("username").getAsString());
                config.setPassword(mysql.get("password").getAsString());
                config.setMaximumPoolSize(10);
            }

            dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().info("Datenbank verbunden (" + (sqliteMode ? "SQLite lokal" : "MySQL") + ").");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS lobby_players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(16)," +
                    "coins INT DEFAULT 0," +
                    "last_reward BIGINT DEFAULT 0," +
                    "player_visibility INT DEFAULT 0," +
                    "fly_enabled TINYINT(1) DEFAULT 0," +
                    "double_jump TINYINT(1) DEFAULT 1" +
                    ");");
        }
    }

    public void createPlayerIfNotExists(UUID uuid, String name) {
        String sql = sqliteMode
                ? "INSERT OR IGNORE INTO lobby_players (uuid, name, coins, last_reward, player_visibility, fly_enabled, double_jump) VALUES (?, ?, 0, 0, 0, 0, 1)"
                : "INSERT IGNORE INTO lobby_players (uuid, name, coins, last_reward, player_visibility, fly_enabled, double_jump) VALUES (?, ?, 0, 0, 0, 0, 1)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getCoins(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT coins FROM lobby_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("coins");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateCoins(UUID uuid, int delta) {
        String sql = sqliteMode
                ? "UPDATE lobby_players SET coins = MAX(0, coins + ?) WHERE uuid = ?"
                : "UPDATE lobby_players SET coins = GREATEST(0, coins + ?) WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int[] getPlayerSettings(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT player_visibility, fly_enabled, double_jump FROM lobby_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new int[]{rs.getInt("player_visibility"), rs.getInt("fly_enabled"), rs.getInt("double_jump")};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 1};
    }

    public void updatePlayerSettings(UUID uuid, int visibility, int fly, int doubleJump) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE lobby_players SET player_visibility = ?, fly_enabled = ?, double_jump = ? WHERE uuid = ?")) {
            ps.setInt(1, visibility);
            ps.setInt(2, fly);
            ps.setInt(3, doubleJump);
            ps.setString(4, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean claimDailyReward(UUID uuid, int rewardCoins) {
        long now = System.currentTimeMillis();
        long dayMillis = 86_400_000L;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement check = conn.prepareStatement("SELECT last_reward FROM lobby_players WHERE uuid = ?")) {
            check.setString(1, uuid.toString());
            ResultSet rs = check.executeQuery();
            if (rs.next() && (now - rs.getLong("last_reward")) < dayMillis) {
                return false;
            }
            try (PreparedStatement update = conn.prepareStatement("UPDATE lobby_players SET coins = coins + ?, last_reward = ? WHERE uuid = ?")) {
                update.setInt(1, rewardCoins);
                update.setLong(2, now);
                update.setString(3, uuid.toString());
                update.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSqliteMode() {
        return sqliteMode;
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
