package br.com.spacedefender.entities;

import br.com.spacedefender.GameObject;
import br.com.spacedefender.audio.SoundManager;
import br.com.spacedefender.audio.SoundType;
import br.com.spacedefender.projectiles.LaserPulse;
import br.com.spacedefender.projectiles.PlasmaBall;
import br.com.spacedefender.projectiles.Projectile;
import br.com.spacedefender.projectiles.WeaponType;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a nave controlada pelo jogador.
 * Implementa movimentação contínua e suave via controle de estados booleanos de teclas,
 * limites de tela, alternância polimórfica de armas e gerenciamento de projéteis.
 */
public class PlayerShip extends GameObject {
    private int lives = 3;
    private int maxHealth = 100;
    private int health = 100;
    private int score = 0;
    private long lastShotTime = 0;
    private WeaponType activeWeapon = WeaponType.LASER;
    private List<Projectile> projectiles = new ArrayList<>();

    // Estados e Temporizadores de Power-Ups
    private boolean doubleScoreActive = false;
    private long doubleScoreEndTime = 0;
    private boolean tripleShotActive = false;
    private long tripleShotEndTime = 0;
    private boolean shieldActive = false;
    private long shieldEndTime = 0;

    // Variáveis booleanas para movimentação e disparo fluidos (Dica do Mestre)
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    public PlayerShip(int screenWidth, int screenHeight) {
        super(
            screenWidth / 2 - 25,
            screenHeight - 80,
            50, 40, 5
        );
    }

