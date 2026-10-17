package com.harukiyu.claritylangcore.dialog;

import com.harukiyu.claritylangcore.api.Language;
import com.harukiyu.claritylangcore.config.ConfigManager;
import com.harukiyu.claritylangcore.language.LanguageManager;
import com.harukiyu.claritylangcore.player.PlayerLanguageManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Manages native Minecraft Dialogs for language selection using Paper 26.3 Native Dialog API.
 */
public class LanguageDialogManager {

    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final PlayerLanguageManager playerLanguageManager;

    public LanguageDialogManager(@NotNull ConfigManager configManager,
                                 @NotNull LanguageManager languageManager,
                                 @NotNull PlayerLanguageManager playerLanguageManager) {
        this.configManager = configManager;
        this.languageManager = languageManager;
        this.playerLanguageManager = playerLanguageManager;
    }

    /**
     * Opens the interactive Native Dialog language selection menu for a player.
     */
    public void openLanguageDialog(@NotNull Player player) {
        // Play open sound
        if (configManager.isSoundEnabled()) {
            player.playSound(player.getLocation(), configManager.getOpenDialogSound(), 1.0f, 1.0f);
        }

        Language currentLang = playerLanguageManager.getEffectiveLanguage(player);
        Locale clientLocale = player.locale();
        Language clientLang = languageManager.findMatchingLocale(clientLocale);

        if (configManager.getDialogMode() == ConfigManager.DialogMode.SINGLE_OPTION) {
            openSingleOptionDialog(player, currentLang, clientLang, clientLocale);
        } else {
            openMultiActionDialog(player, currentLang, clientLang, clientLocale);
        }
    }

