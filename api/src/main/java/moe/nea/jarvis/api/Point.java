package moe.nea.jarvis.api;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public record Point(double x, double y) {
    public double squaredDistanceTo(@NotNull Point other) {
        double distX = other.x - x;
        double distY = other.y - y;
        return distX * distX + distY * distY;
    }

    public double distanceTo(@NotNull Point other) {
        return Math.sqrt(squaredDistanceTo(other));
    }

    public Vector2ic roundToInt() {
        return new Vector2i((int) x, (int) y);
    }
}
