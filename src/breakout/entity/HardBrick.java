package breakout.entity;

import java.awt.Color;

public class HardBrick extends Brick {

    public HardBrick(double x, double y, double width, double height) {
        // HP를 3으로 설정, 기본 색상은 어두운 회색
        super(x, y, width, height, 3, Color.DARK_GRAY); 
    }

    @Override
    public void hit() {
        super.hit(); // HP 감소 (3 -> 2 -> 1 -> 0)
        
        // HP에 따라 색상 변경 (시각적 피드백)
        if (hp == 2) {
            this.color = Color.GRAY;       // 조금 깨짐
        } else if (hp == 1) {
            this.color = Color.ORANGE;     // 거의 깨짐 (일반 벽돌 색과 비슷하게)
        }
    }

    @Override
    public void onCollision(Collidable other) {
        // 필요하다면 단단한 벽돌에 부딪혔을 때 깡! 하는 금속 소리 등을 재생할 수 있음
    }
}