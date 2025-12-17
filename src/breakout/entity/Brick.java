package kr.ac.mnu.c_team.breakout.entity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public abstract class Brick extends GameObject {
    
    public int hp;
    public int scoreValue;
    public Color color;
    public boolean isDestroyed = false;

    public Brick(double x, double y, double width, double height, int hp, Color color) {
        super(x, y, width, height);
        this.hp = hp;
        this.color = color;
        this.scoreValue = 100 * hp;
    }

    public void hit() {
        hp--;
        if (hp <= 0) {
            isDestroyed = true;
        }
    }

    @Override
    public void update() { }

    @Override
    public void draw(Graphics2D g) {
        if (!isDestroyed) {
            int x = (int)position.x;
            int y = (int)position.y;
            int w = (int)width;
            int h = (int)height;
            
            // 1. 기본 그라데이션 채우기 (위에서 아래로 어두워짐 -> 둥근 입체감)
            GradientPaint gp = new GradientPaint(x, y, color.brighter(), x, y + h, color.darker());
            g.setPaint(gp);
            g.fillRect(x, y, w, h);
            
            // 2. 3D 하이라이트 효과 (왼쪽/위쪽은 밝게, 오른쪽/아래쪽은 어둡게)
            // 밝은 테두리 (빛 받는 부분)
            g.setColor(new Color(255, 255, 255, 100)); // 반투명 흰색
            g.fillRect(x, y, w, 4); // 윗면
            g.fillRect(x, y, 4, h); // 왼쪽면
            
            // 어두운 그림자 (그림자 지는 부분)
            g.setColor(new Color(0, 0, 0, 80)); // 반투명 검은색
            g.fillRect(x + w - 4, y, 4, h); // 오른쪽면
            g.fillRect(x, y + h - 4, w, 4); // 아랫면
            
            // 3. 외곽선 (깔끔하게 마무리)
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1)); // 얇은 선
            g.drawRect(x, y, w, h);
        }
    }
}