    /**
     * Atualiza o estado das teclas pressionadas/liberadas (Setas ou W, A, S, D, Barra de Espaço e 1, 2, Q).
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
            spacePressed = pressed;
        }
        // Alternância de Armas (Polimorfismo dinâmico)
        if (pressed) {
            if (keyCode == KeyEvent.VK_1) {
                setActiveWeapon(WeaponType.LASER);
            } else if (keyCode == KeyEvent.VK_2) {
                setActiveWeapon(WeaponType.PLASMA);
            } else if (keyCode == KeyEvent.VK_Q) {
                switchWeapon();
            }
        }
    }

    /**
     * Dispara projéteis respeitando a cadência específica (cooldown) da arma ativa.
     * Suporta disparo simples ou tiro triplo expandido caso o buff de TRIPLE_SHOT esteja ativo.
     */
    private void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime >= activeWeapon.getCooldownMs()) {
            if (activeWeapon == WeaponType.LASER) {
                if (tripleShotActive) {
                    // Tiro Triplo: Asa esquerda, bico central e asa direita
                    projectiles.add(new LaserPulse(getX() + 2, getY() + 6));
                    projectiles.add(new LaserPulse(getX() + getWidth() / 2 - 2, getY() - 14));
                    projectiles.add(new LaserPulse(getX() + getWidth() - 6, getY() + 6));
                } else {
                    // Laser Pulse padrão centrado no bico
                    projectiles.add(new LaserPulse(getX() + getWidth() / 2 - 2, getY() - 14));
                }
                SoundManager.getInstance().play(SoundType.LASER);
            } else if (activeWeapon == WeaponType.PLASMA) {
                if (tripleShotActive) {
                    // Tríade de Plasma destrutiva
                    projectiles.add(new PlasmaBall(getX() - 4, getY() + 4));
                    projectiles.add(new PlasmaBall(getX() + getWidth() / 2 - 6, getY() - 14));
                    projectiles.add(new PlasmaBall(getX() + getWidth() - 8, getY() + 4));
                } else {
                    // Plasma Ball padrão centrado no bico
                    projectiles.add(new PlasmaBall(getX() + getWidth() / 2 - 6, getY() - 14));
                }
                SoundManager.getInstance().play(SoundType.PLASMA);
            }
            lastShotTime = now;
        }
    }

    /**
     * Atualiza as coordenadas x e y da nave conforme as teclas ativas no Game Loop,
     * impede que ela saia dos limites da janela e gerencia o ciclo de vida dos projéteis.
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

        // Disparo contínuo e sincronizado ao Game Loop durante a movimentação da nave
        if (spacePressed) {
            shoot();
        }

        // Expiração dos efeitos temporários de Power-Ups
        long now = System.currentTimeMillis();
        if (doubleScoreActive && now > doubleScoreEndTime) {
            doubleScoreActive = false;
        }
        if (tripleShotActive && now > tripleShotEndTime) {
            tripleShotActive = false;
        }
        if (shieldActive && now > shieldEndTime) {
            shieldActive = false;
        }

        // Atualização polimórfica dos projéteis
        for (Projectile p : projectiles) {
            p.update();
        }

        // Descarte seguro de projéteis que saíram dos limites (evita ConcurrentModificationException)
        projectiles.removeIf(Projectile::isOffScreen);
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

        // Efeito visual do Escudo Protetor (Power-Up SHIELD_BARRIER ativo)
        if (shieldActive) {
            long time = System.currentTimeMillis();
            int alpha = (int) (110 + 40 * Math.sin(time * 0.008));
            g2d.setColor(new Color(0, 220, 255, Math.max(50, Math.min(200, alpha))));
            g2d.fillOval(x - 8, y - 8, width + 16, height + 16);
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.drawOval(x - 8, y - 8, width + 16, height + 16);
        }

        // Desenha os projéteis disparados
        for (Projectile p : projectiles) {
            p.draw(g2d);
        }
    }

    public List<Projectile> getProjectiles() { return projectiles; }
    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(maxHealth, health)); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = Math.max(1, maxHealth); }
    public boolean isAlive() { return lives > 0; }

    public void takeDamage(int damage) {
        // Se o escudo de força estiver ativo, o dano é 100% absorvido!
        if (shieldActive) {
            return;
        }

        if (damage > 0) {
            SoundManager.getInstance().play(SoundType.PLAYER_HIT);
            this.health -= damage;
            if (this.health <= 0) {
                this.health = 0;
            }
        }
    }

    public void reset(int screenWidth, int screenHeight) {
        this.lives = 3;
        this.health = this.maxHealth;
        this.score = 0;
        this.x = screenWidth / 2 - 25;
        this.y = screenHeight - 80;
        this.projectiles.clear();
        this.doubleScoreActive = false;
        this.tripleShotActive = false;
        this.shieldActive = false;
        this.upPressed = false;
        this.downPressed = false;
        this.leftPressed = false;
        this.rightPressed = false;
        this.spacePressed = false;
    }

    public void respawn(int screenWidth, int screenHeight) {
        this.health = this.maxHealth;
        this.x = screenWidth / 2 - 25;
        this.y = screenHeight - 80;
        this.projectiles.clear();
        this.shieldActive = true;
        this.shieldEndTime = System.currentTimeMillis() + 3000;
        this.upPressed = false;
        this.downPressed = false;
        this.leftPressed = false;
        this.rightPressed = false;
        this.spacePressed = false;
    }

    public void heal(int amount) {
        if (amount > 0) {
            this.health = Math.min(this.maxHealth, this.health + amount);
        }
    }

    public int getScore() { return score; }

    public void addScore(int points) {
        if (points > 0) {
            if (doubleScoreActive) {
                score += (points * 2);
            } else {
                score += points;
            }
        }
    }

    // Gerenciamento e Ativação de Buffs
    public void activateDoubleScore(long durationMs) {
        this.doubleScoreActive = true;
        this.doubleScoreEndTime = System.currentTimeMillis() + durationMs;
    }

    public void activateTripleShot(long durationMs) {
        this.tripleShotActive = true;
        this.tripleShotEndTime = System.currentTimeMillis() + durationMs;
    }

    public void activateShield(long durationMs) {
        this.shieldActive = true;
        this.shieldEndTime = System.currentTimeMillis() + durationMs;
    }

    public boolean isDoubleScoreActive() { return doubleScoreActive; }
    public boolean isTripleShotActive() { return tripleShotActive; }
    public boolean isShieldActive() { return shieldActive; }

    public long getDoubleScoreRemainingSeconds() {
        return Math.max(0, (doubleScoreEndTime - System.currentTimeMillis() + 999) / 1000);
    }

    public long getTripleShotRemainingSeconds() {
        return Math.max(0, (tripleShotEndTime - System.currentTimeMillis() + 999) / 1000);
    }

    public long getShieldRemainingSeconds() {
        return Math.max(0, (shieldEndTime - System.currentTimeMillis() + 999) / 1000);
    }

    public boolean isUpPressed() { return upPressed; }
    public void setUpPressed(boolean upPressed) { this.upPressed = upPressed; }

    public boolean isDownPressed() { return downPressed; }
    public void setDownPressed(boolean downPressed) { this.downPressed = downPressed; }

    public boolean isLeftPressed() { return leftPressed; }
    public void setLeftPressed(boolean leftPressed) { this.leftPressed = leftPressed; }

    public boolean isRightPressed() { return rightPressed; }
    public void setRightPressed(boolean rightPressed) { this.rightPressed = rightPressed; }

    public boolean isSpacePressed() { return spacePressed; }
    public void setSpacePressed(boolean spacePressed) { this.spacePressed = spacePressed; }

    public WeaponType getActiveWeapon() {
        return activeWeapon;
    }

    public void setActiveWeapon(WeaponType weapon) {
        if (weapon != null) {
            this.activeWeapon = weapon;
        }
    }

    public void switchWeapon() {
        this.activeWeapon = (activeWeapon == WeaponType.LASER) ? WeaponType.PLASMA : WeaponType.LASER;
    }
}
