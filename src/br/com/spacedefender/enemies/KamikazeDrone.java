package br.com.spacedefender.enemies;

import br.com.spacedefender.entities.PlayerShip;
import br.com.spacedefender.projectiles.EnemyProjectile;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Drone interceptor autônomo e kamikaze.
 * Trava mira no eixo horizontal da nave do jogador e mergulha em alta velocidade.
 * Possui 5 mecânicas comportamentais e renderização com luz estroboscópica de alerta.
 */
public class KamikazeDrone extends EnemyShip {
    private static final int DEFAULT_WIDTH = 26;
    private static final int DEFAULT_HEIGHT = 26;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_HEALTH = 15;
    private static final int DEFAULT_POINTS = 150;

    private double exactX;
    private double exactY;
    private double speedY = 2.2;
    private boolean lockedOn = false;
    private double targetX = 400;
    private double strobeAnim = 0;

    public KamikazeDrone(int x, int y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, DEFAULT_HEALTH, DEFAULT_POINTS);
        this.exactX = x;
        this.exactY = y;
    }

    @Override
    public void update() {
        strobeAnim += 0.25;

        // Mecânica 1: Aceleração contínua de mergulho (speed dive)
        if (lockedOn) {
            speedY = Math.min(5.2, speedY + 0.08); // Mergulho supersônico
        }
        exactY += speedY;
        setY((int) exactY);

        // Mecânica 2: Rastreamento teleguiado (Homing Lock-on) convergindo no alvo
        if (!lockedOn || getY() < 300) {
            double dx = targetX - (exactX + getWidth() / 2.0);
            exactX += Math.signum(dx) * Math.min(2.8, Math.abs(dx) * 0.06);
            setX((int) exactX);
        }
    }

    @Override
    public void updateAI(PlayerShip player, List<EnemyProjectile> enemyProjectiles) {
        if (player != null) {
            targetX = player.getX() + player.getWidth() / 2.0;

            // Mecânica 3: Trava de mira definitiva (Lock-on Dive) quando entra na área de perigo
            if (getY() > 180 && !lockedOn) {
                lockedOn = true;
            }
        }
        update();
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Mecânica 4: Luz estroboscópica de perigo pulsante
        boolean strobe = Math.sin(strobeAnim) > 0;
        g2d.setColor(strobe ? new Color(255, 0, 0, 140) : new Color(255, 200, 0, 80));
        g2d.fillOval(x - 4, y - 4, w + 8, h + 8);

        // Casco delta em ponta afiada (Formato de asa voadora furtiva)
        int[] droneX = { x + w / 2, x + w, x + w / 2, x };
        int[] droneY = { y + h, y, y + 6, y };

        // Pintura preta e amarela (alerta de risco de colisão)
        g2d.setColor(new Color(30, 30, 35));
        g2d.fillPolygon(droneX, droneY, 4);

        g2d.setColor(strobe ? Color.RED : Color.ORANGE);
        g2d.drawPolygon(droneX, droneY, 4);

        // Olho ótico central do drone
        g2d.setColor(Color.CYAN);
        g2d.fillOval(x + w / 2 - 2, y + 8, 5, 5);

        drawShieldIfActive(g2d);
    }
}
