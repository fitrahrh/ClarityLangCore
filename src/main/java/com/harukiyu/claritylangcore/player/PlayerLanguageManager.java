package com.harukiyu.claritylangcore.player;

import com.harukiyu.claritylangcore.ClarityLangCore;
import com.harukiyu.claritylangcore.api.Language;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Manages player language preferences, client locale detection, and persistent YAML storage.
 */
public class PlayerLanguageManager {

    private final ClarityLangCore plugin;
    private final ConfigManager configManager;
    private LanguageManager languageManager;

    private final File dataFile;
    private final Map<UUID, String> playerPreferences = new ConcurrentHashMap<>();
    private final Set<UUID> knownPlayers = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean isSaving = new AtomicBoolean(false);
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public PlayerLanguageManager(@NotNull ClarityLangCore plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    public void setLanguageManager(@NotNull LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    /**
     * Loads saved player preferences from playerdata.yml.
     */
    public final synchronized void loadData() {
        playerPreferences.clear();
        knownPlayers.clear();

        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        if (config.isConfigurationSection("players")) {
            for (String key : config.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    knownPlayers.add(uuid);
                    String lang = config.getString("players." + key + ".language");
                    if (lang != null && !lang.isBlank() && !lang.equalsIgnoreCase("AUTO")) {
                        playerPreferences.put(uuid, lang);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /**
     * Saves player preferences to disk asynchronously.
     */
    public void saveDataAsync() {
        dirty.set(true);
        if (isSaving.compareAndSet(false, true)) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                try {
                    saveDataInternal();
                } finally {
                    isSaving.set(false);
                    if (dirty.get()) {
                        saveDataAsync();
                    }
                }
            });
        }
    }

    /**
     * Saves synchronously (used on plugin disable).
     */
    public synchronized void saveDataSync() {
        saveDataInternal();
    }

    private synchronized void saveDataInternal() {
        dirty.set(false);
        YamlConfiguration config = new YamlConfiguration();
        for (UUID uuid : knownPlayers) {
            String lang = playerPreferences.get(uuid);
            config.set("players." + uuid + ".language", lang != null ? lang : "AUTO");
        }

        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e, () -> "Failed to save player language data to " + dataFile.getName());
        }
    }

    /**
     * Checks if this player is joining for the very first time.
     */
    public boolean isFirstTimeJoiner(@NotNull UUID uuid) {
        return !knownPlayers.contains(uuid);
    }

    /**
     * Marks player as known.
     */
    public void markPlayerSeen(@NotNull UUID uuid) {
        if (knownPlayers.add(uuid)) {
            saveDataAsync();
        }
    }

    /**
     * Gets the explicitly saved manual language preference for a player UUID.
     */
    @Nullable
    public String getSavedPreference(@NotNull UUID uuid) {
        return playerPreferences.get(uuid);
    }

    /**
     * Sets a manual language preference for a player.
     */
    public boolean setPlayerPreference(@NotNull UUID uuid, @NotNull String langCode) {
        Language lang = languageManager.getLanguage(langCode);
        if (lang == null) {
            return false;
        }

        playerPreferences.put(uuid, lang.getCode());
        knownPlayers.add(uuid);
        saveDataAsync();
        return true;
    }

    /**
     * Resets a player's language preference to auto-detect.
     */
    public void resetPlayerPreference(@NotNull UUID uuid) {
        playerPreferences.remove(uuid);
        knownPlayers.add(uuid);
        saveDataAsync();
    }

    /**
     * Determines the effective language for a player.
     * Priority:
     * 1. Manual user preference (if saved and valid)
     * 2. Client locale auto-detection (if enabled)
     * 3. Server default language
     */
    @NotNull
    public Language getEffectiveLanguage(@NotNull Player player) {
        // 1. Check manual preference
        String saved = playerPreferences.get(player.getUniqueId());
        if (saved != null) {
            Language lang = languageManager.getLanguage(saved);
            if (lang != null) {
                return lang;
            }
        }

        // 2. Client auto-detect
        if (configManager.isAutoDetectClientLanguage()) {
            Locale clientLocale = player.locale();
            Language detected = languageManager.findMatchingLocale(clientLocale);
            if (detected != null) {
                return detected;
            }
        }

        // 3. Server default fallback
        Language defaultLang = languageManager.getLanguage(configManager.getDefaultLanguage());
        if (defaultLang != null) {
            return defaultLang;
        }

        // 4. Ultimate fallback (first available or en_US)
        return languageManager.getRegisteredLanguages().stream()
                .findFirst()
                .orElse(new Language("en_US", "English", "🇬🇧", null));
    }

    /**
     * Gets the effective language code for a player.
     */
    @NotNull
    public String getEffectiveLanguageCode(@NotNull Player player) {
        return getEffectiveLanguage(player).getCode();
    }
}
