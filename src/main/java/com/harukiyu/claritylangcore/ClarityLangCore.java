package com.harukiyu.claritylangcore;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import com.harukiyu.claritylangcore.api.LanguageService;
import com.harukiyu.claritylangcore.commands.LanguageCommand;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.dialog.LanguageDialogManager;
import com.harukiyu.claritylangcore.language.LanguageManager;
import com.harukiyu.claritylangcore.listeners.PlayerJoinListener;
import com.harukiyu.claritylangcore.listeners.PlayerLocaleChangeListener;
import com.harukiyu.claritylangcore.listeners.PlayerQuitListener;
import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ClarityLangCore - Centralized Modular Multi-Language Core for Paper 26.3.
 */
public class ClarityLangCore extends JavaPlugin {

    private static ClarityLangCore instance;

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private PlayerLanguageManager playerLanguageManager;
    private LanguageDialogManager dialogManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize Configuration
        this.configManager = new ConfigManager(this);

        // 2. Initialize Player Preference Manager & Language Manager
        this.playerLanguageManager = new PlayerLanguageManager(this, configManager);
        this.languageManager = new LanguageManager(this, configManager);

        // Wire dependencies
        this.playerLanguageManager.setLanguageManager(languageManager);
        this.languageManager.setPlayerLanguageManager(playerLanguageManager);

        // 3. Initialize Native Dialog API Manager
        this.dialogManager = new LanguageDialogManager(configManager, languageManager, playerLanguageManager);
        this.languageManager.setDialogManager(dialogManager);

        // 4. Register Services & API
        ClarityLangAPI.setService(languageManager);
        getServer().getServicesManager().register(LanguageService.class, languageManager, this, ServicePriority.High);

        // 5. Register Commands (Paper 26.3 Native Command Registration)
        LanguageCommand commandHandler = new LanguageCommand(configManager, languageManager, playerLanguageManager, dialogManager);
        registerCommand("lang", "Open language selection menu or change language", List.of("language", "bahasa"), commandHandler);

        // 6. Register Event Listeners
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this, configManager, playerLanguageManager, dialogManager), this);
        pm.registerEvents(new PlayerQuitListener(playerLanguageManager), this);
        pm.registerEvents(new PlayerLocaleChangeListener(configManager, languageManager, playerLanguageManager), this);

        getLogger().info("=================================================");
        getLogger().info(() -> " ClarityLangCore v" + getPluginMeta().getVersion() + " has been successfully enabled!");
        getLogger().info(() -> " Registered Languages: " + languageManager.getRegisteredLanguages().size());
        getLogger().info(" Native Dialog API: Paper 26.3 Ready");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (playerLanguageManager != null) {
            playerLanguageManager.saveDataSync();
        }
        getLogger().info(() -> getName() + " has been cleanly disabled!");
    }

    @NotNull
    public static ClarityLangCore getInstance() {
        return instance;
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @NotNull
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    @NotNull
    public PlayerLanguageManager getPlayerLanguageManager() {
        return playerLanguageManager;
    }

    @NotNull
    public LanguageDialogManager getDialogManager() {
        return dialogManager;
    }
}
