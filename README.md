# ClarityLangCore

[![Paper](https://img.shields.io/badge/Paper-26.3%2B-00a8ff?style=for-the-badge&logo=papermc&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-25-f89820?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-9.5-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=for-the-badge)](LICENSE)

A high-performance, modular, and centralized **Multi-Language Core** plugin designed for modern **Paper 26.3 (Minecraft 1.21.7+)** servers. Powered by Paper's **Native Dialog API** for native in-game modal popups and full **Adventure MiniMessage** text formatting.

---

## 📑 Table of Contents

- [Features](#-features)
- [Supported Languages](#-supported-languages)
- [Native Dialog UI](#-native-dialog-ui)
- [File & Directory Structure](#-file--directory-structure)
- [Commands & Permissions](#-commands--permissions)
- [Configuration (`config.yml`)](#-configuration-configyml)
- [Developer API Integration](#-developer-api-integration)
- [Building from Source](#-building-from-source)
- [Additional Documentation](#-additional-documentation)
- [License](#-license)

---

## 🌟 Features

- **🌐 Centralized & Modular Architecture**: One language core for your entire network. External plugins (Economy, Clans, Spawns, RPG, BattlePass) can retrieve translations with a single line of code.
- **💬 Paper 26.3 Native Dialog API**: Replaces clunky Chest GUIs with official Minecraft client modal dialogs (`io.papermc.paper.dialog.Dialog`). Clean, responsive, immune to item-duplication exploits, and zero custom resource packs required.
- **🎯 Automatic Client Locale Detection**: Detects each player's Minecraft client language (`player.locale()`) in real time. First-time players instantly see their preferred language automatically.
- **⚡ Asynchronous Data Storage**: Player preferences are saved non-blocking in `playerdata.yml`, ensuring zero tick lag on server joins or language switches.
- **🎨 MiniMessage & Legacy Color Support**: Full support for Kyori Adventure MiniMessage tags (`<gradient>`, `<rainbow>`, `<hover>`, `<click>`) while automatically parsing legacy color codes (`&a`, `&c`, `§e`).
- **🛡️ Multi-tier Fallback Engine (Anti-Null)**: If a translation key is missing in a player's language, the engine gracefully falls back to the language's defined fallback (e.g. `en_US`), then to the server default language, preventing missing text or console errors.
- **📦 9 Production-Ready Languages Pre-installed**: Complete translations out-of-the-box.

---

## 🌍 Supported Languages

| Flag | Language Name | Language Code | Fallback |
| :---: | :--- | :---: | :---: |
| 🇬🇧 | **English** (Default) | `en_US` | *Self* |
| 🇮🇩 | **Bahasa Indonesia** | `id_ID` | `en_US` |
| 🇧🇷 | **Português (Brasil)** | `pt_BR` | `en_US` |
| 🇪🇸 | **Español** | `es_ES` | `en_US` |
| 🇨🇳 | **简体中文 (Simplified Chinese)** | `zh_CN` | `en_US` |
| 🇯🇵 | **日本語 (Japanese)** | `ja_JP` | `en_US` |
| 🇩🇪 | **Deutsch (German)** | `de_DE` | `en_US` |
| 🇫🇷 | **Français (French)** | `fr_FR` | `en_US` |
| 🇷🇺 | **Русский (Russian)** | `ru_RU` | `en_US` |

---

## 🖼️ Native Dialog UI

ClarityLangCore uses Paper 26.3's native client dialog engine (`player.showDialog(dialog)`). Two customizable dialog modes are supported in `config.yml`:

### 1. `MULTI_ACTION` (Recommended / Default)
- Interactive grid of buttons displaying country flags, language names, and active status indicator (`✓`).
- Hover tooltips provide additional context.
- Single click immediately applies the language, plays a subtle chime sound, sends a localized notification, and closes cleanly.

### 2. `SINGLE_OPTION` (Radio Button Form)
- Vertical list of radio-selectable language entries.
- Displays player's current language alongside auto-detected client locale.
- Requires confirmation via a **Confirm** action button, with an optional **Cancel** action.

---

## 📂 File & Directory Structure

```text
plugins/ClarityLangCore/
├── config.yml                      # Main plugin configuration
├── playerdata.yml                  # Asynchronous player preference storage
└── languages/
    ├── en_US.yml                   # English translations
    ├── id_ID.yml                   # Indonesian translations
    ├── pt_BR.yml                   # Brazilian Portuguese translations
    ├── es_ES.yml                   # Spanish translations
    ├── zh_CN.yml                   # Chinese translations
    ├── ja_JP.yml                   # Japanese translations
    ├── de_DE.yml                   # German translations
    ├── fr_FR.yml                   # French translations
    ├── ru_RU.yml                   # Russian translations
    └── plugins/                    # External plugin translation bundles
        └── CustomEconomy/
            ├── en_US.yml
            └── id_ID.yml
```

### Translation File Structure (`en_US.yml`)

```yaml
# Language Metadata
language:
  code: "en_US"
  name: "English"
  flag: "🇬🇧"
  fallback: "en_US"

# Core Plugin Namespace
core:
  prefix: "<gradient:#38ef7d:#11998e><bold>[ClarityLang]</bold></gradient> "
  commands:
    success: "<prefix><green>Your language has been updated to <yellow><lang_name></yellow>!</green>"
  dialog:
    title: "<gradient:#38ef7d:#11998e><bold>SELECT LANGUAGE</bold></gradient>"
    body_header: "<white>Welcome! Please select your preferred language below.</white>"

# External Plugin Namespace Example
economy:
  balance: "<prefix><gray>Your balance: <gold><amount></gold></gray>"
  pay_success: "<prefix><green>Successfully transferred <gold><amount></gold> to <yellow><target></yellow>.</green>"
```

---

## 🎮 Commands & Permissions

Commands are registered dynamically using Paper's native `BasicCommand` engine (supporting aliases out of the box).

| Command | Aliases | Description | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lang` | `/language`, `/bahasa` | Opens the Native Dialog language selection menu | `claritylang.use` | Everyone (`true`) |
| `/lang <code/name>` | `/bahasa id_ID` | Directly switches language | `claritylang.use` | Everyone (`true`) |
| `/lang list` | | Lists all registered languages with active indicator | `claritylang.use` | Everyone (`true`) |
| `/lang reset` | | Resets preference to client auto-detected locale | `claritylang.use` | Everyone (`true`) |
| `/lang reload` | | Reloads all configurations and translation bundles | `claritylang.admin` | OP only |
| `/lang help` | | Displays command help | `claritylang.use` | Everyone (`true`) |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Default server fallback language if client locale is unknown or unmapped
default_language: "en_US"

# Automatically detect client Minecraft language on join
auto_detect_client_language: true

# Prompt first-time players with the native dialog on join
prompt_on_first_join: true
first_join_delay_ticks: 25

dialog:
  # Display mode: MULTI_ACTION (Button Grid) or SINGLE_OPTION (Radio Choice)
  mode: "MULTI_ACTION"
  columns: 2
  can_close_with_escape: true
  show_notice_after_selection: false

  # Clean layout settings (combined into a single body without scrollbars)
  layout:
    show_header: true
    show_current_language: true
    show_client_detected: false
    show_instruction: false

  # Button formatting
  buttons:
    show_flags: false
    active_badge: "● "

sound:
  enabled: true
  success: "minecraft:entity.experience_orb.pickup"
  open_dialog: "minecraft:ui.button.click"

storage:
  type: "YAML"
  async_save: true
```

---

## 💻 Developer API Integration

External plugins can hook into `ClarityLangCore` through the static facade `ClarityLangAPI` or the Bukkit `ServicesManager`.

### 1. Add Dependency to Your Plugin

#### Gradle Kotlin DSL (`build.gradle.kts`)
```kotlin
dependencies {
    compileOnly(files("libs/ClarityLangCore-1.0.0.jar"))
}
```

#### Paper Plugin Manifest (`paper-plugin.yml`)
```yaml
dependencies:
  server:
    ClarityLangCore:
      load: BEFORE
      required: true
```

---

### 2. Send Localized Messages to Players

```java
import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

// Automatically uses the player's preferred language (or detected client locale)
ClarityLangAPI.sendMessage(player, "economy:pay_success",
    Placeholder.parsed("amount", "$500"),
    Placeholder.unparsed("target", "Alex")
);
```

---

### 3. Retrieve Adventure Components (GUI Titles, Item Lore)

```java
import net.kyori.adventure.text.Component;
import java.util.List;

// Single component
Component title = ClarityLangAPI.getComponent("economy:gui.title", player);

// Multi-line Component List (e.g. Item Lore)
List<Component> lore = ClarityLangAPI.getComponentList("rpg:sword.lore", player,
    Placeholder.unparsed("owner", player.getName())
);
```

---

### 4. Register Custom Translation Bundles

In your plugin's `onEnable()` method, automatically register your own bundled language files:

```java
@Override
public void onEnable() {
    // Registers the /languages folder inside your plugin's JAR
    ClarityLangAPI.registerBundle(this, "myplugin", "languages");

    // Or register dynamic translations directly via code
    ClarityLangAPI.addTranslation("myplugin", "id_ID", "welcome", "<green>Selamat datang di server!</green>");
    ClarityLangAPI.addTranslation("myplugin", "en_US", "welcome", "<green>Welcome to the server!</green>");
}
```

---

### 5. Open Native Dialog Programmatically

```java
// Opens the language picker dialog for any online player
ClarityLangAPI.openLanguageDialog(player);
```

---

## 📦 Building from Source

### Prerequisites
- **JDK 25** (64-bit)
- Git

### Build Command
```bash
# Clone the repository
git clone https://github.com/USERNAME/ClarityLangCore.git
cd ClarityLangCore

# Build with Gradle Wrapper
./gradlew build
```

The compiled, shaded artifact will be generated at:
```text
build/libs/ClarityLangCore-1.0.0.jar
```

---

## 📚 Additional Documentation

- [**`setup.md`**](setup.md) — Comprehensive developer setup guide for Paper 26.3, dual Java (Java 8 + JDK 25 isolation for launchers like TLauncher), Gradle modern configuration, and the **Paper Native Dialog API blueprint**.
- [**`Tips.md`**](Tips.md) — Detailed integration cookbook featuring 6 real-world external plugin cases (Economy, RPG Items, Clans, Custom Join/Quit, Broadcasts).

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for the full text.
Created with ❤️ by **Harukiyu** for the **Clarity Network**.
