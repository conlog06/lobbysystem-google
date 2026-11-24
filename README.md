# LobbySystem (Minecraft 1.21.10 – 1.26.3)

Modernes Netzwerk-Lobbysystem für **Paper / Spigot** und **CloudNet v3** (Minecraft **1.21.10 bis 1.26.3**) mit nativem **MiniMessage-, Hex-Gradient- & Small-Caps-Schriftarten-System**, **Scoreboard ohne rote Zahlen (`NumberFormat.blank()`)**, Tablist- & Chat-Prefixen, SQLite (lokal) & MySQL-Synchronisation, Lizenz-System (exklusiv ausgestellt durch `SujucDerBreite`) und Kompass-Navigator.

---

## Vorschau & Screenshots

| Navigator & Kompass | Daily-Reward Hexe | Einstellungs-Dorfbewohner |
| :---: | :---: | :---: |
| ![Navigator](docs/images/ChatGPT-Bild%2023.%20Nov.%202026,%2023_37_31.png) | ![Daily Reward](docs/images/ChatGPT-Bild%2023.%20Nov.%202026,%2023_37_28.png) | ![Einstellungen](docs/images/ChatGPT-Bild%2023.%20Nov.%202026,%2023_37_24.png) |

| Scoreboard & Ränge | Lobby-Spawn | Warp-System |
| :---: | :---: | :---: |
| ![Scoreboard](docs/images/ChatGPT-Bild%2023.%20Nov.%202026,%2023_37_20.png) | ![Spawn](docs/images/ChatGPT-Bild%2023.%20Nov.%202026,%2023_37_16.png) | ![Warps](docs/images/ChatGPT-Bild%2023.%20Nov.%202026,%2023_37_10.png) |

---

## Moderne 1.21.10 – 1.26.3 Features

- **Schriftarten & Prefixe (`TextUtil`):** Unterstützt moderne **MiniMessage-Gradients** (`<gradient:#00E5FF:#2979FF>...</gradient>`), **Hex-Farben** (`<#FFD740>`), automatische **Small-Caps-Schriftart** (`<smallcaps>Lobby</smallcaps>` → `ʟᴏʙʙʏ`) sowie klassische `§`-/`&`-Farbcodes.
- **Scoreboard & Tablist (`1.21.10–1.26.3`):** Verwendet `NumberFormat.blank()`, um die roten Zahlen rechts in der Sidebar komplett auszublenden, sowie flackerfreie `Score.customName()`-Zeilen und sortierte Tablist-/Nametag-Teams aus der `ranks.json`.
- **Proxy-Kompatibilität:** Nutzt den aktuellen `bungeecord:main` Plugin-Channel für BungeeCord & Velocity (`connect:CityBuild-1`).

---

## Commands

| Command | Beschreibung |
| :--- | :--- |
| `/build` | De-/aktiviert den Baumodus |
| `/lobbycoins add\|remove\|get <Spielername> [Coins]` | Vergebe, entferne oder lasse dir die Coins anzeigen |
| `/setreward [remove]` | Setzt die Daily-Reward Hexe oder entfernt sie |
| `/setsettings [remove]` | Setzt den Einstellungs-Dorfbewohner oder entfernt ihn |
| `/setspawn` | Setzt den Spawn-Punkt |
| `/setwarp <Name>` | Setzt einen Warp-Punkt |
| `/tpcoord <X> <Y> <Z> [Yaw] [Pitch]` | Teleportiert dich zu Koordinaten |
