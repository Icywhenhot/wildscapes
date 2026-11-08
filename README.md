# Wildscapes

A Fabric mod for Minecraft **26.2** that adds wetland-themed nature content.

## Build

Use Java 25 and run `./gradlew build` (or `gradlew.bat build` on Windows). The jar is in `build/libs`.

The build uses the Fabric 26.2 build of Icy’s Lib from the adjacent `Icy's lib` project or Maven Local. To supply a jar directly, pass `-Picys_lib_jar=/path/to/icys_lib-fabric-26.2-1.0.0.jar`.

Install Fabric API, GeckoLib 5, and Icy’s Lib for Minecraft 26.2 alongside Wildscapes. Use `./gradlew runClient` to launch the development client.

## Project layout

- `src/main/java/com/wildscapes/` â€” mod code (registration, custom blocks, worldgen)
- `src/main/resources/assets/wildscapes/` â€” models, blockstates, textures, lang
- `src/main/resources/data/wildscapes/` â€” recipes, loot tables, tags, tree feature

## Credits

Goozt, for Block/item models and textures, created in Blockbench.

---
