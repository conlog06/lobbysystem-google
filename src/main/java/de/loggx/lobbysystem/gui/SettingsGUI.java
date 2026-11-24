package de.loggx.lobbysystem.gui;

import de.loggx.lobbysystem.LobbySystem;
import de.loggx.lobbysystem.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SettingsGUI {

    public static final String GUI_TITLE_RAW = "<dark_gray>» <gradient:#00E5FF:#2979FF><bold><smallcaps>Einstellungen</smallcaps></bold></gradient>";

    public static void openSettings(LobbySystem plugin, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TextUtil.parse(GUI_TITLE_RAW));
        int[] settings = plugin.getMySQLManager().getPlayerSettings(player.getUniqueId());
        int visibility = settings[0]; // 0 = Alle, 1 = VIPs, 2 = Keine
        boolean flyEnabled = settings[1] == 1;
        boolean doubleJump = settings[2] == 1;

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Slot 11: Spieler-Sichtbarkeit
        Material visMat = visibility == 0 ? Material.LIME_DYE : (visibility == 1 ? Material.PURPLE_DYE : Material.RED_DYE);
        String visLabel = visibility == 0 ? "<#69F0AE>Alle Spieler" : (visibility == 1 ? "<#E040FB>Nur VIPs" : "<#FF5252>Keine Spieler");
        ItemStack visItem = new ItemStack(visMat);
        ItemMeta visMeta = visItem.getItemMeta();
        visMeta.displayName(TextUtil.parse("<dark_gray>» <#40C4FF><bold><smallcaps>Spieler-Sichtbarkeit</smallcaps></bold>"));
        visMeta.lore(List.of(
                TextUtil.parse("<gray>Aktuell: " + visLabel),
                TextUtil.parse("<dark_gray>» <gray>Klicke zum Wechseln")
        ));
        visItem.setItemMeta(visMeta);
        inv.setItem(11, visItem);

        // Slot 13: Flugmodus (Fly-Toggle)
        ItemStack flyItem = new ItemStack(flyEnabled ? Material.FEATHER : Material.GRAY_DYE);
        ItemMeta flyMeta = flyItem.getItemMeta();
        flyMeta.displayName(TextUtil.parse("<dark_gray>» <#FFD740><bold><smallcaps>Flugmodus</smallcaps></bold>"));
        flyMeta.lore(List.of(
                TextUtil.parse("<gray>Status: " + (flyEnabled ? "<#69F0AE>Aktiviert" : "<#FF5252>Deaktiviert")),
                TextUtil.parse("<gray>Benötigt: <#FFD740>" + plugin.getConfigManager().getPermission("suffix_fly"))
        ));
        flyItem.setItemMeta(flyMeta);
        inv.setItem(13, flyItem);

        // Slot 15: Doppelsprung (Double-Jump)
        ItemStack djItem = new ItemStack(doubleJump ? Material.RABBIT_FOOT : Material.BARRIER);
        ItemMeta djMeta = djItem.getItemMeta();
        djMeta.displayName(TextUtil.parse("<dark_gray>» <#69F0AE><bold><smallcaps>Doppelsprung</smallcaps></bold>"));
        djMeta.lore(List.of(
                TextUtil.parse("<gray>Status: " + (doubleJump ? "<#69F0AE>Aktiviert" : "<#FF5252>Deaktiviert")),
                TextUtil.parse("<dark_gray>» <gray>Klicke zum Umschalten")
        ));
        djItem.setItemMeta(djMeta);
        inv.setItem(15, djItem);

        player.openInventory(inv);
    }
}
