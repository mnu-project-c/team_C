package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import breakout.manager.ScoreEntry;
import breakout.manager.ScoreManager;
import breakout.manager.MouseHandler;

public class LeaderboardPanel {
    private ScoreManager scoreManager;
    private GameButton backButton;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    public LeaderboardPanel(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        this.backButton = new GameButton(
                GamePanel.WIDTH / 2 - 100, 
                500, 
                200, 
                50, 
                "뒤로가기"
        );
    }

    /**
     * Update returns true when back button is clicked (request to close leaderboard)
     */
    public boolean update(MouseHandler mouseHandler) {
        backButton.update(mouseHandler);
        return backButton.isClicked(mouseHandler);
    }

    public void draw(Graphics2D g2, Font customFont) {
        // Dimmed background
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // Title
        String title = "LEADERBOARD";
        Font titleFont;
        if (customFont != null) {
            titleFont = customFont.deriveFont(Font.BOLD, 48f);
        } else {
            titleFont = new Font("SansSerif", Font.BOLD, 48);
        }
        
        g2.setFont(titleFont);
        g2.setColor(new Color(255, 215, 0)); // gold-ish
        drawCenteredString(g2, title, GamePanel.WIDTH / 2, 90);

        // Column headers
        Font headerFont;
        if (customFont != null) {
            headerFont = customFont.deriveFont(Font.BOLD, 20f);
        } else {
            headerFont = new Font("SansSerif", Font.BOLD, 20);
        }
        
        g2.setFont(headerFont);
        g2.setColor(new Color(200, 200, 200));
        
        int leftX = GamePanel.WIDTH / 2 - 240;
        int nameX = GamePanel.WIDTH / 2 - 120;
        int scoreX = GamePanel.WIDTH / 2 + 100;
        int dateX = GamePanel.WIDTH - 140;
        
        g2.drawString("No.", leftX, 140);
        g2.drawString("이름", nameX, 140);
        g2.drawString("점수", scoreX, 140);
        g2.drawString("일자", dateX, 140);

        // Entries
        List<ScoreEntry> top = scoreManager.getTopScores();
        int startY = 170;
        int rowH = 36;

        Font nameFont;
        if (customFont != null) {
            nameFont = customFont.deriveFont(Font.BOLD, 22f);
        } else {
            nameFont = new Font("SansSerif", Font.BOLD, 22);
        }
        
        Font scoreFont = new Font("Consolas", Font.BOLD, 20);
        
        Font dateFont;
        if (customFont != null) {
            dateFont = customFont.deriveFont(Font.PLAIN, 14f);
        } else {
            dateFont = new Font("SansSerif", Font.PLAIN, 14);
        }

        for (int i = 0; i < top.size(); i++) {
            ScoreEntry e = top.get(i);
            int y = startY + i * rowH;

            // subtle separator
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRect(GamePanel.WIDTH / 2 - 300, y - 18, 600, 1);

            // Rank
            g2.setFont(nameFont);
            g2.setColor(new Color(180, 180, 255));
            g2.drawString(String.format("%2d.", i + 1), leftX, y);

            // Name (truncate if long)
            String name = e.getName();
            if (name.length() > 12) {
                name = name.substring(0, 12) + "…";
            }
            g2.setColor(Color.WHITE);
            g2.drawString(name, nameX, y);

            // Score
            g2.setFont(scoreFont);
            String scoreStr = String.valueOf(e.getScore());
            g2.drawString(scoreStr, scoreX, y);

            // Date (small, gray)
            g2.setFont(dateFont);
            String dateText = formatDate(e.getDate());
            g2.setColor(new Color(180, 180, 180));
            g2.drawString(dateText, dateX, y);
        }

        backButton.draw(g2, customFont);
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "-";
        }
        
        try {
            Instant t = Instant.parse(iso);
            return DATE_FMT.format(t);
        } catch (Exception ex) {
            // fall back to first 10 chars
            if (iso.length() >= 10) {
                return iso.substring(0, 10);
            } else {
                return iso;
            }
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int tx = (x - fm.stringWidth(text) / 2);
        g2.drawString(text, tx, y);
    }
}