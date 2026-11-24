package br.com.spacedefender.projectiles;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Projétil de energia disparado por naves inimigas em direção ao jogador.
 * Causa dano direto aos escudos/vida da nave do jogador.
 */
public class EnemyProjectile extends Projectile {
    private static final int DEFAULT_WIDTH = 6;
    private static final int DEFAULT_HEIGHT = 14;
    private static final int DEFAULT_SPEED = 6;
    private static final int DEFAULT_DAMAGE = 15;

    public EnemyProjectile(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_DAMAGE);
    }

    public EnemyProjectile(int x, int y, int speed, int damage) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, speed, damage);
    }

    @Override
    public void update() {
        // Viaja para baixo, em direção à nave do jogador
        setY(getY() + getSpeed());
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Brilho de dispersão térmica carmesim/laranja
        g2d.setColor(new Color(255, 40, 20, 160));
        g2d.fillOval(x - 2, y - 2, w + 4, h + 4);

        // Núcleo do disparo de plasma inimigo
        g2d.setColor(new Color(255, 120, 0));
        g2d.fillRoundRect(x, y, w, h, 4, 4);

        // Ponto quente incandescente central
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x + 1, y + 2, w - 2, h - 6);
    }

    @Override
    public boolean isOffScreen() {
        return (y > 620) || (y < -20) || (x < -20) || (x > 820);
    }
}
