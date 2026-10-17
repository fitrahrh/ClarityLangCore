package com.harukiyu.claritylangcore.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility helper for color formatting without external BungeeCord chat dependencies.
 */
public final class Utils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})|#([a-fA-F0-9]{6})");

    private Utils() {}

    /**
     * Converts legacy color codes (&a-f, &0-9, &k-o, &r) and hex (#RRGGBB, &#RRGGBB) to section signs (§).
     *
     * @param msg Input text with color codes
     * @return Formatted string with section signs
     */
    public static String colorize(String msg) {
        if (msg == null || msg.isEmpty()) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(msg);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString().replace('&', '§');
    }
}
