package br.com.spacedefender.managers;

import br.com.spacedefender.enemies.EnemyShip;
import br.com.spacedefender.entities.PlayerShip;
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

    /**
     * Processa todas as verificações de colisão do quadro atual do jogo.
     * @param player Nave controlada pelo jogador
     * @param enemies Lista ativa de naves inimigas
     */
    public void checkCollisions(PlayerShip player, List<EnemyShip> enemies) {
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
                    // Causa o dano polimórfico específico do projétil no inimigo
                    enemy.takeDamage(proj.getDamage());
                    projectilesToRemove.add(proj);

                    // Se a blindagem do inimigo zerar, pontua e agenda remoção
                    if (enemy.isDestroyed()) {
                        player.addScore(enemy.getPointValue());
                        enemiesToRemove.add(enemy);
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
                // A nave inimiga colidida é destruída no choque
                enemy.takeDamage(9999);
                enemiesToRemove.add(enemy);
            }
        }

        // 3. Remoção segura dos projéteis consumidos
        projectiles.removeAll(projectilesToRemove);

        // 4. Remoção segura dos inimigos destruídos
        enemies.removeAll(enemiesToRemove);

        // 5. Descarte de inimigos que ultrapassaram a borda inferior da tela
        enemies.removeIf(EnemyShip::isOffScreen);
    }
}
