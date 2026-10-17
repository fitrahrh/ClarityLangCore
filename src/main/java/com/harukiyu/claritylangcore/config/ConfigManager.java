package com.harukiyu.claritylangcore.config;

import com.harukiyu.claritylangcore.ClarityLangCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Manages plugin configuration settings.
 */
public class ConfigManager {

    public enum DialogMode {
        MULTI_ACTION,
        SINGLE_OPTION
    }

    private final ClarityLangCore plugin;

    private String defaultLanguage;
    private boolean autoDetectClientLanguage;
    private boolean promptOnFirstJoin;
    private long firstJoinDelayTicks;

    private DialogMode dialogMode;
    private int dialogColumns;
    private boolean canCloseWithEscape;
    private boolean showNoticeAfterSelection;

    private boolean soundEnabled;
    private Sound successSound;
    private Sound openDialogSound;

    private String storageType;
    private boolean asyncSave;

    public ConfigManager(@NotNull ClarityLangCore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads or reloads the configuration from config.yml.
     */
    public final void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.defaultLanguage = config.getString("default_language", "en_US");
        this.autoDetectClientLanguage = config.getBoolean("auto_detect_client_language", true);
        this.promptOnFirstJoin = config.getBoolean("prompt_on_first_join", true);
        this.firstJoinDelayTicks = config.getLong("first_join_delay_ticks", 25L);

        // Dialog
        String modeStr = config.getString("dialog.mode", "MULTI_ACTION").toUpperCase();
        try {
            this.dialogMode = DialogMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            this.dialogMode = DialogMode.MULTI_ACTION;
        }

        this.dialogColumns = Math.max(1, Math.min(6, config.getInt("dialog.columns", 2)));
        this.canCloseWithEscape = config.getBoolean("dialog.can_close_with_escape", true);
        this.showNoticeAfterSelection = config.getBoolean("dialog.show_notice_after_selection", false);

        // Sound
        this.soundEnabled = config.getBoolean("sound.enabled", true);
        this.successSound = parseSound(config.getString("sound.success", "minecraft:entity.experience_orb.pickup"), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.openDialogSound = parseSound(config.getString("sound.open_dialog", "minecraft:ui.button.click"), Sound.UI_BUTTON_CLICK);

        // Storage
        this.storageType = config.getString("storage.type", "YAML");
        this.asyncSave = config.getBoolean("storage.async_save", true);
    }

    @SuppressWarnings("removal")
    private Sound parseSound(String key, Sound fallback) {
        if (key == null) return fallback;
        try {
            String soundName = key.replace("minecraft:", "").replace('.', '_').toUpperCase();
            return Sound.valueOf(soundName);
        } catch (Exception e) {
            for (Sound s : Sound.values()) {
                if (s.name().equalsIgnoreCase(key) || s.getKey().asString().equalsIgnoreCase(key)) {
                    return s;
                }
            }
            plugin.getLogger().log(Level.WARNING, () -> "Unknown sound effect: " + key + ", using fallback.");
            return fallback;
        }
    }

    @NotNull
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public boolean isAutoDetectClientLanguage() {
        return autoDetectClientLanguage;
    }

    public boolean isPromptOnFirstJoin() {
        return promptOnFirstJoin;
    }

    public long getFirstJoinDelayTicks() {
        return firstJoinDelayTicks;
    }

    @NotNull
    public DialogMode getDialogMode() {
        return dialogMode;
    }

    public int getDialogColumns() {
        return dialogColumns;
    }

    public boolean isCanCloseWithEscape() {
        return canCloseWithEscape;
    }

    public boolean isShowNoticeAfterSelection() {
        return showNoticeAfterSelection;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    @NotNull
    public Sound getSuccessSound() {
        return successSound;
    }

    @NotNull
    public Sound getOpenDialogSound() {
        return openDialogSound;
    }

    @NotNull
    public String getStorageType() {
        return storageType;
    }

    public boolean isAsyncSave() {
        return asyncSave;
    }
}
