package breakout.view;

import java.awt.*;
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
        // 버튼 배치 (모달 하단)
        confirmBtn = new GameButton(x + 40, y + height - 70, 100, 40, "CONFIRM");
        cancelBtn = new GameButton(x + width - 140, y + height - 70, 100, 40, "CANCEL");
    }

    public void update(MouseHandler mouse, SoundManager sound) {
        confirmBtn.update(mouse);
        cancelBtn.update(mouse);

        if (confirmBtn.isClicked(mouse)) {
            sound.playClickSound(); //
            isFinished = true;
        }
        if (cancelBtn.isClicked(mouse)) {
            sound.playClickSound();
            isCancelled = true;
            isFinished = true;
        }
    }

    // GamePanel의 KeyListener에서 호출할 메서드
    public void handleKeyPress(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_BACK_SPACE && inputName.length() > 0) {
            inputName.deleteCharAt(inputName.length() - 1);
        } else if (code == KeyEvent.VK_ENTER && inputName.length() > 0) {
            isFinished = true;
        }
    }
    public void handleKeyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        
        if (inputName.length() < MAX_CHAR && c != KeyEvent.CHAR_UNDEFINED && c >= ' ') {
            inputName.append(c);
        }
    }
    public void draw(Graphics2D g, Font font) {
        // 1. 반투명 배경 (오버레이)
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // 2. 모달 박스
        g.setColor(new Color(50, 50, 50));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);

        // 3. 텍스트 정보
        g.setFont(new Font("Consolas", Font.BOLD, 25));
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, bounds.x + (bounds.width - titleW) / 2, bounds.y + 50);

        // 4. 입력 필드 박스
        g.setColor(Color.BLACK);
        g.fillRect(bounds.x + 30, bounds.y + 80, bounds.width - 60, 50);
        g.setColor(Color.CYAN);
        g.drawRect(bounds.x + 30, bounds.y + 80, bounds.width - 60, 50);

        // 5. 입력 중인 이름
        g.setColor(Color.WHITE);
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        String display = inputName.toString() + (System.currentTimeMillis() % 1000 < 500 ? "|" : "");
        g.drawString(display, bounds.x + 45, bounds.y + 115);

        confirmBtn.draw(g,font);
        cancelBtn.draw(g,font);
    }

    public String getInputName() { return inputName.length() == 0 ? "익명" : inputName.toString(); }
    public boolean isFinished() { return isFinished; }
    public boolean isCancelled() { return isCancelled; }
}