package br.com.spacedefender.core;

import br.com.spacedefender.audio.SoundManager;
import br.com.spacedefender.audio.SoundType;
import br.com.spacedefender.enemies.EnemyShip;
import br.com.spacedefender.entities.PlayerShip;
import br.com.spacedefender.managers.CollisionManager;
import br.com.spacedefender.managers.HighScoreManager;
import br.com.spacedefender.managers.PowerUpManager;
import br.com.spacedefender.managers.WaveManager;
import br.com.spacedefender.projectiles.EnemyProjectile;
import br.com.spacedefender.scores.ScoreEntry;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
 * Orquestra o Game Loop a 60 FPS, a Máquina de Estados (Menu, Partida, Continue, Game Over Arcade,
 * High Scores e Configurações de Volume) e renderização gráfica retro-futurista.
 */
public class GamePanel extends JPanel implements ActionListener, Runnable {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int FPS = 60;
    public static final int DELAY = 1000 / FPS;

    private Timer gameTimer;
    private PlayerShip player;
    private List<EnemyShip> enemies = new ArrayList<>();
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private CollisionManager collisionManager = new CollisionManager();
    private WaveManager waveManager = new WaveManager();
    private PowerUpManager powerUpManager = new PowerUpManager();
    private SpaceBackground background;
    private HighScoreManager highScoreManager = new HighScoreManager();

    // Máquina de Estados
    private GameState gameState = GameState.MENU;

    // Estado do Menu Principal
    private int menuIndex = 0;
    private final String[] menuOptions = {"NOVO JOGO", "SCORE", "CONFIGURAÇÃO"};

    // Estado da Tela Continue
    private int continueSelection = 0; // 0 = SIM, 1 = NÃO
    private long continueStartTime = 0;
    private static final int CONTINUE_DURATION_SEC = 9;

    // Estado da Tela Game Over Arcade (Inserção de iniciais)
    private char[] initialsSlots = {'A', 'A', 'A'};
    private int slotIndex = 0;
    private double animPulse = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setDoubleBuffered(true);

        this.background = new SpaceBackground(SCREEN_WIDTH, SCREEN_HEIGHT);
        this.collisionManager.setPowerUpManager(this.powerUpManager);
        this.player = new PlayerShip(SCREEN_WIDTH, SCREEN_HEIGHT);

