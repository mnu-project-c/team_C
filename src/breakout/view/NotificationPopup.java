package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class NotificationPopup {
    private String message;
    private int displayTimer;
    private final int DISPLAY_DURATION = 180; // 3초 (60FPS 기준)
    private boolean isVisible;
    private final Font font;

    public NotificationPopup() {
        this.isVisible = false;
        // 폰트는 GamePanel에서 로드한 것을 쓰거나 별도로 지정
        this.font = new Font("Malgun Gothic", Font.BOLD, 16);
    }

    // 알림을 시작하는 메서드
    public void show(String achievementTitle) {
        this.message = "업적 달성! : " + achievementTitle;
        this.displayTimer = DISPLAY_DURATION;
        this.isVisible = true;
    }

    // 매 프레임 상태 업데이트 (타이머 감소)
    public void update() {
        if (!isVisible) return;

        displayTimer--;
        if (displayTimer <= 0) {
            isVisible = false;
        }
    }

    // 화면에 그리기
    public void draw(Graphics2D g2, int screenWidth) {
        if (!isVisible) return;

        // 페이드 아웃 효과 계산 (마지막 0.5초 동안 투명해짐)
        float alpha = 1.0f;
        if (displayTimer < 30) {
            alpha = displayTimer / 30.0f;
        }
        // alpha 값이 0~1 사이를 벗어나지 않도록 안전장치
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int boxWidth = fm.stringWidth(message) + 40;
        int boxHeight = 40;
        int x = (screenWidth - boxWidth) / 2;
        int y = 20; // 화면 상단에서 떨어진 거리

        // 배경 박스 그리기 (반투명 검정)
        g2.setColor(new Color(0, 0, 0, (int)(180 * alpha)));
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

        // 테두리 그리기 (반투명 노랑)
        g2.setColor(new Color(255, 215, 0, (int)(255 * alpha))); // 금색
        g2.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);

        // 텍스트 그리기 (반투명 흰색)
        g2.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        g2.drawString(message, x + 20, y + 25);
    }
}