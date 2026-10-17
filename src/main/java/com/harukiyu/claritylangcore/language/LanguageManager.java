package com.harukiyu.claritylangcore.language;

import com.harukiyu.claritylangcore.ClarityLangCore;
import com.harukiyu.claritylangcore.api.Language;
import com.harukiyu.claritylangcore.api.LanguageService;
import com.harukiyu.claritylangcore.api.TranslationKey;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.dialog.LanguageDialogManager;
import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Core centralized language manager that implements {@link LanguageService}.
 */
public class LanguageManager implements LanguageService {

    private final ClarityLangCore plugin;
    private final ConfigManager configManager;
    private PlayerLanguageManager playerLanguageManager;
    private LanguageDialogManager dialogManager;

    private final Map<String, Language> languages = new ConcurrentHashMap<>();
    private final Map<String, TranslationBundle> bundles = new ConcurrentHashMap<>();
    private final File languagesFolder;

    private static final List<String> DEFAULT_LANG_FILES = List.of(
            "en_US.yml", "id_ID.yml", "pt_BR.yml", "es_ES.yml",
            "zh_CN.yml", "ja_JP.yml", "de_DE.yml", "fr_FR.yml", "ru_RU.yml"
    );

    public LanguageManager(@NotNull ClarityLangCore plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.languagesFolder = new File(plugin.getDataFolder(), "languages");
        loadAll();
    }

    public void setPlayerLanguageManager(@NotNull PlayerLanguageManager playerLanguageManager) {
        this.playerLanguageManager = playerLanguageManager;
    }

    public void setDialogManager(@NotNull LanguageDialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }

    /**
     * Extracts default files and loads all language bundles from disk.
     */
    public final synchronized void loadAll() {
        languages.clear();
        bundles.clear();

        // 1. Ensure languages folder exists
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();
        }

        // 2. Extract default embedded language resources if not already present
        for (String file : DEFAULT_LANG_FILES) {
            File target = new File(languagesFolder, file);
            if (!target.exists()) {
                plugin.saveResource("languages/" + file, false);
            }
        }

