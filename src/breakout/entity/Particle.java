package breakout.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Particle extends GameObject {

    private double velX, velY;
    private float alpha = 1.0f;
    private float life = 0.02f;
    private Color color;
    
    private double gravity = 0.4;
    private double rotation = 0;
    private double rotSpeed;

    public Particle(double x, double y, Color color) {
        super(x, y, 8, 8);
        this.color = color;
        
        this.velX = (Math.random() * 10) - 5;
        this.velY = (Math.random() * 10) - 8;
        
        this.rotSpeed = (Math.random() * 0.4) - 0.2;
    }

    @Override
    public void update() {
        velY += gravity;
        
        position.x += velX;
        position.y += velY;
        
        rotation += rotSpeed;
        alpha -= life;
    }

    @Override
    public void draw(Graphics2D g) {
        if (alpha > 0) {
            AffineTransform oldTransform = g.getTransform();
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            g.translate(position.x + width/2, position.y + height/2);
            g.rotate(rotation);
            g.translate(-(position.x + width/2), -(position.y + height/2));
            
            g.setColor(color);
            g.fillRect((int)position.x, (int)position.y, (int)width, (int)height);
            
            g.setTransform(oldTransform);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public boolean isDead() { return alpha <= 0; }
    
    @Override
    public void onCollision(Collidable other) {}
}