# Wildscapes

A Fabric mod for Minecraft **1.20.1** that adds wetland-themed nature content.

## Build

Use Java 21 and run `./gradlew build` (or `gradlew.bat build` on Windows). The jar is in `build/libs`.

The build uses the Fabric 1.20.1 build of Icy’s Lib from the adjacent `Icy's lib` project or Maven Local. To supply a jar directly, pass `-Picys_lib_jar=/path/to/icys_lib-fabric-1.20.1-1.1.0.jar`.

Install Fabric API, GeckoLib 4, and Icy’s Lib for Minecraft 1.20.1 alongside Wildscapes. Use `./gradlew runClient` to launch the development client.

## Project layout

- `src/main/java/com/wildscapes/` â€” mod code (registration, custom blocks, worldgen)
- `src/main/resources/assets/wildscapes/` â€” models, blockstates, textures, lang
- `src/main/resources/data/wildscapes/` â€” recipes, loot tables, tags, tree feature

## Credits

Goozt, for Block/item models and textures, created in Blockbench.

---
