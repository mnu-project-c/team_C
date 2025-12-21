package breakout.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import breakout.manager.MouseHandler;

public class LevelSelectPanel {

    private GamePanel panel;
    private List<GameButton> levelButtons;
    private GameButton customPlayButton, lvlBackButton;

    public LevelSelectPanel(GamePanel panel) {
        this.panel = panel;
        initButtons();
    }

    private void initButtons() {
        levelButtons = new ArrayList<>();

        int cols = 4;
        int rows = 4;
        int btnWidth = 100;
        int btnHeight = 50;
        int gap = 20;

        int totalWidth = (cols * btnWidth) + ((cols - 1) * gap);
        int startX = (GamePanel.WIDTH - totalWidth) / 2;
        int startY = 150;

        for (int i = 0; i < 16; i++) {
            int r = i / cols;
            int c = i % cols;

            int x = startX + c * (btnWidth + gap);
            int y = startY + r * (btnHeight + gap);

            levelButtons.add(new GameButton(x, y, btnWidth, btnHeight, "LV " + (i + 1)));
        }

        int centerX = GamePanel.WIDTH / 2 - 100;
        customPlayButton = new GameButton(centerX, 450, 200, 50, "커스텀 맵");
        lvlBackButton = new GameButton(centerX, 510, 200, 50, "뒤로가기");
    }

    public boolean update(MouseHandler mouseHandler) {
        for (int i = 0; i < levelButtons.size(); i++) {
            GameButton btn = levelButtons.get(i);
            btn.update(mouseHandler);
            
            if (btn.isClicked(mouseHandler)) {
                panel.startGameWithLevel(i + 1);
                return false;
            }
        }

        customPlayButton.update(mouseHandler);
        lvlBackButton.update(mouseHandler);

        if (customPlayButton.isClicked(mouseHandler)) {
            panel.startGameWithLevel(0);
            return false;
        }
        
        if (lvlBackButton.isClicked(mouseHandler)) {
            return true;
        }

        return false;
    }

    public void draw(Graphics2D g2, Font customFont) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));
        panel.drawCenteredString(g2, "SELECT LEVEL", GamePanel.WIDTH / 2, 100);

        for (GameButton btn : levelButtons) {
            btn.draw(g2, customFont);
        }

        customPlayButton.draw(g2, customFont);
        lvlBackButton.draw(g2, customFont);
    }
}