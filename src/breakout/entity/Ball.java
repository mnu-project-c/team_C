package breakout.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import breakout.engine.Vector2D;
import breakout.view.GamePanel;

public class Ball extends GameObject { 
    
    private Vector2D velocity;
    private final double SPEED = 5.0;
    private Image skin;
    
    private List<Vector2D> trailHistory = new ArrayList<>();
    private int maxTrailSize = 10; 
    
    public Ball(double x, double y) {
        super(x, y, 30, 30); 
        velocity = new Vector2D(3, -SPEED);
    }

    public Vector2D getVelocity() { return velocity; }
    public void setVelocity(Vector2D v) { this.velocity = v; }
    public void reverseX() { velocity.x = -velocity.x; }
    public void reverseY() { velocity.y = -velocity.y; }
    public void setSkin(Image skin) { this.skin = skin; } // 이 메서드가 정상 인식되어야 함

    @Override
    public void update() {
        // 1. 위치 업데이트
        position.x += velocity.x;
        position.y += velocity.y;

       // if (position.x < 0) { position.x = 0; velocity.x = -velocity.x; }
       // if (position.x > GamePanel.WIDTH - width) { position.x = GamePanel.WIDTH - width; velocity.x = -velocity.x; }
       // if (position.y < 0) { position.y = 0; velocity.y = -velocity.y; }
    }

    @Override
    public void draw(Graphics2D g) {
        int x = (int)position.x;
        int y = (int)position.y;
        int w = (int)width;
        int h = (int)height;
        
        trailHistory.add(new Vector2D(position.x, position.y));
        if (trailHistory.size() > maxTrailSize) {
            trailHistory.remove(0);
        }
         java.awt.Composite originalComposite = g.getComposite();
        
        // 1. 잔상 그리기
        for (int i = 0; i < trailHistory.size(); i++) {
            Vector2D pos = trailHistory.get(i);
            float alpha = (float) (i + 1) / (maxTrailSize + 5); 
            if (alpha > 1.0f) alpha = 1.0f;
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
            
            int trailSize = (int)width - (maxTrailSize - i); 
            if (trailSize < 5) trailSize = 5;
            
            double drawX = pos.x + (width - trailSize) / 2;
            double drawY = pos.y + (height - trailSize) / 2;
            
            g.fillOval((int)drawX, (int)drawY, trailSize, trailSize);
        }
        
        g.setComposite(originalComposite); // 투명도 복구
    

    

        // 2. 공 본체 그리기
        if (skin != null) {
            Shape originalClip = g.getClip();
            Ellipse2D circleClip = new Ellipse2D.Double(position.x, position.y, width, height);
            
            g.setClip(circleClip); 
            g.drawImage(skin, (int)position.x, (int)position.y, (int)width, (int)height, null);
            g.setClip(originalClip); 
            
            g.setColor(new Color(0, 0, 0, 50));
            g.drawOval((int)position.x, (int)position.y, (int)width, (int)height);
        } else {
            g.fillOval((int)position.x, (int)position.y, (int)width, (int)height);
        }
    }

    @Override
    public void onCollision(Collidable other) {
        if (other instanceof Paddle) {
             velocity.y = -Math.abs(velocity.y);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)position.x, (int)position.y, (int)width, (int)height);
    }
}