package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

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

    public ShopOverlayPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setOpaque(false);
        setFocusable(true);

        int centerX = GamePanel.WIDTH / 2 - 100;

        buyPaddleBtn = new GameButton(centerX, 240, 200, 50, "LONG PADDLE - " + PADDLE_PRICE);
        buySlowBtn   = new GameButton(centerX, 310, 200, 50, "SLOW BALL - " + SLOW_PRICE);
        buyLifeBtn   = new GameButton(centerX, 380, 200, 50, "EXTRA LIFE - " + LIFE_PRICE);
        backBtn      = new GameButton(centerX, 470, 200, 50, "BACK");
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void refreshTexts() {
        // 필요하면 버튼 텍스트 갱신하는 방식으로 확장 가능
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

        // 구매
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

        // 반투명 배경
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 타이틀/점수
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 42));
        drawCentered(g2, "SHOP", GamePanel.WIDTH / 2, 140);

        g2.setFont(new Font("Consolas", Font.BOLD, 24));
        drawCentered(g2, "SCORE: " + gamePanel.getScore(), GamePanel.WIDTH / 2, 190);

        // 버튼들
        buyPaddleBtn.draw(g2);
        buySlowBtn.draw(g2);
        buyLifeBtn.draw(g2);
        backBtn.draw(g2);

        // 메시지
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