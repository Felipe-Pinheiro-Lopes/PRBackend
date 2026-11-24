package br.com.spacedefender.scores;

import java.io.Serializable;

/**
 * Registro de pontuação histórica estilo Arcade.
 * Encapsula as iniciais do jogador (máx 3 letras), pontuação total e onda alcançada.
 */
public class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
    private static final long serialVersionUID = 1L;

    private final String initials;
    private final int score;
    private final int wave;
    private final long timestamp;

    public ScoreEntry(String initials, int score, int wave) {
        this.initials = (initials == null || initials.trim().isEmpty()) ? "AAA" : initials.trim().toUpperCase();
        this.score = score;
        this.wave = wave;
        this.timestamp = System.currentTimeMillis();
    }

    public String getInitials() {
        return initials;
    }

    public int getScore() {
        return score;
    }

    public int getWave() {
        return wave;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        // Ordenação decrescente por pontuação; desempate por onda e timestamp mais recente
        if (this.score != other.score) {
            return Integer.compare(other.score, this.score);
        }
        if (this.wave != other.wave) {
            return Integer.compare(other.wave, this.wave);
        }
        return Long.compare(other.timestamp, this.timestamp);
    }
}

