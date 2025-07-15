package moe.nea.jarvis.api;

public record Rectangle(double left, double top, double right, double bottom) {
    public Rectangle(Point topLeft, Point bottomRight) {
        this(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
}
