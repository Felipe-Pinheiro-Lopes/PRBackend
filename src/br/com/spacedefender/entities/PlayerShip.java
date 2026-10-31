package br.com.spacedefender.entities;

import br.com.spacedefender.GameObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class PlayerShip extends GameObject {
    private int lives = 3;
    private int score = 0;
    private long lastShotTime = 0;
    private static final int SHOT_COOLDOWN = 200;
    private List<Projectile> projectiles = new ArrayList<>();

    public PlayerShip(int screenWidth, int screenHeight) {
        super(
            screenWidth / 2 - 25,
            screenHeight - 80,
            50, 40, 5
        );
    }

    public void handleInput(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            if (pressed) setX(getX() - getSpeed());
        }
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            if (pressed) setX(getX() + getSpeed());
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            if (pressed) shoot();
        }
    }

    private void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime >= SHOT_COOLDOWN) {
            projectiles.add(new Projectile(getX() + getWidth() / 2 - 2, getY()));
            lastShotTime = now;
        }
    }

    @Override
    public void update() {
        if (getX() < 0) setX(0);
        if (getX() > 800 - getWidth()) setX(800 - getWidth());

        projectiles.removeIf(p -> {
            p.update();
            return p.isOffScreen();
        });
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.CYAN);
        g2d.fillRect(getX(), getY(), getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.drawRect(getX(), getY(), getWidth(), getHeight());

        for (Projectile p : projectiles) {
            p.draw(g2d);
        }
    }

    public List<Projectile> getProjectiles() { return projectiles; }
    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    public int getScore() { return score; }
    public void addScore(int points) { score += points; }

    public static class Projectile extends GameObject {
        private static final int PROJ_SPEED = 10;

        public Projectile(int x, int y) {
            super(x, y, 4, 10, PROJ_SPEED);
        }

        @Override
        public void update() {
            setY(getY() - getSpeed());
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(getX(), getY(), getWidth(), getHeight());
        }

        public boolean isOffScreen() {
            return getY() + getHeight() < 0;
        }
    }
}