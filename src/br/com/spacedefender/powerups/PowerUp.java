package br.com.spacedefender.powerups;

import br.com.spacedefender.GameObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Superclasse/entidade de Power-Up coletável pelo jogador.
 * Possui física de descida suave, animação de pulso e renderização customizada para cada tipo.
 */
public class PowerUp extends GameObject {
    private static final int DEFAULT_SIZE = 26;
    private static final int DEFAULT_SPEED = 2;
    private final PowerUpType type;
    private double pulseAnim = 0;

    public PowerUp(int x, int y, PowerUpType type) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SPEED);
        this.type = type;
    }

    @Override
    public void update() {
        pulseAnim += 0.08;
        // Desce suavemente em direção à base da tela
        setY(getY() + getSpeed());
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();
        int s = getWidth();

        double pulse = Math.sin(pulseAnim) * 0.2 + 0.8;

        switch (type) {
            case DOUBLE_SCORE:
                // 💊 Pílula de Pontos 2X (Metade Vermelha, Metade Branca com aura dourada)
                g2d.setColor(new Color(255, 215, 0, (int) (140 * pulse)));
                g2d.fillOval(x - 3, y - 3, s + 6, s + 6);

                // Corpo da pílula oval
                g2d.setColor(new Color(230, 40, 40));
                g2d.fillRoundRect(x, y, s, s, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(x + s / 2, y, s / 2, s, 12, 12);
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawRoundRect(x, y, s, s, 12, 12);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2d.setColor(Color.BLACK);
                g2d.drawString("2X", x + 5, y + 17);
                break;

            case HEALTH_REPAIR:
                // 💖 Pacote de Reparo (+50 HP) verde esmeralda com cruz branca
                g2d.setColor(new Color(0, 255, 100, (int) (140 * pulse)));
                g2d.fillOval(x - 3, y - 3, s + 6, s + 6);

                g2d.setColor(new Color(15, 150, 75));
                g2d.fillRoundRect(x, y, s, s, 8, 8);
                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(x, y, s, s, 8, 8);

                // Cruz médica central
                g2d.fillRect(x + s / 2 - 2, y + 4, 4, s - 8);
                g2d.fillRect(x + 4, y + s / 2 - 2, s - 8, 4);
                break;

            case TRIPLE_SHOT:
                // 🅰️ Tiro Triplo / Letra 'A' (Cápsula de energia azul neon)
                g2d.setColor(new Color(0, 180, 255, (int) (160 * pulse)));
                g2d.fillOval(x - 3, y - 3, s + 6, s + 6);

                g2d.setColor(new Color(10, 40, 90));
                g2d.fillRoundRect(x, y, s, s, 8, 8);
                g2d.setColor(new Color(0, 220, 255));
                g2d.drawRoundRect(x, y, s, s, 8, 8);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
                g2d.setColor(Color.WHITE);
                g2d.drawString("A", x + 7, y + 19);
                break;

            case SHIELD_BARRIER:
                // 🛡️ Escudo de Força (Orbe ciano concêntrico protetor)
                g2d.setColor(new Color(0, 220, 255, (int) (130 * pulse)));
                g2d.fillOval(x - 4, y - 4, s + 8, s + 8);

                g2d.setColor(new Color(20, 80, 140));
                g2d.fillOval(x, y, s, s);
                g2d.setColor(Color.CYAN);
                g2d.drawOval(x, y, s, s);
                g2d.setColor(Color.WHITE);
                g2d.drawOval(x + 4, y + 4, s - 8, s - 8);
                break;

            case NOVA_BOMB:
                // 💣 Bomba Nuclear Nova (Esfera atômica amarela/laranja radiante)
                g2d.setColor(new Color(255, 120, 0, (int) (170 * pulse)));
                g2d.fillOval(x - 5, y - 5, s + 10, s + 10);

                g2d.setColor(new Color(40, 20, 20));
                g2d.fillOval(x, y, s, s);
                g2d.setColor(new Color(255, 200, 0));
                g2d.drawOval(x, y, s, s);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.setColor(Color.YELLOW);
                g2d.drawString("💥", x + 3, y + 18);
                break;
        }
    }

    public boolean isOffScreen() {
        return getY() > 630;
    }

    public PowerUpType getType() {
        return type;
    }
}
