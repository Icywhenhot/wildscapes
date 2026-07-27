# Wildscapes

A NeoForge mod for Minecraft **1.21.1** that adds wetland-themed nature content.

## Content

**Cypress wood set**
- Cypress Log / Wood (+ stripped variants) — strippable with an axe, flammable, usable as fuel
- Cypress Planks, Cypress Leaves (decay naturally, drop saplings & sticks)
- Cypress Sapling — grows into a cypress tree (bonemeal works)
- Cypress Door & Cypress Trapdoor

**Wetland plants**
- Duckweed — floats flat on the surface of still water, like a lily pad
- Rushes — a two-block-tall plant that grows on dirt, mud, clay and sand

All blocks have crafting recipes, loot tables, and the appropriate vanilla tags
(planks, logs, leaves, saplings, doors, trapdoors, mineable/axe, etc.).

## Building

This project requires **JDK 21**. If your default `java` is newer, point Gradle at a
21 install first:

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
./gradlew build

# to launch the game in a dev environment
./gradlew runClient
```

The built jar is written to `build/libs/`.

## Project layout

- `src/main/java/com/wildscapes/` — mod code (registration, custom blocks, worldgen)
- `src/main/resources/assets/wildscapes/` — models, blockstates, textures, lang
- `src/main/resources/data/wildscapes/` — recipes, loot tables, tags, tree feature

## Credits

Block/item models and textures created in Blockbench.

---

Additional Resources:
- Community Documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/
