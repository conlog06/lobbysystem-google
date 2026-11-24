package de.loggx.lobbysystem.commands;

import de.loggx.lobbysystem.LobbySystem;
import de.loggx.lobbysystem.config.ConfigManager;
import de.loggx.lobbysystem.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;

public class LobbyCommands implements CommandExecutor {

    private final LobbySystem plugin;
    public static final NamespacedKey NPC_KEY = new NamespacedKey("lobbysystem", "npc_type");

    public LobbyCommands(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur für Spieler.");
            return true;
        }
        ConfigManager cfg = plugin.getConfigManager();
        String name = cmd.getName().toLowerCase();

        switch (name) {
            case "build" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_build"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                if (plugin.getBuildModePlayers().contains(player.getUniqueId())) {
                    plugin.getBuildModePlayers().remove(player.getUniqueId());
                    player.sendMessage(cfg.getMessage("build_disabled"));
                } else {
                    plugin.getBuildModePlayers().add(player.getUniqueId());
                    player.sendMessage(cfg.getMessage("build_enabled"));
                }
            }
            case "lobbycoins" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_coins"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.parse("<#FF5252>Nutze: /lobbycoins add|remove|get <Spielername> [Coins]"));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                String action = args[0].toLowerCase();
                if (action.equals("get")) {
                    int coins = plugin.getMySQLManager().getCoins(target.getUniqueId());
                    player.sendMessage(TextUtil.parse("<gray>Coins von <#40C4FF>" + target.getName() + "<gray>: <#FFD740>" + coins));
                } else if ((action.equals("add") || action.equals("remove")) && args.length >= 3) {
                    int amount = Integer.parseInt(args[2]);
                    if (action.equals("remove")) amount = -amount;
                    plugin.getMySQLManager().updateCoins(target.getUniqueId(), amount);
                    player.sendMessage(TextUtil.parse("<#69F0AE>Coins von <#40C4FF>" + target.getName() + " <#69F0AE>aktualisiert."));
                    if (target.isOnline() && target.getPlayer() != null) {
                        plugin.getScoreboardManager().updatePlayer(target.getPlayer());
                    }
                }
            }
            case "setreward" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_setreward"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
                    removeNPCs(player, "reward");
                    player.sendMessage(cfg.getMessage("reward_npc_removed"));
                } else {
                    removeNPCs(player, "reward");
                    Witch witch = (Witch) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITCH);
                    configureStaticNPC(witch, "<gradient:#FFD740:#FF9100><bold><smallcaps>Daily-Reward</smallcaps></bold></gradient>", "reward");
                    player.sendMessage(cfg.getMessage("reward_npc_set"));
                }
            }
            case "setsettings" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_setsettings"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
                    removeNPCs(player, "settings");
                    player.sendMessage(cfg.getMessage("settings_npc_removed"));
                } else {
                    removeNPCs(player, "settings");
                    Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                    configureStaticNPC(villager, "<gradient:#00E5FF:#2979FF><bold><smallcaps>Einstellungen</smallcaps></bold></gradient>", "settings");
                    player.sendMessage(cfg.getMessage("settings_npc_set"));
                }
            }
            case "setspawn" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_setspawn"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                cfg.saveLocation("spawn", player.getLocation());
                player.sendMessage(cfg.getMessage("spawn_set"));
            }
            case "setwarp" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_setwarp"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage(TextUtil.parse("<#FF5252>Nutze: /setwarp <Name>"));
                    return true;
                }
                cfg.saveLocation("warps." + args[0].toLowerCase(), player.getLocation());
                player.sendMessage(TextUtil.parse("<#69F0AE>Warp-Punkt <#FFD740>" + args[0] + " <#69F0AE>gesetzt."));
            }
            case "tpcoord" -> {
                if (!player.hasPermission(cfg.getPermission("suffix_tpcoord"))) {
                    player.sendMessage(cfg.getMessage("no_permission"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(TextUtil.parse("<#FF5252>Nutze: /tpcoord <X> <Y> <Z> [Yaw] [Pitch]"));
                    return true;
                }
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                float yaw = args.length >= 4 ? Float.parseFloat(args[3]) : player.getLocation().getYaw();
                float pitch = args.length >= 5 ? Float.parseFloat(args[4]) : player.getLocation().getPitch();
                player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
                player.sendMessage(TextUtil.parse("<#69F0AE>Teleportiert zu <#FFD740>" + x + ", " + y + ", " + z));
            }
        }
        return true;
    }

    public static void configureStaticNPC(LivingEntity entity, String displayName, String type) {
        entity.customName(TextUtil.parse(displayName));
        entity.setCustomNameVisible(true);
        entity.setAI(false);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setCollidable(false);
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        entity.getPersistentDataContainer().set(NPC_KEY, PersistentDataType.STRING, type);
    }

    private void removeNPCs(Player player, String type) {
        for (Entity entity : player.getWorld().getEntities()) {
            String tag = entity.getPersistentDataContainer().get(NPC_KEY, PersistentDataType.STRING);
            if (type.equals(tag)) {
                entity.remove();
            }
        }
    }
}
