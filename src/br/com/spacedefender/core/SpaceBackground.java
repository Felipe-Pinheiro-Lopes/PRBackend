package br.com.spacedefender.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Fundo espacial dinâmico com efeito Parallax multicamadas.
 * Renderiza nebulosas cósmicas, planetas distantes e três camadas de estrelas
 * com velocidades de deslocamento independentes para criar forte sensação de profundidade e velocidade.
 */
public class SpaceBackground {
    private final int width;
    private final int height;
    private final Random random = new Random();

    // Camada 1: Corpos Celestes e Nebulosas distantes (lento: ~0.2 px/frame)
    private double planetY = 80;
    private double planetX = 620;
    private double nebulaY = -100;

    // Camada 2: Estrelas Médias (velocidade moderada: ~1.0 px/frame)
    private static final int NUM_STARS_MID = 85;
    private final double[] midStarsX = new double[NUM_STARS_MID];
    private final double[] midStarsY = new double[NUM_STARS_MID];
    private final Color[] midStarsColor = new Color[NUM_STARS_MID];
    private final int[] midStarsSize = new int[NUM_STARS_MID];

    // Camada 3: Estrelas Próximas / Velozes com Rastro (rápido: ~2.8 px/frame)
    private static final int NUM_STARS_FAST = 28;
    private final double[] fastStarsX = new double[NUM_STARS_FAST];
    private final double[] fastStarsY = new double[NUM_STARS_FAST];
    private final double[] fastStarsSpeed = new double[NUM_STARS_FAST];

    private double twinkleAnim = 0;

    public SpaceBackground(int width, int height) {
        this.width = width;
        this.height = height;
        initStars();
    }

    private void initStars() {
        Color[] starPalette = {
            new Color(255, 255, 255),
            new Color(180, 220, 255),
            new Color(255, 240, 200),
            new Color(200, 240, 255)
        };

        // Inicializa estrelas médias
        for (int i = 0; i < NUM_STARS_MID; i++) {
            midStarsX[i] = random.nextInt(width);
            midStarsY[i] = random.nextInt(height);
            midStarsColor[i] = starPalette[random.nextInt(starPalette.length)];
            midStarsSize[i] = random.nextInt(2) + 1; // 1 a 2 pixels
        }

        // Inicializa estrelas rápidas
        for (int i = 0; i < NUM_STARS_FAST; i++) {
            fastStarsX[i] = random.nextInt(width);
            fastStarsY[i] = random.nextInt(height);
            fastStarsSpeed[i] = 2.4 + random.nextDouble() * 1.2; // 2.4 a 3.6 px/frame
        }
    }

    /**
     * Atualiza o deslocamento de todas as camadas do parallax.
     */
    public void update() {
        twinkleAnim += 0.05;

        // Deslocamento da Nebulosa e Planeta distante
        nebulaY += 0.15;
        if (nebulaY > height + 300) {
            nebulaY = -400;
        }

        planetY += 0.22;
        if (planetY > height + 150) {
            planetY = -150;
            planetX = 80 + random.nextInt(width - 200);
        }

        // Deslocamento das Estrelas Médias
        for (int i = 0; i < NUM_STARS_MID; i++) {
            midStarsY[i] += 0.95;
            if (midStarsY[i] > height) {
                midStarsY[i] = -5;
                midStarsX[i] = random.nextInt(width);
            }
        }

        // Deslocamento das Estrelas Rápidas
        for (int i = 0; i < NUM_STARS_FAST; i++) {
            fastStarsY[i] += fastStarsSpeed[i];
            if (fastStarsY[i] > height + 10) {
                fastStarsY[i] = -10;
                fastStarsX[i] = random.nextInt(width);
            }
        }
    }

    /**
     * Renderiza o cenário espacial completo.
     */
    public void draw(Graphics2D g2d) {
        // 1. Fundo Espaço Profundo (Preto azulado cósmico)
        g2d.setColor(new Color(6, 6, 16));
        g2d.fillRect(0, 0, width, height);

        // 2. Nebulosa Cósmica Procedural (Brilho radial sutil)
        drawNebula(g2d, (int) (width * 0.3), (int) nebulaY, 320, new Color(75, 20, 110, 45));
        drawNebula(g2d, (int) (width * 0.75), (int) (nebulaY + 280), 280, new Color(15, 60, 120, 40));

        // 3. Planeta Distante com Anel
        drawDistantPlanet(g2d, (int) planetX, (int) planetY);

        // 4. Camada de Estrelas Médias com Cintilação
        double twinkle = Math.sin(twinkleAnim) * 0.2 + 0.8;
        for (int i = 0; i < NUM_STARS_MID; i++) {
            Color base = midStarsColor[i];
            int alpha = (int) Math.min(255, Math.max(120, base.getAlpha() * twinkle));
            g2d.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
            g2d.fillRect((int) midStarsX[i], (int) midStarsY[i], midStarsSize[i], midStarsSize[i]);
        }

        // 5. Camada de Estrelas Próximas com Efeito de Rastro (Warp Speed)
        for (int i = 0; i < NUM_STARS_FAST; i++) {
            int sx = (int) fastStarsX[i];
            int sy = (int) fastStarsY[i];
            int streakLength = (int) (fastStarsSpeed[i] * 3.5);

            // Rastro com degradê de luz
            g2d.setColor(new Color(180, 220, 255, 90));
            g2d.drawLine(sx, sy - streakLength, sx, sy);

            // Núcleo brilhante da estrela
            g2d.setColor(Color.WHITE);
            g2d.fillRect(sx, sy, 2, 2);
        }
    }

    private void drawNebula(Graphics2D g2d, int cx, int cy, int radius, Color color) {
        if (cy + radius < 0 || cy - radius > height) {
            return;
        }
        Point2D center = new Point2D.Float(cx, cy);
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            color,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2),
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)
        };
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(p);
        g2d.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    private void drawDistantPlanet(Graphics2D g2d, int px, int py) {
        if (py < -120 || py > height + 80) {
            return;
        }
        int radius = 38;

        // Atmosfera e corpo do planeta (Planeta gasoso azul-esverdeado)
        g2d.setColor(new Color(25, 80, 110));
        g2d.fillOval(px - radius, py - radius, radius * 2, radius * 2);

        // Faixas atmosféricas
        g2d.setColor(new Color(40, 120, 150));
        g2d.fillArc(px - radius, py - radius, radius * 2, radius * 2, 10, 160);

        // Sombra de iluminação espacial
        g2d.setColor(new Color(4, 8, 20, 180));
        g2d.fillOval(px - radius + 8, py - radius, radius * 2, radius * 2);

        // Anel planetário elíptico inclinado
        g2d.setColor(new Color(160, 200, 220, 110));
        g2d.drawOval(px - radius - 24, py - 10, (radius + 24) * 2, 20);
        g2d.setColor(new Color(220, 240, 255, 60));
        g2d.drawOval(px - radius - 20, py - 8, (radius + 20) * 2, 16);
    }
}
