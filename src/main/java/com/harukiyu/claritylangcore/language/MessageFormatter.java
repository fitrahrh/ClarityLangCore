package com.harukiyu.claritylangcore.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles text formatting using MiniMessage and legacy color code translations.
 */
public class MessageFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[&§]([0-9a-fk-orA-FK-OR])");

    /**
     * Converts a raw message string into an Adventure Component using MiniMessage.
     * Also gracefully converts any legacy color codes (& / §) to MiniMessage syntax.
     */
    @NotNull
    public static Component format(@Nullable String message, @NotNull TagResolver... resolvers) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        String converted = convertLegacyToMiniMessage(message);
        if (resolvers.length == 0) {
            return MINI_MESSAGE.deserialize(converted);
        }
        return MINI_MESSAGE.deserialize(converted, TagResolver.resolver(resolvers));
    }

    /**
     * Formats a list of strings into a list of Components.
     */
    @NotNull
    public static List<Component> formatList(@Nullable List<String> lines, @NotNull TagResolver... resolvers) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }

        List<Component> components = new ArrayList<>(lines.size());
        for (String line : lines) {
            components.add(format(line, resolvers));
        }
        return components;
    }

    /**
     * Converts legacy color codes (&a, &b, §c, etc.) into modern MiniMessage tags.
     */
    @NotNull
    public static String convertLegacyToMiniMessage(@NotNull String input) {
        if (input.indexOf('&') == -1 && input.indexOf('§') == -1) {
            return input;
        }

        Matcher matcher = LEGACY_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            String replacement = switch (code) {
                case '0' -> "<black>";
                case '1' -> "<dark_blue>";
                case '2' -> "<dark_green>";
                case '3' -> "<dark_aqua>";
                case '4' -> "<dark_red>";
                case '5' -> "<dark_purple>";
                case '6' -> "<gold>";
                case '7' -> "<gray>";
                case '8' -> "<dark_gray>";
                case '9' -> "<blue>";
                case 'a' -> "<green>";
                case 'b' -> "<aqua>";
                case 'c' -> "<red>";
                case 'd' -> "<light_purple>";
                case 'e' -> "<yellow>";
                case 'f' -> "<white>";
                case 'k' -> "<obfuscated>";
                case 'l' -> "<bold>";
                case 'm' -> "<strikethrough>";
                case 'n' -> "<underlined>";
                case 'o' -> "<italic>";
                case 'r' -> "<reset>";
                default -> matcher.group(0);
            };
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Gets the MiniMessage instance.
     */
    @NotNull
    public static MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }
}
