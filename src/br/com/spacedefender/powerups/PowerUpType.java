package br.com.spacedefender.powerups;

/**
 * Tipos de Power-Ups disponíveis no jogo.
 * Encapsula o nome de exibição, duração do buff e a via de obtenção.
 */
public enum PowerUpType {
    DOUBLE_SCORE("2x Pontos (Pílula)", 15000, true),
    HEALTH_REPAIR("+50 HP (Vida)", 0, true),
    TRIPLE_SHOT("Tiro Triplo (A)", 15000, false),
    SHIELD_BARRIER("Escudo Protetor", 12000, false),
    NOVA_BOMB("Bomba Nova", 0, false);

    private final String displayName;
    private final long durationMs;
    private final boolean droppedByEnemy;

    PowerUpType(String displayName, long durationMs, boolean droppedByEnemy) {
        this.displayName = displayName;
        this.durationMs = durationMs;
        this.droppedByEnemy = droppedByEnemy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isDroppedByEnemy() {
        return droppedByEnemy;
    }
}
