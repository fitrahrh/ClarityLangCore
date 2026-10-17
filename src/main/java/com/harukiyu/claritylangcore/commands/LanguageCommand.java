package com.harukiyu.claritylangcore.commands;

import com.harukiyu.claritylangcore.api.Language;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.dialog.LanguageDialogManager;
import com.harukiyu.claritylangcore.language.LanguageManager;
import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Command executor and tab completer for /lang, /language, and /bahasa.
 * Implements Paper BasicCommand for native Paper plugin registration
 * and Bukkit CommandExecutor / TabCompleter for backwards compatibility.
 */
public class LanguageCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final PlayerLanguageManager playerLanguageManager;
    private final LanguageDialogManager dialogManager;

    public LanguageCommand(@NotNull ConfigManager configManager,
                           @NotNull LanguageManager languageManager,
                           @NotNull PlayerLanguageManager playerLanguageManager,
                           @NotNull LanguageDialogManager dialogManager) {
        this.configManager = configManager;
        this.languageManager = languageManager;
        this.playerLanguageManager = playerLanguageManager;
        this.dialogManager = dialogManager;
    }

    // ========================================================
    // Paper BasicCommand Implementation
    // ========================================================

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        executeLogic(source.getSender(), args);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        return suggestLogic(source.getSender(), args);
    }

    @Override
    public @Nullable String permission() {
        return "claritylang.use";
    }

    // ========================================================
    // Bukkit CommandExecutor & TabCompleter (Fallback)
    // ========================================================

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        return executeLogic(sender, args);
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String label,
                                      @NotNull String[] args) {
        return suggestLogic(sender, args);
    }

    // ========================================================
    // Unified Command & Tab Logic
    // ========================================================

    public boolean executeLogic(@NotNull CommandSender sender, @NotNull String[] args) {
        // 1. No arguments: Open Native Dialog for players
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                languageManager.sendMessage(sender, "core:commands.help");
                return true;
            }
            dialogManager.openLanguageDialog(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // 2. Subcommand: reload
        if (sub.equals("reload")) {
            if (!sender.hasPermission("claritylang.admin")) {
                languageManager.sendMessage(sender, "core:commands.no_permission");
                return true;
            }
            long time = languageManager.reload();
            languageManager.sendMessage(sender, "core:commands.reload_success",
                    Placeholder.unparsed("time", String.valueOf(time))
            );
            return true;
        }

        // 3. Subcommand: help
        if (sub.equals("help")) {
            languageManager.sendMessage(sender, "core:commands.help");
            return true;
        }

        // 4. Subcommand: list
        if (sub.equals("list")) {
            Collection<Language> registered = languageManager.getRegisteredLanguages();
            languageManager.sendMessage(sender, "core:commands.list_header");

            String activeCode = (sender instanceof Player p) ?
                    playerLanguageManager.getEffectiveLanguageCode(p) :
                    configManager.getDefaultLanguage();

            String activeTagStr = languageManager.getRawString("core:commands.list_active_tag", activeCode);
            if (activeTagStr == null) activeTagStr = "[Active]";

            for (Language lang : registered) {
                boolean isActive = lang.getCode().equalsIgnoreCase(activeCode);
                languageManager.sendMessage(sender, "core:commands.list_entry",
                        Placeholder.unparsed("flag", lang.getFlag()),
                        Placeholder.unparsed("lang_name", lang.getName()),
                        Placeholder.unparsed("lang_code", lang.getCode()),
                        Placeholder.parsed("active", isActive ? activeTagStr : "")
                );
            }
            return true;
        }

        // 5. Subcommand: reset (players only)
        if (sub.equals("reset")) {
            if (!(sender instanceof Player player)) {
                languageManager.sendMessage(sender, "core:commands.only_players");
                return true;
            }

            playerLanguageManager.resetPlayerPreference(player.getUniqueId());
            Language detected = playerLanguageManager.getEffectiveLanguage(player);

            if (configManager.isSoundEnabled()) {
                player.playSound(player.getLocation(), configManager.getSuccessSound(), 1.0f, 1.2f);
            }

            languageManager.sendMessage(player, "core:commands.reset",
                    Placeholder.unparsed("lang_name", detected.getName()),
                    Placeholder.unparsed("lang_code", detected.getCode())
            );
            return true;
        }

        // 6. Direct language change: /lang <code/name>
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "core:commands.only_players");
            return true;
        }

        Language targetLang = languageManager.getLanguage(args[0]);
        if (targetLang == null) {
            languageManager.sendMessage(player, "core:commands.invalid_language",
                    Placeholder.unparsed("lang", args[0])
            );
            return true;
        }

        playerLanguageManager.setPlayerPreference(player.getUniqueId(), targetLang.getCode());

        if (configManager.isSoundEnabled()) {
            player.playSound(player.getLocation(), configManager.getSuccessSound(), 1.0f, 1.2f);
        }

        languageManager.sendMessage(player, "core:commands.success",
                Placeholder.unparsed("lang_name", targetLang.getName()),
                Placeholder.unparsed("lang_code", targetLang.getCode())
        );
        return true;
    }

    public List<String> suggestLogic(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String prefix = args[0].toLowerCase(Locale.ROOT);

            // Subcommands
            if ("list".startsWith(prefix)) suggestions.add("list");
            if ("reset".startsWith(prefix)) suggestions.add("reset");
            if ("help".startsWith(prefix)) suggestions.add("help");
            if (sender.hasPermission("claritylang.admin") && "reload".startsWith(prefix)) {
                suggestions.add("reload");
            }

            // Language codes and names
            for (Language lang : languageManager.getRegisteredLanguages()) {
                if (lang.getCode().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    suggestions.add(lang.getCode());
                }
            }

            return suggestions;
        }
        return List.of();
    }
}
