package de.loggx.lobbysystem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.loggx.lobbysystem.commands.LobbyCommands;
import de.loggx.lobbysystem.config.ConfigManager;
import de.loggx.lobbysystem.database.MySQLManager;
import de.loggx.lobbysystem.license.LicenseManager;
import de.loggx.lobbysystem.listeners.LobbyListener;
import de.loggx.lobbysystem.util.ScoreboardDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LobbySystem extends JavaPlugin {

    private static LobbySystem instance;
    private ConfigManager configManager;
    private MySQLManager mySQLManager;
    private LicenseManager licenseManager;
    private ScoreboardDisplayManager scoreboardManager;
    private final Set<UUID> buildModePlayers = new HashSet<>();

    // Moderner Plugin-Channel für Paper 1.21.10 - 1.26.3 (BungeeCord & Velocity)
    public static final String PROXY_CHANNEL = "bungeecord:main";

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        this.licenseManager = new LicenseManager(this);
        if (!this.licenseManager.validateLicense()) {
            getLogger().severe("==========================================");
            getLogger().severe("UNGÜLTIGE LIZENZ! Plugin wird deaktiviert.");
            getLogger().severe("Bitte trage eine gültige Lizenz in der config.json ein.");
            getLogger().severe("==========================================");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.mySQLManager = new MySQLManager(this);
        if (!this.mySQLManager.connect()) {
            getLogger().severe("Datenbank-Verbindung (SQLite/MySQL) fehlgeschlagen! Bitte config.json prüfen.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.scoreboardManager = new ScoreboardDisplayManager(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PROXY_CHANNEL);

        LobbyCommands commands = new LobbyCommands(this);
        getCommand("build").setExecutor(commands);
        getCommand("lobbycoins").setExecutor(commands);
        getCommand("setreward").setExecutor(commands);
        getCommand("setsettings").setExecutor(commands);
        getCommand("setspawn").setExecutor(commands);
        getCommand("setwarp").setExecutor(commands);
        getCommand("tpcoord").setExecutor(commands);

        Bukkit.getPluginManager().registerEvents(new LobbyListener(this), this);
        getLogger().info("LobbySystem v1.1.0 (MC 1.21.10 - 1.26.3) gestartet! (Lizenz verifiziert)");
    }

    @Override
    public void onDisable() {
        if (mySQLManager != null) {
            mySQLManager.disconnect();
        }
    }

    public void connectToProxyServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(this, PROXY_CHANNEL, out.toByteArray());
    }

    public void applyPlayerVisibility(Player viewer) {
        int mode = mySQLManager.getPlayerSettings(viewer.getUniqueId())[0]; // 0 = Alle, 1 = VIPs, 2 = Keine
        String vipPerm = configManager.getPermission("suffix_vip");
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(viewer)) continue;
            if (mode == 0) {
                viewer.showPlayer(this, target);
            } else if (mode == 1) {
                if (target.hasPermission(vipPerm)) {
                    viewer.showPlayer(this, target);
                } else {
                    viewer.hidePlayer(this, target);
                }
            } else {
                viewer.hidePlayer(this, target);
            }
        }
    }

    public static LobbySystem getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MySQLManager getMySQLManager() { return mySQLManager; }
    public ScoreboardDisplayManager getScoreboardManager() { return scoreboardManager; }
    public Set<UUID> getBuildModePlayers() { return buildModePlayers; }
}
