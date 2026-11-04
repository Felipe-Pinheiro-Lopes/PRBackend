package br.com.spacedefender.core;

import br.com.spacedefender.entities.PlayerShip;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Painel principal do jogo Space Defender: Galaxy Core.
 * Responsável pelo Game Loop a 60 FPS (via javax.swing.Timer), captura de eventos de teclado (KeyListener)
 * e ciclo de renderização gráfica com Graphics2D.
 */
public class GamePanel extends JPanel implements ActionListener, Runnable {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int FPS = 60;
    public static final int DELAY = 1000 / FPS; // ~16ms para gerar 60 quadros por segundo

    private Timer gameTimer;
    private PlayerShip player;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setDoubleBuffered(true);

        // Instancia a nave do jogador centralizada na base da tela
        this.player = new PlayerShip(SCREEN_WIDTH, SCREEN_HEIGHT);

        // KeyListener para capturar as teclas (Setas ou W, A, S, D e Barra de Espaço)
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.handleInput(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleInput(e.getKeyCode(), false);
            }
        });

        // Configura o loop do jogo usando javax.swing.Timer (disparando a cada 16ms)
        this.gameTimer = new Timer(DELAY, this);
    }

    /**
     * Inicia o loop de jogo a 60 FPS e garante foco no painel para recebimento de teclado.
     */
    public void startGame() {
        this.requestFocusInWindow();
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    /**
     * Pausa ou encerra o loop de jogo.
     */
    public void stopGame() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    /**
     * Implementação de Runnable para compatibilidade com inicialização em Thread.
     */
    @Override
    public void run() {
        startGame();
    }

    /**
     * Atualiza a lógica do jogo (física, posições da nave e projéteis).
     */
    public void update() {
        player.update();
    }

    /**
     * Disparado a cada 16ms pelo javax.swing.Timer (60 vezes por segundo).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    /**
     * Desenha os elementos do jogo a cada quadro usando Graphics2D.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Renderização suave com Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Desenha a nave do jogador
        player.draw(g2d);

        // Sincroniza a fila gráfica do sistema operacional (garante 60 FPS suaves no Linux/X11 e Windows)
        Toolkit.getDefaultToolkit().sync();
    }

    public PlayerShip getPlayer() {
        return player;
    }

    public Timer getGameTimer() {
        return gameTimer;
    }
}
