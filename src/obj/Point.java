package obj;

import java.awt.*;

/**
 * Created by chengle on 16/10/3.
 */
public class Point {
    private int x;
    private int y;
    private Color color;
    public static final int DIAMETER = 30;

    public Point(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }
}
