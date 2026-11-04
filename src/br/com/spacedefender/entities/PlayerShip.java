package br.com.spacedefender.entities;

import br.com.spacedefender.GameObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a nave controlada pelo jogador.
 * Implementa movimentação contínua e suave via controle de estados booleanos de teclas
 * e limites de tela para impedir que a nave ultrapasse os limites jogáveis.
 */
public class PlayerShip extends GameObject {
    private int lives = 3;
    private int score = 0;
    private long lastShotTime = 0;
    private static final int SHOT_COOLDOWN = 200; // ms entre tiros
    private List<Projectile> projectiles = new ArrayList<>();

    // Variáveis booleanas para movimentação fluida (Dica do Mestre)
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public PlayerShip(int screenWidth, int screenHeight) {
        super(
            screenWidth / 2 - 25,
            screenHeight - 80,
            50, 40, 5
        );
    }

    /**
     * Atualiza o estado das teclas pressionadas/liberadas (Setas ou W, A, S, D).
     * @param keyCode Código da tecla capturada pelo KeyListener
     * @param pressed true se pressionada (keyPressed), false se liberada (keyReleased)
     */
    public void handleInput(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            upPressed = pressed;
        }
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            downPressed = pressed;
        }
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            leftPressed = pressed;
        }
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            rightPressed = pressed;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            if (pressed) {
                shoot();
            }
        }
    }

    /**
     * Dispara projéteis respeitando o tempo de recarga (cooldown).
     */
    private void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime >= SHOT_COOLDOWN) {
            projectiles.add(new Projectile(getX() + getWidth() / 2 - 2, getY()));
            lastShotTime = now;
        }
    }

    /**
     * Atualiza as coordenadas x e y da nave conforme as teclas ativas no Game Loop
     * e impede que ela saia dos limites da janela.
     */
    @Override
    public void update() {
        // Movimentação suave baseada nas flags ativas
        if (upPressed) {
            y -= speed;
        }
        if (downPressed) {
            y += speed;
        }
        if (leftPressed) {
            x -= speed;
        }
        if (rightPressed) {
            x += speed;
        }

        // Impedir que a nave saia da tela (limites de 800x600)
        if (x < 0) {
            x = 0;
        }
        if (x > 800 - width) {
            x = 800 - width;
        }
        if (y < 0) {
            y = 0;
        }
        if (y > 600 - height) {
            y = 600 - height;
        }

        // Atualiza os projéteis e remove aqueles que já saíram da tela
        projectiles.removeIf(p -> {
            p.update();
            return p.isOffScreen();
        });
    }

    /**
     * Desenha a nave estelar com Graphics2D, exibindo asas, bico, cabine e propulsores.
     */
    @Override
    public void draw(Graphics2D g2d) {
        // Efeito visual dos propulsores (fogo quando em movimento, brilho ocioso parada)
        if (upPressed || downPressed || leftPressed || rightPressed) {
            // Chama da turbina externa
            g2d.setColor(new Color(255, 120, 0, 220));
            int[] flameX = { x + width / 2 - 6, x + width / 2, x + width / 2 + 6 };
            int[] flameY = { y + height - 4, y + height + 10, y + height - 4 };
            g2d.fillPolygon(flameX, flameY, 3);

            // Núcleo quente da chama
            g2d.setColor(new Color(255, 230, 50, 240));
            int[] innerFlameX = { x + width / 2 - 3, x + width / 2, x + width / 2 + 3 };
            int[] innerFlameY = { y + height - 4, y + height + 5, y + height - 4 };
            g2d.fillPolygon(innerFlameX, innerFlameY, 3);
        } else {
            // Brilho azul ocioso da turbina
            g2d.setColor(new Color(0, 180, 255, 180));
            g2d.fillRect(x + width / 2 - 4, y + height - 3, 8, 4);
        }

        // Polígono do casco da nave (formato triangular com reentrâncias para asas)
        int[] shipX = {
            x + width / 2,          // Bico frontal da nave
            x + width,              // Ponta da asa direita
            x + width - 8,          // Recuo da asa direita
            x + width / 2 + 7,      // Traseira direita
            x + width / 2 - 7,      // Traseira esquerda
            x + 8,                  // Recuo da asa esquerda
            x                       // Ponta da asa esquerda
        };
        int[] shipY = {
            y,                      // Bico
            y + height,             // Asa direita
            y + height - 6,         // Recuo direita
            y + height - 2,         // Traseira direita
            y + height - 2,         // Traseira esquerda
            y + height - 6,         // Recuo esquerda
            y + height              // Asa esquerda
        };

        // Pintura principal do casco (Ciano Metálico espacial)
        g2d.setColor(new Color(0, 190, 230));
        g2d.fillPolygon(shipX, shipY, 7);

        // Contorno elegante em branco
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(shipX, shipY, 7);

        // Cabine de comando (Cockpit)
        g2d.setColor(new Color(15, 35, 75));
        g2d.fillOval(x + width / 2 - 5, y + 10, 10, 15);
        g2d.setColor(new Color(140, 220, 255));
        g2d.drawOval(x + width / 2 - 5, y + 10, 10, 15);

        // Canhões nas extremidades das asas
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(x, y + height - 10, 3, 10);
        g2d.fillRect(x + width - 3, y + height - 10, 3, 10);

        // Desenha os projéteis disparados
        for (Projectile p : projectiles) {
            p.draw(g2d);
        }
    }

    public List<Projectile> getProjectiles() { return projectiles; }
    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    public int getScore() { return score; }
    public void addScore(int points) { score += points; }

    public boolean isUpPressed() { return upPressed; }
    public void setUpPressed(boolean upPressed) { this.upPressed = upPressed; }

    public boolean isDownPressed() { return downPressed; }
    public void setDownPressed(boolean downPressed) { this.downPressed = downPressed; }

    public boolean isLeftPressed() { return leftPressed; }
    public void setLeftPressed(boolean leftPressed) { this.leftPressed = leftPressed; }

    public boolean isRightPressed() { return rightPressed; }
    public void setRightPressed(boolean rightPressed) { this.rightPressed = rightPressed; }

    /**
     * Projétil temporário disparado pela nave (será refatorado na Sprint 2).
     */
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
