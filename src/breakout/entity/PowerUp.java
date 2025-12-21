package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import breakout.view.GamePanel;

public abstract class PowerUp extends GameObject {
    
    protected int type; // 0: Life, 1: Expand
    private double fallSpeed = 3.0; // 떨어지는 속도

    public PowerUp(double x, double y, int type) {
        super(x, y, 20, 20); // 20x20 크기
        this.type = type;
    }

    @Override
    public void update() {
        position.y += fallSpeed; // 아래로 떨어짐
    }

    // 아이템 효과 적용 (추상 메소드)
    public abstract void applyEffect(GamePanel game);

    @Override
    public void draw(Graphics2D g) {
        // 아이템 모양 그리기 (동그라미)
        if (type == 0) g.setColor(Color.RED);       // 생명 (빨강)
        else g.setColor(Color.BLUE);                // 패들 확장 (파랑)
        
        g.fillOval((int)position.x, (int)position.y, (int)width, (int)height);
        
        // 테두리
        g.setColor(Color.WHITE);
        g.drawOval((int)position.x, (int)position.y, (int)width, (int)height);
        
        // 글자 표시 (L 또는 E)
        g.setColor(Color.WHITE);
        g.drawString(type == 0 ? "L" : "E", (int)position.x + 6, (int)position.y + 15);
    }
    
    @Override
    public void onCollision(Collidable other) {
        
    }
}