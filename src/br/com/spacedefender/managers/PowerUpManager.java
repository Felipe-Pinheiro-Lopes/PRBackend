package br.com.spacedefender.managers;

import br.com.spacedefender.enemies.BossShip;
import br.com.spacedefender.enemies.EnemyShip;
import br.com.spacedefender.entities.PlayerShip;
import br.com.spacedefender.powerups.PowerUp;
import br.com.spacedefender.powerups.PowerUpType;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gerenciador responsável pelo ciclo de vida, spawn e coleta dos 5 Power-Ups do jogo.
 * Aplica os dois modos de obtenção solicitados:
 * 1. Morte de Inimigos: Pílula de Pontos 2X (DOUBLE_SCORE) e Reparo de Vida (HEALTH_REPAIR).
 * 2. Queda Espacial (Topo da tela): Tiro Triplo 'A' (TRIPLE_SHOT), Escudo Protetor (SHIELD_BARRIER)
 *    e Bomba Nova (NOVA_BOMB).
 */
public class PowerUpManager {
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final Random random = new Random();
    private long lastSpaceDropTime;
    private static final long SPACE_DROP_INTERVAL_MS = 20000; // A cada 20 segundos

    public PowerUpManager() {
        this.lastSpaceDropTime = System.currentTimeMillis();
    }

    /**
     * Invocado quando uma nave inimiga é destruída.
     * Gera drops de itens de sobrevivência e pontuação com chance balanceada.
     */
    public void onEnemyDestroyed(EnemyShip enemy) {
        if (enemy == null) {
            return;
        }

        boolean isBoss = enemy instanceof BossShip;
        // Boss dropa com 100% de certeza; inimigos comuns com 30% de chance
        if (isBoss || random.nextDouble() < 0.30) {
            PowerUpType type = random.nextBoolean() ? PowerUpType.DOUBLE_SCORE : PowerUpType.HEALTH_REPAIR;
            int spawnX = enemy.getX() + enemy.getWidth() / 2 - 13;
            int spawnY = enemy.getY() + enemy.getHeight() / 2 - 13;
            powerUps.add(new PowerUp(spawnX, spawnY, type));
        }
    }

    /**
     * Realiza o drop espacial vindo do topo da tela (Armas, Escudos e Bombas).
     */
    public void spawnSpacePowerUp() {
        // Seleciona aleatoriamente entre Tiro Triplo, Escudo e Bomba Nova
        PowerUpType[] spaceTypes = {
            PowerUpType.TRIPLE_SHOT,
            PowerUpType.SHIELD_BARRIER,
            PowerUpType.NOVA_BOMB
        };
        PowerUpType selected = spaceTypes[random.nextInt(spaceTypes.length)];
        int spawnX = 50 + random.nextInt(700);
        int spawnY = -30;
        powerUps.add(new PowerUp(spawnX, spawnY, selected));
    }

    /**
     * Atualiza a física de descida dos power-ups, verifica colisões com o jogador e
     * aplica os efeitos polimórficos de cada buff.
     */
    public void update(PlayerShip player, List<EnemyShip> enemies) {
        long now = System.currentTimeMillis();
        if (now - lastSpaceDropTime >= SPACE_DROP_INTERVAL_MS) {
            lastSpaceDropTime = now;
            spawnSpacePowerUp();
        }

        List<PowerUp> collectedOrExpired = new ArrayList<>();
        Rectangle playerBounds = (player != null) ? player.getBounds() : null;

        for (PowerUp p : powerUps) {
            p.update();

            // Verifica colisão (coleta) com a nave do jogador
            if (playerBounds != null && playerBounds.intersects(p.getBounds())) {
                applyPowerUpEffect(p.getType(), player, enemies);
                collectedOrExpired.add(p);
            } else if (p.isOffScreen()) {
                collectedOrExpired.add(p);
            }
        }

        powerUps.removeAll(collectedOrExpired);
    }

    /**
     * Executa a regra de negócio específica do Power-Up coletado.
     */
    private void applyPowerUpEffect(PowerUpType type, PlayerShip player, List<EnemyShip> enemies) {
        if (player == null || type == null) {
            return;
        }

        switch (type) {
            case DOUBLE_SCORE:
                player.activateDoubleScore(type.getDurationMs());
                break;

            case HEALTH_REPAIR:
                player.heal(50);
                break;

            case TRIPLE_SHOT:
                player.activateTripleShot(type.getDurationMs());
                break;

            case SHIELD_BARRIER:
                player.activateShield(type.getDurationMs());
                break;

            case NOVA_BOMB:
                // Detonação em massa: destrói inimigos comuns e causa alto dano ao Boss
                if (enemies != null) {
                    for (EnemyShip enemy : enemies) {
                        if (enemy instanceof BossShip) {
                            enemy.takeDamage(350);
                        } else {
                            enemy.takeDamage(999);
                        }
                    }
                }
                player.addScore(300);
                break;
        }
    }

    /**
     * Renderiza todos os power-ups ativos no espaço.
     */
    public void draw(Graphics2D g2d) {
        for (PowerUp p : powerUps) {
            p.draw(g2d);
        }
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public void clear() {
        powerUps.clear();
        lastSpaceDropTime = System.currentTimeMillis();
    }
}
