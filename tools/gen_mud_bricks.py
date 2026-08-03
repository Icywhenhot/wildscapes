#!/usr/bin/env python3
"""Generates the dyed mud brick assets and data files.

The set is 16 dye colors x 5 block types (bricks, chiseled, stairs, slab, wall),
plus one undyed `chiseled_mud_bricks` base — around 600 small JSON files. They are
all mechanical variations on the vanilla mud brick files, so they live here rather
than being hand-written.

Textures are lifted straight out of the Blockbench projects in models/blocks/mud/,
which carry them base64-embedded. The geometry in those projects is the vanilla
stairs/slab/wall shape, so the models here just parent to the vanilla templates
instead of shipping their own elements.

Re-run after touching a .bbmodel or adding a variant:

    python tools/gen_mud_bricks.py

It is idempotent, and only ever adds keys to the hand-maintained en_us.json.
"""

import base64
import json
import os
import re

MODID = "wildscapes"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BB = os.path.join(ROOT, "models", "blocks", "mud")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", MODID)
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

# Dye order matches net.minecraft.world.item.DyeColor.
COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
          "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]

# The Blockbench projects were authored in Spanish and carry a few typos, so the
# embedded texture names need fixing up before they can be addressed by block name.
# "chinseled" is a typo for chiseled, and "blanco"/"cian" are Spanish.
COLOR_ALIASES = {"blu": "blue", "cian": "cyan", "blank": "white"}

# Vanilla's mud_brick_stairs blockstate, with the model name factored out.
STAIR_VARIANTS = {
    'facing=east,half=bottom,shape=inner_left': ('_inner', {'uvlock': True, 'y': 270}),
    'facing=east,half=bottom,shape=inner_right': ('_inner', {}),
    'facing=east,half=bottom,shape=outer_left': ('_outer', {'uvlock': True, 'y': 270}),
    'facing=east,half=bottom,shape=outer_right': ('_outer', {}),
    'facing=east,half=bottom,shape=straight': ('', {}),
    'facing=east,half=top,shape=inner_left': ('_inner', {'uvlock': True, 'x': 180}),
    'facing=east,half=top,shape=inner_right': ('_inner', {'uvlock': True, 'x': 180, 'y': 90}),
    'facing=east,half=top,shape=outer_left': ('_outer', {'uvlock': True, 'x': 180}),
    'facing=east,half=top,shape=outer_right': ('_outer', {'uvlock': True, 'x': 180, 'y': 90}),
    'facing=east,half=top,shape=straight': ('', {'uvlock': True, 'x': 180}),
    'facing=north,half=bottom,shape=inner_left': ('_inner', {'uvlock': True, 'y': 180}),
    'facing=north,half=bottom,shape=inner_right': ('_inner', {'uvlock': True, 'y': 270}),
    'facing=north,half=bottom,shape=outer_left': ('_outer', {'uvlock': True, 'y': 180}),
    'facing=north,half=bottom,shape=outer_right': ('_outer', {'uvlock': True, 'y': 270}),
    'facing=north,half=bottom,shape=straight': ('', {'uvlock': True, 'y': 270}),
    'facing=north,half=top,shape=inner_left': ('_inner', {'uvlock': True, 'x': 180, 'y': 270}),
    'facing=north,half=top,shape=inner_right': ('_inner', {'uvlock': True, 'x': 180}),
    'facing=north,half=top,shape=outer_left': ('_outer', {'uvlock': True, 'x': 180, 'y': 270}),
    'facing=north,half=top,shape=outer_right': ('_outer', {'uvlock': True, 'x': 180}),
    'facing=north,half=top,shape=straight': ('', {'uvlock': True, 'x': 180, 'y': 270}),
    'facing=south,half=bottom,shape=inner_left': ('_inner', {}),
    'facing=south,half=bottom,shape=inner_right': ('_inner', {'uvlock': True, 'y': 90}),
    'facing=south,half=bottom,shape=outer_left': ('_outer', {}),
    'facing=south,half=bottom,shape=outer_right': ('_outer', {'uvlock': True, 'y': 90}),
    'facing=south,half=bottom,shape=straight': ('', {'uvlock': True, 'y': 90}),
    'facing=south,half=top,shape=inner_left': ('_inner', {'uvlock': True, 'x': 180, 'y': 90}),
    'facing=south,half=top,shape=inner_right': ('_inner', {'uvlock': True, 'x': 180, 'y': 180}),
    'facing=south,half=top,shape=outer_left': ('_outer', {'uvlock': True, 'x': 180, 'y': 90}),
    'facing=south,half=top,shape=outer_right': ('_outer', {'uvlock': True, 'x': 180, 'y': 180}),
    'facing=south,half=top,shape=straight': ('', {'uvlock': True, 'x': 180, 'y': 90}),
    'facing=west,half=bottom,shape=inner_left': ('_inner', {'uvlock': True, 'y': 90}),
    'facing=west,half=bottom,shape=inner_right': ('_inner', {'uvlock': True, 'y': 180}),
    'facing=west,half=bottom,shape=outer_left': ('_outer', {'uvlock': True, 'y': 90}),
    'facing=west,half=bottom,shape=outer_right': ('_outer', {'uvlock': True, 'y': 180}),
    'facing=west,half=bottom,shape=straight': ('', {'uvlock': True, 'y': 180}),
    'facing=west,half=top,shape=inner_left': ('_inner', {'uvlock': True, 'x': 180, 'y': 180}),
    'facing=west,half=top,shape=inner_right': ('_inner', {'uvlock': True, 'x': 180, 'y': 270}),
    'facing=west,half=top,shape=outer_left': ('_outer', {'uvlock': True, 'x': 180, 'y': 180}),
    'facing=west,half=top,shape=outer_right': ('_outer', {'uvlock': True, 'x': 180, 'y': 270}),
    'facing=west,half=top,shape=straight': ('', {'uvlock': True, 'x': 180, 'y': 180}),
}

