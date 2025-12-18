package breakout.entity;

import java.awt.Color;

public class MediumBrick extends Brick {

    public MediumBrick(double x, double y, double width, double height) {
        // HP 2, 색상은 초록색(GREEN)
        super(x, y, width, height, 2, Color.GREEN); 
    }

    @Override
    public void hit() {
        super.hit(); // HP 감소 (2 -> 1 -> 0)
        
        // HP가 1 남았을 때 시각적 피드백 (좀 더 밝은 색으로 변경)
        if (hp == 1) {
            this.color = new Color(144, 238, 144); // Light Green (연두색)
        }
    }
    
    @Override
    public void onCollision(Collidable other) {
        // 필요 시 충돌 효과음 등 추가
    }
}