package br.com.spacedefender.enemies;

import br.com.spacedefender.GameObject;
import java.awt.Graphics2D;

/**
 * Superclasse abstrata que define a estrutura de todas as naves invasoras (inimigos).
 * Aplica os pilares da Abstração, Encapsulamento e Herança como base para o Polimorfismo.
 */
public abstract class EnemyShip extends GameObject {
    private int health;
    private int maxHealth;
    private int pointValue;
    private double speedY;
    private boolean shieldActive = false;
    private long shieldEndTime = 0;

    public EnemyShip(int x, int y, int width, int height, int speed, int health, int pointValue) {
        super(x, y, width, height, speed);
        this.health = health;
        this.maxHealth = health;
        this.pointValue = pointValue;
        this.speedY = speed;
    }

    /**
     * Atualização do comportamento de movimento e física do inimigo no Game Loop.
     */
    @Override
    public abstract void update();

    /**
     * Ciclo de IA e tomada de decisão contextualizada (disparo de projéteis, esquiva, mira no jogador).
     * @param player Referência da nave do jogador
     * @param enemyProjectiles Coleção compartilhada de tiros inimigos
     */
    public void updateAI(br.com.spacedefender.entities.PlayerShip player, java.util.List<br.com.spacedefender.projectiles.EnemyProjectile> enemyProjectiles) {
        update();
    }

    /**
     * Renderização visual polimórfica específica de cada modelo de nave inimiga.
     */
    @Override
    public abstract void draw(Graphics2D g2d);

    /**
     * Alias pedagógico para draw(Graphics2D g2d).
     */
    public void render(Graphics2D g2d) {
        draw(g2d);
    }

    /**
     * Aplica dano à nave inimiga respeitando o limite mínimo de zero e o escudo de energia.
     * @param damage Quantidade de pontos de vida a subtrair
     */
    public void takeDamage(int damage) {
        if (damage > 0) {
            // Se o escudo defletor de energia estiver ativo, absorve 100% do impacto
            if (isShieldActive()) {
                return;
            }
            this.health -= damage;
            if (this.health < 0) {
                this.health = 0;
            }
        }
    }

    /**
     * Ativa barreira de força temporária.
     */
    public void activateShield(long durationMs) {
        this.shieldActive = true;
        this.shieldEndTime = System.currentTimeMillis() + durationMs;
    }

    public boolean isShieldActive() {
        if (shieldActive && System.currentTimeMillis() > shieldEndTime) {
            shieldActive = false;
        }
        return shieldActive;
    }

    /**
     * Desenha campo de força protetor sobre o casco caso ativo.
     */
    protected void drawShieldIfActive(Graphics2D g2d) {
        if (isShieldActive()) {
            g2d.setColor(new java.awt.Color(0, 220, 255, 120));
            g2d.fillOval(getX() - 6, getY() - 6, getWidth() + 12, getHeight() + 12);
            g2d.setColor(java.awt.Color.WHITE);
            g2d.drawOval(getX() - 6, getY() - 6, getWidth() + 12, getHeight() + 12);
        }
    }

    /**
     * Indica se a nave foi completamente aniquilada.
     */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /**
     * Verifica se o inimigo ultrapassou a base da tela ou limites operacionais.
     */
    public boolean isOffScreen() {
        return (y > 650) || (y < -150) || (x < -150) || (x > 950);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
    }

    public int getPointValue() {
        return pointValue;
    }

    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }
}