written = []


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")
    written.append(path)


# --------------------------------------------------------------------------- textures

def texture_target(name):
    """Maps a Blockbench texture name onto the name it ships under, or None to skip."""
    if name == "mud_bricks.png":
        return None  # vanilla's own texture, in the project only for reference
    if name == "chinseled_mud_bricks.png":
        return "chiseled_mud_bricks.png"  # the undyed base
    if name == "brown_bricks.png":
        return "brown_mud_bricks.png"  # the one that lost its "mud"
    match = re.fullmatch(r"(.+?)_(chinseled_)?mud_bricks\.png", name)
    if not match:
        return None
    color = COLOR_ALIASES.get(match.group(1), match.group(1))
    return f"chiseled_{color}_mud_bricks.png" if match.group(2) else f"{color}_mud_bricks.png"


def extract_textures():
    """Pulls every embedded PNG out of the Blockbench projects into textures/block/."""
    out = os.path.join(ASSETS, "textures", "block")
    os.makedirs(out, exist_ok=True)
    seen = {}
    for name in sorted(os.listdir(BB)):
        if not name.endswith(".bbmodel"):
            continue
        with open(os.path.join(BB, name), encoding="utf-8") as f:
            project = json.load(f)
        for tex in project.get("textures", []):
            src = tex.get("source", "")
            target = texture_target(tex["name"])
            if target is None or "," not in src:
                continue
            seen.setdefault(target, base64.b64decode(src.split(",", 1)[1]))
    for target, raw in sorted(seen.items()):
        path = os.path.join(out, target)
        if not os.path.exists(path) or open(path, "rb").read() != raw:
            open(path, "wb").write(raw)
        written.append(path)
    return sorted(seen)


# --------------------------------------------------------------------------- names

def names(color):
    """The five block ids for one dye color."""
    return {
        "bricks": f"{color}_mud_bricks",
        "chiseled": f"chiseled_{color}_mud_bricks",
        "stairs": f"{color}_mud_brick_stairs",
        "slab": f"{color}_mud_brick_slab",
        "wall": f"{color}_mud_brick_wall",
    }


