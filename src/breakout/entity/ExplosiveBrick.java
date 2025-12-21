package breakout.entity;

import java.awt.Color;

public class ExplosiveBrick extends Brick {

    public ExplosiveBrick(double x, double y, double width, double height) {
        // HP 1, 빨간색(RED)으로 설정
        super(x, y, width, height, 1, Color.RED);
    }

    // 폭발 벽돌은 충돌 시 별도의 로직(소리 재생 등)이 필요하면 여기에 작성
    @Override
    public void onCollision(Collidable other) {
    }
}