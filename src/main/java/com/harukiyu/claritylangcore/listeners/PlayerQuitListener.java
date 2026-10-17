package com.harukiyu.claritylangcore.listeners;

import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles player quit events and data saves.
 */
public class PlayerQuitListener implements Listener {

    private final PlayerLanguageManager playerLanguageManager;

    public PlayerQuitListener(@NotNull PlayerLanguageManager playerLanguageManager) {
        this.playerLanguageManager = playerLanguageManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Ensure any pending preferences are saved
        playerLanguageManager.saveDataAsync();
    }
}