        // 3. Load all .yml files in languages/ folder
        File[] files = languagesFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            for (File file : files) {
                loadFile(file);
            }
        }

        // 4. Load modular plugin translations from languages/plugins/<PluginName>/*.yml
        File pluginsFolder = new File(languagesFolder, "plugins");
        if (pluginsFolder.exists() && pluginsFolder.isDirectory()) {
            File[] pluginDirs = pluginsFolder.listFiles(File::isDirectory);
            if (pluginDirs != null) {
                for (File pDir : pluginDirs) {
                    String namespace = pDir.getName().toLowerCase();
                    File[] pFiles = pDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
                    if (pFiles != null) {
                        for (File pFile : pFiles) {
                            loadPluginFile(namespace, pFile);
                        }
                    }
                }
            }
        }

        plugin.getLogger().info(() -> "Loaded " + languages.size() + " languages and " + bundles.size() + " translation bundles.");
    }

    private void loadFile(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Read Language metadata
            ConfigurationSection langSection = config.getConfigurationSection("language");
            if (langSection == null) {
                plugin.getLogger().warning(() -> "Skipping " + file.getName() + ": missing 'language' metadata section.");
                return;
            }

            String code = langSection.getString("code");
            String name = langSection.getString("name");
            String flag = langSection.getString("flag", "🌐");
            String fallback = langSection.getString("fallback", "en_US");

            if (code == null || name == null) {
                plugin.getLogger().warning(() -> "Skipping " + file.getName() + ": missing 'code' or 'name' in metadata.");
                return;
            }

            Language language = new Language(code, name, flag, fallback);
            languages.put(code.toLowerCase(), language);

            // Read all root sections into their respective translation bundles
            for (String rootKey : config.getKeys(false)) {
                if (rootKey.equalsIgnoreCase("language")) continue;

                if (config.isConfigurationSection(rootKey)) {
                    TranslationBundle bundle = bundles.computeIfAbsent(rootKey.toLowerCase(), TranslationBundle::new);
                    ConfigurationSection section = config.getConfigurationSection(rootKey);
                    if (section != null) {
                        bundle.loadSection(code, section);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, e, () -> "Failed to load language file: " + file.getName());
        }
    }

    private void loadPluginFile(String namespace, File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String fileName = file.getName();
            String langCode = fileName.substring(0, fileName.lastIndexOf('.'));

            TranslationBundle bundle = bundles.computeIfAbsent(namespace.toLowerCase(), TranslationBundle::new);
            bundle.loadSection(langCode, config);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load external plugin language file: " + file.getPath(), e);
        }
    }

    @Nullable
    public Language findMatchingLocale(@NotNull Locale locale) {
        for (Language lang : languages.values()) {
            if (lang.matchesLocale(locale)) {
                return lang;
            }
        }
        return null;
    }

    @Override
    @NotNull
    public Component getComponent(@NotNull String key, @NotNull Player player, @NotNull TagResolver... resolvers) {
        String langCode = getPlayerLanguageCode(player);
        return formatMessage(key, langCode, resolvers);
    }

    @Override
    @NotNull
    public Component getComponent(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        return formatMessage(key, langCode, resolvers);
    }

    @NotNull
    private Component formatMessage(@NotNull String key, @NotNull String langCode, @NotNull TagResolver[] resolvers) {
        String raw = getRawString(key, langCode);
        if (raw == null) {
            return Component.text("<" + key + ">");
        }

        List<TagResolver> resolverList = new ArrayList<>(resolvers.length + 1);
        Collections.addAll(resolverList, resolvers);

        if (raw.contains("<prefix>")) {
            String prefix = getRawString("core:prefix", langCode);
            resolverList.add(Placeholder.parsed("prefix", prefix != null ? prefix : ""));
        }

        return MessageFormatter.format(raw, resolverList.toArray(TagResolver[]::new));
    }

    @Override
    @NotNull
    public List<Component> getComponentList(@NotNull String key, @NotNull Player player, @NotNull TagResolver... resolvers) {
        String langCode = getPlayerLanguageCode(player);
        return formatMessageList(key, langCode, resolvers);
    }

    @Override
    @NotNull
    public List<Component> getComponentList(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        return formatMessageList(key, langCode, resolvers);
    }

    @NotNull
    private List<Component> formatMessageList(@NotNull String key, @NotNull String langCode, @NotNull TagResolver[] resolvers) {
        List<String> rawList = getRawStringList(key, langCode);
        if (rawList == null || rawList.isEmpty()) {
            return List.of(Component.text("<" + key + ">"));
        }

        List<TagResolver> resolverList = new ArrayList<>(resolvers.length + 1);
        Collections.addAll(resolverList, resolvers);

        String prefix = null;
        for (String line : rawList) {
            if (line.contains("<prefix>") && prefix == null) {
                prefix = getRawString("core:prefix", langCode);
                if (prefix != null) {
                    resolverList.add(Placeholder.parsed("prefix", prefix));
                }
                break;
            }
        }

        return MessageFormatter.formatList(rawList, resolverList.toArray(TagResolver[]::new));
    }

    @Override
    @Nullable
    public String getRawString(@NotNull String rawKey, @NotNull String langCode) {
        TranslationKey key = TranslationKey.parse(rawKey);
        TranslationBundle bundle = bundles.get(key.namespace());
        if (bundle == null) return null;

        // 1. Try requested language
        String result = bundle.getRawString(langCode, key.key());
        if (result != null) return result;

        // 2. Try fallback language configured for this language
        Language lang = getLanguage(langCode);
        if (lang != null && !lang.getFallbackCode().equalsIgnoreCase(langCode)) {
            result = bundle.getRawString(lang.getFallbackCode(), key.key());
            if (result != null) return result;
        }

        // 3. Try server default language
        String defaultCode = configManager.getDefaultLanguage();
        if (!defaultCode.equalsIgnoreCase(langCode)) {
            result = bundle.getRawString(defaultCode, key.key());
            if (result != null) return result;
        }

        // 4. Try English (en_US)
        if (!langCode.equalsIgnoreCase("en_US") && !defaultCode.equalsIgnoreCase("en_US")) {
            result = bundle.getRawString("en_US", key.key());
            if (result != null) return result;
        }

        return null;
    }

    @Override
    @Nullable
    public List<String> getRawStringList(@NotNull String rawKey, @NotNull String langCode) {
        TranslationKey key = TranslationKey.parse(rawKey);
        TranslationBundle bundle = bundles.get(key.namespace());
        if (bundle == null) return null;

        List<String> result = bundle.getRawList(langCode, key.key());
        if (result != null) return result;

        Language lang = getLanguage(langCode);
        if (lang != null && !lang.getFallbackCode().equalsIgnoreCase(langCode)) {
            result = bundle.getRawList(lang.getFallbackCode(), key.key());
            if (result != null) return result;
        }

        String defaultCode = configManager.getDefaultLanguage();
        if (!defaultCode.equalsIgnoreCase(langCode)) {
            result = bundle.getRawList(defaultCode, key.key());
            if (result != null) return result;
        }

        if (!langCode.equalsIgnoreCase("en_US") && !defaultCode.equalsIgnoreCase("en_US")) {
            result = bundle.getRawList("en_US", key.key());
            if (result != null) return result;
        }

        return null;
    }

    @Override
    public void sendMessage(@NotNull Audience audience, @NotNull String key, @NotNull TagResolver... resolvers) {
        if (audience instanceof Player player) {
            sendMessage(player, key, resolvers);
        } else {
            // Console or other audience uses server default language
            Component component = getComponent(key, configManager.getDefaultLanguage(), resolvers);
            audience.sendMessage(component);
        }
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        String langCode = getPlayerLanguageCode(player);
        List<String> list = getRawStringList(key, langCode);
        if (list != null && list.size() > 1) {
            List<Component> components = getComponentList(key, player, resolvers);
            for (Component line : components) {
                player.sendMessage(line);
            }
        } else {
            Component component = getComponent(key, player, resolvers);
            player.sendMessage(component);
        }
    }

    @Override
    @Nullable
    public String getSavedPlayerLanguage(@NotNull UUID playerUuid) {
        return playerLanguageManager.getSavedPreference(playerUuid);
    }

    @Override
    @NotNull
    public String getPlayerLanguageCode(@NotNull Player player) {
        return playerLanguageManager.getEffectiveLanguageCode(player);
    }

    @Override
    @NotNull
    public Language getPlayerLanguage(@NotNull Player player) {
        return playerLanguageManager.getEffectiveLanguage(player);
    }

    @Override
    public boolean setPlayerLanguage(@NotNull UUID playerUuid, @NotNull String langCode) {
        return playerLanguageManager.setPlayerPreference(playerUuid, langCode);
    }

    @Override
    public void resetPlayerLanguage(@NotNull UUID playerUuid) {
        playerLanguageManager.resetPlayerPreference(playerUuid);
    }

    @Override
    @NotNull
    public Collection<Language> getRegisteredLanguages() {
        return Collections.unmodifiableCollection(languages.values());
    }

    @Override
    @Nullable
    public Language getLanguage(@NotNull String langCode) {
        Language direct = languages.get(langCode.toLowerCase());
        if (direct != null) return direct;

        for (Language lang : languages.values()) {
            if (lang.matches(langCode)) {
                return lang;
            }
        }
        return null;
    }

    @Override
    public void registerBundle(@NotNull Plugin plugin, @NotNull String namespace, @NotNull String resourceFolderPath) {
        String cleanPath = resourceFolderPath.startsWith("/") ? resourceFolderPath.substring(1) : resourceFolderPath;
        cleanPath = cleanPath.endsWith("/") ? cleanPath : cleanPath + "/";

        TranslationBundle bundle = bundles.computeIfAbsent(namespace.toLowerCase(), TranslationBundle::new);

        for (Language lang : languages.values()) {
            String path = cleanPath + lang.getCode() + ".yml";
            try (InputStream stream = plugin.getResource(path)) {
                if (stream != null) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    bundle.loadSection(lang.getCode(), config);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, e, () -> "Failed to load bundle resource: " + path + " for plugin " + plugin.getName());
            }
        }

        this.plugin.getLogger().info(() -> "Registered translation bundle '" + namespace + "' for plugin: " + plugin.getName());
    }

    @Override
    public void registerBundle(@NotNull String namespace, @NotNull File directory) {
        if (!directory.exists() || !directory.isDirectory()) return;

        TranslationBundle bundle = bundles.computeIfAbsent(namespace.toLowerCase(), TranslationBundle::new);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String code = name.substring(0, name.lastIndexOf('.'));
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    bundle.loadSection(code, config);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, e, () -> "Failed to load file " + file.getPath() + " for bundle " + namespace);
                }
            }
        }
    }

    @Override
    public void addTranslation(@NotNull String namespace, @NotNull String langCode, @NotNull String key, @NotNull Object value) {
        TranslationBundle bundle = bundles.computeIfAbsent(namespace.toLowerCase(), TranslationBundle::new);
        bundle.setTranslation(langCode, key, value);
    }

    @Override
    public void openLanguageDialog(@NotNull Player player) {
        if (dialogManager != null) {
            dialogManager.openLanguageDialog(player);
        }
    }

    @Override
    public long reload() {
        long start = System.currentTimeMillis();
        configManager.loadConfig();
        loadAll();
        playerLanguageManager.loadData();
        return System.currentTimeMillis() - start;
    }
}
