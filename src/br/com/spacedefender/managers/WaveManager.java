package br.com.spacedefender.managers;

import br.com.spacedefender.enemies.ArmoredCruiser;
import br.com.spacedefender.enemies.EnemyShip;
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
    private long spawnIntervalMs = 2000; // Inicia gerando um inimigo a cada 2 segundos
    private int enemiesSpawnedInWave = 0;
    private int enemiesPerWave = 8;
    private final Random random = new Random();

    /**
     * Atualiza o temporizador de surgimento e instancia novos inimigos na coleção ativa.
     * @param enemies Lista genérica de naves inimigas ativas no jogo
     */
    public void update(List<EnemyShip> enemies) {
        if (enemies == null) {
            return;
        }

        long now = System.currentTimeMillis();

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
     * Sorteia as coordenadas de entrada e instancia polimorficamente uma nave inimiga.
     */
    private void spawnEnemy(List<EnemyShip> enemies) {
        // Coordenada horizontal aleatória dentro da área jogável (entre 60 e 720 px)
        int spawnX = 60 + random.nextInt(660);
        int spawnY = -50;

        // Probabilidade de surgimento: 75% ScoutShip (ágil) / 25% ArmoredCruiser (pesado)
        double roll = random.nextDouble();
        if (roll < 0.25 || (currentWave >= 3 && roll < 0.40)) {
            enemies.add(new ArmoredCruiser(spawnX, spawnY));
        } else {
            enemies.add(new ScoutShip(spawnX, spawnY));
        }
    }

    /**
     * Avança a contagem de ondas e intensifica a cadência de invasão.
     */
    private void advanceWave() {
        currentWave++;
        enemiesSpawnedInWave = 0;
        enemiesPerWave += 4; // Aumenta a quantidade de inimigos por onda

        // Reduz o intervalo de spawn para elevar o dinamismo (mínimo de 900ms)
        spawnIntervalMs = Math.max(900, spawnIntervalMs - 150);
        lastSpawnTime = System.currentTimeMillis() + 1000; // Breve pausa de 1s entre ondas
    }

    /**
     * Reinicializa os contadores para uma nova partida.
     */
    public void reset() {
        this.currentWave = 1;
        this.enemiesSpawnedInWave = 0;
        this.enemiesPerWave = 8;
        this.spawnIntervalMs = 2000;
        this.lastSpawnTime = 0;
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
}
