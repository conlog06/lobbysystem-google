package de.loggx.lobbysystem.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.loggx.lobbysystem.LobbySystem;
import de.loggx.lobbysystem.commands.LobbyCommands;
import de.loggx.lobbysystem.gui.NavigatorGUI;
import de.loggx.lobbysystem.gui.SettingsGUI;
import de.loggx.lobbysystem.util.TextUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class LobbyListener implements Listener {

    private final LobbySystem plugin;

    public LobbyListener(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        plugin.getMySQLManager().createPlayerIfNotExists(player.getUniqueId(), player.getName());

        Location spawn = plugin.getConfigManager().getLocation("spawn");
        if (spawn != null) player.teleport(spawn);

        JsonObject nav = plugin.getConfigManager().getConfig().getAsJsonObject("navigator");
        int compassSlot = nav.has("compass_slot") ? nav.get("compass_slot").getAsInt() : 0;
        String compassName = nav.has("compass_name")
                ? nav.get("compass_name").getAsString()
                : "<dark_gray>» <gradient:#00E5FF:#2979FF><bold><smallcaps>Navigator</smallcaps></bold></gradient>";

        player.getInventory().clear();
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.displayName(TextUtil.parse(compassName));
        compass.setItemMeta(meta);
        player.getInventory().setItem(compassSlot, compass);

        int[] settings = plugin.getMySQLManager().getPlayerSettings(player.getUniqueId());
        boolean flyEnabled = settings[1] == 1 && player.hasPermission(plugin.getConfigManager().getPermission("suffix_fly"));
        boolean doubleJump = settings[2] == 1;

        player.setAllowFlight(flyEnabled || doubleJump);
        player.setFlying(flyEnabled);

        plugin.applyPlayerVisibility(player);
        for (Player online : Bukkit.getOnlinePlayers()) {
            plugin.applyPlayerVisibility(online);
        }

        plugin.getScoreboardManager().updateAll();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getScoreboardManager().updateAll(), 1L);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        JsonObject rank = plugin.getScoreboardManager().getPlayerRank(player);
        String prefix = rank != null ? rank.get("prefix").getAsString() : "<gray><smallcaps>Spieler</smallcaps> <dark_gray>| <gray>";
        String chatFormat = plugin.getConfigManager().getConfig()
                .getAsJsonObject("messages")
                .get("chat_format")
                .getAsString();

        event.renderer((source, sourceDisplayName, message, viewer) ->
                TextUtil.parse(chatFormat
                        .replace("%prefix%", prefix)
                        .replace("%player%", source.getName())
                        .replace("%message%", ""))
                        .append(message)
        );
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Stellt sicher, dass NPCs in 1.21.10 - 1.26.3 nach Chunk-Neuladen dauerhaft statisch bleiben
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof LivingEntity living) {
                String type = living.getPersistentDataContainer().get(LobbyCommands.NPC_KEY, PersistentDataType.STRING);
                if ("reward".equals(type)) {
                    LobbyCommands.configureStaticNPC(living, "<gradient:#FFD740:#FF9100><bold><smallcaps>Daily-Reward</smallcaps></bold></gradient>", "reward");
                } else if ("settings".equals(type)) {
                    LobbyCommands.configureStaticNPC(living, "<gradient:#00E5FF:#2979FF><bold><smallcaps>Einstellungen</smallcaps></bold></gradient>", "settings");
                }
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        int[] settings = plugin.getMySQLManager().getPlayerSettings(player.getUniqueId());
        boolean flyEnabled = settings[1] == 1 && player.hasPermission(plugin.getConfigManager().getPermission("suffix_fly"));
        boolean doubleJump = settings[2] == 1;

        if (flyEnabled) return;

        if (doubleJump) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            Vector velocity = player.getLocation().getDirection().multiply(1.2).setY(0.85);
            player.setVelocity(velocity);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.4f);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isOnGround() && !player.getAllowFlight()) {
            int[] settings = plugin.getMySQLManager().getPlayerSettings(player.getUniqueId());
            if (settings[2] == 1) {
                player.setAllowFlight(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
            event.setCancelled(true);
            NavigatorGUI.openNavigator(plugin, event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (plugin.getBuildModePlayers().contains(player.getUniqueId())) return;

        event.setCancelled(true);
        JsonObject nav = plugin.getConfigManager().getConfig().getAsJsonObject("navigator");
        Component navTitle = TextUtil.parse(nav.get("title").getAsString());

        // Navigator-Klicks
        if (event.getView().title().equals(navTitle)) {
            int clickedSlot = event.getRawSlot();
            for (JsonElement el : nav.getAsJsonArray("navigatorItems")) {
                String[] parts = el.getAsString().split(";", 5);
                if (Integer.parseInt(parts[0]) == clickedSlot) {
                    String action = parts[2];
                    player.closeInventory();
                    if (action.startsWith("connect:")) {
                        plugin.connectToProxyServer(player, action.substring(8));
                    } else if (action.equalsIgnoreCase("spawn")) {
                        Location spawn = plugin.getConfigManager().getLocation("spawn");
                        if (spawn != null) player.teleport(spawn);
                    } else {
                        Location warp = plugin.getConfigManager().getLocation("warps." + action.toLowerCase());
                        if (warp != null) player.teleport(warp);
                    }
                    break;
                }
            }
            return;
        }

        // Einstellungs-GUI Klicks
        if (event.getView().title().equals(TextUtil.parse(SettingsGUI.GUI_TITLE_RAW))) {
            int slot = event.getRawSlot();
            int[] settings = plugin.getMySQLManager().getPlayerSettings(player.getUniqueId());
            int vis = settings[0];
            int fly = settings[1];
            int dj = settings[2];

            if (slot == 11) {
                vis = (vis + 1) % 3;
                plugin.getMySQLManager().updatePlayerSettings(player.getUniqueId(), vis, fly, dj);
                plugin.applyPlayerVisibility(player);
                String modeName = vis == 0 ? "Alle Spieler" : (vis == 1 ? "Nur VIPs" : "Keine Spieler");
                player.sendMessage(plugin.getConfigManager().getMessage("visibility_updated", modeName));
                SettingsGUI.openSettings(plugin, player);
            } else if (slot == 13) {
                if (!player.hasPermission(plugin.getConfigManager().getPermission("suffix_fly"))) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
                    return;
                }
                fly = fly == 1 ? 0 : 1;
                plugin.getMySQLManager().updatePlayerSettings(player.getUniqueId(), vis, fly, dj);
                player.setAllowFlight(fly == 1 || dj == 1);
                player.setFlying(fly == 1);
                player.sendMessage(plugin.getConfigManager().getMessage("fly_toggled"));
                SettingsGUI.openSettings(plugin, player);
            } else if (slot == 15) {
                dj = dj == 1 ? 0 : 1;
                plugin.getMySQLManager().updatePlayerSettings(player.getUniqueId(), vis, fly, dj);
                player.setAllowFlight(fly == 1 || dj == 1);
                player.sendMessage(plugin.getConfigManager().getMessage("doublejump_toggled"));
                SettingsGUI.openSettings(plugin, player);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        String npcType = event.getRightClicked().getPersistentDataContainer().get(LobbyCommands.NPC_KEY, PersistentDataType.STRING);
        if (npcType == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();

        if (npcType.equals("reward")) {
            if (plugin.getMySQLManager().claimDailyReward(player.getUniqueId(), 250)) {
                player.sendMessage(plugin.getConfigManager().getMessage("daily_claimed"));
                plugin.getScoreboardManager().updatePlayer(player);
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("daily_cooldown"));
            }
        } else if (npcType.equals("settings")) {
            SettingsGUI.openSettings(plugin, player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getBuildModePlayers().contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getBuildModePlayers().contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
