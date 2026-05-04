# FPerk

> A fully GUI-driven perk plugin for Minecraft Paper/Spigot servers, with BungeeCord support.

[![Modrinth](https://img.shields.io/badge/Modrinth-FPerk-1bd96a?logo=modrinth)](https://modrinth.com/plugin/fperk)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](https://github.com/Florianpal1/FPerk/blob/master/LICENCE)
[![bStats](https://bstats.org/signatures/bukkit/FPerk.svg)](https://bstats.org/plugin/bukkit/FPerk)

---

## 📖 Description

**FPerk** is a 100% GUI perk plugin for Minecraft servers. Everything is managed through a single command, and all GUIs are fully configurable. It supports BungeeCord for multi-server setups and synchronizes player perk states via a shared database.

---

## ✨ Features

- 🖥️ **100% GUI-based** — all interactions go through fully configurable chest menus
- 🔧 **Single command** — `/perk` opens everything
- 🌍 **Full language customization** — English and French supported out of the box
- 🎨 **Fully customizable perks and skills**
- 🔑 **Per-perk permission system**
- ⏱️ **Timed perks** — activation duration and cooldown configurable per perk
- 🔗 **BungeeCord support** — sync player data across multiple servers via MySQL/MariaDB
- 📦 **Pre-configured perks** — Fly, Heal, Night Vision, Glowing, Keep Inventory, Keep Experience, and more

---

## 🧩 Skill Types

The following skill types are available for building custom perks:

| Type | Description |
|---|---|
| `FLY` | Allows the player to fly |
| `KEEP_INVENTORY` | Player keeps their inventory on death |
| `KEEP_EXPERIENCE` | Player keeps their experience on death |
| `EFFECT` | Applies a potion effect to the player |
| `PACIFICATION` | Mobs no longer attack the player |
| `ANTI_PHANTOM` | Phantoms no longer attack the player |
| `FLY_SPEED` | Increases fly speed (requires Fly) |
| `AUTO_SMELT` | Automatically smelts mined ores |
| `ANTI_KNOCKBACK` | Prevents knockback |
| `CURE_EFFECT` | Removes all negative effects |
| `SECOND_CHANCE` | Acts as a Totem of Undying |
| `BROKEN_FALL` | Removes fall damage |
| `VACCUM` | Automatically picks up nearby items |

---

## 📋 Requirements

- **Java 16+**
- **[Vault](https://www.spigotmc.org/resources/vault.34315/)**
- **[LuckPerms](https://luckperms.net/)** *(required dependency)*
- **Database:** SQLite *(single server)* or MySQL/MariaDB *(multi-server / BungeeCord)*

---

## 🖥️ Compatibility

- **Minecraft Java Edition:** 1.16.x — 1.21.x
- **Platforms:** Bukkit, Spigot, Paper, Purpur

---

## 🚀 Installation

1. Download the latest JAR from the [Releases page](https://github.com/Florianpal1/FPerk/releases) or [Modrinth](https://modrinth.com/plugin/fperk).
2. Place `FPerk-x.x.x.jar` in the `plugins/` folder of your server(s).
3. Start (or restart) the server to generate default configuration files.
4. Edit the files in `plugins/FPerk/` to your liking (see [Configuration](#-configuration)).
5. Run `/perk admin reload` in-game or restart the server to apply changes.

---

## ⚙️ Configuration

### `config.yml`

```yaml
lang: "en"         # "en" or "fr"

status:
  enabled: "&2Enabled"
  disabled: "&cDisabled"
```

### `database.yml`

**SQLite** (single server, no setup needed):
```yaml
database:
  type: SQLite
  url: "jdbc:sqlite:database.db"
  user: "root"
  password: ""
```

**MySQL** (multi-server / BungeeCord):
```yaml
database:
  type: MySQL
  url: "jdbc:mysql://localhost:3306/fperk"
  user: "root"
  password: ""
```

### `perk.yml`

Define your perks here. Example:

```yaml
"fly":
  displayName: "Fly"         # Name shown in GUI
  material: "ELYTRA"         # Item icon in GUI
  skills:                    # List of skills to apply
    - "fly"
  delais: 500                # Cooldown in milliseconds between activations
  ignoreDelais: true         # Ignore cooldown
  time: 500                  # Activation duration in milliseconds
  persistant: false          # If true, ignores the time limit
  permission: "fperk.fly"    # Required permission node
  bannedWorld: []            # Worlds where this perk is disabled
```

### `skill.yml`

Define your skills here. Example:

```yaml
"heal":
  displayName:
    - "Heal"              # Description shown in GUI
  type: EFFECT            # Skill type (see Skill Types above)
  effect: "instant_heal"  # Potion effect ID (only for EFFECT type)
  level: 1                # Effect level (only for EFFECT type)
```

---

## 🎮 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/perk` | `fperk.show` | Opens the perk list GUI |
| `/perk admin toggle [player]` | `fperk.admin.toggle` | Toggle a perk for another player |
| `/perk admin reload` | `fperk.admin.reload` | Reload the plugin configuration |

Individual perk permissions are defined per perk in `perk.yml` (e.g. `fperk.fly`).

---

## 🔗 Links

- 📦 [Modrinth](https://modrinth.com/plugin/fperk)
- 📦 [Spigot](https://www.spigotmc.org/resources/fperk.122294/)
- 📖 [Wiki](https://github.com/Florianpal1/FPerk/wiki)
- 🐛 [Report a bug](https://github.com/Florianpal1/FPerk/issues)
- 💬 [Discord](https://discord.com/invite/dEvR34ZzYQ)
- [BStats](https://bstats.org/plugin/bukkit/FPerk/24472)

---

## 📜 License

This project is licensed under the [GPL-3.0 License](https://github.com/Florianpal1/FPerk/blob/master/LICENCE).
