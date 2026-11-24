package br.com.spacedefender.core;

import br.com.spacedefender.enemies.EnemyShip;
import br.com.spacedefender.entities.PlayerShip;
import br.com.spacedefender.managers.CollisionManager;
import br.com.spacedefender.managers.WaveManager;
import br.com.spacedefender.projectiles.EnemyProjectile;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel principal do jogo Space Defender: Galaxy Core.
 * Responsável pelo Game Loop a 60 FPS (via javax.swing.Timer), captura de eventos de teclado (KeyListener),
 * ciclo de renderização gráfica com Graphics2D e orquestração do motor de colisões.
 */
public class GamePanel extends JPanel implements ActionListener, Runnable {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int FPS = 60;
    public static final int DELAY = 1000 / FPS; // ~16ms para gerar 60 quadros por segundo

    private Timer gameTimer;
    private PlayerShip player;
    private List<EnemyShip> enemies = new ArrayList<>();
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private CollisionManager collisionManager = new CollisionManager();
    private WaveManager waveManager = new WaveManager();
    private SpaceBackground background;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setDoubleBuffered(true);

        this.background = new SpaceBackground(SCREEN_WIDTH, SCREEN_HEIGHT);

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
     * Atualiza a lógica do jogo (física da nave, IA inimiga, projéteis mútuos e resolução de colisões).
     */
    public void update() {
        // Atualiza a movimentação do fundo espacial Parallax
        background.update();

        player.update();

        // Gera novas ondas de invasores inimigos
        waveManager.update(enemies);

        // Atualização polimórfica e IA das naves inimigas
        for (EnemyShip enemy : enemies) {
            enemy.updateAI(player, enemyProjectiles);
        }

        // Atualização da física dos projéteis disparados pelos inimigos
        for (EnemyProjectile ep : enemyProjectiles) {
            ep.update();
        }
        enemyProjectiles.removeIf(EnemyProjectile::isOffScreen);

        // Resolução de colisões AABB (Tiros vs Inimigos, Inimigos vs Jogador e Tiros Inimigos vs Jogador)
        collisionManager.checkCollisions(player, enemies, enemyProjectiles);
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

        // 1. Renderiza o cenário cósmico Parallax ao fundo
        background.draw(g2d);

        // 2. Desenha todos os inimigos ativos na tela
        for (EnemyShip enemy : enemies) {
            enemy.draw(g2d);
        }

        // 3. Desenha projéteis disparados pelos inimigos
        for (EnemyProjectile ep : enemyProjectiles) {
            ep.draw(g2d);
        }

        // 4. Desenha a nave do jogador
        player.draw(g2d);

        // --- HUD SUPERIOR COMPLETO ---
        drawHUD(g2d);

        // Sincroniza a fila gráfica do sistema operacional (garante 60 FPS suaves no Linux/X11 e Windows)
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Renderiza as informações vitais do jogo: Placar, Vidas, Barra de Vida (HP) e Arma Ativa.
     */
    private void drawHUD(Graphics2D g2d) {
        // Faixa de fundo semitransparente para o HUD
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, SCREEN_WIDTH, 48);
        g2d.setColor(new Color(60, 60, 80));
        g2d.drawLine(0, 48, SCREEN_WIDTH, 48);

        // 1. Placar (Score) e Onda Atual
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(new Color(255, 215, 0)); // Dourado
        g2d.drawString(String.format("SCORE: %05d   |   ONDA %d", player.getScore(), waveManager.getCurrentWave()), 20, 22);

        // 2. Indicador da Arma Ativa (Polimorfismo)
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Arma [1/2/Q]: ", 20, 40);
        if (player.getActiveWeapon() == br.com.spacedefender.projectiles.WeaponType.LASER) {
            g2d.setColor(new Color(0, 220, 255));
            g2d.drawString("LASER PULSE (10 dmg)", 110, 40);
        } else {
            g2d.setColor(new Color(255, 230, 0));
            g2d.drawString("PLASMA BALL (25 dmg)", 110, 40);
        }

        // 3. Vidas do Jogador
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(Color.WHITE);
        g2d.drawString("VIDAS: ", SCREEN_WIDTH - 280, 22);
        for (int i = 0; i < 3; i++) {
            if (i < player.getLives()) {
                g2d.setColor(new Color(255, 60, 80)); // Coração ativo
                g2d.fillOval(SCREEN_WIDTH - 225 + (i * 20), 10, 14, 14);
            } else {
                g2d.setColor(new Color(80, 80, 80)); // Vida perdida
                g2d.drawOval(SCREEN_WIDTH - 225 + (i * 20), 10, 14, 14);
            }
        }

        // 4. Barra de Integridade / Escudo (HP)
        int hpBarWidth = 100;
        int hpBarHeight = 12;
        int hpBarX = SCREEN_WIDTH - 130;
        int hpBarY = 28;

        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + player.getHealth() + "%", SCREEN_WIDTH - 200, 39);

        // Fundo da barra
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);

        // Preenchimento proporcional
        double hpRatio = (double) player.getHealth() / player.getMaxHealth();
        int filledWidth = (int) (hpBarWidth * hpRatio);
        if (hpRatio > 0.5) {
            g2d.setColor(new Color(0, 220, 100)); // Verde saudável
        } else if (hpRatio > 0.25) {
            g2d.setColor(new Color(255, 180, 0)); // Amarelo alerta
        } else {
            g2d.setColor(new Color(240, 50, 50)); // Vermelho crítico
        }
        g2d.fillRect(hpBarX, hpBarY, filledWidth, hpBarHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);
    }

    public PlayerShip getPlayer() {
        return player;
    }

    public List<EnemyShip> getEnemies() {
        return enemies;
    }

    public List<EnemyProjectile> getEnemyProjectiles() {
        return enemyProjectiles;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public SpaceBackground getSpaceBackground() {
        return background;
    }

    public Timer getGameTimer() {
        return gameTimer;
    }
}
