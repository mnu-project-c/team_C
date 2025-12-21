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

        shopOverlay.addMouseListener(gamePanel.getMouseHandler());
        shopOverlay.addMouseMotionListener(gamePanel.getMouseHandler());

        gamePanel.setShopOverlay(shopOverlay);

        gamePanel.setShopOpener(() -> {
            gamePanel.pauseForOverlay();
            shopOverlay.refreshTexts();
            shopOverlay.setVisible(true);
            shopOverlay.requestFocusInWindow();
        });

        shopOverlay.setOnClose(() -> {
            shopOverlay.setVisible(false);
            gamePanel.resumeFromOverlay();
            gamePanel.requestFocusInWindow();
        });

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        
        Dimension paneSize = new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT);
        layeredPane.setPreferredSize(paneSize);
        
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(shopOverlay, JLayeredPane.MODAL_LAYER);

        add(layeredPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });
        
        gamePanel.startGame();
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}