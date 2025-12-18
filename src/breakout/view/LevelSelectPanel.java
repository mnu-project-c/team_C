package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import breakout.manager.MouseHandler;

public class LevelSelectPanel {
    private GamePanel panel;
    private GameButton lvl1Button, lvl2Button, lvl3Button, customPlayButton, lvlBackButton;

    public LevelSelectPanel(GamePanel panel) {
        this.panel = panel;
        initButtons();
    }

    private void initButtons() {
        int centerX = GamePanel.WIDTH / 2 - 100;
        lvl1Button = new GameButton(centerX, 200, 200, 50, "1단계");
        lvl2Button = new GameButton(centerX, 270, 200, 50, "2단계");
        lvl3Button = new GameButton(centerX, 340, 200, 50, "3단계");
        customPlayButton = new GameButton(centerX, 410, 200, 50, "커스텀 맵");
        lvlBackButton = new GameButton(centerX, 480, 200, 50, "뒤로가기");
    }

    /**
     * Update returns true when back button was clicked
     */
    public boolean update(MouseHandler mouseHandler) {
        lvl1Button.update(mouseHandler); lvl2Button.update(mouseHandler);
        lvl3Button.update(mouseHandler); customPlayButton.update(mouseHandler);
        lvlBackButton.update(mouseHandler);
        if (lvl1Button.isClicked(mouseHandler)) { panel.startGameWithLevel(1); return false; }
        if (lvl2Button.isClicked(mouseHandler)) { panel.startGameWithLevel(2); return false; }
        if (lvl3Button.isClicked(mouseHandler)) { panel.startGameWithLevel(3); return false; }
        if (customPlayButton.isClicked(mouseHandler)) { panel.startGameWithLevel(0); return false; }
        if (lvlBackButton.isClicked(mouseHandler)) { return true; }
        return false;
    }

    public void draw(Graphics2D g2, Font customFont) {
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 40));
        panel.drawCenteredString(g2, "SELECT LEVEL", GamePanel.WIDTH/2, 120);
        lvl1Button.draw(g2, customFont); lvl2Button.draw(g2, customFont); 
        lvl3Button.draw(g2, customFont); customPlayButton.draw(g2, customFont);
        lvlBackButton.draw(g2, customFont);
    }
}