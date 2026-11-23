package br.com.spacedefender.enemies;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Cruzador de batalha blindado e resistente.
 * Possui alta durabilidade (50 pts de vida), desce em velocidade constante em linha reta
 * e recompensa o jogador com 250 pontos ao ser abatido.
 */
public class ArmoredCruiser extends EnemyShip {
    private static final int DEFAULT_WIDTH = 54;
    private static final int DEFAULT_HEIGHT = 42;
    private static final int DEFAULT_SPEED = 1;
    private static final int DEFAULT_HEALTH = 50;
    private static final int DEFAULT_POINTS = 250;

    public ArmoredCruiser(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_HEALTH, DEFAULT_POINTS);
    }

    @Override
    public void update() {
        // Desce continuamente de forma pesada e firme
        setY(getY() + getSpeed());
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Casco principal robusto hexagonal / blindado
        int[] hullX = {
            x + 10,
            x + w - 10,
            x + w,
            x + w - 8,
            x + w / 2,
            x + 8,
            x
        };
        int[] hullY = {
            y,
            y,
            y + 12,
            y + h - 8,
            y + h,
            y + h - 8,
            y + 12
        };

        // Pintura de armadura pesada (Roxo Metálico / Aço Espacial)
        g2d.setColor(new Color(90, 45, 140));
        g2d.fillPolygon(hullX, hullY, 7);

        // Placas de blindagem cinza-chumbo
        g2d.setColor(new Color(140, 110, 180));
        g2d.fillRect(x + 14, y + 6, w - 28, 14);

        // Contorno reforçado
        g2d.setColor(new Color(200, 180, 255));
        g2d.drawPolygon(hullX, hullY, 7);

        // Canhões frontais duplos inferiores
        g2d.setColor(new Color(255, 60, 60));
        g2d.fillRect(x + 10, y + h - 6, 4, 8);
        g2d.fillRect(x + w - 14, y + h - 6, 4, 8);

        // Minibarra de vida sobre a nave (feedback visual de dano)
        int barWidth = w;
        int barHeight = 4;
        int barY = y - 7;
        g2d.setColor(new Color(40, 40, 40, 180));
        g2d.fillRect(x, barY, barWidth, barHeight);

        double healthRatio = (double) getHealth() / getMaxHealth();
        int currentBarW = (int) (barWidth * healthRatio);
        g2d.setColor(healthRatio > 0.4 ? new Color(50, 220, 90) : new Color(240, 60, 60));
        g2d.fillRect(x, barY, currentBarW, barHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, barY, barWidth, barHeight);
    }
}
