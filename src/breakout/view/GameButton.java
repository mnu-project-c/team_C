package breakout.view;

import java.awt.AlphaComposite; // 추가됨
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite; // 추가됨
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import breakout.manager.MouseHandler;
import breakout.manager.SoundManager;

public class GameButton {
    
    public Rectangle bounds;
    public String text;
    private boolean isHovered = false;
    
    // ★ 반투명 모드 플래그
    private boolean semiTransparentMode = false;
    
    private final Color goldMain = new Color(255, 215, 0);
    private final Color goldDark = new Color(184, 134, 11);

    public GameButton(int x, int y, int width, int height, String text) {
        this.bounds = new Rectangle(x, y, width, height);
        this.text = text;
    }
    
    // ★ 반투명 모드 설정 메소드
    public void setSemiTransparentMode(boolean enable) {
        this.semiTransparentMode = enable;
    }

    public void update(MouseHandler mouse) {
        if (bounds.contains(mouse.x, mouse.y)) {
            isHovered = true;
        } else {
            isHovered = false;
        }
    }

    public boolean isClicked(MouseHandler mouse) {
        if (isHovered && mouse.isPressed) {
            mouse.isPressed = false;
            // 중앙에서 클릭 사운드를 재생하도록 처리하여, 버튼 클릭시 항상 소리가 나게 함
            try { SoundManager.playClick(); } catch (Exception e) {}
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g, Font customFont) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // ★ 반투명 처리 로직
        Composite originalComposite = g.getComposite();
        if (semiTransparentMode && !isHovered) {
            // 마우스가 올라가지 않았을 때 반투명 (50%)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        } else {
            // 마우스가 올라갔거나 기본 모드일 때 불투명 (100%)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        // 1. 버튼 배경
        GradientPaint gp;
        if (isHovered) {
            gp = new GradientPaint(bounds.x, bounds.y, Color.WHITE, bounds.x, bounds.y + bounds.height, goldMain);
        } else {
            gp = new GradientPaint(bounds.x, bounds.y, goldMain, bounds.x, bounds.y + bounds.height, goldDark);
        }
        g.setPaint(gp);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15);
        
        // 2. 하이라이트
        g.setColor(new Color(255, 255, 255, 100));
        g.fillRoundRect(bounds.x + 5, bounds.y + 5, bounds.width - 10, bounds.height / 2 - 5, 10, 10);

        // 3. 테두리
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(255, 255, 220)); 
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15);
        
        // 4. 텍스트 (폰트 적용)
        if (customFont != null) {
            g.setFont(customFont.deriveFont(Font.BOLD, 20f));
        } else {
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        
        g.setColor(new Color(100, 70, 0)); 
        drawCenteredString(g, text, bounds.x + bounds.width / 2 + 1, bounds.y + bounds.height / 2 + 1);
        
        if (isHovered) g.setColor(new Color(139, 69, 19));
        else g.setColor(Color.WHITE);
        
        drawCenteredString(g, text, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        
        // ★ Composite 복구 (이후 그려질 요소들을 위해 필수)
        g.setComposite(originalComposite);
    }
    
    private void drawCenteredString(Graphics2D g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textX = x - fm.stringWidth(text) / 2;
        int textY = y + (fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, textX, textY);
    }
}