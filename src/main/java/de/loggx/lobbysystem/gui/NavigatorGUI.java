package de.loggx.lobbysystem.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public class NavigatorGUI {

    public static void openNavigator(LobbySystem plugin, Player player) {
        JsonObject nav = plugin.getConfigManager().getConfig().getAsJsonObject("navigator");
        Component title = TextUtil.parse(nav.get("title").getAsString());
        Inventory inv = Bukkit.createInventory(null, nav.get("size").getAsInt(), title);

        // Moderner 1.21.10 - 1.26.3 Rahmen mit grauen Glasscheiben
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.empty());
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        JsonArray items = nav.getAsJsonArray("navigatorItems");
        for (JsonElement el : items) {
            String[] parts = el.getAsString().split(";", 5);
            if (parts.length < 5) continue;

            int slot = Integer.parseInt(parts[0]);
            Material mat = Material.matchMaterial(parts[1].toUpperCase());
            if (mat == null) mat = Material.STONE;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(TextUtil.parse(parts[3]));
            meta.lore(List.of(TextUtil.parse(parts[4])));
            item.setItemMeta(meta);

            inv.setItem(slot, item);
        }
        player.openInventory(inv);
    }
}
