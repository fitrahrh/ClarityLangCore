package com.harukiyu.claritylangcore.listeners;

import com.harukiyu.claritylangcore.ClarityLangCore;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.dialog.LanguageDialogManager;
import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handles player join events and first-join native dialog prompt.
 */
public class PlayerJoinListener implements Listener {

    private final ClarityLangCore plugin;
    private final ConfigManager configManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final LanguageDialogManager dialogManager;

    public PlayerJoinListener(@NotNull ClarityLangCore plugin,
                              @NotNull ConfigManager configManager,
                              @NotNull PlayerLanguageManager playerLanguageManager,
                              @NotNull LanguageDialogManager dialogManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerLanguageManager = playerLanguageManager;
        this.dialogManager = dialogManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean firstJoin = playerLanguageManager.isFirstTimeJoiner(uuid);
        playerLanguageManager.markPlayerSeen(uuid);

        if (firstJoin && configManager.isPromptOnFirstJoin()) {
            long delay = configManager.getFirstJoinDelayTicks();
            // Folia and Paper compatible entity scheduler
            player.getScheduler().runDelayed(plugin, task -> {
                if (player.isOnline()) {
                    dialogManager.openLanguageDialog(player);
                }
            }, null, delay);
        }
    }
}
