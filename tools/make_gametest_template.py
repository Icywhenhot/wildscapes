"""Writes the bare dirt platform the tree game tests run on.

    python tools/make_gametest_template.py

Game tests need a structure to define their area; this one is a 24x24 floor of dirt with room
above it — wide enough that the biggest tree stays inside its own plot rather than spilling
into the next test's and tripping the overlap check.
"""

import gzip
import pathlib
import struct

OUT = pathlib.Path(__file__).resolve().parent.parent / (
    "src/main/resources/data/wildscapes/structure/gametest/tree_platform.nbt")

DATA_VERSION = 3955  # 1.21.1
SIZE = (24, 20, 24)


def _string(value):
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def _named(tag, name, payload):
    return struct.pack(">b", tag) + _string(name) + payload


def _int_list(values):
    return struct.pack(">bi", 3, len(values)) + b"".join(struct.pack(">i", v) for v in values)


def _compound(*parts):
    return b"".join(parts) + b"\x00"


def main():
    palette = struct.pack(">bi", 10, 1) + _compound(_named(8, "Name", _string("minecraft:dirt")))

    floor = [(x, 0, z) for x in range(SIZE[0]) for z in range(SIZE[2])]
    blocks = struct.pack(">bi", 10, len(floor)) + b"".join(
        _compound(_named(3, "state", struct.pack(">i", 0)),
                  _named(9, "pos", _int_list(pos)))
        for pos in floor)

    root = _compound(
        _named(3, "DataVersion", struct.pack(">i", DATA_VERSION)),
        _named(9, "size", _int_list(SIZE)),
        _named(9, "palette", palette),
        _named(9, "blocks", blocks),
        _named(9, "entities", struct.pack(">bi", 0, 0)))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    with gzip.open(OUT, "wb") as f:
        f.write(_named(10, "", root))
    print("wrote %s (%d blocks)" % (OUT, len(floor)))


if __name__ == "__main__":
    main()
