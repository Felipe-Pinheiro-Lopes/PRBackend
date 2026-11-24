package br.com.spacedefender.projectiles;

import br.com.spacedefender.GameObject;
import java.awt.Graphics2D;

/**
 * Superclasse abstrata que define a estrutura de todos os projéteis do jogo.
 * Aplica os pilares da Abstração e Encapsulamento, servindo como base polimórfica.
 */
public abstract class Projectile extends GameObject {
    private int damage;

    public Projectile(int x, int y, int width, int height, int speed, int damage) {
        super(x, y, width, height, speed);
        this.damage = damage;
    }

    /**
     * Atualização da física do projétil a cada ciclo de clock do Game Loop.
     */
    @Override
    public abstract void update();

    /**
     * Renderização visual polimórfica específica de cada projétil.
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
     * Verifica se o projétil ultrapassou os limites visíveis da tela (800x600).
     * @return true se o projétil saiu completamente da área de jogo.
     */
    public boolean isOffScreen() {
        return (y + height < -20) || (y > 620) || (x + width < -20) || (x > 820);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        if (damage >= 0) {
            this.damage = damage;
        }
    }
}