def all_block_ids():
    ids = ["chiseled_mud_bricks"]
    for color in COLORS:
        ids.extend(names(color).values())
    return ids


# --------------------------------------------------------------------------- models

def gen_models(color):
    n = names(color)
    tex = f"{MODID}:block/{n['bricks']}"
    block = lambda name, obj: write_json(os.path.join(ASSETS, "models", "block", name + ".json"), obj)
    item = lambda name, parent: write_json(
        os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": f"{MODID}:block/{parent}"})

    # Full cubes. The chiseled texture is a single face design used on all six sides,
    # exactly as the Blockbench project assigns it.
    block(n["bricks"], {"parent": "minecraft:block/cube_all", "textures": {"all": tex}})
    block(n["chiseled"], {"parent": "minecraft:block/cube_all",
                          "textures": {"all": f"{MODID}:block/{n['chiseled']}"}})

    faces = {"bottom": tex, "side": tex, "top": tex}
    block(n["slab"], {"parent": "minecraft:block/slab", "textures": faces})
    block(n["slab"] + "_top", {"parent": "minecraft:block/slab_top", "textures": faces})
    block(n["stairs"], {"parent": "minecraft:block/stairs", "textures": faces})
    block(n["stairs"] + "_inner", {"parent": "minecraft:block/inner_stairs", "textures": faces})
    block(n["stairs"] + "_outer", {"parent": "minecraft:block/outer_stairs", "textures": faces})

    for suffix, parent in [("_post", "template_wall_post"), ("_side", "template_wall_side"),
                           ("_side_tall", "template_wall_side_tall"), ("_inventory", "wall_inventory")]:
        block(n["wall"] + suffix, {"parent": f"minecraft:block/{parent}", "textures": {"wall": tex}})

    for key in ("bricks", "chiseled", "slab", "stairs"):
        item(n[key], n[key])
    item(n["wall"], n["wall"] + "_inventory")  # walls show the inventory model, not the post


def gen_undyed_chiseled():
    write_json(os.path.join(ASSETS, "models", "block", "chiseled_mud_bricks.json"),
               {"parent": "minecraft:block/cube_all",
                "textures": {"all": f"{MODID}:block/chiseled_mud_bricks"}})
    write_json(os.path.join(ASSETS, "models", "item", "chiseled_mud_bricks.json"),
               {"parent": f"{MODID}:block/chiseled_mud_bricks"})
    write_json(os.path.join(ASSETS, "blockstates", "chiseled_mud_bricks.json"),
               {"variants": {"": {"model": f"{MODID}:block/chiseled_mud_bricks"}}})
    write_json(os.path.join(DATA, MODID, "loot_table", "blocks", "chiseled_mud_bricks.json"),
               simple_loot("chiseled_mud_bricks"))


# --------------------------------------------------------------------------- blockstates

def gen_blockstates(color):
    n = names(color)
    path = lambda name: os.path.join(ASSETS, "blockstates", name + ".json")

    for key in ("bricks", "chiseled"):
        write_json(path(n[key]), {"variants": {"": {"model": f"{MODID}:block/{n[key]}"}}})

    write_json(path(n["slab"]), {"variants": {
        "type=bottom": {"model": f"{MODID}:block/{n['slab']}"},
        "type=double": {"model": f"{MODID}:block/{n['bricks']}"},
        "type=top": {"model": f"{MODID}:block/{n['slab']}_top"},
    }})

    variants = {}
    for state, (suffix, extra) in STAIR_VARIANTS.items():
        variants[state] = {"model": f"{MODID}:block/{n['stairs']}{suffix}", **extra}
    write_json(path(n["stairs"]), {"variants": variants})

    multipart = [{"when": {"up": "true"}, "apply": {"model": f"{MODID}:block/{n['wall']}_post"}}]
    for shape in ("side", "side_tall"):
        value = "low" if shape == "side" else "tall"
        for i, facing in enumerate(("north", "east", "south", "west")):
            apply = {"model": f"{MODID}:block/{n['wall']}_{shape}", "uvlock": True}
            if i:
                apply["y"] = i * 90
            multipart.append({"when": {facing: value}, "apply": apply})
    write_json(path(n["wall"]), {"multipart": multipart})


# --------------------------------------------------------------------------- loot

def simple_loot(block_id):
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "bonus_rolls": 0,
            "entries": [{"type": "minecraft:item", "name": f"{MODID}:{block_id}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }


def slab_loot(block_id):
    """A double slab has to drop two, so the count is set from the block state."""
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "bonus_rolls": 0,
            "entries": [{
                "type": "minecraft:item",
                "name": f"{MODID}:{block_id}",
                "functions": [
                    {
                        "function": "minecraft:set_count",
                        "add": False,
                        "count": 2,
                        "conditions": [{
                            "condition": "minecraft:block_state_property",
                            "block": f"{MODID}:{block_id}",
                            "properties": {"type": "double"},
                        }],
                    },
                    {"function": "minecraft:explosion_decay"},
                ],
            }],
        }],
    }


