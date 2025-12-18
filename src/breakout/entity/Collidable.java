package breakout.entity;

import java.awt.Rectangle;

public interface Collidable {
    // 충돌 박스(히트박스)를 반환하는 메소드
    Rectangle getBounds();
    
    // 충돌했을 때의 행동을 정의하는 메소드
    void onCollision(Collidable other);
}
