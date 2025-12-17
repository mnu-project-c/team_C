package breakout.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import breakout.engine.Vector2D;

public abstract class GameObject implements Collidable {
    protected Vector2D position;
    protected double width, height;

    public GameObject(double x, double y, double width, double height) {
        this.position = new Vector2D(x, y);
        this.width = width;
        this.height = height;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g);

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)position.x, (int)position.y, (int)width, (int)height);
    }
    
    // ★ 이 Getter들이 꼭 있어야 합니다!
    public Vector2D getPosition() { return position; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}