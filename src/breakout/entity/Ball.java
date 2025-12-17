package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import breakout.engine.Vector2D;
import breakout.view.GamePanel;

public class Ball extends GameObject {
    
    private Vector2D velocity;

    public Ball(double x, double y) {
        super(x, y, 20, 20);
        this.velocity = new Vector2D(4.0, -4.0);
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    @Override
    public void update() {
        position.x += velocity.x;
        position.y += velocity.y;

        if (position.x < 0) {
            position.x = 0;
            velocity.x = -velocity.x;
        }
        if (position.x > GamePanel.WIDTH - width) {
            position.x = GamePanel.WIDTH - width;
            velocity.x = -velocity.x;
        }
        if (position.y < 0) {
            position.y = 0;
            velocity.y = -velocity.y;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // 현재 설정된 색상(GamePanel에서 setColor함)을 가져옴
        Color baseColor = g.getColor();
        
        int x = (int)position.x;
        int y = (int)position.y;
        int w = (int)width;
        int h = (int)height;
        
        // 3D 구슬 효과: 빛이 왼쪽 상단(25%, 25%) 위치에서 비친다고 가정
        Point2D center = new Point2D.Float(x + w/2, y + h/2);
        float radius = w/2;
        Point2D focus = new Point2D.Float(x + w/4, y + h/4); // 하이라이트 위치
        
        float[] dist = {0.0f, 1.0f};
        // 중심은 거의 흰색(하이라이트) -> 외곽은 원래 색상 -> 더 외곽은 어두운 색
        Color[] colors = {Color.WHITE, baseColor};
        
        try {
            // 방사형 그라데이션 적용
            RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, RadialGradientPaint.CycleMethod.NO_CYCLE);
            g.setPaint(p);
            g.fillOval(x, y, w, h);
        } catch (Exception e) {
            // RadialGradientPaint가 지원되지 않는 환경일 경우 대비 (안전장치)
            g.setColor(baseColor);
            g.fillOval(x, y, w, h);
            // 단순 하이라이트
            g.setColor(new Color(255, 255, 255, 100));
            g.fillOval(x + 5, y + 5, w/3, h/3);
        }
    }

    @Override
    public void onCollision(Collidable other) {
        // 기본 반사 (나중에 물리 엔진이 덮어쓸 수 있음)
        velocity.y = -velocity.y;
    }
}