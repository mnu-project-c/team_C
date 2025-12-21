package breakout.manager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import breakout.entity.Brick;
import breakout.entity.HardBrick;
import breakout.entity.MediumBrick;
import breakout.entity.NormalBrick;
import breakout.entity.ExplosiveBrick;
import breakout.entity.MovingBrick;
import breakout.view.GamePanel;

public class MapGenerator {

    public ArrayList<Brick> bricks;
    private int brickWidth = 80;
    private int brickHeight = 30;

    public MapGenerator() {
        bricks = new ArrayList<>();
    }

    // 레벨 번호에 따른 맵 생성 로직
    public void loadLevel(int level) {
        bricks.clear();

        switch (level) {
            case 1:
                createStandardGrid(4, 8);
                break;
            case 2:
                createCheckeredPattern();
                break;
            case 3:
                createPyramid();
                break;
            case 4:
                createHeartShape();
                break;
            case 5:
                createTunnel();
                break;
            case 6:
                createXShape();
                break;
            case 7:
                createInvaders();
                break;
            case 8:
                createDiamond();
                break;
            case 9:
                createZigZag();
                break;
            case 10:
                createDoubleStairs();
                break;
            case 11:
                createSmileFace();
                break;
            case 12:
                createRandomChaos();
                break;
            case 13:
                createIronWall();
                break;
            case 14:
                createMineField();
                break;
            case 15:
                createCircle();
                break;
            case 16:
                createTheEnd();
                break;
            default:
                createStandardGrid(4, 8);
                break;
        }
    }

    private int getStartX(int cols) {
        return (GamePanel.WIDTH - (cols * (brickWidth + 5))) / 2;
    }

