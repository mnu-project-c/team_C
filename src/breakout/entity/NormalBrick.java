package breakout.entity;

import java.awt.Color;

public class NormalBrick extends Brick {

    public NormalBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 1, Color.YELLOW); // 내구도 1, 노란색
    }

    @Override
    public void onCollision(Collidable other) {
        // 벽돌의 충돌 로직(hp 감소)은 GamePanel.update()에서 처리하고 있습니다.
        // 여기서는 소리나 특수 효과 등 추가적인 처리를 할 수 있지만, 현재는 비워둡니다.
    }
}