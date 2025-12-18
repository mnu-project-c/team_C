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
        confirmBtn = new GameButton(x + 40, y + height - 70, 100, 40, "CONFIRM");
        cancelBtn = new GameButton(x + width - 140, y + height - 70, 100, 40, "CANCEL");
    }

    public void update(MouseHandler mouse, SoundManager sound) {
    // 1. 각 버튼의 호버 및 상태 업데이트
    confirmBtn.update(mouse);
    cancelBtn.update(mouse);

    // 2. 버튼 객체에 구현된 isClicked 메서드를 직접 호출하여 체크
    if (confirmBtn.isClicked(mouse)) {
        sound.playClickSound();
        // 이름이 한 글자라도 입력되었을 때만 완료 처리 (선택 사항)
        if (inputName.length() > 0) {
            isFinished = true;
        }
    } 
    else if (cancelBtn.isClicked(mouse)) {
        sound.playClickSound();
        isCancelled = true;
        isFinished = true;
    }
}

    // 1. 제어 키 처리 (백스페이스, 엔터) - GamePanel의 keyPressed에서 호출
    public void handleKeyPress(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_BACK_SPACE && inputName.length() > 0) {
            inputName.deleteCharAt(inputName.length() - 1);
        } else if (code == KeyEvent.VK_ENTER && inputName.length() > 0) {
            isFinished = true;
        }
    }

    // 2. 실제 문자 입력 처리 (한글, 영문, 숫자, 특수문자) - GamePanel의 keyTyped에서 호출
    public void handleKeyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        // 제어 문자가 아니고 가시적인 유니코드 문자일 때만 추가
        if (inputName.length() < MAX_CHAR && c != KeyEvent.CHAR_UNDEFINED && c >= ' ') {
            inputName.append(c);
        }
    }

    public void draw(Graphics2D g, Font font) {
        // 배경 어둡게
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // 모달 상자
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);

        // 타이틀
        g.setFont(font.deriveFont(Font.BOLD, 22f));
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, bounds.x + (bounds.width - titleW) / 2, bounds.y + 45);

        // 입력 필드 박스
        g.setColor(Color.BLACK);
        g.fillRect(bounds.x + 30, bounds.y + 70, bounds.width - 60, 50);
        g.setColor(Color.CYAN);
        g.drawRect(bounds.x + 30, bounds.y + 70, bounds.width - 60, 50);

        // 입력 중인 이름
        g.setColor(Color.WHITE);
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 20)); // 한글 지원 폰트
        String display = inputName.toString() + (System.currentTimeMillis() % 1000 < 500 ? "|" : "");
        g.drawString(display, bounds.x + 45, bounds.y + 103);

        confirmBtn.draw(g, font);
        cancelBtn.draw(g, font);
    }

    public String getInputName() { return inputName.length() == 0 ? "익명" : inputName.toString(); }
    public boolean isFinished() { return isFinished; }
    public boolean isCancelled() { return isCancelled; }
}