def gen_loot(color):
    n = names(color)
    out = os.path.join(DATA, MODID, "loot_table", "blocks")
    for key in ("bricks", "chiseled", "stairs", "wall"):
        write_json(os.path.join(out, n[key] + ".json"), simple_loot(n[key]))
    write_json(os.path.join(out, n["slab"] + ".json"), slab_loot(n["slab"]))


# --------------------------------------------------------------------------- tags

# Each entry is the vanilla block the set extends, or None where vanilla has none.
TAG_SOURCES = {
    "mud_bricks": ("bricks", "minecraft:mud_bricks"),
    "chiseled_mud_bricks": ("chiseled", f"{MODID}:chiseled_mud_bricks"),
    "mud_brick_stairs": ("stairs", "minecraft:mud_brick_stairs"),
    "mud_brick_slabs": ("slab", "minecraft:mud_brick_slab"),
    "mud_brick_walls": ("wall", "minecraft:mud_brick_wall"),
}


def gen_tags():
    # Mod tags naming every dyeable member of a family, including the undyed base.
    # The dye recipes take these as their ingredient, which is what lets you re-dye
    # an already-coloured block instead of only starting from a plain one.
    for tag, (key, base) in TAG_SOURCES.items():
        values = [base] + [f"{MODID}:{names(c)[key]}" for c in COLORS]
        for kind in ("block", "item"):
            write_json(os.path.join(DATA, MODID, "tags", kind, tag + ".json"),
                       {"replace": False, "values": values})

    # One umbrella tag so mineable/pickaxe only needs a single extra line.
    write_json(os.path.join(DATA, MODID, "tags", "block", "mud_brick_blocks.json"),
               {"replace": False, "values": [f"#{MODID}:{t}" for t in sorted(TAG_SOURCES)]})

    # Vanilla tags. #minecraft:walls is the important one — WallBlock reads it to
    # decide what to connect to, so without it the walls ignore each other.
    for kind, tag, key in [("block", "walls", "wall"), ("block", "stairs", "stairs"),
                           ("block", "slabs", "slab"), ("item", "walls", "wall"),
                           ("item", "stairs", "stairs"), ("item", "slabs", "slab")]:
        write_json(os.path.join(DATA, "minecraft", "tags", kind, tag + ".json"),
                   {"replace": False, "values": [f"{MODID}:{names(c)[key]}" for c in COLORS]})


# --------------------------------------------------------------------------- recipes

def shapeless(result, count, ingredients, group, category="building"):
    return {"type": "minecraft:crafting_shapeless", "category": category, "group": group,
            "ingredients": ingredients, "result": {"id": result, "count": count}}


def shaped(result, count, pattern, ingredient, group, category="building"):
    return {"type": "minecraft:crafting_shaped", "category": category, "group": group,
            "key": {"#": {"item": ingredient}}, "pattern": pattern,
            "result": {"id": result, "count": count}}


