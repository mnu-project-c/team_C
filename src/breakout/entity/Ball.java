package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import breakout.view.GamePanel;
import breakout.engine.Vector2D;

public class Ball extends GameObject {
    
    private Vector2D velocity;
    private Image skin; // ★ 스킨 이미지 변수

    public Ball(double x, double y) {
        super(x, y, 40, 40); // 20x20 크기
        this.velocity = new Vector2D(4.0, -4.0);
    }
    
    // ★ 스킨 설정 메소드
    public void setSkin(Image skin) {
        this.skin = skin;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    @Override
    public void update() {
        position.x += velocity.x;
        position.y += velocity.y;

        if (position.x < 0) { position.x = 0; velocity.x = -velocity.x; }
        if (position.x > GamePanel.WIDTH - width) { position.x = GamePanel.WIDTH - width; velocity.x = -velocity.x; }
        if (position.y < 0) { position.y = 0; velocity.y = -velocity.y; }
    }

    @Override
    public void draw(Graphics2D g) {
        int x = (int)position.x;
        int y = (int)position.y;
        int w = (int)width;
        int h = (int)height;
        
        // ★ 스킨이 있으면 이미지를 원형으로 그림
        if (skin != null) {
            Shape oldClip = g.getClip(); // 기존 클리핑 영역 저장
            // 원형 클리핑 영역 설정
            g.setClip(new Ellipse2D.Float(x, y, w, h));
            // 이미지 그리기 (크기에 맞게 조절)
            g.drawImage(skin, x, y, w, h, null);
            // 클리핑 해제 (원상복구)
            g.setClip(oldClip);
            
            // 테두리 살짝 추가 (깔끔하게)
            g.setColor(new Color(0, 0, 0, 50));
            g.drawOval(x, y, w, h);
        } 
        // 스킨이 없으면 기존 3D 구슬 효과 사용
        else {
            Color baseColor = g.getColor();
            Point2D center = new Point2D.Float(x + w/2, y + h/2);
            float radius = w/2;
            Point2D focus = new Point2D.Float(x + w/4, y + h/4);
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {Color.WHITE, baseColor};
            
            try {
                RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, RadialGradientPaint.CycleMethod.NO_CYCLE);
                g.setPaint(p);
                g.fillOval(x, y, w, h);
            } catch (Exception e) {
                g.setColor(baseColor);
                g.fillOval(x, y, w, h);
            }
        }
    }

    @Override
    public void onCollision(Collidable other) {
        // 간단한 충돌 반사 (상세 로직은 CollisionDetector가 처리)
        if (other instanceof Paddle) {
            velocity.y = -Math.abs(velocity.y);
        }
    }
}