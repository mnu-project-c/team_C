package breakout.engine;

import java.awt.Rectangle;
import java.util.List;

import breakout.entity.Ball;
import breakout.entity.Brick;
import breakout.entity.GameObject;
import breakout.entity.Paddle;
import breakout.entity.Collidable;
import breakout.manager.SoundManager;

public final class CollisionDetector {

    private CollisionDetector() {

    }

    public static boolean isColliding(Collidable a, Collidable b) {
        if (a == null || b == null) 
            return false;
        return a.getBounds().intersects(b.getBounds());
    }

    public static void handleWallCollision(Ball ball, int minX, int minY, int maxX, int maxY, SoundManager soundManager) {
        if (ball == null) 
            return;

        double x = ball.getPosition().x;
        double y = ball.getPosition().y;
        double w = ball.getBounds().getWidth();
        Vector2D v = ball.getVelocity();
        boolean hit = false;

        // 좌측 벽: 왼쪽으로 가고 있을 때만 체크
             if (x <= minX && v.x < 0) { 
                v.x = Math.abs(v.x); // 강제로 오른쪽(+) 방향으로 설정 (이중 반사 방지)
                ball.getPosition().x = minX; 
                hit = true;
            } 
    // 우측 벽: 오른쪽으로 가고 있을 때만 체크
            else if (x + w >= maxX && v.x > 0) { 
                v.x = -Math.abs(v.x); // 강제로 왼쪽(-) 방향으로 설정
                ball.getPosition().x = maxX - w; 
                hit = true;
            }
    
    // 상단 벽: 위로 가고 있을 때만 체크
            if (y <= minY && v.y < 0) { 
                v.y = Math.abs(v.y); // 강제로 아래(+) 방향으로 설정
                ball.getPosition().y = minY; 
                hit = true;
            }

    // 충돌이 발생했을 때만 소리 재생
            if (hit && soundManager != null) {
             soundManager.playWallSound();
             }
     }

    public static void handlePaddleCollision(Ball ball, Paddle paddle) {
        if (ball == null || paddle == null) 
            return;
        if (!isColliding(ball, paddle)) 
            return;

        Rectangle b = ball.getBounds();
        Rectangle p = paddle.getBounds();
        Vector2D v = ball.getVelocity();

        // 공이 내려오고 있을 때만 반사
        if (v.y > 0) {
            double paddleCenter = p.getCenterX();
            double ballCenter = b.getCenterX();
            // -1.0 (왼쪽 끝) ~ 0.0 (중앙) ~ 1.0 (오른쪽 끝)
            double offset = (ballCenter - paddleCenter) / (p.getWidth() / 2.0);
            
            double speed = v.magnitude(); // 현재 속력 유지

            switch (paddle.getShapeType()) {
                case Paddle.SHAPE_RECT:
                case Paddle.SHAPE_ROUND:
                    // 기존: 중앙에서 멀어질수록 각도가 커짐
                    v.x = speed * offset;
                    v.y = -Math.abs(v.y); 
                    break;

                case Paddle.SHAPE_DIAMOND:
                    // 마름모: 경사면 효과 (강제 굴절)
                    if (offset < 0) {
                        v.x = -speed * 0.8; 
                    } else {
                        v.x = speed * 0.8;  
                    }
                    v.y = -Math.sqrt(speed*speed - v.x*v.x);
                    break;

                case Paddle.SHAPE_WAVE: // ★ 물결 반사 로직
                    // 사인파의 기울기(cos)를 이용하여 반사각 계산
                    double angle = (offset + 1.0) * Math.PI; // 0 ~ 2PI
                    double slope = Math.cos(angle); 
                    
                    // 기울기에 따라 x축 속도 가속/감속
                    v.x = speed * (offset + slope * 0.5);
                    
                    // 최대 속도 제한
                    if (v.x > speed * 0.95)
                         v.x = speed * 0.95;
                    if (v.x < -speed * 0.95) 
                        v.x = -speed * 0.95;
                    
                    v.y = -Math.sqrt(Math.abs(speed*speed - v.x*v.x));
                    break;
            }

            // 공 겹침 방지 보정
            if (paddle.getShapeType() == Paddle.SHAPE_DIAMOND || paddle.getShapeType() == Paddle.SHAPE_WAVE) {
                ball.getPosition().y = p.y - b.height - 10;
            } else {
                ball.getPosition().y = p.y - b.height - 1;
            }
        }
    }

    public static void handleBrickCollisions(Ball ball, List<? extends Brick> bricks) {
        if (ball == null || bricks == null) 
            return;
        for (Brick brick : bricks) {
            if (brick == null || brick.isDestroyed) 
                continue;
            if (isColliding(ball, brick)) {
                resolveBallVsRect(ball, brick);
                brick.hit();
                // brick.onCollision(ball);
                break; 
            }
        }
    }

    public static void resolveBallVsRect(Ball ball, GameObject rect) {
        if (ball == null || rect == null) 
            return;
        Rectangle b = ball.getBounds();
        Rectangle r = rect.getBounds();
        if (!b.intersects(r)) 
            return; 

        Vector2D v = ball.getVelocity();
        double overlapLeft   = b.getMaxX() - r.getMinX();
        double overlapRight  = r.getMaxX() - b.getMinX();
        double overlapTop    = b.getMaxY() - r.getMinY();
        double overlapBottom = r.getMaxY() - b.getMinY();

        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

        if (minOverlap == overlapLeft) {
             ball.getPosition().x -= overlapLeft; v.x = -Math.abs(v.x); 
            }
        else if (minOverlap == overlapRight) {
             ball.getPosition().x += overlapRight; v.x = Math.abs(v.x); 
            }
        else if (minOverlap == overlapTop) { 
            ball.getPosition().y -= overlapTop; v.y = -Math.abs(v.y);
         }
        else {
             ball.getPosition().y += overlapBottom; v.y = Math.abs(v.y);
             }
    }
}