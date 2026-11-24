package br.com.spacedefender;

import br.com.spacedefender.core.GameWindow;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada principal do jogo Space Defender: Galaxy Core.
 * Desenvolvido por Felipe Lopes & Bianca.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Space Defender: Galaxy Core - Inicializando Janela...");
        // Executa a inicialização gráfica na thread segura de eventos do Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}
