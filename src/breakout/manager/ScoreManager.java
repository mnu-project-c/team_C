package breakout.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreManager {

    private static final String FILE_PATH = "highscore.txt";
    private static final int MAX_ENTRIES = 10;

    private List<ScoreEntry> entries = new ArrayList<>();

    public ScoreManager() {
        load();
    }

    public void load() {
        entries.clear();
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line.trim());
            reader.close();

            String content = sb.toString().trim();
            if (content.isEmpty()) return;

            if (content.startsWith("[")) {
                int idx = 0;
                while (true) {
                    int objStart = content.indexOf('{', idx);
                    if (objStart == -1) break;
                    int objEnd = content.indexOf('}', objStart);
                    if (objEnd == -1) break;
                    String obj = content.substring(objStart, objEnd + 1);
                    ScoreEntry e = ScoreEntry.fromJson(obj);
                    if (e != null) entries.add(e);
                    idx = objEnd + 1;
                }
                sortAndTrim();
            } else {
                // old format: single integer
                try {
                    int oldScore = Integer.parseInt(content);
                    entries.add(new ScoreEntry("익명", oldScore, Instant.now().toString()));
                    sortAndTrim();
                    save(); // migrate to JSON format
                } catch (NumberFormatException nfe) {
                    // ignore corrupt file
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sortAndTrim() {
        Collections.sort(entries, Comparator.comparingInt(ScoreEntry::getScore).reversed());
        if (entries.size() > MAX_ENTRIES) entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
    }

    public void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH));
            writer.write("[");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) writer.write(",");
                writer.write(entries.get(i).toJson());
            }
            writer.write("]");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addScore(String name, int score) {
        if (name == null || name.trim().isEmpty()) name = "익명";
        if (name.length() > 20) name = name.substring(0, 20);
        ScoreEntry e = new ScoreEntry(name, score, Instant.now().toString());
        entries.add(e);
        sortAndTrim();
        save();
    }

    public boolean isHighScore(int score) {
        if (entries.size() < MAX_ENTRIES) return true;
        return score > entries.get(entries.size() - 1).getScore();
    }

    public int getHighScore() {
        if (entries.isEmpty()) return 0;
        return entries.get(0).getScore();
    }

    public List<ScoreEntry> getTopScores() {
        return new ArrayList<>(entries);
    }
}