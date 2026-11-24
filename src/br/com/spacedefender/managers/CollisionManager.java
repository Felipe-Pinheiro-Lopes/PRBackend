package br.com.spacedefender.managers;

import br.com.spacedefender.enemies.EnemyShip;
import br.com.spacedefender.entities.PlayerShip;
import br.com.spacedefender.projectiles.EnemyProjectile;
import br.com.spacedefender.projectiles.Projectile;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de detecção e resolução de colisões espaciais (AABB - Axis-Aligned Bounding Box).
 * Responsável por gerenciar interações físicas entre Projéteis, Inimigos e a Nave do Jogador,
 * atualizando placar e integridade dos envolvidos.
 */
public class CollisionManager {
    private PowerUpManager powerUpManager;

    public CollisionManager() {
    }

    public CollisionManager(PowerUpManager powerUpManager) {
        this.powerUpManager = powerUpManager;
    }

    public void setPowerUpManager(PowerUpManager powerUpManager) {
        this.powerUpManager = powerUpManager;
    }

    /**
     * Sobrecarga de compatibilidade para 2 parâmetros.
     */
    public void checkCollisions(PlayerShip player, List<EnemyShip> enemies) {
        checkCollisions(player, enemies, null);
    }

    /**
     * Processa todas as verificações de colisão do quadro atual do jogo.
     * @param player Nave controlada pelo jogador
     * @param enemies Lista ativa de naves inimigas
     * @param enemyProjectiles Lista de tiros disparados por inimigos
     */
    public void checkCollisions(PlayerShip player, List<EnemyShip> enemies, List<EnemyProjectile> enemyProjectiles) {
        if (player == null || enemies == null) {
            return;
        }

        List<Projectile> projectiles = player.getProjectiles();
        List<Projectile> projectilesToRemove = new ArrayList<>();
        List<EnemyShip> enemiesToRemove = new ArrayList<>();

        // 1. Colisão: Projéteis do Jogador vs Naves Inimigas
        for (Projectile proj : projectiles) {
            Rectangle projBounds = proj.getBounds();

            for (EnemyShip enemy : enemies) {
                if (enemiesToRemove.contains(enemy)) {
                    continue;
                }

                if (projBounds.intersects(enemy.getBounds())) {
                    // Causa o dano polimórfico específico do projétil no inimigo (ou absorvido pelo escudo)
                    enemy.takeDamage(proj.getDamage());
                    projectilesToRemove.add(proj);

                    // Se a blindagem do inimigo zerar, pontua e agenda remoção
                    if (enemy.isDestroyed()) {
                        player.addScore(enemy.getPointValue());
                        enemiesToRemove.add(enemy);
                        if (powerUpManager != null) {
                            powerUpManager.onEnemyDestroyed(enemy);
                        }
                    }
                    break; // Um projétil consome seu impacto em um único alvo
                }
            }
        }

        // 2. Colisão: Naves Inimigas vs Nave do Jogador
        Rectangle playerBounds = player.getBounds();
        for (EnemyShip enemy : enemies) {
            if (enemiesToRemove.contains(enemy)) {
                continue;
            }

            if (playerBounds.intersects(enemy.getBounds())) {
                // Jogador sofre dano severo de colisão direta (25 pts de escudo/vida)
                player.takeDamage(25);
                // A nave inimiga colidida é destruída no choque (a não ser que seja o Boss)
                enemy.takeDamage(50);
                if (enemy.isDestroyed()) {
                    enemiesToRemove.add(enemy);
                    if (powerUpManager != null) {
                        powerUpManager.onEnemyDestroyed(enemy);
                    }
                }
            }
        }

        // 3. Colisão: Projéteis Inimigos vs Nave do Jogador
        if (enemyProjectiles != null) {
            List<EnemyProjectile> enemyProjToRemove = new ArrayList<>();
            for (EnemyProjectile ep : enemyProjectiles) {
                if (playerBounds.intersects(ep.getBounds())) {
                    player.takeDamage(ep.getDamage());
                    enemyProjToRemove.add(ep);
                }
            }
            enemyProjectiles.removeAll(enemyProjToRemove);
        }

        // 4. Remoção segura dos projéteis consumidos
        projectiles.removeAll(projectilesToRemove);

        // 5. Remoção segura dos inimigos destruídos
        enemies.removeAll(enemiesToRemove);

        // 6. Descarte de inimigos que ultrapassaram a borda inferior da tela
        enemies.removeIf(EnemyShip::isOffScreen);
    }
}
