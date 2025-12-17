package breakout.manager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

import breakout.entity.Particle;

public class EffectManager {
    
    private ArrayList<Particle> particles = new ArrayList<>();

    // 벽돌이 깨질 때 호출할 메소드
    public void createExplosion(double x, double y, Color color) {
        // 파편 15개 생성
        for (int i = 0; i < 15; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update();
            if (p.isDead()) {
                it.remove(); // 수명이 다한 파티클 제거 (메모리 관리)
            }
        }
    }

    public void draw(Graphics2D g) {
        for (Particle p : particles) {
            p.draw(g);
        }
    }
}
