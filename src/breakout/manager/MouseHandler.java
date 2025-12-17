package breakout.manager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {
    
    public int x, y;
    public boolean isPressed = false; // 드래그/꾹 누르기 용
    public boolean clicked = false;   // ★ 에디터용 클릭 감지 변수 추가

    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        // 드래그 중에도 위치 업데이트
    }

    @Override
    public void mousePressed(MouseEvent e) {
        isPressed = true;
        clicked = true; // 누르는 순간 클릭 상태 활성화
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isPressed = false;
        clicked = false; // 떼면 비활성화
    }
}