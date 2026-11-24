package de.loggx.lobbysystem.config;

import com.google.gson.*;
import de.loggx.lobbysystem.LobbySystem;
import de.loggx.lobbysystem.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigManager {

    private final LobbySystem plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private JsonObject config;
    private JsonObject ranks;
    private File configFile;
    private File ranksFile;

    public ConfigManager(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "config.json");
        ranksFile = new File(plugin.getDataFolder(), "ranks.json");

        if (!configFile.exists()) {
            createDefaultConfig();
        }
        if (!ranksFile.exists()) {
            createDefaultRanks();
        }

        try {
            config = JsonParser.parseString(Files.readString(configFile.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
            ranks = JsonParser.parseString(Files.readString(ranksFile.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("license_key", "DEINE-LIZENZ-HIER");
        root.addProperty("license_server_url", "http://127.0.0.1:3000/api/v1/verify");
        root.addProperty("storage_type", "sqlite");
        root.addProperty("sqlite_file", "database.db");

        JsonObject mysql = new JsonObject();
        mysql.addProperty("host", "127.0.0.1");
        mysql.addProperty("port", 3306);
        mysql.addProperty("database", "modularbungee");
        mysql.addProperty("username", "root");
        mysql.addProperty("password", "passwort");
        root.add("mysql", mysql);

        JsonObject permissions = new JsonObject();
        permissions.addProperty("prefix", "lobby");
        permissions.addProperty("suffix_build", "build");
        permissions.addProperty("suffix_coins", "coins");
        permissions.addProperty("suffix_setreward", "setreward");
        permissions.addProperty("suffix_setsettings", "setsettings");
        permissions.addProperty("suffix_setspawn", "setspawn");
        permissions.addProperty("suffix_setwarp", "setwarp");
        permissions.addProperty("suffix_tpcoord", "tpcoord");
        permissions.addProperty("suffix_fly", "fly");
        permissions.addProperty("suffix_vip", "vip");
        root.add("permissions", permissions);

        // Modernes 1.21.10 - 1.26.3 Prefix-, Farb- & Small-Caps-System (MiniMessage + Hex Gradients)
        JsonObject messages = new JsonObject();
        messages.addProperty("prefix", "<dark_gray>[<gradient:#00E5FF:#2979FF><bold><smallcaps>Lobby</smallcaps></bold></gradient><dark_gray>] <gray>");
        messages.addProperty("no_permission", "<#FF5252>Dazu hast du keine Rechte!");
        messages.addProperty("build_enabled", "<#69F0AE>Baumodus aktiviert.");
        messages.addProperty("build_disabled", "<#FF5252>Baumodus deaktiviert.");
        messages.addProperty("spawn_set", "<#69F0AE>Der Spawn-Punkt wurde gesetzt.");
        messages.addProperty("reward_npc_set", "<#69F0AE>Daily-Reward Hexe wurde gesetzt.");
        messages.addProperty("reward_npc_removed", "<#FF5252>Daily-Reward Hexe wurde entfernt.");
        messages.addProperty("settings_npc_set", "<#69F0AE>Einstellungs-Dorfbewohner wurde gesetzt.");
        messages.addProperty("settings_npc_removed", "<#FF5252>Einstellungs-Dorfbewohner wurde entfernt.");
        messages.addProperty("daily_claimed", "<#69F0AE>Du hast deine tägliche Belohnung (<#FFD740>+250 Coins<#69F0AE>) abgeholt!");
        messages.addProperty("daily_cooldown", "<#FF5252>Du hast deine tägliche Belohnung heute bereits abgeholt!");
        messages.addProperty("visibility_updated", "<gray>Spieler-Sichtbarkeit geändert auf: <#40C4FF>");
        messages.addProperty("fly_toggled", "<gray>Flugmodus wurde umgeschaltet.");
        messages.addProperty("doublejump_toggled", "<gray>Doppelsprung wurde umgeschaltet.");
        messages.addProperty("chat_format", "%prefix%<gray>%player% <dark_gray>» <white>%message%");
        root.add("messages", messages);

        // Modernes Scoreboard & Tablist-System (ohne rote Score-Zahlen)
        JsonObject scoreboard = new JsonObject();
        scoreboard.addProperty("title", " <gradient:#00E5FF:#2979FF><bold><smallcaps>NETZWERK</smallcaps></bold></gradient> ");
        JsonArray lines = new JsonArray();
        lines.add("<dark_gray><st>                    </st>");
        lines.add("<gray>» <#40C4FF><smallcaps>Rang</smallcaps>");
        lines.add("  %rank%");
        lines.add(" ");
        lines.add("<gray>» <#FFD740><smallcaps>Coins</smallcaps>");
        lines.add("  <#FFD740>%coins%");
        lines.add("  ");
        lines.add("<gray>» <#69F0AE><smallcaps>Online</smallcaps>");
        lines.add("  <white>%online%");
        lines.add("<dark_gray><st>                    </st> ");
        scoreboard.add("lines", lines);
        scoreboard.addProperty("tablist_header", "\n <gradient:#00E5FF:#2979FF><bold><smallcaps>NETZWERK LOBBY</smallcaps></bold></gradient> \n <gray>Version <white>1.21.10 - 1.26.3 <dark_gray>| <gray>Online: <#69F0AE>%online% \n");
        scoreboard.addProperty("tablist_footer", "\n <gray>Deine Coins: <#FFD740>%coins% <dark_gray>| <gray>Shop: <#40C4FF>store.deinedomain.de \n");
        root.add("scoreboard", scoreboard);

        // Kompass-Navigator (unterstützt sowohl §-Codes als auch MiniMessage/Hex)
        JsonObject navigator = new JsonObject();
        navigator.addProperty("title", "<dark_gray>» <gradient:#00E5FF:#2979FF><bold><smallcaps>Navigator</smallcaps></bold></gradient>");
        navigator.addProperty("size", 27);
        navigator.addProperty("compass_slot", 0);
        navigator.addProperty("compass_name", "<dark_gray>» <gradient:#00E5FF:#2979FF><bold><smallcaps>Navigator</smallcaps></bold></gradient> <dark_gray>(<gray>Rechtsklick<dark_gray>)");
        JsonArray items = new JsonArray();
        items.add("12;IRON_AXE;connect:CityBuild-1;§7» §2CityBuild;§7» §7Klicke zum Joinen");
        items.add("13;NETHER_STAR;spawn;<dark_gray>» <gradient:#00E5FF:#2979FF><bold><smallcaps>Spawn</smallcaps></bold></gradient>;<dark_gray>» <gray>Teleportiert dich zum Spawn");
        items.add("14;GRASS_BLOCK;connect:SkyBlock-1;§7» §aSkyBlock;§7» §7Klicke zum Joinen");
        navigator.add("navigatorItems", items);
        root.add("navigator", navigator);

        root.add("locations", new JsonObject());
        root.add("warps", new JsonObject());

        saveJson(configFile, root);
    }

    private void createDefaultRanks() {
        JsonObject root = new JsonObject();
        JsonArray rankList = new JsonArray();

        JsonObject owner = new JsonObject();
        owner.addProperty("name", "Owner");
        owner.addProperty("permission", "rank.owner");
        owner.addProperty("prefix", "<gradient:#FF1744:#FF9100><bold><smallcaps>Owner</smallcaps></bold></gradient> <dark_gray>| <#FF5252>");
        owner.addProperty("scoreboard_display", "<gradient:#FF1744:#FF9100><bold><smallcaps>Owner</smallcaps></bold></gradient>");
        owner.addProperty("priority", 1);
        rankList.add(owner);

        JsonObject admin = new JsonObject();
        admin.addProperty("name", "Admin");
        admin.addProperty("permission", "rank.admin");
        admin.addProperty("prefix", "<#FF5252><bold><smallcaps>Admin</smallcaps></bold> <dark_gray>| <#FF5252>");
        admin.addProperty("scoreboard_display", "<#FF5252><bold><smallcaps>Admin</smallcaps></bold>");
        admin.addProperty("priority", 2);
        rankList.add(admin);

        JsonObject vip = new JsonObject();
        vip.addProperty("name", "VIP");
        vip.addProperty("permission", "rank.vip");
        vip.addProperty("prefix", "<gradient:#FFD740:#FFA000><bold><smallcaps>VIP</smallcaps></bold></gradient> <dark_gray>| <#FFD740>");
        vip.addProperty("scoreboard_display", "<gradient:#FFD740:#FFA000><bold><smallcaps>VIP</smallcaps></bold></gradient>");
        vip.addProperty("priority", 10);
        rankList.add(vip);

        JsonObject spieler = new JsonObject();
        spieler.addProperty("name", "Spieler");
        spieler.addProperty("permission", "rank.default");
        spieler.addProperty("prefix", "<gray><smallcaps>Spieler</smallcaps> <dark_gray>| <gray>");
        spieler.addProperty("scoreboard_display", "<gray><smallcaps>Spieler</smallcaps>");
        spieler.addProperty("priority", 99);
        rankList.add(spieler);

        root.add("ranks", rankList);
        saveJson(ranksFile, root);
    }

    public String getPermission(String suffixKey) {
        JsonObject perms = config.getAsJsonObject("permissions");
        String prefix = perms.get("prefix").getAsString();
        String suffix = perms.get(suffixKey).getAsString();
        return prefix + "." + suffix;
    }

    public Component getMessage(String key) {
        JsonObject msgs = config.getAsJsonObject("messages");
        return TextUtil.parse(msgs.get("prefix").getAsString() + msgs.get(key).getAsString());
    }

    public Component getMessage(String key, String appendRaw) {
        JsonObject msgs = config.getAsJsonObject("messages");
        return TextUtil.parse(msgs.get("prefix").getAsString() + msgs.get(key).getAsString() + appendRaw);
    }

    public void saveLocation(String path, Location loc) {
        JsonObject locObj = new JsonObject();
        locObj.addProperty("world", loc.getWorld().getName());
        locObj.addProperty("x", loc.getX());
        locObj.addProperty("y", loc.getY());
        locObj.addProperty("z", loc.getZ());
        locObj.addProperty("yaw", loc.getYaw());
        locObj.addProperty("pitch", loc.getPitch());

        if (path.startsWith("warps.")) {
            config.getAsJsonObject("warps").add(path.substring(6), locObj);
        } else {
            config.getAsJsonObject("locations").add(path, locObj);
        }
        saveJson(configFile, config);
    }

    public Location getLocation(String path) {
        JsonObject parent = path.startsWith("warps.")
                ? config.getAsJsonObject("warps")
                : config.getAsJsonObject("locations");
        String key = path.startsWith("warps.") ? path.substring(6) : path;

        if (!parent.has(key)) return null;
        JsonObject obj = parent.getAsJsonObject(key);
        return new Location(
                Bukkit.getWorld(obj.get("world").getAsString()),
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(),
                obj.get("pitch").getAsFloat()
        );
    }

    private void saveJson(File file, JsonObject json) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getConfig() { return config; }
    public JsonObject getRanks() { return ranks; }
}
