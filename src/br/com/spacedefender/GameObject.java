package br.com.spacedefender;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Superclasse base para todas as entidades e objetos interativos do jogo Space Defender.
 * Fornece atributos de posicionamento, dimensões, velocidade e métodos de ciclo de vida.
 */
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

    /**
     * Atualização de lógica, física e coordenadas do objeto a cada ciclo do Game Loop.
     */
    public void update() {}

    /**
     * Renderização visual do objeto através do Graphics2D.
     */
    public void draw(Graphics2D g2d) {}

    /**
     * Retorna o retângulo de colisão da entidade (AABB).
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
}
