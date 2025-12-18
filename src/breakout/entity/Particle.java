package breakout.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

public class Particle extends GameObject {

    private double velX, velY;
    private float alpha = 1.0f; // 투명도 (1.0: 불투명 -> 0.0: 투명)
    private float life = 0.03f; // 수명 감소 속도
    private Color color;

    public Particle(double x, double y, Color color) {
        super(x, y, 6, 6); // 6x6 크기의 작은 조각
        this.color = color;
        
        // 사방으로 퍼지는 랜덤 속도
        this.velX = (Math.random() * 6) - 3; // -3 ~ 3
        this.velY = (Math.random() * 6) - 3; 
    }

    @Override
    public void update() {
        position.x += velX;
        position.y += velY;
        
        // 시간이 지날수록 투명해짐
        alpha -= life;
    }

    @Override
    public void draw(Graphics2D g) {
        if (alpha > 0) {
            // 투명도 적용을 위한 합성 모드 설정
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(color);
            g.fillRect((int)position.x, (int)position.y, (int)width, (int)height);
            
            // 다시 원래대로 복구 (필수!)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public boolean isDead() {
        return alpha <= 0;
    }

    // 파티클은 충돌 처리 안 함
    @Override
    public void onCollision(Collidable other) {}
}
