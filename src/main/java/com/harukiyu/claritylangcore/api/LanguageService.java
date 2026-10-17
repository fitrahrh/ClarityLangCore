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
import java.util.UUID;

/**
 * Service interface for multi-language management.
 * Can be retrieved via {@link ClarityLangAPI} or Bukkit's ServicesManager.
 */
public interface LanguageService {

    /**
     * Retrieves a localized MiniMessage Component for a player based on their chosen language.
     *
     * @param key       Translation key (e.g. "core:commands.help" or "economy:balance")
     * @param player    The target player
     * @param resolvers Optional MiniMessage tag resolvers / placeholders
     * @return Formatted Component
     */
    @NotNull
    Component getComponent(@NotNull String key, @NotNull Player player, @NotNull TagResolver... resolvers);

    /**
     * Retrieves a localized MiniMessage Component for a specific language code.
     *
     * @param key       Translation key
     * @param langCode  Language code (e.g. "id_ID", "en_US")
     * @param resolvers Optional MiniMessage tag resolvers / placeholders
     * @return Formatted Component
     */
    @NotNull
    Component getComponent(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers);

    /**
     * Retrieves a list of localized Components (e.g. for multi-line messages, item lore, books).
     *
     * @param key       Translation key
     * @param player    The target player
     * @param resolvers Optional MiniMessage tag resolvers / placeholders
     * @return List of formatted Components
     */
    @NotNull
    List<Component> getComponentList(@NotNull String key, @NotNull Player player, @NotNull TagResolver... resolvers);

    /**
     * Retrieves a list of localized Components for a specific language code.
     */
    @NotNull
    List<Component> getComponentList(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers);

    /**
     * Gets the raw string translation from the bundle without formatting.
     */
    @Nullable
    String getRawString(@NotNull String key, @NotNull String langCode);

    /**
     * Gets the raw list of strings for a key.
     */
    @Nullable
    List<String> getRawStringList(@NotNull String key, @NotNull String langCode);

    /**
     * Sends a localized message directly to an audience (e.g. Player, Console).
     */
    void sendMessage(@NotNull Audience audience, @NotNull String key, @NotNull TagResolver... resolvers);

    /**
     * Sends a localized message directly to a player using their personal language setting.
     */
    void sendMessage(@NotNull Player player, @NotNull String key, @NotNull TagResolver... resolvers);

    /**
     * Gets the explicitly saved language code for a player UUID, or null if they use auto-detect.
     */
    @Nullable
    String getSavedPlayerLanguage(@NotNull UUID playerUuid);

    /**
     * Gets the effective language code for a player (checking manual preference -> client auto-detect -> server default).
     */
    @NotNull
    String getPlayerLanguageCode(@NotNull Player player);

    /**
     * Gets the effective {@link Language} object for a player.
     */
    @NotNull
    Language getPlayerLanguage(@NotNull Player player);

    /**
     * Sets a player's manual language preference.
     *
     * @param playerUuid The player UUID
     * @param langCode   The language code (e.g. "id_ID")
     * @return true if successfully set and language exists, false otherwise
     */
    boolean setPlayerLanguage(@NotNull UUID playerUuid, @NotNull String langCode);

    /**
     * Resets a player's language preference to auto-detect from client locale.
     */
    void resetPlayerLanguage(@NotNull UUID playerUuid);

    /**
     * Gets all currently registered languages.
     */
    @NotNull
    Collection<Language> getRegisteredLanguages();

    /**
     * Finds a registered language by code or query (case-insensitive).
     */
    @Nullable
    Language getLanguage(@NotNull String langCode);

    /**
     * Registers a translation bundle from another plugin's embedded resource folder.
     *
     * @param plugin             The custom plugin registering translations
     * @param namespace          Unique namespace (e.g. "economy", "clans")
     * @param resourceFolderPath Path to language files inside the jar (e.g. "languages")
     */
    void registerBundle(@NotNull Plugin plugin, @NotNull String namespace, @NotNull String resourceFolderPath);

    /**
     * Registers a translation bundle from a physical folder on disk.
     */
    void registerBundle(@NotNull String namespace, @NotNull File directory);

    /**
     * Adds or overrides a single translation dynamically in code.
     */
    void addTranslation(@NotNull String namespace, @NotNull String langCode, @NotNull String key, @NotNull Object value);

    /**
     * Opens the interactive Native Dialog language selection menu for a player.
     */
    void openLanguageDialog(@NotNull Player player);

    /**
     * Reloads all configuration and translation files.
     *
     * @return Elapsed milliseconds taken to reload
     */
    long reload();
}
