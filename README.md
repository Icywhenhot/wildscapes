# Wildscapes

A Forge mod for Minecraft **1.20.1** that adds wetland-themed nature content.

## Installation

Use Java 17 and Forge 47.4.28. Put the Wildscapes JAR, GeckoLib 4.8.4 for Forge 1.20.1,
and Icy's Lib 1.1.0 for Forge 1.20.1 in the `mods` folder on both the client and server.

## Building

The Gradle build expects the Forge build of Icy's Lib in Maven Local under
`icy.lib:icys_lib-forge-1.20.1:1.1.0`. With that dependency installed and Java 17 available,
run `./gradlew build` (or `.\gradlew.bat build` on Windows). The release JAR is
`build/libs/wildscapes-forge-1.20.1-1.0.0.jar`.

## Project layout

- `src/main/java/com/wildscapes/` — mod code (registration, custom blocks, worldgen)
- `src/main/resources/assets/wildscapes/` — models, blockstates, textures, lang
- `src/main/resources/data/wildscapes/` — recipes, loot tables, tags, tree feature

## Credits

Goozt, for Block/item models and textures, created in Blockbench.

---
