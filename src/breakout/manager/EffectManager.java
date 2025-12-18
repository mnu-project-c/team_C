package breakout.manager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import breakout.entity.FloatingText;
import breakout.entity.Particle;

public class EffectManager {
    
    private List<Particle> particles = new ArrayList<>();
    private List<FloatingText> texts = new ArrayList<>();

    public void createExplosion(double x, double y, Color color) {
        for (int i = 0; i < 20; i++) {
            particles.add(new Particle(x, y, color));
        }
    }
    
    public void addFloatingText(double x, double y, String msg, Color color) {
        texts.add(new FloatingText(x, y, msg, color));
    }

    public void update() {
        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            Particle p = pIt.next();
            p.update();
            if (p.isDead()) pIt.remove();
        }
        
        Iterator<FloatingText> tIt = texts.iterator();
        while (tIt.hasNext()) {
            FloatingText t = tIt.next();
            t.update();
            if (t.isDead()) tIt.remove();
        }
    }

    public void draw(Graphics2D g) {
        for (Particle p : particles) p.draw(g);
        for (FloatingText t : texts) t.draw(g);
    }
}