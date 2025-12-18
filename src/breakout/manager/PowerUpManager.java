package breakout.manager;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import breakout.entity.ExpandPaddlePowerUp;
import breakout.entity.ExtraLifePowerUp;
import breakout.entity.Paddle;
import breakout.entity.PowerUp;
import breakout.view.GamePanel;

public class PowerUpManager {
    
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Random random = new Random();

    // 벽돌 깨질 때 호출 (20% 확률로 아이템 생성)
    public void maybeSpawn(double x, double y) {
        if (random.nextInt(100) < 20) { // 20% 확률
            int type = random.nextInt(2);
            if (type == 0) {
                powerUps.add(new ExtraLifePowerUp(x, y));
            } else {
                powerUps.add(new ExpandPaddlePowerUp(x, y));
            }
        }
    }

    public void update(GamePanel game, Paddle paddle) {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            p.update();

            // 1. 패들과 충돌 체크
            if (p.getBounds().intersects(paddle.getBounds())) {
                game.getSoundManager().playPowerupSound();
                p.applyEffect(game); // 효과 적용
                it.remove(); // 아이템 제거
                continue;
            }

            // 2. 화면 밖으로 나가면 제거
            if (p.getPosition().y > GamePanel.HEIGHT) {
                it.remove();
            }
        }
    }

    public void draw(Graphics2D g) {
        for (PowerUp p : powerUps) {
            p.draw(g);
        }
    }
    
    public void clear() {
        powerUps.clear();
    }
}