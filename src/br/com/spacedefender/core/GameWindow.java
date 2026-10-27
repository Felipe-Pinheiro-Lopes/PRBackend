package br.com.spacedefender.core;

import javax.swing.JFrame;

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
        this.pack(); // Ajusta o tamanho da moldura da janela ao tamanho do painel interno (800x600)
        this.setLocationRelativeTo(null); // Centraliza a janela na tela
        this.setVisible(true);
    }
}
