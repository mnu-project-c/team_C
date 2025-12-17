package breakout.manager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import breakout.entity.Brick;
import breakout.entity.HardBrick;
import breakout.entity.MediumBrick;
import breakout.entity.NormalBrick;

import breakout.view.GamePanel;

public class MapGenerator {
    
    public ArrayList<Brick> bricks;
    private int brickWidth = 80;
    private int brickHeight = 30;
    private int padding = 5;

    public MapGenerator() {
        bricks = new ArrayList<>();
        // 생성 시에는 비워두고 GamePanel에서 loadLevel 호출
    }
    
    // ★ 레벨에 따라 다른 맵 생성
    public void loadLevel(int level) {
        bricks.clear(); // 기존 벽돌 제거
        
        switch(level) {
            case 1: createLevel1(); break;
            case 2: createLevel2(); break;
            case 3: createLevel3(); break;
            default: createLevel1(); break;
        }
    }
    
    // Level 1: 기본 4줄 배치
    private void createLevel1() {
        int rows = 4;
        int cols = 8;
        int startX = (GamePanel.WIDTH - (cols * (brickWidth + padding))) / 2;
        int startY = 60;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bricks.add(new NormalBrick(startX + j * (brickWidth + padding),
                                           startY + i * (brickHeight + padding),
                                           brickWidth, brickHeight));
            }
        }
    }
    
    // Level 2: 체크무늬 배치 (구멍 뚫린 맵, 폭발 벽돌 추가)
    private void createLevel2() {
        int rows = 6;
        int cols = 8;
        int startX = (GamePanel.WIDTH - (cols * 85)) / 2;
        int startY = 60;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 체크무늬 위치에만 벽돌 생성
                if ((i + j) % 2 == 0) {
                    
                    double x = startX + j * 85;
                    double y = startY + i * 35;
                    
                    // 0.0 ~ 1.0 사이의 난수 생성
                    double chance = Math.random();

                    // ★ 확률 설정 구간
                    // 10% 확률 (0.0 ~ 0.1) -> 폭발 벽돌
                    if (chance < 0.1) {
                        bricks.add(new breakout.entity.ExplosiveBrick(x, y, 80, 30));
                    } 
                    // 30% 확률 (0.1 ~ 0.4) -> 중간 벽돌 (Medium)
                    else if (chance < 0.4) {
                        bricks.add(new MediumBrick(x, y, 80, 30));
                    } 
                    // 나머지 60% (0.4 ~ 1.0) -> 일반 벽돌
                    else {
                        bricks.add(new NormalBrick(x, y, 80, 30));
                    }
                }
            }
        }
    }
    
    // Level 3: 랜덤 배치 & 피라미드
    private void createLevel3() {
        int rows = 7;
        int startY = 50;
        
        for (int i = 0; i < rows; i++) {
            int colsInRow = i + 2; 
            int totalWidth = colsInRow * (80 + 5); 
            int startX = (GamePanel.WIDTH - totalWidth) / 2;
            
            for (int j = 0; j < colsInRow; j++) {
                double x = startX + j * 85;
                double y = startY + i * 35;
                
                // 1. 가장 바깥쪽 줄(맨 아래 or 양옆)은 일반 벽돌 (Normal)
                if (i == rows - 1 || j == 0 || j == colsInRow - 1) {
                    bricks.add(new NormalBrick(x, y, 80, 30));
                }
                // 2. 그 바로 안쪽 줄은 중간 벽돌 (Medium)
                // (맨 아래에서 두번째 줄 or 양옆에서 두번째 칸)
                else if (i == rows - 2 || j == 1 || j == colsInRow - 2) {
                    bricks.add(new MediumBrick(x, y, 80, 30));
                }
                // 3. 나머지(가장 깊은 안쪽)는 단단한 벽돌 (Hard)
                else {
                    bricks.add(new HardBrick(x, y, 80, 30));
                }
            }
        }
    }
    
    public void draw(Graphics2D g) {
        for (Brick b : bricks) {
            if (!b.isDestroyed) {
                b.draw(g);
            }
        }
    }
}