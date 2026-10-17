package com.harukiyu.claritylangcore.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a namespaced translation key, e.g. "core:commands.help" or "economy:balance".
 */
public record TranslationKey(@NotNull String namespace, @NotNull String key) {

    public static final String DEFAULT_NAMESPACE = "core";

    public TranslationKey {
        Objects.requireNonNull(namespace, "namespace cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
    }

    /**
     * Parses a raw string into a TranslationKey.
     * E.g. "economy:balance" -> namespace: "economy", key: "balance"
     * "commands.help" -> namespace: "core", key: "commands.help"
     */
    @NotNull
    public static TranslationKey parse(@NotNull String rawKey) {
        int colonIndex = rawKey.indexOf(':');
        if (colonIndex != -1) {
            String namespace = rawKey.substring(0, colonIndex).trim().toLowerCase();
            String key = rawKey.substring(colonIndex + 1).trim();
            return new TranslationKey(namespace.isEmpty() ? DEFAULT_NAMESPACE : namespace, key);
        }
        return new TranslationKey(DEFAULT_NAMESPACE, rawKey.trim());
    }

    @Override
    public String toString() {
        return namespace + ":" + key;
    }
}
