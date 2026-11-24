package br.com.spacedefender.audio;

import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Motor de áudio do jogo Space Defender: Galaxy Core.
 * Utiliza javax.sound.sampled com síntese procedural de formas de onda PCM (8-bit/16-bit 44.1kHz),
 * garantindo fidelidade de áudio arcade retrô, latência zero, independência de bibliotecas externas
 * e tolerância a falhas caso o ambiente não possua dispositivo de saída física (ex: CI/containers headless).
 */
public class SoundManager {
    private static SoundManager instance;

    private static final int SAMPLE_RATE = 44100;
    private static final int POOL_SIZE_PER_SOUND = 3;

    private float volume = 0.8f; // Volume padrão de 80%
    private boolean muted = false;
    private boolean audioAvailable = true;

    // Pool de Clips para suportar polifonia (múltiplos tiros/explosões simultâneas)
    private final Map<SoundType, Clip[]> soundPools = new EnumMap<>(SoundType.class);
    private final Map<SoundType, Integer> poolIndices = new EnumMap<>(SoundType.class);

    private SoundManager() {
        initAudio();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void initAudio() {
        try {
            for (SoundType type : SoundType.values()) {
                byte[] pcmData = generateSoundData(type);
                Clip[] pool = new Clip[POOL_SIZE_PER_SOUND];
                for (int i = 0; i < POOL_SIZE_PER_SOUND; i++) {
                    pool[i] = createClip(pcmData);
                }
                soundPools.put(type, pool);
                poolIndices.put(type, 0);
            }
            applyVolume();
        } catch (Throwable t) {
            // Em ambientes sem driver de som, degrada graciosamente sem impedir a execução
            audioAvailable = false;
        }
    }

    private Clip createClip(byte[] pcmData) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(pcmData),
                format,
                pcmData.length / 2
            );
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Toca o efeito sonoro especificado de forma assíncrona.
     */
    public void play(SoundType type) {
        if (!audioAvailable || muted || volume <= 0.01f || type == null) {
            return;
        }

        Clip[] pool = soundPools.get(type);
        if (pool == null || pool.length == 0) {
            return;
        }

        int index = poolIndices.getOrDefault(type, 0);
        Clip clip = pool[index];
        poolIndices.put(type, (index + 1) % pool.length);

        if (clip != null) {
            try {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                clip.start();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Ajusta o volume geral (0.0 a 1.0).
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        applyVolume();
    }

    public float getVolume() {
        return volume;
    }

    public void increaseVolume() {
        setVolume(volume + 0.1f);
    }

    public void decreaseVolume() {
        setVolume(volume - 0.1f);
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        applyVolume();
    }

    public void toggleMute() {
        setMuted(!muted);
    }

    private void applyVolume() {
        for (Clip[] pool : soundPools.values()) {
            if (pool == null) continue;
            for (Clip clip : pool) {
                if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    try {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        if (muted || volume <= 0.01f) {
                            gain.setValue(gain.getMinimum());
                        } else {
                            // Converte escala linear (0.0 a 1.0) para dB logarítmico (-40dB a 0dB)
                            float dB = (float) (Math.log10(volume) * 20.0);
                            float clampedDB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                            gain.setValue(clampedDB);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    /**
     * Síntese procedural de efeitos sonoros retrô estilo Arcade dos anos 80/90.
     */
    private byte[] generateSoundData(SoundType type) {
        switch (type) {
            case LASER:
                // Descida rápida de frequência (900 Hz -> 300 Hz em ~110ms)
                return synthFrequencySweep(900, 300, 110, 0.7);

            case PLASMA:
                // Tom pesado e ressonante grave (350 Hz -> 90 Hz em ~200ms)
                return synthFrequencySweep(350, 90, 200, 0.9);

            case EXPLOSION:
                // Explosão estilo ruído branco (White Noise) com decaimento exponencial (~300ms)
                return synthExplosionNoise(320);

            case POWERUP:
                // Arpeggio brilhante de 4 notas ascendentes (C5, E5, G5, C6)
                return synthArpeggio(new double[]{523.25, 659.25, 783.99, 1046.50}, 75);

            case PLAYER_HIT:
                // Impacto grave de impacto com distorção e corte (~160ms)
                return synthFrequencySweep(220, 60, 160, 0.85);

            case GAME_OVER:
                // Melodia menor descendente melancólica (E4, D4, C4, A3)
                return synthArpeggio(new double[]{329.63, 293.66, 261.63, 220.00}, 250);

            case MENU_BEEP:
                // Beep curto e nítido (880 Hz em ~45ms)
                return synthTone(880, 45, 0.5);

            default:
                return synthTone(440, 50, 0.5);
        }
    }

    private byte[] synthTone(double freq, int durationMs, double maxAmp) {
        int totalSamples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] buffer = new byte[totalSamples * 2];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double envelope = 1.0 - ((double) i / totalSamples); // Decaimento linear
            double sample = Math.sin(2.0 * Math.PI * freq * t) * envelope * maxAmp;
            short val = (short) (sample * 32767);
            buffer[i * 2] = (byte) (val & 0xFF);
            buffer[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
        return buffer;
    }

    private byte[] synthFrequencySweep(double startFreq, double endFreq, int durationMs, double maxAmp) {
        int totalSamples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] buffer = new byte[totalSamples * 2];
        double phase = 0;

        for (int i = 0; i < totalSamples; i++) {
            double progress = (double) i / totalSamples;
            double currentFreq = startFreq + (endFreq - startFreq) * progress;
            phase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE;

            double envelope = 1.0 - progress; // Fade out suave
            double sample = Math.sin(phase) * envelope * maxAmp;
            short val = (short) (sample * 32767);
            buffer[i * 2] = (byte) (val & 0xFF);
            buffer[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
        return buffer;
    }

    private byte[] synthExplosionNoise(int durationMs) {
        int totalSamples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] buffer = new byte[totalSamples * 2];
        Random rand = new Random(42);
        double lastSample = 0;

        for (int i = 0; i < totalSamples; i++) {
            double progress = (double) i / totalSamples;
            double envelope = Math.exp(-progress * 4.5); // Decaimento exponencial rápido
            double noise = (rand.nextDouble() * 2.0 - 1.0);

            // Filtro passa-baixa simples para dar peso grave ao estrondo
            lastSample = lastSample * 0.75 + noise * 0.25;
            double sample = lastSample * envelope * 0.95;

            short val = (short) (sample * 32767);
            buffer[i * 2] = (byte) (val & 0xFF);
            buffer[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
        return buffer;
    }

    private byte[] synthArpeggio(double[] freqs, int noteDurationMs) {
        int totalSamplesPerNote = (SAMPLE_RATE * noteDurationMs) / 1000;
        int totalSamples = totalSamplesPerNote * freqs.length;
        byte[] buffer = new byte[totalSamples * 2];

        int bufferIndex = 0;
        for (double freq : freqs) {
            for (int i = 0; i < totalSamplesPerNote; i++) {
                double t = (double) i / SAMPLE_RATE;
                double envelope = 1.0 - (0.6 * ((double) i / totalSamplesPerNote));
                double sample = Math.sin(2.0 * Math.PI * freq * t) * envelope * 0.7;
                short val = (short) (sample * 32767);
                buffer[bufferIndex++] = (byte) (val & 0xFF);
                buffer[bufferIndex++] = (byte) ((val >> 8) & 0xFF);
            }
        }
        return buffer;
    }
}
