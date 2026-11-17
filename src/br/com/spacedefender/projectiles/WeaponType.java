package br.com.spacedefender.projectiles;

/**
 * Representa os tipos de armas disponíveis para a nave do jogador.
 * Encapsula o nome de exibição, cooldown característico e metadados.
 */
public enum WeaponType {
    LASER("Laser Pulse", 200),
    PLASMA("Plasma Ball", 450);

    private final String displayName;
    private final int cooldownMs;

    WeaponType(String displayName, int cooldownMs) {
        this.displayName = displayName;
        this.cooldownMs = cooldownMs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCooldownMs() {
        return cooldownMs;
    }
}
