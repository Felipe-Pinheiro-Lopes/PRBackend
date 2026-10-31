package br.com.spacedefender.core;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Janela principal (JFrame) do jogo Space Defender: Galaxy Core.
 * Responsável pelo container da janela, dimensões, título e encerramento.
 */
public class GameWindow extends JFrame {

    public GameWindow() {
        super("Space Defender: Galaxy Core");

        GamePanel panel = new GamePanel();
        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // Inicia o game loop após a janela estar visível
        SwingUtilities.invokeLater(panel::startGame);
    }
}
