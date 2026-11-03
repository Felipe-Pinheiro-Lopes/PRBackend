package br.com.spacedefender;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GameObject {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int speed;

    public GameObject(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void update() {}

    public void draw(Graphics2D g2d) {}

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSpeed() { return speed; }
}