def stonecutting(result, count, ingredient):
    return {"type": "minecraft:stonecutting", "ingredient": {"item": ingredient},
            "result": {"id": result, "count": count}}


def gen_recipes(color):
    n = names(color)
    out = os.path.join(DATA, MODID, "recipe")
    dye = f"minecraft:{color}_dye"
    bricks = f"{MODID}:{n['bricks']}"

    # Dyeing. One shapeless recipe per colour per family, taking the family tag, so
    # any member (vanilla or already dyed) plus a dye gives that colour.
    for key, tag in [("bricks", "mud_bricks"), ("chiseled", "chiseled_mud_bricks"),
                     ("stairs", "mud_brick_stairs"), ("slab", "mud_brick_slabs"),
                     ("wall", "mud_brick_walls")]:
        write_json(os.path.join(out, n[key] + "_from_dye.json"),
                   shapeless(f"{MODID}:{n[key]}", 1,
                             [{"tag": f"{MODID}:{tag}"}, {"item": dye}],
                             group=f"dyed_{tag}",
                             category="misc" if key == "wall" else "building"))

    # The normal vanilla shapes, so a dyed brick behaves like any other brick.
    write_json(os.path.join(out, n["stairs"] + ".json"),
               shaped(f"{MODID}:{n['stairs']}", 4, ["#  ", "## ", "###"], bricks, "mud_brick_stairs"))
    write_json(os.path.join(out, n["slab"] + ".json"),
               shaped(f"{MODID}:{n['slab']}", 6, ["###"], bricks, "mud_brick_slabs"))
    write_json(os.path.join(out, n["wall"] + ".json"),
               shaped(f"{MODID}:{n['wall']}", 6, ["###", "###"], bricks, "mud_brick_walls", "misc"))
    write_json(os.path.join(out, n["chiseled"] + ".json"),
               shaped(f"{MODID}:{n['chiseled']}", 1, ["#", "#"], f"{MODID}:{n['slab']}",
                      "chiseled_mud_bricks"))

    for key, count in [("stairs", 1), ("slab", 2), ("wall", 1), ("chiseled", 1)]:
        write_json(os.path.join(out, f"{n[key]}_from_{n['bricks']}_stonecutting.json"),
                   stonecutting(f"{MODID}:{n[key]}", count, bricks))


def gen_undyed_recipes():
    out = os.path.join(DATA, MODID, "recipe")
    write_json(os.path.join(out, "chiseled_mud_bricks.json"),
               shaped(f"{MODID}:chiseled_mud_bricks", 1, ["#", "#"], "minecraft:mud_brick_slab",
                      "chiseled_mud_bricks"))
    write_json(os.path.join(out, "chiseled_mud_bricks_from_mud_bricks_stonecutting.json"),
               stonecutting(f"{MODID}:chiseled_mud_bricks", 1, "minecraft:mud_bricks"))


# --------------------------------------------------------------------------- lang

def title(block_id):
    return " ".join(w.capitalize() for w in block_id.split("_"))


def gen_lang():
    """Adds any missing block names to the hand-maintained en_us.json, in place."""
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as f:
        lang = json.load(f)
    added = 0
    for block_id in all_block_ids():
        key = f"block.{MODID}.{block_id}"
        if key not in lang:
            lang[key] = title(block_id)
            added += 1
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write("\n")
    return added


# --------------------------------------------------------------------------- main

def main():
    textures = extract_textures()
    print(f"textures: {len(textures)}")

    gen_undyed_chiseled()
    gen_undyed_recipes()
    for color in COLORS:
        gen_models(color)
        gen_blockstates(color)
        gen_loot(color)
        gen_recipes(color)
    gen_tags()
    added = gen_lang()

    print(f"json files: {len(written) - len(textures)}")
    print(f"lang keys added: {added}")
    print(f"blocks: {len(all_block_ids())}")


if __name__ == "__main__":
    main()
