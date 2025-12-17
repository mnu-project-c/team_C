package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import breakout.manager.MouseHandler;

public class GameButton {

    private int x, y, width, height;
    private String text;
    private Rectangle bounds;
    
    // 버튼 상태
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    // 클릭 이벤트 감지용
    private boolean wasClicked = false;

    public GameButton(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void update(MouseHandler mouse) {
        // 마우스가 버튼 영역 안에 있는지 확인
        if (bounds.contains(mouse.x, mouse.y)) {
            isHovered = true;
            if (mouse.clicked) {
                isPressed = true;
                wasClicked = true;
            } else {
                isPressed = false;
            }
        } else {
            isHovered = false;
            isPressed = false;
            wasClicked = false; // 영역 밖으로 나가면 클릭 취소
        }
    }

    // 마우스를 뗐을 때 비로소 "클릭되었다"고 판단 (버그 방지)
    public boolean isClicked(MouseHandler mouse) {
        if (isHovered && wasClicked && !mouse.clicked) {
            wasClicked = false;
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g) {
        // 1. 배경 그리기 (상태에 따라 색상 변경)
        if (isPressed) {
            g.setColor(new Color(100, 100, 100)); // 클릭 중: 어두운 회색
        } else if (isHovered) {
            g.setColor(new Color(150, 150, 150)); // 마우스 오버: 밝은 회색
        } else {
            g.setColor(Color.WHITE);              // 평상시: 흰색
        }
        g.fillRect(x, y, width, height);

        // 2. 테두리 그리기
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        // 3. 텍스트 그리기 (중앙 정렬)
        g.setColor(isPressed ? Color.WHITE : Color.BLACK);
        g.setFont(new Font("Consolas", Font.BOLD, 20));
        
        int stringWidth = g.getFontMetrics().stringWidth(text);
        int stringHeight = g.getFontMetrics().getAscent();
        
        // 정중앙 좌표 계산
        int textX = x + (width - stringWidth) / 2;
        int textY = y + (height + stringHeight) / 2 - 4;
        
        g.drawString(text, textX, textY);
    }
}