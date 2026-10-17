package com.harukiyu.claritylangcore.language;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores translations for a specific namespace (e.g. "core", "economy", "clans").
 */
public class TranslationBundle {

    private final String namespace;
    // Map<LanguageCodeLowercase, Map<Key, Value>>
    private final Map<String, Map<String, Object>> languageTranslations = new ConcurrentHashMap<>();

    public TranslationBundle(@NotNull String namespace) {
        this.namespace = namespace.toLowerCase().trim();
    }

    @NotNull
    public String getNamespace() {
        return namespace;
    }

    /**
     * Loads a ConfigurationSection into this bundle for a specific language.
     */
    public void loadSection(@NotNull String langCode, @NotNull ConfigurationSection section) {
        String code = langCode.toLowerCase().trim();
        Map<String, Object> translations = languageTranslations.computeIfAbsent(code, k -> new ConcurrentHashMap<>());
        flattenSection("", section, translations);
    }

    /**
     * Recursively flattens a ConfigurationSection into dot-notation keys.
     */
    private void flattenSection(@NotNull String currentPath, @NotNull ConfigurationSection section, @NotNull Map<String, Object> out) {
        for (String key : section.getKeys(false)) {
            String fullPath = currentPath.isEmpty() ? key : currentPath + "." + key;
            if (section.isConfigurationSection(key)) {
                ConfigurationSection sub = section.getConfigurationSection(key);
                if (sub != null) {
                    flattenSection(fullPath, sub, out);
                }
            } else if (section.isList(key)) {
                out.put(fullPath, section.getStringList(key));
            } else {
                Object val = section.get(key);
                if (val != null) {
                    out.put(fullPath, val.toString());
                }
            }
        }
    }

    /**
     * Sets or overrides a translation key.
     */
    public void setTranslation(@NotNull String langCode, @NotNull String key, @NotNull Object value) {
        String code = langCode.toLowerCase().trim();
        Map<String, Object> map = languageTranslations.computeIfAbsent(code, k -> new ConcurrentHashMap<>());
        map.put(key, value);
    }

    /**
     * Gets a raw object (String or List<String>) for a key in a language.
     */
    @Nullable
    public Object getRaw(@NotNull String langCode, @NotNull String key) {
        Map<String, Object> map = languageTranslations.get(langCode.toLowerCase().trim());
        if (map == null) return null;
        return map.get(key);
    }

    /**
     * Gets a raw string translation.
     */
    @Nullable
    public String getRawString(@NotNull String langCode, @NotNull String key) {
        Object val = getRaw(langCode, key);
        if (val instanceof String s) {
            return s;
        } else if (val instanceof List<?> list && !list.isEmpty()) {
            return String.join("\n", list.stream().map(Object::toString).toList());
        }
        return val != null ? val.toString() : null;
    }

    /**
     * Gets a raw list of strings for a key.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public List<String> getRawList(@NotNull String langCode, @NotNull String key) {
        Object val = getRaw(langCode, key);
        if (val instanceof List<?> list) {
            return (List<String>) list;
        } else if (val instanceof String s) {
            return List.of(s);
        }
        return null;
    }

    /**
     * Clears all translations in this bundle.
     */
    public void clear() {
        languageTranslations.clear();
    }
}
