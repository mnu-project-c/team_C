package breakout.manager;

import java.time.Instant;

public class ScoreEntry {
    private String name;
    private int score;
    private String date; // ISO-8601

    public ScoreEntry(String name, int score, String date) {
        this.name = name;
        this.score = score;
        this.date = date;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public String getDate() { return date; }

    public String toJson() {
        return "{\"name\":\"" + escape(name) + "\",\"score\":" + score + ",\"date\":\"" + date + "\"}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static ScoreEntry fromJson(String json) {
        if (json == null) return null;
        try {
            String nKey = "\"name\":\"";
            String sKey = "\"score\":";
            String dKey = "\"date\":\"";
            int ni = json.indexOf(nKey);
            String name = "";
            if (ni != -1) {
                int nStart = ni + nKey.length();
                int nEnd = json.indexOf('"', nStart);
                if (nEnd > nStart) name = json.substring(nStart, nEnd).replace("\\\"", "\"");
            }
            int si = json.indexOf(sKey);
            int score = 0;
            if (si != -1) {
                int sStart = si + sKey.length();
                int sEnd = sStart;
                while (sEnd < json.length() && (Character.isDigit(json.charAt(sEnd)) || json.charAt(sEnd) == '-')) sEnd++;
                score = Integer.parseInt(json.substring(sStart, sEnd));
            }
            int di = json.indexOf(dKey);
            String date = "";
            if (di != -1) {
                int dStart = di + dKey.length();
                int dEnd = json.indexOf('"', dStart);
                if (dEnd > dStart) date = json.substring(dStart, dEnd);
            }
            if (date == null || date.isEmpty()) date = Instant.now().toString();
            if (name == null || name.isEmpty()) name = "익명";
            return new ScoreEntry(name, score, date);
        } catch (Exception e) {
            return null;
        }
    }
}
