package br.com.spacedefender.projectiles;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Tiro comum, veloz e de alta cadência disparado pelas armas da nave.
 * Causa dano moderado (10 pts) e viaja rapidamente em direção ao topo da tela.
 */
public class LaserPulse extends Projectile {
    private static final int DEFAULT_WIDTH = 4;
    private static final int DEFAULT_HEIGHT = 16;
    private static final int DEFAULT_SPEED = 12;
    private static final int DEFAULT_DAMAGE = 10;

    public LaserPulse(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_DAMAGE);
    }

    @Override
    public void update() {
        setY(getY() - getSpeed());
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Brilho externo neon (Azul / Ciano brilhante)
        g2d.setColor(new Color(0, 220, 255, 180));
        g2d.fillRect(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2);

        // Feixe de laser central
        g2d.setColor(new Color(0, 180, 255));
        g2d.fillRect(getX(), getY(), getWidth(), getHeight());

        // Núcleo hiper-aquecido em branco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(getX() + 1, getY() + 2, getWidth() - 2, getHeight() - 4);
    }
}
