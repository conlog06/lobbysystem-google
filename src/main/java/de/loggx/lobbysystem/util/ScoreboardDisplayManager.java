package de.loggx.lobbysystem.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.loggx.lobbysystem.LobbySystem;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardDisplayManager {

    private final LobbySystem plugin;

    public ScoreboardDisplayManager(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public JsonObject getPlayerRank(Player player) {
        JsonArray ranks = plugin.getConfigManager().getRanks().getAsJsonArray("ranks");
        JsonObject bestRank = null;
        int bestPriority = Integer.MAX_VALUE;

        for (JsonElement el : ranks) {
            JsonObject r = el.getAsJsonObject();
            int prio = r.has("priority") ? r.get("priority").getAsInt() : 99;
            if (player.hasPermission(r.get("permission").getAsString()) && prio < bestPriority) {
                bestPriority = prio;
                bestRank = r;
            }
        }
        if (bestRank == null && !ranks.isEmpty()) {
            bestRank = ranks.get(ranks.size() - 1).getAsJsonObject();
        }
        return bestRank;
    }

    public void updateAll() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            updatePlayer(online);
        }
    }

    public void updatePlayer(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        JsonObject sbCfg = plugin.getConfigManager().getConfig().getAsJsonObject("scoreboard");
        String titleStr = sbCfg.get("title").getAsString();

        // Moderne 1.21.10 - 1.26.3 Sidebar ohne rote Zahlen rechts (NumberFormat.blank())
        Objective obj = board.registerNewObjective("lobby_sb", Criteria.DUMMY, TextUtil.parse(titleStr));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.numberFormat(NumberFormat.blank());

        JsonObject rank = getPlayerRank(player);
        String rankPrefix = rank != null ? rank.get("prefix").getAsString() : "<gray>Spieler";
        String rankDisplay = rank != null && rank.has("scoreboard_display")
                ? rank.get("scoreboard_display").getAsString()
                : rankPrefix;

        int coins = plugin.getMySQLManager().getCoins(player.getUniqueId());
        int onlineCount = Bukkit.getOnlinePlayers().size();

        JsonArray lines = sbCfg.getAsJsonArray("lines");
        int scoreVal = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String rawLine = lines.get(i).getAsString()
                    .replace("%rank%", rankDisplay)
                    .replace("%coins%", String.valueOf(coins))
                    .replace("%online%", String.valueOf(onlineCount))
                    .replace("%player%", player.getName());

            // Eindeutiger Entry-Key + moderne customName Component (kein 40-Zeichen-Limit!)
            Score score = obj.getScore("line_" + i);
            score.setScore(scoreVal--);
            score.customName(TextUtil.parse(rawLine));
        }

        // Tablist & Nametag-Teams für alle Online-Spieler synchronisieren
        for (Player target : Bukkit.getOnlinePlayers()) {
            JsonObject targetRank = getPlayerRank(target);
            int prio = targetRank != null && targetRank.has("priority") ? targetRank.get("priority").getAsInt() : 99;
            String teamName = String.format("%04d_%s", prio, target.getName());
            if (teamName.length() > 16) teamName = teamName.substring(0, 16);

            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            if (targetRank != null) {
                team.prefix(TextUtil.parse(targetRank.get("prefix").getAsString()));
                if (targetRank.has("suffix")) {
                    team.suffix(TextUtil.parse(targetRank.get("suffix").getAsString()));
                }
            }
            team.addEntry(target.getName());
        }

        // Tablist Header & Footer (1.21.10 - 1.26.3 Small-Caps & Hex Gradients)
        if (sbCfg.has("tablist_header") && sbCfg.has("tablist_footer")) {
            Component header = TextUtil.parse(sbCfg.get("tablist_header").getAsString()
                    .replace("%online%", String.valueOf(onlineCount)));
            Component footer = TextUtil.parse(sbCfg.get("tablist_footer").getAsString()
                    .replace("%coins%", String.valueOf(coins)));
            player.sendPlayerListHeaderAndFooter(header, footer);
        }

        player.setScoreboard(board);
    }
}
