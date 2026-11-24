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
    private boolean hasShot = false;
    private boolean hasEvaded = false;
    private double currentSpeedY = 1.8;

    public ScoutShip(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_HEALTH, DEFAULT_POINTS);
        this.centerX = x;
        this.exactY = y;
    }

    @Override
    public void update() {
        // Mecânica 1: Deslocamento vertical com aceleração progressiva
        exactY += currentSpeedY;
        setY((int) exactY);

        // Mecânica 2: Movimento senoidal dinâmico em zigue-zague
        double oscillation = Math.sin(getY() * 0.04) * 32.0;
        int newX = (int) (centerX + oscillation);

        if (newX < 10) {
            newX = 10;
        } else if (newX > 800 - getWidth() - 10) {
            newX = 800 - getWidth() - 10;
        }
        setX(newX);

        // Mecânica 3: Arrancada tática (Speed Burst) após ultrapassar metade da tela
        if (getY() > 280 && currentSpeedY < 2.5) {
            currentSpeedY = 2.5;
        }
    }

    @Override
    public void updateAI(br.com.spacedefender.entities.PlayerShip player, java.util.List<br.com.spacedefender.projectiles.EnemyProjectile> enemyProjectiles) {
        update();

        if (player == null || enemyProjectiles == null) {
            return;
        }

        // Mecânica 4: Esquiva lateral evasiva caso o player esteja na linha de tiro frontal
        if (!hasEvaded && getY() > 100 && getY() < 350) {
            if (Math.abs((getX() + getWidth() / 2) - (player.getX() + player.getWidth() / 2)) < 35) {
                centerX += (centerX > 400) ? -55 : 55;
                hasEvaded = true;
            }
        }

        // Mecânica 5: Disparo de pulso de laser inimigo oportuno ao se aproximar
        if (!hasShot && getY() > 140 && getY() < 420) {
            if (Math.abs((getX() + getWidth() / 2) - (player.getX() + player.getWidth() / 2)) < 80) {
                enemyProjectiles.add(new br.com.spacedefender.projectiles.EnemyProjectile(getX() + getWidth() / 2 - 3, getY() + getHeight()));
                hasShot = true;
            }
        }
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
