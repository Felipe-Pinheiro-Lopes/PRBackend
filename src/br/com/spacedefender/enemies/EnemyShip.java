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
     * Aplica dano à nave inimiga respeitando o limite mínimo de zero.
     * @param damage Quantidade de pontos de vida a subtrair
     */
    public void takeDamage(int damage) {
        if (damage > 0) {
            this.health -= damage;
            if (this.health < 0) {
                this.health = 0;
            }
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
