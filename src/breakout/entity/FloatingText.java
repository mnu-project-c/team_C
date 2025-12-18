package breakout.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class FloatingText extends GameObject {

    private String text;
    private float alpha = 1.0f;
    private Color color;
    private double speedY = 1.5;

    public FloatingText(double x, double y, String text, Color color) {
        super(x, y, 0, 0);
        this.text = text;
        this.color = color;
    }

    @Override
    public void update() {
        position.y -= speedY;
        alpha -= 0.02f;
        
        if (alpha < 0) alpha = 0;
    }

    @Override
    public void draw(Graphics2D g) {
        if (alpha > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            g.setFont(new Font("Consolas", Font.BOLD, 20));
            g.setColor(Color.BLACK);
            g.drawString(text, (int)position.x + 1, (int)position.y + 1);
            
            g.setColor(color);
            g.drawString(text, (int)position.x, (int)position.y);
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    @Override
    public void onCollision(Collidable other) {}

    public boolean isDead() {
        return alpha <= 0;
    }
}