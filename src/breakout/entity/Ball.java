package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import breakout.engine.Vector2D;
import breakout.view.GamePanel;

public class Ball extends GameObject {
    
    private Vector2D velocity;

    public Ball(double x, double y) {
        super(x, y, 20, 20);
        this.velocity = new Vector2D(4.0, -4.0);
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    @Override
    public void update() {
        position.x += velocity.x;
        position.y += velocity.y;

        // 벽에 튕기는 물리 로직
        if (position.x < 0) {
            position.x = 0;
            velocity.x = -velocity.x;
        }
        if (position.x > GamePanel.WIDTH - width) {
            position.x = GamePanel.WIDTH - width;
            velocity.x = -velocity.x;
        }
        if (position.y < 0) {
            position.y = 0;
            velocity.y = -velocity.y;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Color baseColor = g.getColor();
        int x = (int)position.x;
        int y = (int)position.y;
        int w = (int)width;
        int h = (int)height;
        
        Point2D center = new Point2D.Float(x + w/2, y + h/2);
        Point2D focus = new Point2D.Float(x + w/4, y + h/4);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {Color.WHITE, baseColor};
        
        try {
            RadialGradientPaint p = new RadialGradientPaint(center, w/2, focus, dist, colors, RadialGradientPaint.CycleMethod.NO_CYCLE);
            g.setPaint(p);
            g.fillOval(x, y, w, h);
        } catch (Exception e) {
            g.setColor(baseColor);
            g.fillOval(x, y, w, h);
        }
    }

    @Override
    public void onCollision(Collidable other) {
        velocity.y = -velocity.y;
    }
}