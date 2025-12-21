package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class NotificationPopup {

    private String message;
    private int displayTimer;
    private final int DISPLAY_DURATION = 180;
    private boolean isVisible;
    private final Font font;

    public NotificationPopup() {
        this.isVisible = false;
        this.font = new Font("Malgun Gothic", Font.BOLD, 16);
    }

    public void show(String achievementTitle) {
        this.message = "업적 달성! : " + achievementTitle;
        this.displayTimer = DISPLAY_DURATION;
        this.isVisible = true;
    }

    public void update() {
        if (!isVisible) {
            return;
        }

        displayTimer--;
        
        if (displayTimer <= 0) {
            isVisible = false;
        }
    }

    public void draw(Graphics2D g2, int screenWidth) {
        if (!isVisible) {
            return;
        }

        float alpha = 1.0f;
        
        if (displayTimer < 30) {
            alpha = displayTimer / 30.0f;
        }
        
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);
        
        FontMetrics fm = g2.getFontMetrics();

        int boxWidth = fm.stringWidth(message) + 40;
        int boxHeight = 40;
        int x = (screenWidth - boxWidth) / 2;
        int y = 20;

        g2.setColor(new Color(0, 0, 0, (int) (180 * alpha)));
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

        g2.setColor(new Color(255, 215, 0, (int) (255 * alpha)));
        g2.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);

        g2.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
        g2.drawString(message, x + 20, y + 25);
    }
}