package br.com.spacedefender.core;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Painel de renderização e tela jogável do Space Defender.
 * Responsável pela resolução (800x600) e futuramente pelo Game Loop e desenho.
 */
public class GamePanel extends JPanel {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setDoubleBuffered(true); // Garante renderização suave sem flicker
    }
}
