package br.com.spacedefender.projectiles;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Tiro pesado e concentrado disparado pelo canhão de plasma da nave.
 * Causa alto dano (25 pts), mas viaja em velocidade mais reduzida.
 */
public class PlasmaBall extends Projectile {
    private static final int DEFAULT_WIDTH = 12;
    private static final int DEFAULT_HEIGHT = 12;
    private static final int DEFAULT_SPEED = 7;
    private static final int DEFAULT_DAMAGE = 25;

    public PlasmaBall(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_DAMAGE);
    }

    @Override
    public void update() {
        setY(getY() - getSpeed());
    }

    @Override
    public void draw(Graphics2D g2d) {
        // Halo de energia externa (Verde / Amarelo incandescente com transparência)
        g2d.setColor(new Color(100, 255, 50, 120));
        g2d.fillOval(getX() - 3, getY() - 3, getWidth() + 6, getHeight() + 6);

        // Esfera de contenção de plasma
        g2d.setColor(new Color(255, 230, 0));
        g2d.fillOval(getX(), getY(), getWidth(), getHeight());

        // Núcleo denso e instável
        g2d.setColor(Color.WHITE);
        g2d.fillOval(getX() + 3, getY() + 3, getWidth() - 6, getHeight() - 6);
    }
}
