package br.com.spacedefender.managers;

import br.com.spacedefender.scores.ScoreEntry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gerenciador de persistência do ranking de pontuações estilo Arcade (Top 10 High Scores).
 * Salva e carrega os registros históricos localmente em arquivo CSV.
 */
public class HighScoreManager {
    private static final String DATA_DIR = "data";
    private static final String FILE_PATH = "data/highscores.csv";
    private static final int MAX_ENTRIES = 10;

    private final List<ScoreEntry> scores = new ArrayList<>();

    public HighScoreManager() {
        load();
    }

    public synchronized void addScore(String initials, int score, int wave) {
        String cleanInitials = (initials == null || initials.trim().isEmpty()) ? "AAA" : initials.trim().toUpperCase();
        if (cleanInitials.length() > 3) {
            cleanInitials = cleanInitials.substring(0, 3);
        }
        scores.add(new ScoreEntry(cleanInitials, score, wave));
        Collections.sort(scores);
        while (scores.size() > MAX_ENTRIES) {
            scores.remove(scores.size() - 1);
        }
        save();
    }

    public synchronized List<ScoreEntry> getScores() {
        return Collections.unmodifiableList(new ArrayList<>(scores));
    }

    public synchronized boolean isHighScore(int score) {
        if (score <= 0) return false;
        if (scores.size() < MAX_ENTRIES) return true;
        return score > scores.get(scores.size() - 1).getScore();
    }

    private synchronized void load() {
        scores.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            initDefaultScores();
            save();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String initials = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    int wave = Integer.parseInt(parts[2].trim());
                    scores.add(new ScoreEntry(initials, score, wave));
                }
            }
            Collections.sort(scores);
        } catch (Throwable e) {
            initDefaultScores();
        }

        if (scores.isEmpty()) {
            initDefaultScores();
            save();
        }
    }

    private synchronized void save() {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
                writer.println("# Space Defender Arcade High Scores");
                for (ScoreEntry entry : scores) {
                    writer.printf("%s,%d,%d\n", entry.getInitials(), entry.getScore(), entry.getWave());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void initDefaultScores() {
        scores.clear();
        scores.add(new ScoreEntry("ACE", 5000, 5));
        scores.add(new ScoreEntry("FEL", 3800, 4));
        scores.add(new ScoreEntry("BIA", 3000, 3));
        scores.add(new ScoreEntry("NEO", 2200, 2));
        scores.add(new ScoreEntry("FOX", 1400, 1));
        scores.add(new ScoreEntry("MAX", 1000, 1));
        Collections.sort(scores);
    }
}

