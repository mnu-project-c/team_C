package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JPanel;

import breakout.manager.MouseHandler;

public class ShopOverlayPanel extends JPanel {

    private final GamePanel gamePanel;
    private Runnable onClose = null;

    private GameButton buyPaddleBtn, buySlowBtn, buyLifeBtn, backBtn;

    private static final int PADDLE_PRICE = 200;
    private static final int SLOW_PRICE   = 150;
    private static final int LIFE_PRICE   = 300;

    private String msg = "";
    private int msgTimer = 0;
    
    private Image shopBgImage;

    public ShopOverlayPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setOpaque(false);
        setFocusable(true);
        
        // 상점 배경 이미지 로드 (shop_bg.jpg)
        try {
            File file = new File("assets/shop_bg.jpg");
            if (file.exists()) {
                shopBgImage = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int centerX = GamePanel.WIDTH / 2 - 100;

        buyPaddleBtn = new GameButton(centerX, 240, 200, 50, "LONG PADDLE - " + PADDLE_PRICE);
        buySlowBtn   = new GameButton(centerX, 310, 200, 50, "SLOW BALL - " + SLOW_PRICE);
        buyLifeBtn   = new GameButton(centerX, 380, 200, 50, "EXTRA LIFE - " + LIFE_PRICE);
        backBtn      = new GameButton(centerX, 470, 200, 50, "BACK");
        
        // ★ 버튼 반투명 모드 활성화 (평소엔 투명, 마우스 올리면 선명)
        buyPaddleBtn.setSemiTransparentMode(true);
        buySlowBtn.setSemiTransparentMode(true);
        buyLifeBtn.setSemiTransparentMode(true);
        backBtn.setSemiTransparentMode(true);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void refreshTexts() {
        msg = "";
        msgTimer = 0;
        repaint();
    }

    public void updateOverlay(MouseHandler mouseHandler) {
        if (!isVisible()) return;

        if (msgTimer > 0) msgTimer--;

        buyPaddleBtn.update(mouseHandler);
        buySlowBtn.update(mouseHandler);
        buyLifeBtn.update(mouseHandler);
        backBtn.update(mouseHandler);

        // 구매 로직
        if (buyPaddleBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= PADDLE_PRICE) {
                gamePanel.spendScore(PADDLE_PRICE);
                gamePanel.applyLongPaddleFromShop();
                showMsg("PURCHASED!");
            } else showMsg("NOT ENOUGH SCORE!");
        }

        if (buySlowBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= SLOW_PRICE) {
                gamePanel.spendScore(SLOW_PRICE);
                gamePanel.applySlowBallFromShop();
                showMsg("PURCHASED!");
            } else showMsg("NOT ENOUGH SCORE!");
        }

        if (buyLifeBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= LIFE_PRICE) {
                gamePanel.spendScore(LIFE_PRICE);
                gamePanel.addLifeFromShop();
                showMsg("LIFE +1!");
            } else showMsg("NOT ENOUGH SCORE!");
        }

        if (backBtn.isClicked(mouseHandler)) {
            if (onClose != null) onClose.run();
        }
        
        repaint();
    }

    private void showMsg(String m) {
        msg = m;
        msgTimer = 90;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) return;

        Graphics2D g2 = (Graphics2D) g;

        // 배경 그리기
        if (shopBgImage != null) {
            g2.drawImage(shopBgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // ★ 1. 타이틀: "SSAGAL STORE" (3D 스타일, 정열적인 빨강)
        g2.setFont(new Font("Consolas", Font.BOLD, 50));
        
        String title = "SSAGAL STORE";
        int titleX = GamePanel.WIDTH / 2;
        int titleY = 140;

        // 그림자 (검붉은색으로 여러 겹 그려 입체감 표현)
        g2.setColor(new Color(100, 0, 0));
        for (int i = 5; i > 0; i--) {
            drawCentered(g2, title, titleX + i, titleY + i);
        }
        
        // 메인 텍스트 (정열적인 빨강)
        g2.setColor(Color.RED);
        drawCentered(g2, title, titleX, titleY);

        // ★ 2. 점수: "SCORE" (골드 색상)
        g2.setFont(new Font("Consolas", Font.BOLD, 28));
        g2.setColor(new Color(255, 215, 0)); // Gold Color
        drawCentered(g2, "SCORE: " + gamePanel.getScore(), GamePanel.WIDTH / 2, 190);

        // 버튼들 그리기
        Font btnFont = new Font("Consolas", Font.BOLD, 24);

        buyPaddleBtn.draw(g2, btnFont);
        buySlowBtn.draw(g2, btnFont);
        buyLifeBtn.draw(g2, btnFont);
        backBtn.draw(g2, btnFont);

        // 메시지 표시
        if (msgTimer > 0 && msg != null && !msg.isEmpty()) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Consolas", Font.BOLD, 22));
            drawCentered(g2, msg, GamePanel.WIDTH / 2, 530);
        }
    }

    private void drawCentered(Graphics2D g2, String text, int x, int y) {
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x - tw / 2, y);
    }
}