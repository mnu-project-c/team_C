package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import breakout.manager.InputManager;
import breakout.view.GamePanel;

public class Paddle extends GameObject {

    private InputManager input;
    private double speed = 7.0;
    
    // 원래 크기 저장
    private double originalWidth;

    public Paddle(double x, double y, InputManager input) {
        super(x, y, 100, 20); // 기본 너비 100
        this.input = input;
        this.originalWidth = 100;
    }

    @Override
    public void update() {
        if (input.left) {
            position.x -= speed;
        }
        if (input.right) {
            position.x += speed;
        }

        // 화면 밖으로 나가지 않게
        if (position.x < 0) position.x = 0;
        if (position.x > GamePanel.WIDTH - width) position.x = GamePanel.WIDTH - width;
    }
    
    // ★ 아이템 효과: 패들 늘리기
    public void expand() {
        if (width < 200) { // 최대 200까지만 커짐
            width += 50;
        }
    }
    
    // 리셋용
    public void resetWidth() {
        width = originalWidth;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.CYAN);
        g.fillRoundRect((int)position.x, (int)position.y, (int)width, (int)height, 10, 10);
    }

    @Override
    public void onCollision(Collidable other) { }
}