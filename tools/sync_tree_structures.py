"""Rebuilds the tree configured features from whatever .nbt files are in the structure folders.

Drop structure-block exports into
    src/main/resources/data/wildscapes/structure/tree/{land,water,fallen,roots}/
and run this from the repo root:

    python tools/sync_tree_structures.py

Each folder's files are listed into the matching configured feature, so adding a tree to the
world is copying a file in and re-running this. Nothing else needs editing.
"""

import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/wildscapes"
STRUCTURES = DATA / "structure"
FEATURES = DATA / "worldgen/configured_feature"

# structure subfolder -> configured feature that grows what is in it
GROUPS = {
    "tree/land": "swamp_trees_land",
    "tree/water": "swamp_trees_water",
    "tree/fallen": "swamp_fallen_trees",
    "tree/roots": "swamp_tree_roots",
}


def main() -> int:
    total = 0
    for folder, feature in GROUPS.items():
        directory = STRUCTURES / folder
        directory.mkdir(parents=True, exist_ok=True)
        names = sorted(p.stem for p in directory.glob("*.nbt"))
        templates = ["wildscapes:%s/%s" % (folder, name) for name in names]

        path = FEATURES / ("%s.json" % feature)
        config = json.loads(path.read_text(encoding="utf-8"))
        config["config"]["templates"] = templates
        path.write_text(json.dumps(config, indent=2) + "\n", encoding="utf-8")

        print("%-12s %2d structure(s) -> %s" % (folder, len(names), path.name))
        for name in names:
            print("               %s" % name)
        total += len(names)

    if total == 0:
        print("\nNo .nbt files found. Copy your structure-block exports from")
        print("  <world>/generated/minecraft/structures/")
        print("into the folders above, then run this again.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