        // Captura de eventos do teclado em conformidade com o GameState ativo
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleGlobalKeyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (gameState == GameState.PLAYING) {
                    player.handleInput(e.getKeyCode(), false);
                }
            }
        });

        this.gameTimer = new Timer(DELAY, this);
    }

    private void handleGlobalKeyPressed(int keyCode) {
        SoundManager sound = SoundManager.getInstance();

        switch (gameState) {
            case MENU:
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                    menuIndex = (menuIndex - 1 + menuOptions.length) % menuOptions.length;
                    sound.play(SoundType.MENU_BEEP);
                } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                    menuIndex = (menuIndex + 1) % menuOptions.length;
                    sound.play(SoundType.MENU_BEEP);
                } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                    sound.play(SoundType.MENU_BEEP);
                    if (menuIndex == 0) {
                        startNewGame();
                    } else if (menuIndex == 1) {
                        gameState = GameState.HIGH_SCORES;
                    } else if (menuIndex == 2) {
                        gameState = GameState.SETTINGS;
                    }
                }
                break;

            case PLAYING:
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.MENU;
                } else {
                    player.handleInput(keyCode, true);
                }
                break;

            case CONTINUE_SCREEN:
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                    continueSelection = 0;
                    sound.play(SoundType.MENU_BEEP);
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    continueSelection = 1;
                    sound.play(SoundType.MENU_BEEP);
                } else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_Y) {
                    continueGame();
                } else if (keyCode == KeyEvent.VK_N || keyCode == KeyEvent.VK_ESCAPE) {
                    giveUpGame();
                } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                    if (continueSelection == 0) {
                        continueGame();
                    } else {
                        giveUpGame();
                    }
                }
                break;

            case GAME_OVER_ARCADE:
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                    initialsSlots[slotIndex] = (char) (initialsSlots[slotIndex] == 'Z' ? 'A' : initialsSlots[slotIndex] + 1);
                    sound.play(SoundType.MENU_BEEP);
                } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                    initialsSlots[slotIndex] = (char) (initialsSlots[slotIndex] == 'A' ? 'Z' : initialsSlots[slotIndex] - 1);
                    sound.play(SoundType.MENU_BEEP);
                } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                    if (slotIndex > 0) {
                        slotIndex--;
                        sound.play(SoundType.MENU_BEEP);
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    if (slotIndex < 2) {
                        slotIndex++;
                        sound.play(SoundType.MENU_BEEP);
                    }
                } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                    if (slotIndex < 2) {
                        slotIndex++;
                        sound.play(SoundType.MENU_BEEP);
                    } else {
                        // Confirma o registro das 3 letras do jogador
                        String name = new String(initialsSlots);
                        highScoreManager.addScore(name, player.getScore(), waveManager.getCurrentWave());
                        sound.play(SoundType.POWERUP);
                        gameState = GameState.HIGH_SCORES;
                    }
                }
                break;

            case HIGH_SCORES:
                if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_SPACE) {
                    sound.play(SoundType.MENU_BEEP);
                    gameState = GameState.MENU;
                }
                break;

            case SETTINGS:
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                    sound.decreaseVolume();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    sound.increaseVolume();
                } else if (keyCode == KeyEvent.VK_M) {
                    sound.toggleMute();
                } else if (keyCode == KeyEvent.VK_T) {
                    sound.play(SoundType.LASER);
                } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_SPACE) {
                    sound.play(SoundType.MENU_BEEP);
                    gameState = GameState.MENU;
                }
                break;

            case VICTORY:
                if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                    sound.play(SoundType.POWERUP);
                    slotIndex = 0;
                    initialsSlots = new char[]{'A', 'A', 'A'};
                    gameState = GameState.GAME_OVER_ARCADE;
                }
                break;
        }
    }

    public void startNewGame() {
        player.reset(SCREEN_WIDTH, SCREEN_HEIGHT);
        enemies.clear();
        enemyProjectiles.clear();
        powerUpManager.clear();
        waveManager.reset();
        gameState = GameState.PLAYING;
    }

    private void continueGame() {
        player.loseLife();
        player.respawn(SCREEN_WIDTH, SCREEN_HEIGHT);
        enemies.clear();
        enemyProjectiles.clear();
        gameState = GameState.PLAYING;
        SoundManager.getInstance().play(SoundType.POWERUP);
    }

    private void giveUpGame() {
        SoundManager.getInstance().play(SoundType.GAME_OVER);
        slotIndex = 0;
        initialsSlots = new char[]{'A', 'A', 'A'};
        gameState = GameState.GAME_OVER_ARCADE;
    }

    public void startGame() {
        this.requestFocusInWindow();
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    public void stopGame() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    @Override
    public void run() {
        startGame();
    }

    public void update() {
        background.update();
        animPulse += 0.08;

        if (gameState == GameState.PLAYING) {
            player.update();
            waveManager.update(enemies);

            for (EnemyShip enemy : enemies) {
                enemy.updateAI(player, enemyProjectiles);
            }

            for (EnemyProjectile ep : enemyProjectiles) {
                ep.update();
            }
            enemyProjectiles.removeIf(EnemyProjectile::isOffScreen);

            collisionManager.checkCollisions(player, enemies, enemyProjectiles);
            powerUpManager.update(player, enemies);

            // Condição de Vitória: Boss abatido e tela limpa
            if (waveManager.isBossWave() && waveManager.isBossSpawned() && enemies.isEmpty()) {
                gameState = GameState.VICTORY;
                SoundManager.getInstance().play(SoundType.POWERUP);
                return;
            }

            // Verificação de Integridade Zero
            if (player.getHealth() <= 0) {
                if (player.getLives() > 0) {
                    gameState = GameState.CONTINUE_SCREEN;
                    continueStartTime = System.currentTimeMillis();
                    continueSelection = 0;
                } else {
                    SoundManager.getInstance().play(SoundType.GAME_OVER);
                    slotIndex = 0;
                    initialsSlots = new char[]{'A', 'A', 'A'};
                    gameState = GameState.GAME_OVER_ARCADE;
                }
            }
        } else if (gameState == GameState.CONTINUE_SCREEN) {
            long elapsed = (System.currentTimeMillis() - continueStartTime) / 1000;
            if (elapsed >= CONTINUE_DURATION_SEC) {
                giveUpGame();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Sempre desenha o cenário cósmico Parallax ao fundo
        background.draw(g2d);

        switch (gameState) {
            case MENU:
                drawMenu(g2d);
                break;

            case PLAYING:
                drawGameplay(g2d);
                break;

            case CONTINUE_SCREEN:
                drawGameplay(g2d);
                drawContinueScreen(g2d);
                break;

            case GAME_OVER_ARCADE:
                drawGameOverArcade(g2d);
                break;

            case HIGH_SCORES:
                drawHighScores(g2d);
                break;

            case SETTINGS:
                drawSettings(g2d);
                break;

            case VICTORY:
                drawVictory(g2d);
                break;
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawGameplay(Graphics2D g2d) {
        for (EnemyShip enemy : enemies) {
            enemy.draw(g2d);
        }
        for (EnemyProjectile ep : enemyProjectiles) {
            ep.draw(g2d);
        }
        powerUpManager.draw(g2d);
        player.draw(g2d);
        drawHUD(g2d);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, SCREEN_WIDTH, 48);
        g2d.setColor(new Color(60, 60, 80));
        g2d.drawLine(0, 48, SCREEN_WIDTH, 48);

        // 1. Placar e Onda
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(String.format("SCORE: %05d   |   ONDA %d", player.getScore(), waveManager.getCurrentWave()), 20, 22);

        // 2. Arma Ativa
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
                g2d.setColor(new Color(255, 60, 80));
                g2d.fillOval(SCREEN_WIDTH - 225 + (i * 20), 10, 14, 14);
            } else {
                g2d.setColor(new Color(80, 80, 80));
                g2d.drawOval(SCREEN_WIDTH - 225 + (i * 20), 10, 14, 14);
            }
        }

        // 4. Barra de HP
        int hpBarWidth = 100;
        int hpBarHeight = 12;
        int hpBarX = SCREEN_WIDTH - 130;
        int hpBarY = 28;

        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + player.getHealth() + "%", SCREEN_WIDTH - 200, 39);

        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);

        double hpRatio = (double) player.getHealth() / player.getMaxHealth();
        int filledWidth = (int) (hpBarWidth * hpRatio);
        if (hpRatio > 0.5) {
            g2d.setColor(new Color(0, 220, 100));
        } else if (hpRatio > 0.25) {
            g2d.setColor(new Color(255, 180, 0));
        } else {
            g2d.setColor(new Color(240, 50, 50));
        }
        g2d.fillRect(hpBarX, hpBarY, filledWidth, hpBarHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);

        // 5. Indicadores de Buffs
        int buffX = 320;
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        if (player.isDoubleScoreActive()) {
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("[2X SCORE " + player.getDoubleScoreRemainingSeconds() + "s]", buffX, 22);
            buffX += 95;
        }
        if (player.isTripleShotActive()) {
            g2d.setColor(new Color(0, 220, 255));
            g2d.drawString("[TRIPLE " + player.getTripleShotRemainingSeconds() + "s]", buffX, 22);
            buffX += 85;
        }
        if (player.isShieldActive()) {
            g2d.setColor(new Color(120, 255, 180));
            g2d.drawString("[SHIELD " + player.getShieldRemainingSeconds() + "s]", buffX, 22);
        }
    }

    private void drawMenu(Graphics2D g2d) {
        // Título Principal com Brilho Neon Espacial
        g2d.setFont(new Font("SansSerif", Font.BOLD, 46));
        String title = "SPACE DEFENDER";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (SCREEN_WIDTH - fm.stringWidth(title)) / 2;

        // Aura Neon
        g2d.setColor(new Color(0, 180, 255, 140));
        g2d.drawString(title, titleX - 2, 162);
        g2d.drawString(title, titleX + 2, 158);
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, titleX, 160);

        // Subtítulo
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(new Color(255, 215, 0));
        String sub = "G A L A X Y   C O R E";
        FontMetrics fmSub = g2d.getFontMetrics();
        g2d.drawString(sub, (SCREEN_WIDTH - fmSub.stringWidth(sub)) / 2, 205);

        // Caixa de Opções do Menu
        int startY = 290;
        for (int i = 0; i < menuOptions.length; i++) {
            boolean selected = (i == menuIndex);
            int optY = startY + (i * 60);

            if (selected) {
                double pulse = Math.sin(animPulse * 1.5) * 0.15 + 0.85;
                g2d.setColor(new Color(0, 220, 255, (int) (60 * pulse)));
                g2d.fillRoundRect(SCREEN_WIDTH / 2 - 140, optY - 32, 280, 44, 15, 15);
                g2d.setColor(new Color(0, 240, 255));
                g2d.drawRoundRect(SCREEN_WIDTH / 2 - 140, optY - 32, 280, 44, 15, 15);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2d.setColor(Color.YELLOW);
                g2d.drawString(">  " + menuOptions[i] + "  <", SCREEN_WIDTH / 2 - 95, optY);
            } else {
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString(menuOptions[i], SCREEN_WIDTH / 2 - 60, optY);
            }
        }

        // Rodapé com dicas de navegação
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(150, 160, 180));
        String tip = "[↑ / ↓] Navegar   |   [ENTER] Selecionar";
        FontMetrics fmTip = g2d.getFontMetrics();
        g2d.drawString(tip, (SCREEN_WIDTH - fmTip.stringWidth(tip)) / 2, 530);
    }

    private void drawContinueScreen(Graphics2D g2d) {
        // Escurecimento translúcido sobre a partida congelada
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2d.setColor(new Color(255, 60, 60));
        String warning = "ALERTA: CASCO DESTRUÍDO!";
        FontMetrics fmW = g2d.getFontMetrics();
        g2d.drawString(warning, (SCREEN_WIDTH - fmW.stringWidth(warning)) / 2, 170);

        // Blinking CONTINUE?
        g2d.setFont(new Font("SansSerif", Font.BOLD, 52));
        double pulse = Math.sin(animPulse * 2.5) * 0.5 + 0.5;
        g2d.setColor(new Color(255, 220, 0, (int) (155 + 100 * pulse)));
        String cont = "CONTINUE?";
        FontMetrics fmC = g2d.getFontMetrics();
        g2d.drawString(cont, (SCREEN_WIDTH - fmC.stringWidth(cont)) / 2, 245);

        // Vidas restantes e Temporizador
        long remaining = Math.max(0, CONTINUE_DURATION_SEC - ((System.currentTimeMillis() - continueStartTime) / 1000));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2d.setColor(Color.WHITE);
        String timerStr = "0" + remaining;
        FontMetrics fmT = g2d.getFontMetrics();
        g2d.drawString(timerStr, (SCREEN_WIDTH - fmT.stringWidth(timerStr)) / 2, 305);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2d.setColor(new Color(180, 200, 240));
        String livesInfo = String.format("Vidas restantes após continuar: %d", player.getLives() - 1);
        FontMetrics fmL = g2d.getFontMetrics();
        g2d.drawString(livesInfo, (SCREEN_WIDTH - fmL.stringWidth(livesInfo)) / 2, 345);

        // Botões SIM / NÃO
        int btnY = 410;
        int btnWidth = 120;
        int btnHeight = 44;
        int btnSimX = SCREEN_WIDTH / 2 - 140;
        int btnNaoX = SCREEN_WIDTH / 2 + 20;

        // Botão SIM
        if (continueSelection == 0) {
            g2d.setColor(new Color(0, 255, 120, 100));
            g2d.fillRoundRect(btnSimX, btnY, btnWidth, btnHeight, 10, 10);
            g2d.setColor(Color.GREEN);
            g2d.drawRoundRect(btnSimX, btnY, btnWidth, btnHeight, 10, 10);
        } else {
            g2d.setColor(new Color(40, 40, 40));
            g2d.fillRoundRect(btnSimX, btnY, btnWidth, btnHeight, 10, 10);
        }
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(continueSelection == 0 ? Color.WHITE : Color.GRAY);
        g2d.drawString("SIM [S]", btnSimX + 25, btnY + 29);

        // Botão NÃO
        if (continueSelection == 1) {
            g2d.setColor(new Color(255, 50, 50, 100));
            g2d.fillRoundRect(btnNaoX, btnY, btnWidth, btnHeight, 10, 10);
            g2d.setColor(Color.RED);
            g2d.drawRoundRect(btnNaoX, btnY, btnWidth, btnHeight, 10, 10);
        } else {
            g2d.setColor(new Color(40, 40, 40));
            g2d.fillRoundRect(btnNaoX, btnY, btnWidth, btnHeight, 10, 10);
        }
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(continueSelection == 1 ? Color.WHITE : Color.GRAY);
        g2d.drawString("NÃO [N]", btnNaoX + 20, btnY + 29);

        // Instruções
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(Color.LIGHT_GRAY);
        String tip = "[← / →] Escolher   |   [ENTER] Confirmar";
        FontMetrics fmTip = g2d.getFontMetrics();
        g2d.drawString(tip, (SCREEN_WIDTH - fmTip.stringWidth(tip)) / 2, 500);
    }

    private void drawGameOverArcade(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 230));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Header GAME OVER
        g2d.setFont(new Font("SansSerif", Font.BOLD, 54));
        g2d.setColor(new Color(255, 30, 30));
        String go = "GAME OVER";
        FontMetrics fmGO = g2d.getFontMetrics();
        g2d.drawString(go, (SCREEN_WIDTH - fmGO.stringWidth(go)) / 2, 130);

        // Placar Final
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(new Color(255, 215, 0));
        String scoreStr = String.format("PONTUAÇÃO FINAL: %05d   |   ONDA: %d", player.getScore(), waveManager.getCurrentWave());
        FontMetrics fmS = g2d.getFontMetrics();
        g2d.drawString(scoreStr, (SCREEN_WIDTH - fmS.stringWidth(scoreStr)) / 2, 185);

        // Inserção de Iniciais Fliperama (3 Letras)
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        String prompt = "REGISTRE SUAS INICIAIS NO RANKING HISTÓRICO:";
        FontMetrics fmP = g2d.getFontMetrics();
        g2d.drawString(prompt, (SCREEN_WIDTH - fmP.stringWidth(prompt)) / 2, 260);

        // 3 Slots de Letras
        int slotStartX = SCREEN_WIDTH / 2 - 95;
        int slotY = 320;
        int slotSize = 55;
        int slotGap = 20;

        for (int i = 0; i < 3; i++) {
            int curX = slotStartX + (i * (slotSize + slotGap));
            boolean active = (i == slotIndex);

            if (active) {
                double pulse = Math.sin(animPulse * 3.0) * 0.2 + 0.8;
                g2d.setColor(new Color(0, 220, 255, (int) (120 * pulse)));
                g2d.fillRoundRect(curX, slotY, slotSize, slotSize, 12, 12);
                g2d.setColor(Color.CYAN);
                g2d.drawRoundRect(curX, slotY, slotSize, slotSize, 12, 12);

                // Setas indicativas ▲ / ▼
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2d.drawString("▲", curX + 21, slotY - 8);
                g2d.drawString("▼", curX + 21, slotY + slotSize + 18);
            } else {
                g2d.setColor(new Color(30, 30, 45));
                g2d.fillRoundRect(curX, slotY, slotSize, slotSize, 12, 12);
                g2d.setColor(new Color(70, 70, 100));
                g2d.drawRoundRect(curX, slotY, slotSize, slotSize, 12, 12);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
            g2d.setColor(active ? Color.WHITE : Color.LIGHT_GRAY);
            g2d.drawString(String.valueOf(initialsSlots[i]), curX + 16, slotY + 40);
        }

        // Rodapé explicativo
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2d.setColor(new Color(180, 190, 210));
        String controls = "[↑ / ↓] Alterar Letra   |   [ENTER / →] Confirmar / Próxima";
        FontMetrics fmCtrl = g2d.getFontMetrics();
        g2d.drawString(controls, (SCREEN_WIDTH - fmCtrl.stringWidth(controls)) / 2, 470);
    }

    private void drawHighScores(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 225));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 34));
        g2d.setColor(new Color(255, 215, 0));
        String title = "★ HALL DA FAMA - TOP 10 ESPACIAL ★";
        FontMetrics fmT = g2d.getFontMetrics();
        g2d.drawString(title, (SCREEN_WIDTH - fmT.stringWidth(title)) / 2, 70);

        // Cabeçalho da Tabela
        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2d.setColor(new Color(0, 220, 255));
        g2d.drawString("POS", 180, 120);
        g2d.drawString("PILOTO", 280, 120);
        g2d.drawString("PONTUAÇÃO", 410, 120);
        g2d.drawString("ONDA", 570, 120);

        g2d.setColor(new Color(50, 60, 90));
        g2d.drawLine(160, 130, 640, 130);

        List<ScoreEntry> list = highScoreManager.getScores();
        int rowY = 160;

        for (int i = 0; i < list.size() && i < 10; i++) {
            ScoreEntry entry = list.get(i);

            // Destaque visual para os 3 primeiros colocados
            if (i == 0) {
                g2d.setColor(new Color(255, 215, 0)); // Ouro
            } else if (i == 1) {
                g2d.setColor(new Color(220, 220, 230)); // Prata
            } else if (i == 2) {
                g2d.setColor(new Color(205, 127, 50)); // Bronze
            } else {
                g2d.setColor(Color.LIGHT_GRAY);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2d.drawString(String.format("%2d.", i + 1), 185, rowY);
            g2d.drawString(entry.getInitials(), 290, rowY);
            g2d.drawString(String.format("%05d", entry.getScore()), 430, rowY);
            g2d.drawString(String.valueOf(entry.getWave()), 585, rowY);

            rowY += 32;
        }

        // Rodapé de retorno
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(Color.WHITE);
        String tip = "Pressione [ENTER] ou [ESC] para retornar ao Menu Principal";
        FontMetrics fmTip = g2d.getFontMetrics();
        g2d.drawString(tip, (SCREEN_WIDTH - fmTip.stringWidth(tip)) / 2, 530);
    }

    private void drawSettings(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 225));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2d.setColor(new Color(0, 220, 255));
        String title = "CONFIGURAÇÕES DE ÁUDIO";
        FontMetrics fmT = g2d.getFontMetrics();
        g2d.drawString(title, (SCREEN_WIDTH - fmT.stringWidth(title)) / 2, 100);

        SoundManager sound = SoundManager.getInstance();
        float vol = sound.getVolume();
        boolean muted = sound.isMuted();

        // Barra de Volume Estilizada
        int barX = SCREEN_WIDTH / 2 - 150;
        int barY = 200;
        int barWidth = 300;
        int barHeight = 24;

        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("VOLUME PRINCIPAL: %d%%", (int) (vol * 100)), barX, barY - 12);

        g2d.setColor(new Color(30, 30, 40));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        int filled = (int) (barWidth * vol);
        g2d.setColor(muted ? Color.GRAY : new Color(0, 220, 255));
        g2d.fillRect(barX, barY, filled, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);

        // Status Mudo
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        if (muted) {
            g2d.setColor(new Color(255, 80, 80));
            g2d.drawString("ESTADO: [ÁUDIO MUTADO]", barX, barY + 60);
        } else {
            g2d.setColor(new Color(100, 255, 120));
            g2d.drawString("ESTADO: [ÁUDIO ATIVO]", barX, barY + 60);
        }

        // Comandos de Interação
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2d.setColor(Color.LIGHT_GRAY);
        int infoY = 330;
        g2d.drawString("[← / →]  Ajustar Volume (0% a 100%)", barX, infoY);
        g2d.drawString("[M]       Alternar Mudo (Mute)", barX, infoY + 35);
        g2d.drawString("[T]       Testar Disparo Sonoro", barX, infoY + 70);
        g2d.drawString("[ENTER / ESC]  Retornar ao Menu Principal", barX, infoY + 105);
    }

    private void drawVictory(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 46));
        g2d.setColor(new Color(255, 215, 0));
        String title = "★ VITÓRIA GALÁCTICA! ★";
        FontMetrics fmT = g2d.getFontMetrics();
        g2d.drawString(title, (SCREEN_WIDTH - fmT.stringWidth(title)) / 2, 170);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2d.setColor(new Color(0, 255, 150));
        String sub = "O NÚCLEO DA GALÁXIA FOI SALVO!";
        FontMetrics fmS = g2d.getFontMetrics();
        g2d.drawString(sub, (SCREEN_WIDTH - fmS.stringWidth(sub)) / 2, 230);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.setColor(Color.WHITE);
        String score = String.format("PONTUAÇÃO HEROICA: %05d", player.getScore());
        FontMetrics fmSc = g2d.getFontMetrics();
        g2d.drawString(score, (SCREEN_WIDTH - fmSc.stringWidth(score)) / 2, 300);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.setColor(Color.YELLOW);
        String next = "Pressione [ENTER] para registrar seu nome no Hall da Fama!";
        FontMetrics fmN = g2d.getFontMetrics();
        g2d.drawString(next, (SCREEN_WIDTH - fmN.stringWidth(next)) / 2, 420);
    }

    public PlayerShip getPlayer() {
        return player;
    }

    public PowerUpManager getPowerUpManager() {
        return powerUpManager;
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

    public HighScoreManager getHighScoreManager() {
        return highScoreManager;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Timer getGameTimer() {
        return gameTimer;
    }
}
