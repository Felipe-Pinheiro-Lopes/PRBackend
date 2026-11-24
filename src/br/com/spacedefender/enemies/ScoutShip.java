package br.com.spacedefender.enemies;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Inimigo ágil e veloz de patrulha.
 * Possui baixa blindagem (destruído com 1 tiro), desce em trajetória senoidal (zigue-zague)
 * e recompensa o jogador com 100 pontos.
 */
public class ScoutShip extends EnemyShip {
    private static final int DEFAULT_WIDTH = 32;
    private static final int DEFAULT_HEIGHT = 28;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_HEALTH = 10;
    private static final int DEFAULT_POINTS = 100;

    private double centerX;
    private double exactY;

    public ScoutShip(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_HEALTH, DEFAULT_POINTS);
        this.centerX = x;
        this.exactY = y;
    }

    @Override
    public void update() {
        // Desce de forma compassada pelo eixo vertical (velocidade rebalanceada: 1.8 px/frame)
        exactY += 1.8;
        setY((int) exactY);

        // Movimento em zigue-zague senoidal suave ao redor do centro horizontal
        double oscillation = Math.sin(getY() * 0.04) * 32.0;
        int newX = (int) (centerX + oscillation);

        // Previne colisão estrita fora das bordas laterais
        if (newX < 10) {
            newX = 10;
        } else if (newX > 800 - getWidth() - 10) {
            newX = 800 - getWidth() - 10;
        }
        setX(newX);
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Formato de interceptor pontiagudo apontando para baixo (em direção ao jogador)
        int[] shipX = {
            x + w / 2,      // Bico inferior
            x + w,          // Asa direita
            x + w - 4,      // Recuo asa direita
            x + w / 2,      // Traseira central superior
            x + 4,          // Recuo asa esquerda
            x               // Asa esquerda
        };
        int[] shipY = {
            y + h,          // Bico inferior
            y,              // Asa direita
            y + 6,          // Recuo asa direita
            y + 2,          // Traseira central
            y + 6,          // Recuo asa esquerda
            y               // Asa esquerda
        };

        // Casco em Vermelho Carmesim brilhante
        g2d.setColor(new Color(230, 40, 60));
        g2d.fillPolygon(shipX, shipY, 6);

        // Contorno em Laranja Neon
        g2d.setColor(new Color(255, 140, 0));
        g2d.drawPolygon(shipX, shipY, 6);

        // Núcleo de propulsão superior
        g2d.setColor(new Color(255, 220, 50));
        g2d.fillOval(x + w / 2 - 3, y - 2, 6, 6);
    }
}
