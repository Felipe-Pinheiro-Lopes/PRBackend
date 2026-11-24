package br.com.spacedefender.enemies;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class BossShip extends EnemyShip {
    private boolean phaseTwo;
    private boolean movingRight;
    private double pulseAnim;
    private long lastShotTime = 0;

    public BossShip(int x, int y) {
        super(x, y, 160, 110, 1, 1400, 2000);
        this.phaseTwo = false;
        this.movingRight = true;
        this.pulseAnim = 0.0;
        this.lastShotTime = System.currentTimeMillis();
    }

    @Override
    public void update() {
        pulseAnim += 0.08;

        if (getHealth() <= getMaxHealth() / 2 && !phaseTwo) {
            phaseTwo = true;
        }

        double moveSpeed = phaseTwo ? 5.0 : 2.0;
        int currentX = getX();
        int width = getWidth();

        if (movingRight) {
            currentX += moveSpeed;
            if (currentX + width >= 800) {
                currentX = 800 - width;
                movingRight = false;
            }
        } else {
            currentX -= moveSpeed;
            if (currentX <= 0) {
                currentX = 0;
                movingRight = true;
            }
        }
        setX(currentX);

        if (getY() < 75) {
            setY(getY() + (int) Math.ceil(getSpeedY()));
        }
    }

    @Override
    public void updateAI(br.com.spacedefender.entities.PlayerShip player, java.util.List<br.com.spacedefender.projectiles.EnemyProjectile> enemyProjectiles) {
        update();

        if (enemyProjectiles == null) {
            return;
        }

        long now = System.currentTimeMillis();
        long cooldown = phaseTwo ? 1000 : 1700;

        if (now - lastShotTime >= cooldown) {
            int w = getWidth();
            int h = getHeight();
            int x = getX();
            int y = getY();

            if (!phaseTwo) {
                // Fase 1: Disparo duplo pesado das garras laterais
                enemyProjectiles.add(new br.com.spacedefender.projectiles.EnemyProjectile(x + w / 8, y + h, 6, 20));
                enemyProjectiles.add(new br.com.spacedefender.projectiles.EnemyProjectile(x + w * 7 / 8, y + h, 6, 20));
            } else {
                // Fase 2 (Modo Fúria): Disparo triplo concentrado em leque
                enemyProjectiles.add(new br.com.spacedefender.projectiles.EnemyProjectile(x + w / 8, y + h, 7, 25));
                enemyProjectiles.add(new br.com.spacedefender.projectiles.EnemyProjectile(x + w / 2 - 3, y + h + 8, 8, 30));
                enemyProjectiles.add(new br.com.spacedefender.projectiles.EnemyProjectile(x + w * 7 / 8, y + h, 7, 25));
            }
            lastShotTime = now;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        double pulse = Math.sin(pulseAnim) * 0.3 + 0.7;
        Color auraColor = phaseTwo ? new Color(255, 50, 50, (int) (180 * pulse)) : new Color(180, 50, 255, (int) (180 * pulse));
        g2d.setColor(auraColor);
        g2d.fillOval(x - 10, y - 10, w + 20, h + 20);

        g2d.setColor(new Color(20, 10, 40));
        Polygon shell = new Polygon();
        shell.addPoint(x + w / 2, y);
        shell.addPoint(x + w, y + h / 3);
        shell.addPoint(x + w, y + h * 2 / 3);
        shell.addPoint(x + w / 2, y + h);
        shell.addPoint(x, y + h * 2 / 3);
        shell.addPoint(x, y + h / 3);
        g2d.fillPolygon(shell);

        g2d.setColor(new Color(40, 20, 60));
        g2d.fillRect(x + w / 4, y + h / 4, w / 2, h / 2);

        g2d.setColor(new Color(10, 5, 20));
        int[] clawX = {x + w / 4, x, x + w / 8, x + w / 4};
        int[] clawY = {y + h * 3 / 4, y + h + 15, y + h * 3 / 4, y + h * 3 / 4};
        g2d.fillPolygon(new Polygon(clawX, clawY, 4));
        clawX = new int[]{x + w * 3 / 4, x + w, x + w * 7 / 8, x + w * 3 / 4};
        clawY = new int[]{y + h * 3 / 4, y + h + 15, y + h * 3 / 4, y + h * 3 / 4};
        g2d.fillPolygon(new Polygon(clawX, clawY, 4));

        g2d.setColor(new Color(255, 255, 100));
        g2d.fillOval(x + w / 2 - 15, y + h / 3, 30, 20);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + w / 2 - 5, y + h / 3 + 5, 10, 10);

        g2d.setColor(new Color(100, 255, 255));
        g2d.fillOval(x + w / 5, y + h / 3 + 5, 15, 12);
        g2d.fillOval(x + w * 4 / 5 - 15, y + h / 3 + 5, 15, 12);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + w / 5 + 3, y + h / 3 + 7, 5, 5);
        g2d.fillOval(x + w * 4 / 5 - 12, y + h / 3 + 7, 5, 5);

        g2d.setColor(new Color(180, 50, 255, 100));
        g2d.drawOval(x + w / 2 - 20, y + h / 3 - 5, 40, 30);

        int barWidth = 600;
        int barHeight = 20;
        int barX = (800 - barWidth) / 2;
        int barY = 28;

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);

        double healthRatio = getHealth() / (double) getMaxHealth();
        int currentBarWidth = (int) (barWidth * healthRatio);
        Color healthColor = phaseTwo ? Color.RED : new Color(0, 200, 100);
        g2d.setColor(healthColor);
        g2d.fillRect(barX, barY, currentBarWidth, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);

        String statusText = phaseTwo ? "MODO FÚRIA ATIVADO" : "CHEFE SUPREMO";
        String hpText = getHealth() + " / " + getMaxHealth() + " HP";

        Font font = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(font);

        int statusWidth = fm.stringWidth(statusText);
        g2d.setColor(Color.BLACK);
        g2d.drawString(statusText, barX + (barWidth - statusWidth) / 2 + 1, barY - 5 + 1);
        g2d.setColor(phaseTwo ? Color.RED : Color.CYAN);
        g2d.drawString(statusText, barX + (barWidth - statusWidth) / 2, barY - 5);

        int hpWidth = fm.stringWidth(hpText);
        g2d.setColor(Color.BLACK);
        g2d.drawString(hpText, barX + (barWidth - hpWidth) / 2 + 1, barY + barHeight + 18 + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(hpText, barX + (barWidth - hpWidth) / 2, barY + barHeight + 18);
    }

    public boolean isPhaseTwo() {
        return phaseTwo;
    }
}