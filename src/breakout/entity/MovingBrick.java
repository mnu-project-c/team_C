package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;

public class MovingBrick extends Brick {
    
    private double startX;
    private double range;
    private double speedX;
    
    // 생성자: 위치, 크기, 색상, 체력, 이동범위, 속도
    public MovingBrick(double x, double y, double width, double height, Color color, int hp, double range, double speedX) {
        super(x, y, width, height, hp, color);
        this.startX = x;
        this.range = range;
        this.speedX = speedX;
        this.scoreValue = 300; // 점수
    }

    @Override
    public void update() {
        if (isDestroyed) return;

        // 좌우 이동
        position.x += speedX;
        
        // 이동 범위 체크 및 방향 전환
        if (position.x > startX + range) {
            position.x = startX + range;
            speedX *= -1;
        } else if (position.x < startX - range) {
            position.x = startX - range;
            speedX *= -1;
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        if (isDestroyed) return;
        
        // 그림자
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect((int)position.x + 5, (int)position.y + 5, (int)width, (int)height);
        
        // 부모(Brick)의 그리기 메소드 호출
        super.draw(g);
        
        // 데코레이션 (화살표 느낌)
        g.setColor(new Color(255, 255, 255, 150));
        int midY = (int)position.y + (int)height / 2;
        g.drawLine((int)position.x + 10, midY, (int)position.x + (int)width - 10, midY);
        g.fillRect((int)position.x + (int)width / 2 - 2, midY - 2, 4, 4);
    }

    
    @Override
    public void onCollision(Collidable other) {
        
    }
}