package kr.ac.mnu.c_team.breakout.view;

import javax.swing.JFrame;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("C-Team Breakout Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // 패널 생성 및 추가
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack(); // 패널 크기에 맞춰 창 크기 자동 조절
        
        setLocationRelativeTo(null); // 화면 중앙 배치
        setVisible(true);
        
        // 게임 루프 시작
        gamePanel.startGame();
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}