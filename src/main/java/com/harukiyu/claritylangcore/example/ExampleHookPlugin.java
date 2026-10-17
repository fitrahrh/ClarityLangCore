package com.harukiyu.claritylangcore.example;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Example demonstrating how ANY other custom plugin on the server
 * can seamlessly hook into ClarityLangCore to request localized strings,
 * register bundles, and trigger the native dialog.
 */
public class ExampleHookPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // 1. Verify that ClarityLangCore is active on the server
        if (!getServer().getPluginManager().isPluginEnabled("ClarityLangCore")) {
            getLogger().warning("ClarityLangCore not found! Multi-language features disabled.");
            return;
        }

        // 2. Register your custom plugin's translation bundle from your jar's resources/languages folder!
        // Files like /languages/en_US.yml, /languages/id_ID.yml inside your jar will automatically be registered!
        ClarityLangAPI.registerBundle(this, "myplugin", "languages");

        // 3. Or register/override specific translation keys programmatically
        ClarityLangAPI.addTranslation("myplugin", "en_US", "daily_reward", "<green>You claimed: <gold><amount> coins</gold>!</green>");
        ClarityLangAPI.addTranslation("myplugin", "id_ID", "daily_reward", "<green>Anda mengklaim: <gold><amount> koin</gold>!</green>");

        getLogger().info("Successfully hooked into ClarityLangCore!");
    }

    /**
     * Example: Sending a localized message to a player in their selected language.
     */
    public void giveReward(Player player, int coins) {
        // ClarityLangCore automatically retrieves the player's chosen language (or auto-detected client locale)!
        ClarityLangAPI.sendMessage(player, "myplugin:daily_reward",
                Placeholder.unparsed("amount", String.valueOf(coins))
        );
    }

    /**
     * Example: Getting a Component for item lore or custom GUI titles.
     */
    public Component getMenuTitle(Player player) {
        return ClarityLangAPI.getComponent("myplugin:gui.title", player);
    }

    /**
     * Example: Getting multi-line Components (e.g. for item lore).
     */
    public List<Component> getItemLore(Player player) {
        return ClarityLangAPI.getComponentList("myplugin:items.sword.lore", player,
                Placeholder.unparsed("player", player.getName())
        );
    }

    /**
     * Example: Programmatically opening the Paper Native Dialog language menu for a player.
     */
    public void openLanguageSelector(Player player) {
        ClarityLangAPI.openLanguageDialog(player);
    }
}
