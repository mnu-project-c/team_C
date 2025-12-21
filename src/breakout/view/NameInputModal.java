package breakout.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import breakout.manager.MouseHandler;
import breakout.manager.SoundManager;

public class NameInputModal {

    private String title = "NEW HIGH SCORE!";
    private StringBuilder inputName = new StringBuilder();
    private Rectangle bounds;
    private GameButton confirmBtn, cancelBtn;
    private boolean isFinished = false;
    private boolean isCancelled = false;
    private final int MAX_CHAR = 10;

    public NameInputModal(int x, int y, int width, int height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.confirmBtn = new GameButton(x + 40, y + height - 70, 100, 40, "CONFIRM");
        this.cancelBtn = new GameButton(x + width - 140, y + height - 70, 100, 40, "CANCEL");
    }

    // 입력 완료 및 취소 상태 업데이트
    public void update(MouseHandler mouse, SoundManager sound) {
        confirmBtn.update(mouse);
        cancelBtn.update(mouse);

        if (confirmBtn.isClicked(mouse)) {
            sound.playClickSound();
            if (inputName.length() > 0) {
                isFinished = true;
            }
        } else if (cancelBtn.isClicked(mouse)) {
            sound.playClickSound();
            isCancelled = true;
            isFinished = true;
        }
    }

    // 제어 키 처리 (백스페이스, 엔터)
    public void handleKeyPress(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_BACK_SPACE) {
            if (inputName.length() > 0) {
                inputName.deleteCharAt(inputName.length() - 1);
            }
        } else if (code == KeyEvent.VK_ENTER) {
            if (inputName.length() > 0) {
                isFinished = true;
            }
        }
    }

    // 문자 입력 처리
    public void handleKeyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        
        if (inputName.length() < MAX_CHAR) {
            if (c != KeyEvent.CHAR_UNDEFINED && c >= ' ') {
                inputName.append(c);
            }
        }
    }

    // UI 렌더링
    public void draw(Graphics2D g, Font font) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);

        if (font != null) {
            g.setFont(font.deriveFont(Font.BOLD, 22f));
        }
        
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, bounds.x + (bounds.width - titleW) / 2, bounds.y + 45);

        g.setColor(Color.BLACK);
        g.fillRect(bounds.x + 30, bounds.y + 70, bounds.width - 60, 50);
        
        g.setColor(Color.CYAN);
        g.drawRect(bounds.x + 30, bounds.y + 70, bounds.width - 60, 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        
        String display = inputName.toString();
        if (System.currentTimeMillis() % 1000 < 500) {
            display += "|";
        }
        
        g.drawString(display, bounds.x + 45, bounds.y + 103);

        confirmBtn.draw(g, font);
        cancelBtn.draw(g, font);
    }

    public String getInputName() {
        if (inputName.length() == 0) {
            return "익명";
        }
        return inputName.toString();
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}