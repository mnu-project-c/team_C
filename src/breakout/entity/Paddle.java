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
    
    // ★ 패들 색상 추가 (기본값 CYAN)
    private Color color = Color.CYAN;

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
    
    public void expand() {
        if (width < 200) { 
            width += 50;
        }
    }
    
    public void resetWidth() {
        width = originalWidth;
    }
    
    // ★ 색상 변경 메소드
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        // ★ 3D 입체 효과 (각진 형태 + 입체감)
        g.fill3DRect((int)position.x, (int)position.y, (int)width, (int)height, true);
    }

    @Override
    public void onCollision(Collidable other) { }
}