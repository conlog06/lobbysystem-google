package de.loggx.lobbysystem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern SMALL_CAPS_PATTERN = Pattern.compile("<smallcaps>(.*?)</smallcaps>", Pattern.CASE_INSENSITIVE);

    private static final Map<Character, Character> SMALL_CAPS_MAP = Map.ofEntries(
            Map.entry('a', 'ᴀ'), Map.entry('b', 'ʙ'), Map.entry('c', 'ᴄ'), Map.entry('d', 'ᴅ'),
            Map.entry('e', 'ᴇ'), Map.entry('f', 'ꜰ'), Map.entry('g', 'ɢ'), Map.entry('h', 'ʜ'),
            Map.entry('i', 'ɪ'), Map.entry('j', 'ᴊ'), Map.entry('k', 'ᴋ'), Map.entry('l', 'ʟ'),
            Map.entry('m', 'ᴍ'), Map.entry('n', 'ɴ'), Map.entry('o', 'ᴏ'), Map.entry('p', 'ᴘ'),
            Map.entry('q', 'ǫ'), Map.entry('r', 'ʀ'), Map.entry('s', 's'), Map.entry('t', 'ᴛ'),
            Map.entry('u', 'ᴜ'), Map.entry('v', 'ᴠ'), Map.entry('w', 'ᴡ'), Map.entry('x', 'x'),
            Map.entry('y', 'ʏ'), Map.entry('z', 'ᴢ')
    );

    /**
     * Parst sowohl moderne MiniMessage-Tags (<gradient:...>, <#HEX>, <smallcaps>)
     * als auch klassische § / & Farbcodes nahtlos für Minecraft 1.21.10 - 1.26.3.
     */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        // <smallcaps>...</smallcaps> auflösen
        Matcher matcher = SMALL_CAPS_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(toSmallCaps(matcher.group(1))));
        }
        matcher.appendTail(sb);
        String processed = sb.toString();

        // Wenn Legacy-Codes (§ oder &) enthalten sind, diese in MiniMessage-kompatible Tokens umwandeln
        if (processed.contains("§") || processed.contains("&")) {
            processed = legacyToMiniMessage(processed);
        }

        return MINI_MESSAGE.deserialize(processed).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String toSmallCaps(String text) {
        StringBuilder out = new StringBuilder();
        for (char c : text.toCharArray()) {
            char lower = Character.toLowerCase(c);
            out.append(SMALL_CAPS_MAP.getOrDefault(lower, c));
        }
        return out.toString();
    }

    private static String legacyToMiniMessage(String text) {
        return text
                .replace("&", "§")
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§l", "<bold>")
                .replace("§m", "<strikethrough>")
                .replace("§n", "<underlined>")
                .replace("§o", "<italic>")
                .replace("§r", "<reset>");
    }
}
