package com.harukiyu.claritylangcore.listeners;

import com.harukiyu.claritylangcore.api.Language;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.language.LanguageManager;
import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Handles client locale changes dynamically when players switch language in Minecraft settings.
 */
public class PlayerLocaleChangeListener implements Listener {

    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final PlayerLanguageManager playerLanguageManager;

    public PlayerLocaleChangeListener(@NotNull ConfigManager configManager,
                                      @NotNull LanguageManager languageManager,
                                      @NotNull PlayerLanguageManager playerLanguageManager) {
        this.configManager = configManager;
        this.languageManager = languageManager;
        this.playerLanguageManager = playerLanguageManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocaleChange(PlayerLocaleChangeEvent event) {
        if (!configManager.isAutoDetectClientLanguage()) {
            return;
        }

        Player player = event.getPlayer();
        // If the player has explicitly saved a manual preference, do not override it
        if (playerLanguageManager.getSavedPreference(player.getUniqueId()) != null) {
            return;
        }

        Locale newLocale = event.locale();
        Language matched = languageManager.findMatchingLocale(newLocale);
        if (matched != null) {
            // Player's effective language automatically shifts to the matched client language
            // No spam notification needed, but language lookups will immediately reflect this!
        }
    }
}
