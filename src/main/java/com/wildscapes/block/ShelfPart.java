package com.wildscapes.block;

import net.minecraft.util.StringRepresentable;

public enum ShelfPart implements StringRepresentable {
    UNCONNECTED("unconnected"),
    RIGHT("right"),
    CENTER("center"),
    LEFT("left");

    private final String name;

    ShelfPart(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean isConnected() {
        return this != UNCONNECTED;
    }

    public boolean isConnectionTowards(final ShelfPart endPart) {
        return this == CENTER || this == endPart;
    }

    public boolean isChainEnd() {
        return this != CENTER;
    }

    public ShelfPart whenConnectedToTheRight() {
        return switch (this) {
            case UNCONNECTED, LEFT -> LEFT;
            case RIGHT, CENTER -> CENTER;
        };
    }

    public ShelfPart whenConnectedToTheLeft() {
        return switch (this) {
            case UNCONNECTED, RIGHT -> RIGHT;
            case CENTER, LEFT -> CENTER;
        };
    }

    public ShelfPart whenDisconnectedFromTheRight() {
        return switch (this) {
            case UNCONNECTED, LEFT -> UNCONNECTED;
            case RIGHT, CENTER -> RIGHT;
        };
    }

    public ShelfPart whenDisconnectedFromTheLeft() {
        return switch (this) {
            case UNCONNECTED, RIGHT -> UNCONNECTED;
            case CENTER, LEFT -> LEFT;
        };
    }
}
