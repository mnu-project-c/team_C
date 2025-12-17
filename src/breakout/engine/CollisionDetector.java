package breakout.engine;

import java.awt.Rectangle;
import java.util.List;

import breakout.entity.Ball;
import breakout.entity.Brick;
import breakout.entity.GameObject;
import breakout.entity.Paddle;
import breakout.entity.Collidable;

public final class CollisionDetector {

    /** 외부에서 인스턴스 생성 방지 */
    private CollisionDetector() {}

    /**
     * 두 Collidable 객체의 AABB(축 정렬 박스) 충돌 여부를 반환한다.
     */
    public static boolean isColliding(Collidable a, Collidable b) {
        if (a == null || b == null) return false;
        Rectangle ra = a.getBounds();
        Rectangle rb = b.getBounds();
        return ra.intersects(rb);
    }

    // ============================================================
    //  공 vs 화면 경계(벽) 충돌
    // ============================================================

    /**
     * 공과 화면 경계의 충돌을 처리한다.
     *
     * @param ball  충돌을 검사할 공
     * @param minX  왼쪽 벽 x 좌표
     * @param minY  위쪽 벽 y 좌표
     * @param maxX  오른쪽 벽 x 좌표
     * @param maxY  아래쪽 벽 y 좌표
     */
    public static void handleWallCollision(Ball ball,
                                           int minX, int minY,
                                           int maxX, int maxY) {

        if (ball == null) return;

        Rectangle b = ball.getBounds();
        Vector2D v = ball.getVelocity();

        // 왼쪽 / 오른쪽 벽
        if (b.x <= minX && v.x < 0) {
            v.x = -v.x;
            ball.getPosition().x = minX;  // 살짝 안쪽으로 밀어 넣기
        } else if (b.x + b.width >= maxX && v.x > 0) {
            v.x = -v.x;
            ball.getPosition().x = maxX - b.width;
        }

        // 위쪽 벽
        if (b.y <= minY && v.y < 0) {
            v.y = -v.y;
            ball.getPosition().y = minY;
        }

        // 아래쪽(maxY)은 보통 "목숨 감소 / 게임오버" 처리이므로
        // 여기서는 단순히 반사하지 않고, GamePanel 쪽에서 따로 처리하게 두어도 된다.
    }

    // ============================================================
    //  공 vs 패들 / 벽돌 충돌 (직사각형)
    // ============================================================

    /**
     * 공과 패들의 충돌을 처리한다.
     * 단순히 Y축 속도를 반사시키고, 패들 중앙 기준으로
     * X 속도를 약간 조정해 각도를 주는 방식으로 구현할 수 있다.
     */
    public static void handlePaddleCollision(Ball ball, Paddle paddle) {
        if (ball == null || paddle == null) return;
        if (!isColliding(ball, paddle)) return;

        Rectangle b = ball.getBounds();
        Rectangle p = paddle.getBounds();
        Vector2D v = ball.getVelocity();

        // 공이 패들 위에서 내려오고 있을 때만 반사
        if (v.y > 0) {
            // 패들 중앙 기준으로 공의 위치에 따라 각도 조절
            double paddleCenter = p.getCenterX();
            double ballCenter = b.getCenterX();
            double offset = (ballCenter - paddleCenter) / (p.width / 2.0); // -1 ~ 1

            // 기본 속도를 유지하면서 x 비율만 조정
            double speed = v.magnitude();
            v.x = speed * offset;
            v.y = -Math.abs(v.y); // 위로 튕겨 나가게

            // 공을 패들 위로 살짝 올려서 겹침 방지
            ball.getPosition().y = p.y - b.height - 1;
        }
    }

    /**
     * 공과 여러 벽돌의 충돌을 검사하고, 처음 충돌한 벽돌에 대해
     * 공의 속도를 반사시키고 벽돌의 onCollision()을 호출한다.
     *
     * @param ball   충돌을 검사할 공
     * @param bricks 벽돌 리스트 (Brick, HardBrick, ExplosiveBrick 모두 포함 가능)
     */
    public static void handleBrickCollisions(Ball ball, List<? extends Brick> bricks) {
        if (ball == null || bricks == null) return;

        for (Brick brick : bricks) {
            // [수정] 정의되지 않은 isActive() 대신, public 필드인 isDestroyed를 사용합니다.
            if (brick == null || brick.isDestroyed) continue; // <--- 이 줄을 수정합니다.

            if (isColliding(ball, brick)) {
                resolveBallVsRect(ball, brick);
                brick.onCollision(ball); // 체력 감소 / 폭발 처리 등
                break; // 한 번에 하나만 처리 (원하면 계속 검사해도 됨)
            }
        }
    }

    /**
     * 공과 직사각형 GameObject의 충돌에 대해,
     * 겹친 방향을 기준으로 공의 속도를 반사한다.
     *
     * @param ball  공 오브젝트
     * @param rect  벽돌/패들 등의 직사각형 오브젝트
     */
    public static void resolveBallVsRect(Ball ball, GameObject rect) {
        if (ball == null || rect == null) return;
        if (!isColliding(ball, rect)) return;

        Rectangle b = ball.getBounds();
        Rectangle r = rect.getBounds();
        Vector2D v = ball.getVelocity();

        // 각 방향별 겹친 양 계산
        double overlapLeft   = b.getMaxX() - r.getMinX();
        double overlapRight  = r.getMaxX() - b.getMinX();
        double overlapTop    = b.getMaxY() - r.getMinY();
        double overlapBottom = r.getMaxY() - b.getMinY();

        double minOverlap = Math.min(
                Math.min(overlapLeft, overlapRight),
                Math.min(overlapTop, overlapBottom)
        );

        // 가장 적게 겹친 방향으로 밀어내고, 그 축의 속도를 반사
        if (minOverlap == overlapLeft) {
            // 왼쪽에서 충돌
            ball.getPosition().x -= overlapLeft;
            v.x = -Math.abs(v.x);
        } else if (minOverlap == overlapRight) {
            // 오른쪽에서 충돌
            ball.getPosition().x += overlapRight;
            v.x = Math.abs(v.x);
        } else if (minOverlap == overlapTop) {
            // 위에서 충돌 (공이 위에서 내려와서 벽돌 위를 친 경우)
            ball.getPosition().y -= overlapTop;
            v.y = -Math.abs(v.y);
        } else {
            // 아래에서 충돌
            ball.getPosition().y += overlapBottom;
            v.y = Math.abs(v.y);
        }
    }

    // ============================================================
    //  벡터 기반 반사 (필요하면 사용)
    // ============================================================

    /**
     * 주어진 속도 벡터를 법선 벡터(normal)에 대해 반사시킨 결과를 반환한다.
     * normal은 반드시 단위 벡터(정규화된 상태)여야 한다.
     */
    public static Vector2D reflect(Vector2D velocity, Vector2D normal) {
        if (velocity == null || normal == null) return null;

        // r = v - 2(v · n)n
        double dot = velocity.dot(normal);
        return new Vector2D(
                velocity.x - 2 * dot * normal.x,
                velocity.y - 2 * dot * normal.y
        );
    }
}