    // LV 1: 기본 그리드
    private void createStandardGrid(int rows, int cols) {
        int startX = getStartX(cols);
        int startY = 60;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bricks.add(new NormalBrick(startX + j * 85, startY + i * 35, brickWidth, brickHeight));
            }
        }
    }

    // LV 2: 체크무늬 패턴
    private void createCheckeredPattern() {
        int rows = 6;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i + j) % 2 == 0) {
                    addRandomBrick(startX + j * 85, 60 + i * 35);
                }
            }
        }
    }

    // LV 3: 피라미드 (움직이는 벽돌 포함)
    private void createPyramid() {
        int rows = 7;
        for (int i = 0; i < rows; i++) {
            int colsInRow = i + 2;
            int startX = getStartX(colsInRow);
            for (int j = 0; j < colsInRow; j++) {
                double x = startX + j * 85;
                double y = 50 + i * 35;

                if (i > 4) {
                    bricks.add(new HardBrick(x, y, brickWidth, brickHeight));
                } else if (i == 2 || i == 3) {
                    bricks.add(new MovingBrick(x, y, brickWidth, brickHeight, Color.MAGENTA, 2, 50, 2.0));
                } else if (i > 2) {
                    bricks.add(new MediumBrick(x, y, brickWidth, brickHeight));
                } else {
                    bricks.add(new NormalBrick(x, y, brickWidth, brickHeight));
                }
            }
        }
    }

    // LV 4: 하트 모양
    private void createHeartShape() {
        int[][] heartMap = {
            {0, 1, 1, 0, 0, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 0}
        };
        int startX = getStartX(8);
        for (int i = 0; i < heartMap.length; i++) {
            for (int j = 0; j < 8; j++) {
                if (heartMap[i][j] == 1) {
                    bricks.add(new MediumBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                }
            }
        }
    }

    // LV 5: 터널 형태
    private void createTunnel() {
        int rows = 8;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j < 2 || j >= cols - 2 || i < 2) {
                    bricks.add(new HardBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                }
            }
        }
    }

    // LV 6: X자 형태 (중앙 가로선 움직임)
    private void createXShape() {
        int size = 9;
        int startX = getStartX(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j || i == (size - 1 - j)) {
                    bricks.add(new ExplosiveBrick(startX + j * 85, 50 + i * 35, brickWidth, brickHeight));
                } else if (i == size / 2) {
                    bricks.add(new MovingBrick(startX + j * 85, 50 + i * 35, brickWidth, brickHeight, Color.ORANGE, 2, 40, 3.0));
                } else if (j == size / 2) {
                    bricks.add(new NormalBrick(startX + j * 85, 50 + i * 35, brickWidth, brickHeight));
                }
            }
        }
    }

    // LV 7: 인베이더 패턴
    private void createInvaders() {
        int rows = 6;
        int cols = 9;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j += 2) {
                bricks.add(new MediumBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
            }
        }
    }

    // LV 8: 다이아몬드
    private void createDiamond() {
        int rows = 9;
        int cols = 9;
        int mid = 4;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            int range;
            if (i <= mid) {
                range = i;
            } else {
                range = rows - 1 - i;
            }
            for (int j = mid - range; j <= mid + range; j++) {
                bricks.add(new NormalBrick(startX + j * 85, 50 + i * 35, brickWidth, brickHeight));
            }
        }
    }

    // LV 9: 지그재그 (교차 이동 벽돌)
    private void createZigZag() {
        int rows = 7;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)) {
                    if (i % 2 == 0) {
                        bricks.add(new MovingBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight, Color.PINK, 2, 30, 1.5));
                    } else {
                        bricks.add(new MediumBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                    }
                }
            }
        }
    }

    // LV 10: 이중 계단
    private void createDoubleStairs() {
        int rows = 8;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            bricks.add(new HardBrick(startX + i * 85, 50 + i * 35, brickWidth, brickHeight));
            bricks.add(new HardBrick(startX + (cols - 1 - i) * 85, 50 + i * 35, brickWidth, brickHeight));
        }
    }

    // LV 11: 스마일 페이스
    private void createSmileFace() {
        int[][] face = {
            {0, 0, 1, 1, 1, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 1, 0},
            {1, 0, 2, 0, 0, 2, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 1, 0, 1},
            {0, 1, 0, 1, 1, 0, 1, 0},
            {0, 0, 1, 0, 0, 1, 0, 0}
        };
        int startX = getStartX(8);
        for (int i = 0; i < face.length; i++) {
            for (int j = 0; j < 8; j++) {
                if (face[i][j] == 1) {
                    bricks.add(new NormalBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                } else if (face[i][j] == 2) {
                    bricks.add(new ExplosiveBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                }
            }
        }
    }

    // LV 12: 랜덤 배치 및 이동 벽돌
    private void createRandomChaos() {
        int rows = 6;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() > 0.3) {
                    if (Math.random() < 0.15) {
                        bricks.add(new MovingBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight, Color.RED, 2, 40, 2.5));
                    } else {
                        addRandomBrick(startX + j * 85, 60 + i * 35);
                    }
                }
            }
        }
    }

    // LV 13: 철벽 그리드
    private void createIronWall() {
        int rows = 5;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bricks.add(new HardBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
            }
        }
    }

    // LV 14: 지뢰밭
    private void createMineField() {
        int rows = 6;
        int cols = 8;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < 0.4) {
                    bricks.add(new ExplosiveBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                } else {
                    bricks.add(new HardBrick(startX + j * 85, 60 + i * 35, brickWidth, brickHeight));
                }
            }
        }
    }

    // LV 15: 원형 패턴 (중앙 띠 이동)
    private void createCircle() {
        int centerX = GamePanel.WIDTH / 2;
        int centerY = 200;
        int radius = 180;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 14; j++) {
                int x = (GamePanel.WIDTH - (14 * 55)) / 2 + j * 55;
                int y = 50 + i * 35;

                double dist = Math.sqrt(Math.pow(x + brickWidth / 2 - centerX, 2) + Math.pow(y + brickHeight / 2 - centerY, 2));

                if (dist < radius && dist > radius - 100) {
                    if (i == 4 || i == 5) {
                        bricks.add(new MovingBrick(x, y, 50, 30, Color.CYAN, 2, 30, 2.0));
                    } else {
                        bricks.add(new MediumBrick(x, y, 50, 30));
                    }
                }
            }
        }
    }

    // LV 16: 최종 단계
    private void createTheEnd() {
        int rows = 10;
        int cols = 9;
        int startX = getStartX(cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double r = Math.random();
                if (r < 0.1) {
                    bricks.add(new ExplosiveBrick(startX + j * 85, 40 + i * 35, brickWidth, brickHeight));
                } else if (r < 0.4) {
                    bricks.add(new HardBrick(startX + j * 85, 40 + i * 35, brickWidth, brickHeight));
                } else if (r < 0.7) {
                    bricks.add(new MediumBrick(startX + j * 85, 40 + i * 35, brickWidth, brickHeight));
                } else {
                    bricks.add(new NormalBrick(startX + j * 85, 40 + i * 35, brickWidth, brickHeight));
                }
            }
        }
    }

    private void addRandomBrick(int x, int y) {
        double r = Math.random();
        if (r < 0.1) {
            bricks.add(new ExplosiveBrick(x, y, brickWidth, brickHeight));
        } else if (r < 0.3) {
            bricks.add(new MediumBrick(x, y, brickWidth, brickHeight));
        } else {
            bricks.add(new NormalBrick(x, y, brickWidth, brickHeight));
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