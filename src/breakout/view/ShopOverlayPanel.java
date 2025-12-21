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

    private GameButton buyPaddleBtn, buySlowBtn, buyLifeBtn, buyPierceBtn, buyDoubleBtn, buyLuckyBtn, buyBombBtn, buyMultiBtn, backBtn;

    private static final int PADDLE_PRICE = 200;
    private static final int SLOW_PRICE = 150;
    private static final int LIFE_PRICE = 300;
    private static final int PIERCE_PRICE = 220;
    private static final int DOUBLE_PRICE = 200;
    private static final int LUCKY_PRICE = 250;
    private static final int BOMB_PRICE = 260;
    private static final int MULTI_PRICE = 280;

    private String msg = "";
    private int msgTimer = 0;

    private Image shopBgImage;

    public ShopOverlayPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setOpaque(false);
        setFocusable(true);

        try {
            File file = new File("assets/shop_bg.jpg");
            if (file.exists()) {
                shopBgImage = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initButtons();
    }

    public void initButtons() {
        int rowGap = 65;
        int startY = 200;
        int col1X = GamePanel.WIDTH / 2 - 220;
        int col2X = GamePanel.WIDTH / 2 + 20;

        buyPaddleBtn = new GameButton(col1X, startY, 200, 50, "커져라! >>> " + PADDLE_PRICE);
        buySlowBtn = new GameButton(col2X, startY, 200, 50, "볼 슬로우 >>> " + SLOW_PRICE);
        buyLifeBtn = new GameButton(col1X, startY + rowGap, 200, 50, "하트 추가 >>> " + LIFE_PRICE);
        buyPierceBtn = new GameButton(col2X, startY + rowGap, 200, 50, "관통 볼 10초 >>> " + PIERCE_PRICE);
        buyDoubleBtn = new GameButton(col1X, startY + rowGap * 2, 200, 50, "더블 스코어 >>> " + DOUBLE_PRICE);
        buyLuckyBtn = new GameButton(col2X, startY + rowGap * 2, 200, 50, "럭키 드로우 >>> " + LUCKY_PRICE);
        buyBombBtn = new GameButton(col1X, startY + rowGap * 3, 200, 50, "폭탄볼 x1 >>> " + BOMB_PRICE);
        buyMultiBtn = new GameButton(col2X, startY + rowGap * 3, 200, 50, "멀티볼 x3 >>> " + MULTI_PRICE);
        backBtn = new GameButton(GamePanel.WIDTH / 2 - 100, startY + rowGap * 4 + 10, 200, 50, "뒤로");

        buyPaddleBtn.setSemiTransparentMode(true);
        buySlowBtn.setSemiTransparentMode(true);
        buyLifeBtn.setSemiTransparentMode(true);
        buyPierceBtn.setSemiTransparentMode(true);
        buyDoubleBtn.setSemiTransparentMode(true);
        buyLuckyBtn.setSemiTransparentMode(true);
        buyBombBtn.setSemiTransparentMode(true);
        buyMultiBtn.setSemiTransparentMode(true);
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
        if (!isVisible()) {
            return;
        }

        if (msgTimer > 0) {
            msgTimer--;
        }

        buyPaddleBtn.update(mouseHandler);
        buySlowBtn.update(mouseHandler);
        buyLifeBtn.update(mouseHandler);
        buyPierceBtn.update(mouseHandler);
        buyDoubleBtn.update(mouseHandler);
        buyLuckyBtn.update(mouseHandler);
        buyBombBtn.update(mouseHandler);
        buyMultiBtn.update(mouseHandler);
        backBtn.update(mouseHandler);

        if (buyPaddleBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= PADDLE_PRICE) {
                gamePanel.spendScore(PADDLE_PRICE);
                gamePanel.applyLongPaddleFromShop();
                showMsg("구매완!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buySlowBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= SLOW_PRICE) {
                gamePanel.spendScore(SLOW_PRICE);
                gamePanel.applySlowBallFromShop();
                showMsg("구매완!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buyLifeBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= LIFE_PRICE) {
                gamePanel.spendScore(LIFE_PRICE);
                gamePanel.addLifeFromShop();
                showMsg("하트 +1!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buyPierceBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= PIERCE_PRICE) {
                gamePanel.spendScore(PIERCE_PRICE);
                gamePanel.applyPierceFromShop();
                showMsg("관통 볼 10초!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buyDoubleBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= DOUBLE_PRICE) {
                gamePanel.spendScore(DOUBLE_PRICE);
                gamePanel.applyDoubleScoreFromShop();
                showMsg("더블 스코어 15초!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buyLuckyBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= LUCKY_PRICE) {
                gamePanel.spendScore(LUCKY_PRICE);
                String result = gamePanel.applyLuckyDrawFromShop();
                showMsg(result);
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buyBombBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= BOMB_PRICE) {
                gamePanel.spendScore(BOMB_PRICE);
                gamePanel.applyBombBallFromShop();
                showMsg("BOMB BALL x1!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (buyMultiBtn.isClicked(mouseHandler)) {
            if (gamePanel.getScore() >= MULTI_PRICE) {
                gamePanel.spendScore(MULTI_PRICE);
                gamePanel.applyMultiBallFromShop();
                showMsg("멀티볼 x3!");
            } else {
                showMsg("점수가 모자라요!");
            }
        }

        if (backBtn.isClicked(mouseHandler)) {
            if (onClose != null) {
                onClose.run();
            }
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
        if (!isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        if (shopBgImage != null) {
            g2.drawImage(shopBgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 50));
        String title = "SSAGAL STORE";
        int titleX = GamePanel.WIDTH / 2;
        int titleY = 140;

        g2.setColor(new Color(100, 0, 0));
        for (int i = 5; i > 0; i--) {
            drawCentered(g2, title, titleX + i, titleY + i);
        }

        g2.setColor(Color.RED);
        drawCentered(g2, title, titleX, titleY);

        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.setColor(new Color(255, 215, 0));
        drawCentered(g2, "SCORE: " + gamePanel.getScore(), GamePanel.WIDTH / 2, 190);

        Font btnFont = new Font("SansSerif", Font.BOLD, 24);
        buyPaddleBtn.draw(g2, btnFont);
        buySlowBtn.draw(g2, btnFont);
        buyLifeBtn.draw(g2, btnFont);
        buyPierceBtn.draw(g2, btnFont);
        buyDoubleBtn.draw(g2, btnFont);
        buyLuckyBtn.draw(g2, btnFont);
        buyBombBtn.draw(g2, btnFont);
        buyMultiBtn.draw(g2, btnFont);
        backBtn.draw(g2, btnFont);

        if (msgTimer > 0) {
            if (msg != null) {
                if (!msg.isEmpty()) {
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                    drawCentered(g2, msg, GamePanel.WIDTH / 2, 530);
                }
            }
        }
    }

    private void drawCentered(Graphics2D g2, String text, int x, int y) {
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x - tw / 2, y);
    }
}