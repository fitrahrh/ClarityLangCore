package com.harukiyu.claritylangcore.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a language registered in ClarityLangCore.
 */
public class Language {

    private final String code;
    private final String name;
    private final String flag;
    private final String fallbackCode;
    private final Locale locale;

    public Language(@NotNull String code, @NotNull String name, @Nullable String flag, @Nullable String fallbackCode) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.flag = flag != null ? flag : "🌐";
        this.fallbackCode = fallbackCode != null ? fallbackCode : "en_US";
        this.locale = parseLocale(code);
    }

    /**
     * Gets the standard language code (e.g., "id_ID", "en_US", "pt_BR").
     */
    @NotNull
    public String getCode() {
        return code;
    }

    /**
     * Gets the human-readable native name of the language (e.g., "Bahasa Indonesia", "English").
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the flag emoji or icon (e.g., "🇮🇩", "🇬🇧").
     */
    @NotNull
    public String getFlag() {
        return flag;
    }

    /**
     * Gets the fallback language code if a key is missing in this language.
     */
    @NotNull
    public String getFallbackCode() {
        return fallbackCode;
    }

    /**
     * Gets the Java Locale equivalent of this language.
     */
    @NotNull
    public Locale getLocale() {
        return locale;
    }

    /**
     * Checks if this language matches a query string (code, name, or short code).
     */
    public boolean matches(@NotNull String query) {
        String cleaned = query.trim().replace('-', '_');
        if (code.equalsIgnoreCase(cleaned)) return true;
        if (name.equalsIgnoreCase(query.trim())) return true;
        
        // Match short language code (e.g., "id", "en", "pt", "es")
        String shortCode = code.contains("_") ? code.substring(0, code.indexOf('_')) : code;
        return shortCode.equalsIgnoreCase(cleaned);
    }

    /**
     * Checks if this language matches a client Java Locale.
     */
    public boolean matchesLocale(@NotNull Locale clientLocale) {
        // Direct language tag or string match
        String clientCode = clientLocale.toString(); // e.g. en_US, id_ID, in_ID
        if (code.equalsIgnoreCase(clientCode)) return true;

        // Indonesian legacy Java mapping (in vs id)
        if ((clientCode.startsWith("in") || clientCode.startsWith("id")) && code.startsWith("id")) {
            return true;
        }

        // Language code match (ignoring country if country matches or default)
        if (clientLocale.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
            if (locale.getCountry().isEmpty() || clientLocale.getCountry().isEmpty()) {
                return true;
            }
            return clientLocale.getCountry().equalsIgnoreCase(locale.getCountry());
        }

        return false;
    }

    private static Locale parseLocale(@NotNull String code) {
        String normalized = code.replace('-', '_');
        String[] parts = normalized.split("_");
        if (parts.length >= 2) {
            return Locale.of(parts[0], parts[1]);
        } else if (parts.length == 1) {
            return Locale.of(parts[0]);
        }
        return Locale.ENGLISH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language language)) return false;
        return code.equalsIgnoreCase(language.code);
    }

    @Override
    public int hashCode() {
        return code.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Language{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }
}
