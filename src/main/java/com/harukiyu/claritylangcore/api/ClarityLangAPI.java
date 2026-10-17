package com.harukiyu.claritylangcore.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Static API facade for external plugins to interact with ClarityLangCore easily.
 */
public final class ClarityLangAPI {

    private static LanguageService service;

    private ClarityLangAPI() {
    }

    /**
     * Initializes the static API service instance (internal use only).
     */
    public static void setService(@NotNull LanguageService serviceInstance) {
        service = Objects.requireNonNull(serviceInstance, "service cannot be null");
    }

    /**
     * Gets the active LanguageService instance.
     */
    @NotNull
    public static LanguageService getService() {
        if (service == null) {
            throw new IllegalStateException("ClarityLangCore has not been initialized yet!");
        }
        return service;
    }

    /**
     * Retrieves a localized MiniMessage Component for a player based on their chosen language.
     *
     * @param key       Translation key (e.g. "core:commands.help" or "economy:balance")
     * @param player    The target player
     * @param resolvers Optional MiniMessage tag resolvers / placeholders
     * @return Formatted Component
     */
    @NotNull
    public static Component getComponent(@NotNull String key, @NotNull Player player, @NotNull TagResolver... resolvers) {
        return getService().getComponent(key, player, resolvers);
    }

    /**
     * Retrieves a localized MiniMessage Component for a specific language code.
     *
     * @param key       Translation key
     * @param langCode  Language code (e.g. "id_ID", "en_US")
     * @param resolvers Optional MiniMessage tag resolvers / placeholders
     * @return Formatted Component
     */
    @NotNull
    public static Component getComponent(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        return getService().getComponent(key, langCode, resolvers);
    }

    /**
     * Retrieves a list of localized Components (e.g. for item lore or multi-line broadcasts).
     */
    @NotNull
    public static List<Component> getComponentList(@NotNull String key, @NotNull Player player, @NotNull TagResolver... resolvers) {
        return getService().getComponentList(key, player, resolvers);
    }

    /**
     * Retrieves a list of localized Components for a specific language code.
     */
    @NotNull
    public static List<Component> getComponentList(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        return getService().getComponentList(key, langCode, resolvers);
    }

    /**
     * Gets the raw translation string.
     */
    @Nullable
    public static String getRawString(@NotNull String key, @NotNull String langCode) {
        return getService().getRawString(key, langCode);
    }

    /**
     * Sends a localized message directly to an audience (e.g. Player, Console).
     */
    public static void sendMessage(@NotNull Audience audience, @NotNull String key, @NotNull TagResolver... resolvers) {
        getService().sendMessage(audience, key, resolvers);
    }

    /**
     * Sends a localized message directly to a player using their personal language setting.
     */
    public static void sendMessage(@NotNull Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        getService().sendMessage(player, key, resolvers);
    }

    /**
     * Gets the effective language code for a player.
     */
    @NotNull
    public static String getPlayerLanguageCode(@NotNull Player player) {
        return getService().getPlayerLanguageCode(player);
    }

    /**
     * Gets the effective Language object for a player.
     */
    @NotNull
    public static Language getPlayerLanguage(@NotNull Player player) {
        return getService().getPlayerLanguage(player);
    }

    /**
     * Sets a player's manual language preference.
     */
    public static boolean setPlayerLanguage(@NotNull UUID playerUuid, @NotNull String langCode) {
        return getService().setPlayerLanguage(playerUuid, langCode);
    }

    /**
     * Resets a player's language preference to auto-detect.
     */
    public static void resetPlayerLanguage(@NotNull UUID playerUuid) {
        getService().resetPlayerLanguage(playerUuid);
    }

    /**
     * Gets all registered languages.
     */
    @NotNull
    public static Collection<Language> getRegisteredLanguages() {
        return getService().getRegisteredLanguages();
    }

    /**
     * Finds a language by its code.
     */
    @Nullable
    public static Language getLanguage(@NotNull String langCode) {
        return getService().getLanguage(langCode);
    }

    /**
     * Registers a translation bundle from another plugin's jar resources.
     */
    public static void registerBundle(@NotNull Plugin plugin, @NotNull String namespace, @NotNull String resourceFolderPath) {
        getService().registerBundle(plugin, namespace, resourceFolderPath);
    }

    /**
     * Registers a translation bundle from a disk directory.
     */
    public static void registerBundle(@NotNull String namespace, @NotNull File directory) {
        getService().registerBundle(namespace, directory);
    }

    /**
     * Adds or overrides a translation key in code.
     */
    public static void addTranslation(@NotNull String namespace, @NotNull String langCode, @NotNull String key, @NotNull Object value) {
        getService().addTranslation(namespace, langCode, key, value);
    }

    /**
     * Opens the Native Dialog language selection menu for a player.
     */
    public static void openLanguageDialog(@NotNull Player player) {
        getService().openLanguageDialog(player);
    }
}
