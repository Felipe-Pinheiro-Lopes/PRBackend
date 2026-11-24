package br.com.spacedefender.managers;

import br.com.spacedefender.enemies.ArmoredCruiser;
import br.com.spacedefender.enemies.BossShip;
import br.com.spacedefender.enemies.EnemyShip;
import br.com.spacedefender.enemies.KamikazeDrone;
import br.com.spacedefender.enemies.ScoutShip;
import java.util.List;
import java.util.Random;

/**
 * Gerenciador responsável pelo surgimento (spawning) temporal de ondas de invasores inimigos.
 * Controla o equilíbrio de dificuldade, alternância polimórfica de naves geradas e progressão de ondas.
 */
public class WaveManager {
    private int currentWave = 1;
    private long lastSpawnTime = 0;
    private long spawnIntervalMs = 1100; // Cadência acelerada de surgimento (1.1s)
    private int enemiesSpawnedInWave = 0;
    private int enemiesPerWave = 14;     // Quantidade expandida de invasores por onda
    private final Random random = new Random();
    private static final int BOSS_WAVE = 5;
    private boolean bossSpawned = false;

    /**
     * Atualiza o temporizador de surgimento e instancia novos inimigos na coleção ativa.
     * @param enemies Lista genérica de naves inimigas ativas no jogo
     */
    public void update(List<EnemyShip> enemies) {
        if (enemies == null) {
            return;
        }

        long now = System.currentTimeMillis();

        if (currentWave == BOSS_WAVE && !bossSpawned) {
            enemies.add(new BossShip(320, -50));
            bossSpawned = true;
            enemiesSpawnedInWave = enemiesPerWave;
            return;
        }

        // Verifica se a onda atual ainda tem inimigos pendentes para spawnar
        if (enemiesSpawnedInWave < enemiesPerWave) {
            if (now - lastSpawnTime >= spawnIntervalMs) {
                spawnEnemy(enemies);
                lastSpawnTime = now;
                enemiesSpawnedInWave++;
            }
        } else {
            // Se todos os inimigos da onda foram gerados e eliminados, avança para a próxima onda
            if (enemies.isEmpty()) {
                advanceWave();
            }
        }
    }

    /**
     * Sorteia as coordenadas de entrada e instancia polimorficamente uma nave inimiga,
     * alternando entre Scouts, Cruzadores Blindados e Drones Kamikazes teleguiados.
     */
    private void spawnEnemy(List<EnemyShip> enemies) {
        int spawnX = 60 + random.nextInt(660);
        int spawnY = -50;

        double roll = random.nextDouble();
        if (roll < 0.25 || (currentWave >= 3 && roll < 0.35)) {
            enemies.add(new ArmoredCruiser(spawnX, spawnY));
        } else if (roll < 0.50 || (currentWave >= 2 && roll < 0.65)) {
            enemies.add(new KamikazeDrone(spawnX, spawnY));
        } else {
            enemies.add(new ScoutShip(spawnX, spawnY));

            // 35% de chance de spawnar uma segunda nave de apoio em formação próxima
            if (random.nextDouble() < 0.35 && enemiesSpawnedInWave + 1 < enemiesPerWave) {
                int companionX = (spawnX > 400) ? spawnX - 70 : spawnX + 70;
                enemies.add(new ScoutShip(companionX, spawnY - 30));
                enemiesSpawnedInWave++;
            }
        }
    }

    /**
     * Avança a contagem de ondas e intensifica a cadência de invasão.
     */
    private void advanceWave() {
        currentWave++;
        enemiesSpawnedInWave = 0;
        enemiesPerWave += 6; // Adiciona 6 inimigos a mais a cada onda

        // Reduz o intervalo de spawn progressivamente (mínimo de 650ms)
        spawnIntervalMs = Math.max(650, spawnIntervalMs - 100);
        lastSpawnTime = System.currentTimeMillis() + 1000;
    }

    /**
     * Reinicializa os contadores para uma nova partida.
     */
    public void reset() {
        this.currentWave = 1;
        this.enemiesSpawnedInWave = 0;
        this.enemiesPerWave = 14;
        this.spawnIntervalMs = 1100;
        this.lastSpawnTime = 0;
        this.bossSpawned = false;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = Math.max(1, currentWave);
    }

    public int getEnemiesSpawnedInWave() {
        return enemiesSpawnedInWave;
    }

    public int getEnemiesPerWave() {
        return enemiesPerWave;
    }

    public long getSpawnIntervalMs() {
        return spawnIntervalMs;
    }

    public boolean isBossWave() {
        return currentWave == BOSS_WAVE;
    }

    public boolean isBossSpawned() {
        return bossSpawned;
    }
}
