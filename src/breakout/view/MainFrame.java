package breakout.view;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("C-Team Breakout Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        gamePanel.setBounds(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        ShopOverlayPanel shopOverlay = new ShopOverlayPanel(gamePanel);
        shopOverlay.setBounds(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        shopOverlay.setVisible(false);

        gamePanel.setShopOverlay(shopOverlay);

        // GamePanel이 "상점 열기" 요청하면 MainFrame이 오버레이를 띄워줌
        gamePanel.setShopOpener(() -> {
            gamePanel.pauseForOverlay();     // 게임 업데이트 멈춤
            shopOverlay.refreshTexts();      // 가격/점수 표시 최신화
            shopOverlay.setVisible(true);
            shopOverlay.requestFocusInWindow();
        });

        // ShopOverlay가 닫힐 때 처리
        shopOverlay.setOnClose(() -> {
            shopOverlay.setVisible(false);
            gamePanel.resumeFromOverlay();
            gamePanel.requestFocusInWindow();
        });

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT));
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(shopOverlay, JLayeredPane.MODAL_LAYER);

        add(layeredPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
        gamePanel.startGame();
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}