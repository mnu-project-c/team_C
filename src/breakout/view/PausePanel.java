package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import breakout.manager.MouseHandler;

public class PausePanel {

    private GamePanel panel;
    private GameButton resumeButton, shopButton, pauseSettingsButton, menuButton;

    public PausePanel(GamePanel panel) {
        this.panel = panel;
        initButtons();
    }

    private void initButtons() {
        int centerX = GamePanel.WIDTH / 2 - 100;
        
        resumeButton = new GameButton(centerX, 280, 200, 50, "계속하기");
        shopButton = new GameButton(centerX, 340, 200, 50, "상점");
        pauseSettingsButton = new GameButton(centerX, 400, 200, 50, "설정");
        menuButton = new GameButton(centerX, 460, 200, 50, "메인 메뉴");
    }

    public void update(MouseHandler mouseHandler) {
        resumeButton.update(mouseHandler);
        shopButton.update(mouseHandler);
        pauseSettingsButton.update(mouseHandler);
        menuButton.update(mouseHandler);

        if (resumeButton.isClicked(mouseHandler)) {
            panel.resumeFromPause();
        }
        
        if (shopButton.isClicked(mouseHandler)) {
            panel.openShop();
        }
        
        if (pauseSettingsButton.isClicked(mouseHandler)) {
            panel.openSettingsFromPause();
        }
        
        if (menuButton.isClicked(mouseHandler)) {
            panel.gotoMenuFromPause();
        }
    }

    public void draw(Graphics2D g2, Font customFont) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 60));
        panel.drawCenteredString(g2, "*COWARD*", GamePanel.WIDTH / 2, 200);

        resumeButton.draw(g2, customFont);
        shopButton.draw(g2, customFont);
        pauseSettingsButton.draw(g2, customFont);
        menuButton.draw(g2, customFont);
    }
}