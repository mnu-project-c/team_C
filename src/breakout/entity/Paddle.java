package breakout.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Path2D;

import breakout.manager.InputManager;
import breakout.view.GamePanel;

public class Paddle extends GameObject {

    private InputManager input;
    private double speed = 7.0;
    
    // 원래 크기 저장
    private double originalWidth;
    
    // 패들 색상
    private Color color = Color.CYAN;

    // 패들 모양 상수
    public static final int SHAPE_RECT = 0;   
    public static final int SHAPE_ROUND = 1;  
    public static final int SHAPE_DIAMOND = 2; 
    public static final int SHAPE_WAVE = 3;    
    
    private int shapeType = SHAPE_RECT; // 현재 모양

    public Paddle(double x, double y, InputManager input) {
        super(x, y, 100, 20); // 기본 너비 100
        this.input = input;
        this.originalWidth = 100;
    }

    @Override
    public void update() {
        if (input.left) {
            position.x -= speed;
        }
        if (input.right) {
            position.x += speed;
        }

        // 화면 밖으로 나가지 않게
        if (position.x < 0) position.x = 0;
        if (position.x > GamePanel.WIDTH - width) position.x = GamePanel.WIDTH - width;
    }
    
    public void expand() {
        if (width < 200) { 
            width += 50;
        }
    }
    
    public void resetWidth() {
        width = originalWidth;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setShapeType(int type) {
        this.shapeType = type;
    }
    
    public int getShapeType() {
        return shapeType;
    }

    @Override
    public void draw(Graphics2D g) {
        int x = (int)position.x;
        int y = (int)position.y;
        int w = (int)width;
        int h = (int)height;
        
        // 입체감을 위한 그림자 색상 (기본 색보다 어둡게)
        Color darkColor = color.darker();
        int depth = 8; // 입체 두께를 조금 더 두껍게 (5 -> 8)

        switch (shapeType) {
            case SHAPE_RECT:
                g.setColor(color);
                g.fill3DRect(x, y, w, h, true);
                break;
                
            case SHAPE_ROUND:
                // 1. 그림자 층
                g.setColor(darkColor);
                g.fillRoundRect(x, y + depth, w, h, h, h);
                // 2. 본체 층
                g.setColor(color);
                g.fillRoundRect(x, y, w, h, h, h);
                // 3. 테두리
                g.setColor(new Color(255, 255, 255, 100));
                g.drawRoundRect(x, y, w, h, h, h);
                break;
                
            case SHAPE_DIAMOND:
                
                int[] xPoints = {
                    x,              // 좌하단
                    x,              // 좌측 어깨 (직사각형 부분 위)
                    x + w / 2,      // 중앙 꼭대기 (뾰족한 부분)
                    x + w,          // 우측 어깨
                    x + w           // 우하단
                };
                
                // 1. 그림자 층 (y좌표를 depth만큼 내림)
                int[] yPointsShadow = {
                    y + h + depth,      // 좌하단
                    y + h / 2 + depth,  // 좌측 어깨
                    y - 15 + depth,     // 꼭대기 (위로 15만큼 솟음)
                    y + h / 2 + depth,  // 우측 어깨
                    y + h + depth       // 우하단
                };
                
                // 2. 본체 층
                int[] yPointsMain = {
                    y + h,      
                    y + h / 2,  
                    y - 15,     
                    y + h / 2,  
                    y + h       
                };

                // 그림자 그리기
                g.setColor(darkColor);
                g.fillPolygon(xPoints, yPointsShadow, 5);
                
                // 본체 그리기
                g.setColor(color);
                g.fillPolygon(xPoints, yPointsMain, 5);
                
                // 테두리/하이라이트
                g.setColor(new Color(255, 255, 255, 100));
                g.drawPolygon(xPoints, yPointsMain, 5);
                
                // 입체감을 더하기 위해 앞면과 윗면의 경계선 추가 (선택사항)
                g.drawLine(x, y + h/2, x + w/2, y - 15); // 왼쪽 지붕선
                g.drawLine(x + w, y + h/2, x + w/2, y - 15); // 오른쪽 지붕선
                break;
                
            case SHAPE_WAVE:
                Path2D shadowPath = createWavePath(x, y + depth, w, h);
                Path2D mainPath = createWavePath(x, y, w, h);
                
                g.setColor(darkColor);
                g.fill(shadowPath);
                g.setColor(color);
                g.fill(mainPath);
                g.setColor(new Color(255, 255, 255, 100));
                g.draw(mainPath);
                break;
        }
    }
    
    private Path2D createWavePath(int x, int y, int w, int h) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h);    
        p.lineTo(x, y + h/2);  
        
        for (int i = 0; i <= w; i += 2) {
            double angle = ((double)i / w) * (2 * Math.PI);
            double waveY = Math.sin(angle) * 5.0; 
            p.lineTo(x + i, y + h/2 + waveY);
        }
        
        p.lineTo(x + w, y + h); 
        p.closePath();          
        return p;
    }

    @Override
    public void onCollision(Collidable other) { }
}