    /**
     * Builds and opens a clean MULTI_ACTION button grid dialog.
     */
    private void openMultiActionDialog(@NotNull Player player,
                                       @NotNull Language currentLang,
                                       Language clientLang,
                                       @NotNull Locale clientLocale) {
        // Dialog Title
        Component title = languageManager.getComponent("core:dialog.title", player);

        // Dialog Body (unified into a single clean body without excessive padding/scrollbars)
        List<DialogBody> bodyElements = buildDialogBody(player, currentLang, clientLang, clientLocale);

        // Action Buttons for each language
        Collection<Language> registered = languageManager.getRegisteredLanguages();
        List<ActionButton> buttons = new ArrayList<>();

        for (Language lang : registered) {
            boolean isActive = lang.getCode().equalsIgnoreCase(currentLang.getCode());
            String badge = isActive ? configManager.getActiveBadge() : "";
            String flagPart = (configManager.isShowFlags() && lang.getFlag() != null && !lang.getFlag().isBlank())
                    ? lang.getFlag() + " " : "";

            Component label = Component.text(badge + flagPart + lang.getName());

            Component tooltip = languageManager.getComponent("core:dialog.button_tooltip", player,
                    Placeholder.unparsed("lang_name", lang.getName())
            );

            ActionButton button = ActionButton.builder(label)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            handleLanguageSelect(p, lang);
                        }
                    }, DEFAULT_OPTIONS))
                    .build();

            buttons.add(button);
        }

        // Cancel Button
        ActionButton cancelBtn = ActionButton.builder(
                languageManager.getComponent("core:dialog.button_cancel", player)
        )
                .tooltip(languageManager.getComponent("core:dialog.button_cancel_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS))
                .build();

        int columns = configManager.getDialogColumns();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(configManager.isCanCloseWithEscape())
                        .body(bodyElements)
                        .build())
                .type(DialogType.multiAction(buttons, cancelBtn, columns))
        );

        player.showDialog(dialog);
    }

    /**
     * Builds and opens a SINGLE_OPTION radio button dialog with confirm/cancel buttons.
     */
    private void openSingleOptionDialog(@NotNull Player player,
                                        @NotNull Language currentLang,
                                        Language clientLang,
                                        @NotNull Locale clientLocale) {
        Component title = languageManager.getComponent("core:dialog.title", player);
        List<DialogBody> bodyElements = buildDialogBody(player, currentLang, clientLang, clientLocale);

        // Build radio option entries
        Collection<Language> registered = languageManager.getRegisteredLanguages();
        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();

        for (Language lang : registered) {
            boolean isActive = lang.getCode().equalsIgnoreCase(currentLang.getCode());
            String flagPart = (configManager.isShowFlags() && lang.getFlag() != null && !lang.getFlag().isBlank())
                    ? lang.getFlag() + " " : "";
            Component optionLabel = Component.text(flagPart + lang.getName());
            entries.add(SingleOptionDialogInput.OptionEntry.create(lang.getCode(), optionLabel, isActive));
        }

        Component radioLabel = languageManager.getComponent("core:dialog.radio_label", player);
        SingleOptionDialogInput radioInput = DialogInput.singleOption(
                "selected_language",
                250,
                entries,
                radioLabel,
                true
        );

        // Confirmation Button
        ActionButton confirmBtn = ActionButton.builder(
                languageManager.getComponent("core:dialog.button_confirm", player)
        )
                .tooltip(languageManager.getComponent("core:dialog.button_confirm_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && view != null) {
                        String selectedCode = view.getText("selected_language");
                        if (selectedCode != null) {
                            Language chosen = languageManager.getLanguage(selectedCode);
                            if (chosen != null) {
                                handleLanguageSelect(p, chosen);
                                return;
                            }
                        }
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS))
                .build();

        // Cancel Button
        ActionButton cancelBtn = ActionButton.builder(
                languageManager.getComponent("core:dialog.button_cancel", player)
        )
                .tooltip(languageManager.getComponent("core:dialog.button_cancel_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(configManager.isCanCloseWithEscape())
                        .body(bodyElements)
                        .inputs(List.of(radioInput))
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }

    /**
     * Builds a single unified dialog body component to keep the UI clean, compact,
     * and avoid scrollbars in Minecraft's modal screen.
     */
    private List<DialogBody> buildDialogBody(@NotNull Player player,
                                             @NotNull Language currentLang,
                                             Language clientLang,
                                             @NotNull Locale clientLocale) {
        String currentFlagStr = (configManager.isShowFlags() && currentLang.getFlag() != null && !currentLang.getFlag().isBlank())
                ? currentLang.getFlag() + " " : "";
        String clientFlagStr = (clientLang != null && configManager.isShowFlags() && clientLang.getFlag() != null && !clientLang.getFlag().isBlank())
                ? clientLang.getFlag() + " " : "";

        Component header = languageManager.getComponent("core:dialog.body_header", player);
        Component current = languageManager.getComponent("core:dialog.body_current", player,
                Placeholder.unparsed("current_flag", currentFlagStr),
                Placeholder.unparsed("current_name", currentLang.getName())
        );
        Component client = languageManager.getComponent("core:dialog.body_client_detected", player,
                Placeholder.unparsed("client_flag", clientFlagStr),
                Placeholder.unparsed("client_name", clientLang != null ? clientLang.getName() : clientLocale.toString())
        );
        Component instruction = languageManager.getComponent("core:dialog.body_instruction", player);

        Component combined = Component.empty();
        boolean hasContent = false;

        if (configManager.isShowBodyHeader()) {
            combined = combined.append(header);
            hasContent = true;
        }

        if (configManager.isShowCurrentLanguage()) {
            if (hasContent) combined = combined.append(Component.newline());
            combined = combined.append(current);
            hasContent = true;
        }

        if (configManager.isShowClientDetected()) {
            if (hasContent) combined = combined.append(Component.newline());
            combined = combined.append(client);
            hasContent = true;
        }

        if (configManager.isShowInstruction()) {
            if (hasContent) combined = combined.append(Component.newline());
            combined = combined.append(instruction);
            hasContent = true;
        }

        if (!hasContent) {
            return List.of();
        }

        // Return a single unified DialogBody element so Paper doesn't add multiple cards with huge gaps
        return List.of(DialogBody.plainMessage(combined));
    }

    /**
     * Handles when a language is selected by a player in the dialog.
     */
    private void handleLanguageSelect(@NotNull Player player, @NotNull Language chosen) {
        // Save preference
        playerLanguageManager.setPlayerPreference(player.getUniqueId(), chosen.getCode());

        // Sound feedback
        if (configManager.isSoundEnabled()) {
            player.playSound(player.getLocation(), configManager.getSuccessSound(), 1.0f, 1.2f);
        }

        // Send chat confirmation message
        languageManager.sendMessage(player, "core:commands.success",
                Placeholder.unparsed("lang_name", chosen.getName()),
                Placeholder.unparsed("lang_code", chosen.getCode())
        );

        // Show Notice Dialog or close
        if (configManager.isShowNoticeAfterSelection()) {
            Dialog notice = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(languageManager.getComponent("core:dialog.notice_title", player))
                            .body(List.of(DialogBody.plainMessage(
                                    languageManager.getComponent("core:dialog.notice_message", player,
                                            Placeholder.unparsed("lang_name", chosen.getName())
                                    )
                            )))
                            .canCloseWithEscape(true)
                            .build())
                    .type(DialogType.notice())
            );
            player.showDialog(notice);
        } else {
            player.closeDialog();
        }
    }
}
