package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import breakout.entity.Paddle;
import breakout.manager.MouseHandler;

public class SettingsPanel {
    
    private GamePanel gamePanel;
    private GameButton backButton;
    
    
    private GameButton soundButton;
    private GameButton bgPrevButton, bgNextButton;
    private GameButton ballColorButton;
    private GameButton ballSkinButton;
    private GameButton paddleColorButton;
    private GameButton paddleShapeButton;
    private GameButton brickColorButton;
    
    
    private GameButton crtFilterButton;

    
    private int brickY;       
    private int previewY;     
    private int previewTextY; 

    public SettingsPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        initUI();
    }
    
    private void initUI() {
        int centerX = GamePanel.WIDTH / 2;
        
        int halfBtnWidth = 180;
        int fullBtnWidth = 240;
        int height = 40;
        int gap = 20;
        
        
        int startY = 100; 
        int rowStep = 55;
        
        
        int bgY = startY;
        bgPrevButton = new GameButton(centerX - 120, bgY, 60, height, "◀");
        bgNextButton = new GameButton(centerX + 60, bgY, 60, height, "▶");
        
        
        int soundFilterY = bgY + rowStep;
        soundButton = new GameButton(centerX - gap/2 - halfBtnWidth, soundFilterY, halfBtnWidth, height, "소리: ON");
        crtFilterButton = new GameButton(centerX + gap/2, soundFilterY, halfBtnWidth, height, "CRT 필터: OFF");
        
        
        int ballY = soundFilterY + rowStep;
        ballColorButton = new GameButton(centerX - gap/2 - halfBtnWidth, ballY, halfBtnWidth, height, "공 색상: 빨강");
        ballSkinButton = new GameButton(centerX + gap/2, ballY, halfBtnWidth, height, "공 스킨: 없음");
        
        
        int paddleY = ballY + rowStep;
        paddleColorButton = new GameButton(centerX - gap/2 - halfBtnWidth, paddleY, halfBtnWidth, height, "패들 색상: 하늘");
        paddleShapeButton = new GameButton(centerX + gap/2, paddleY, halfBtnWidth, height, "패들 모양: 기본");
        
        
        this.previewTextY = paddleY + 90; // 약 355
        
        
        this.previewY = previewTextY + 70; // 약 425
        
        
        this.brickY = previewY + 50; // 약 475
        brickColorButton = new GameButton(centerX - fullBtnWidth/2, brickY, fullBtnWidth, height, "벽돌 색상: 노랑");
        
        
        backButton = new GameButton(centerX - 100, 550, 200, 40, "뒤로 가기");
    }
    
    public boolean update(MouseHandler mouse) {
        backButton.update(mouse);
        if (backButton.isClicked(mouse)) return true;
        
        bgPrevButton.update(mouse);
        bgNextButton.update(mouse);
        if (bgPrevButton.isClicked(mouse)) gamePanel.prevBackground();
        if (bgNextButton.isClicked(mouse)) gamePanel.nextBackground();
        
        soundButton.update(mouse);
        if (soundButton.isClicked(mouse)) gamePanel.toggleSound();
        soundButton.setText("소리: " + (gamePanel.isSoundOn() ? "ON" : "OFF"));
        
        crtFilterButton.update(mouse);
        if (crtFilterButton.isClicked(mouse)) gamePanel.toggleCRTFilter();
        crtFilterButton.setText("CRT 필터: " + (gamePanel.isCRTFilterOn() ? "ON" : "OFF"));
        
        ballColorButton.update(mouse);
        if (ballColorButton.isClicked(mouse)) gamePanel.cycleBallColor();
        ballColorButton.setText("공 색상: " + gamePanel.getBallColorName());
        
        ballSkinButton.update(mouse);
        if (ballSkinButton.isClicked(mouse)) gamePanel.cycleBallSkin();
        ballSkinButton.setText("공 스킨: " + gamePanel.getBallSkinName());
        
        paddleColorButton.update(mouse);
        if (paddleColorButton.isClicked(mouse)) gamePanel.cyclePaddleColor();
        paddleColorButton.setText("패들 색상: " + gamePanel.getPaddleColorName());
        
        paddleShapeButton.update(mouse);
        if (paddleShapeButton.isClicked(mouse)) gamePanel.cyclePaddleShape();
        paddleShapeButton.setText("패들 모양: " + gamePanel.getPaddleShapeName());
        
        brickColorButton.update(mouse);
        if (brickColorButton.isClicked(mouse)) gamePanel.cycleBrickColor();
        brickColorButton.setText("벽돌 색상: " + gamePanel.getBrickColorName());
        
        return false;
    }
    
    public void draw(Graphics2D g, Font font) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        
        g.setColor(Color.WHITE);
        if (font != null) g.setFont(font.deriveFont(Font.BOLD, 40f));
        int titleWidth = g.getFontMetrics().stringWidth("설정");
        g.drawString("설정", GamePanel.WIDTH/2 - titleWidth/2, 60);
        
        // 배경화면
        bgPrevButton.draw(g, font);
        bgNextButton.draw(g, font);
        g.setFont(font.deriveFont(Font.BOLD, 16f));
        String bgText = "배경화면";
        int bgTw = g.getFontMetrics().stringWidth(bgText);
        g.drawString(bgText, GamePanel.WIDTH/2 - bgTw/2, bgPrevButton.bounds.y + 27);
        
        // 소리 & 필터
        soundButton.draw(g, font);
        crtFilterButton.draw(g, font);
        
        // 공 & 패들 버튼
        ballColorButton.draw(g, font);
        ballSkinButton.draw(g, font); 
        paddleColorButton.draw(g, font);
        paddleShapeButton.draw(g, font);
        
        // 미리보기 텍스트
        g.setColor(new Color(255, 255, 255, 100));
        g.setFont(font.deriveFont(Font.BOLD, 14f));
        String previewText = "▼ 미리보기 ▼";
        int ptWidth = g.getFontMetrics().stringWidth(previewText);
        g.drawString(previewText, GamePanel.WIDTH/2 - ptWidth/2, previewTextY); 
        
        // 미리보기 그림 (패들 & 공)
        Paddle previewPaddle = gamePanel.getPaddle();
        if (previewPaddle != null) {
            double oldX = previewPaddle.getPosition().x;
            double oldY = previewPaddle.getPosition().y;
            
            // 패들 그리기
            previewPaddle.getPosition().x = GamePanel.WIDTH / 2 - previewPaddle.getWidth() / 2;
            previewPaddle.getPosition().y = previewY; 
            previewPaddle.draw(g);
            
            // 공 그리기
            int ballSize = 20; 
            int ballX = GamePanel.WIDTH / 2 - ballSize / 2;
            int ballY = previewY - 45; 
            
            g.setColor(new java.awt.Color(255, 255, 255, 50));
            g.fillOval(ballX+2, ballY+2, ballSize, ballSize);
            
            g.setColor(gamePanel.getCurrentBallColor());
            g.fillOval(ballX, ballY, ballSize, ballSize);
            
            
            previewPaddle.getPosition().x = oldX;
            previewPaddle.getPosition().y = oldY;
        }

        // 벽돌 버튼
        brickColorButton.draw(g, font);
        
        // 뒤로 가기
        backButton.draw(g, font);
    }
}