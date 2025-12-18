package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import breakout.manager.MouseHandler;

public class SettingsPanel {
    private GamePanel panel;

    private GameButton soundButton, prevBgButton, nextBgButton;
    private GameButton ballColorButton, ballSkinButton;
    private GameButton paddleColorButton, paddleShapeButton;
    private GameButton brickColorButton, backButton;

    public SettingsPanel(GamePanel panel) {
        this.panel = panel;
        initButtons();
    }

    private void initButtons() {
        int centerX = GamePanel.WIDTH/2 - 100;
        int setY = 130;
        int setGap = 50;
        soundButton = new GameButton(centerX, setY, 200, 40, "소리: " + (panel.isSoundOn() ? "켜짐" : "꺼짐"));
        prevBgButton = new GameButton(centerX - 110, setY + setGap, 100, 40, "<< 배경");
        nextBgButton = new GameButton(centerX + 210, setY + setGap, 100, 40, "배경 >>");
        ballColorButton = new GameButton(centerX, setY + setGap*2, 200, 40, "공 색상: " + panel.getBallColorName());
        ballSkinButton = new GameButton(centerX, setY + setGap*3, 200, 40, "공 스킨: " + panel.getBallSkinName());
        paddleColorButton = new GameButton(centerX - 110, setY + setGap*4, 200, 40, "판 색상: " + panel.getPaddleColorName());
        paddleShapeButton = new GameButton(centerX + 110, setY + setGap*4, 200, 40, "판 모양: " + panel.getPaddleShapeName());
        brickColorButton = new GameButton(centerX, setY + setGap*5, 200, 40, "벽돌: " + panel.getBrickColorName());
        backButton = new GameButton(centerX, 500, 200, 50, "뒤로가기");
    }

    /**
     * Update returns true when back button is clicked (request to close settings)
     */
    public boolean update(MouseHandler mouseHandler) {
        soundButton.update(mouseHandler);
        prevBgButton.update(mouseHandler);
        nextBgButton.update(mouseHandler);
        ballColorButton.update(mouseHandler);
        ballSkinButton.update(mouseHandler);
        paddleColorButton.update(mouseHandler);
        paddleShapeButton.update(mouseHandler);
        brickColorButton.update(mouseHandler);
        backButton.update(mouseHandler);

        if (soundButton.isClicked(mouseHandler)) {
            panel.toggleSound();
            soundButton = new GameButton(soundButton.bounds.x, soundButton.bounds.y, soundButton.bounds.width, soundButton.bounds.height,
                    "소리: " + (panel.isSoundOn() ? "켜짐" : "꺼짐"));
        }
        if (prevBgButton.isClicked(mouseHandler)) { panel.prevBackground(); }
        if (nextBgButton.isClicked(mouseHandler)) { panel.nextBackground(); }

        if (ballColorButton.isClicked(mouseHandler)) {
            panel.cycleBallColor();
            ballColorButton = new GameButton(ballColorButton.bounds.x, ballColorButton.bounds.y, ballColorButton.bounds.width, ballColorButton.bounds.height,
                    "공 색상: " + panel.getBallColorName());
        }

        if (ballSkinButton.isClicked(mouseHandler)) {
            panel.cycleBallSkin();
            ballSkinButton = new GameButton(ballSkinButton.bounds.x, ballSkinButton.bounds.y, ballSkinButton.bounds.width, ballSkinButton.bounds.height,
                    "공 스킨: " + panel.getBallSkinName());
        }

        if (paddleColorButton.isClicked(mouseHandler)) {
            panel.cyclePaddleColor();
            paddleColorButton = new GameButton(paddleColorButton.bounds.x, paddleColorButton.bounds.y, paddleColorButton.bounds.width, paddleColorButton.bounds.height,
                    "판 색상: " + panel.getPaddleColorName());
        }

        if (paddleShapeButton.isClicked(mouseHandler)) {
            panel.cyclePaddleShape();
            paddleShapeButton = new GameButton(paddleShapeButton.bounds.x, paddleShapeButton.bounds.y, paddleShapeButton.bounds.width, paddleShapeButton.bounds.height,
                    "판 모양: " + panel.getPaddleShapeName());
        }

        if (brickColorButton.isClicked(mouseHandler)) {
            panel.cycleBrickColor();
            brickColorButton = new GameButton(brickColorButton.bounds.x, brickColorButton.bounds.y, brickColorButton.bounds.width, brickColorButton.bounds.height,
                    "벽돌: " + panel.getBrickColorName());
        }

        if (backButton.isClicked(mouseHandler)) {
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g2, Font customFont) {
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 40));
        drawCenteredString(g2, "SETTINGS", GamePanel.WIDTH/2, 100);

        if (customFont != null) g2.setFont(customFont.deriveFont(Font.BOLD, 20f));
        else g2.setFont(new Font("SansSerif", Font.BOLD, 20));

        soundButton.draw(g2, customFont);
        prevBgButton.draw(g2, customFont);
        nextBgButton.draw(g2, customFont);
        ballColorButton.draw(g2, customFont);
        ballSkinButton.draw(g2, customFont);
        paddleColorButton.draw(g2, customFont);
        paddleShapeButton.draw(g2, customFont);
        brickColorButton.draw(g2, customFont);
        backButton.draw(g2, customFont);

        if (panel != null) {
            // draw preview paddle
            if (panel.getPaddle() != null) {
                double oldX = panel.getPaddle().getPosition().x;
                double oldY = panel.getPaddle().getPosition().y;
                panel.getPaddle().getPosition().x = GamePanel.WIDTH / 2 - 50;
                panel.getPaddle().getPosition().y = 450;
                panel.getPaddle().draw(g2);
                panel.getPaddle().getPosition().x = oldX;
                panel.getPaddle().getPosition().y = oldY;
            }
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int tx = x - fm.stringWidth(text) / 2;
        g2.drawString(text, tx, y);
    }
}
