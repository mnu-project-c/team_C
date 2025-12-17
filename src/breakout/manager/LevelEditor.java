package breakout.manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import breakout.entity.Brick;
import breakout.entity.ExplosiveBrick;
import breakout.entity.HardBrick;
import breakout.entity.MediumBrick;
import breakout.entity.NormalBrick;
import breakout.view.GameButton;
import breakout.view.GamePanel;

public class LevelEditor {

    // 에디터용 상수 (그리드 설정)
    private static final int GRID_ROWS = 10;
    private static final int GRID_COLS = 8;
    private static final int CELL_WIDTH = 80;
    private static final int CELL_HEIGHT = 30;
    private static final int PADDING = 5;
    private static final int START_X = (GamePanel.WIDTH - (GRID_COLS * (CELL_WIDTH + PADDING))) / 2;
    private static final int START_Y = 60;

    // 현재 편집 중인 맵 데이터 (0: 빈공간, 1: Normal, 2: Medium, 3: Hard, 4: Explosive)
    private int[][] mapData;

    // 현재 선택된 브러시 타입 (기본: Normal)
    private int currentBrickType = 1; 

    // UI 버튼들
    private GameButton saveButton;
    private GameButton loadButton;
    private GameButton clearButton;
    private GameButton exitButton;
    private GameButton[] typeButtons; // 벽돌 종류 선택 버튼
    
    private String statusMessage = "EDITOR MODE";
    private int messageTimer = 0;

    public LevelEditor() {
        mapData = new int[GRID_ROWS][GRID_COLS];
        initUI();
    }

    private void initUI() {
        // 하단 메뉴 버튼 배치
        int btnY = GamePanel.HEIGHT - 80;
        saveButton = new GameButton(50, btnY, 100, 40, "SAVE");
        loadButton = new GameButton(160, btnY, 100, 40, "LOAD");
        clearButton = new GameButton(270, btnY, 100, 40, "CLEAR");
        exitButton = new GameButton(GamePanel.WIDTH - 150, btnY, 100, 40, "EXIT");

        // 벽돌 타입 선택 버튼 (상단)
        typeButtons = new GameButton[4];
        String[] labels = {"NORMAL", "MEDIUM", "HARD", "BOMB"};
        for (int i = 0; i < 4; i++) {
            typeButtons[i] = new GameButton(50 + i * 110, 10, 100, 30, labels[i]);
        }
    }

    // ★ [수정됨] 매개변수를 MouseHandler 하나만 받도록 변경
    public void update(MouseHandler mouse) {
        updateButtons(mouse);
        handleGridClick(mouse);
        if (messageTimer > 0) messageTimer--;
    }
    
    private void updateButtons(MouseHandler mouse) {
        saveButton.update(mouse);
        loadButton.update(mouse);
        clearButton.update(mouse);
        exitButton.update(mouse);

        if (saveButton.isClicked(mouse)) saveMap();
        if (loadButton.isClicked(mouse)) loadMap();
        if (clearButton.isClicked(mouse)) clearMap();

        // 타입 선택 버튼
        for (int i = 0; i < 4; i++) {
            typeButtons[i].update(mouse);
            if (typeButtons[i].isClicked(mouse)) {
                currentBrickType = i + 1; // 1 ~ 4
                showStatus("Selected: " + getBrickName(currentBrickType));
            }
        }
    }

    private void handleGridClick(MouseHandler mouse) {
        // 마우스 클릭 시 해당 위치의 그리드 좌표 계산
        if (mouse.clicked) {
            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    int x = START_X + c * (CELL_WIDTH + PADDING);
                    int y = START_Y + r * (CELL_HEIGHT + PADDING);
                    
                    Rectangle cellBounds = new Rectangle(x, y, CELL_WIDTH, CELL_HEIGHT);
                    if (cellBounds.contains(mouse.x, mouse.y)) {
                        // 이미 같은 타입이면 지우기(0), 아니면 현재 타입으로 덮어쓰기
                        if (mapData[r][c] == currentBrickType) {
                            mapData[r][c] = 0;
                        } else {
                            mapData[r][c] = currentBrickType;
                        }
                    }
                }
            }
        }
    }
    
    // 파일 저장 (custom_level.txt)
    private void saveMap() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("custom_level.txt"));
            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    writer.write(mapData[r][c] + " ");
                }
                writer.newLine();
            }
            writer.close();
            showStatus("Level Saved!");
        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Save Failed!");
        }
    }

    // 파일 불러오기
    private void loadMap() {
        try {
            File file = new File("custom_level.txt");
            if (!file.exists()) {
                showStatus("No Saved File!");
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (int r = 0; r < GRID_ROWS; r++) {
                String line = reader.readLine();
                if (line == null) break;
                String[] tokens = line.split(" ");
                for (int c = 0; c < GRID_COLS && c < tokens.length; c++) {
                    mapData[r][c] = Integer.parseInt(tokens[c]);
                }
            }
            reader.close();
            showStatus("Level Loaded!");
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Load Failed!");
        }
    }
    
    private void clearMap() {
        for(int r=0; r<GRID_ROWS; r++) {
            for(int c=0; c<GRID_COLS; c++) {
                mapData[r][c] = 0;
            }
        }
        showStatus("Map Cleared");
    }

    public void draw(Graphics2D g) {
        // 1. 배경 (그리드 가이드)
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        
        // 2. 맵 그리드 그리기
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = START_X + c * (CELL_WIDTH + PADDING);
                int y = START_Y + r * (CELL_HEIGHT + PADDING);
                
                // 빈 칸은 테두리만
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                
                // 데이터가 있으면 색칠
                int type = mapData[r][c];
                if (type > 0) {
                    Color color = getColorForType(type);
                    g.setColor(color);
                    g.fillRect(x + 1, y + 1, CELL_WIDTH - 1, CELL_HEIGHT - 1);
                    
                    // 텍스트로 타입 표시 (N, M, H, B)
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    g.drawString(getBrickInitial(type), x + 35, y + 20);
                }
            }
        }

        // 3. UI 버튼 그리기
        saveButton.draw(g);
        loadButton.draw(g);
        clearButton.draw(g);
        exitButton.draw(g);
        
        for (int i = 0; i < 4; i++) {
            // 현재 선택된 타입은 강조 표시
            if (i + 1 == currentBrickType) {
                g.setColor(Color.RED);
                g.drawRect(typeButtons[i].getX()-2, typeButtons[i].getY()-2, 
                           typeButtons[i].getWidth()+4, typeButtons[i].getHeight()+4);
            }
            typeButtons[i].draw(g);
        }

        // 4. 상태 메시지
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 20));
        g.drawString(statusMessage, GamePanel.WIDTH / 2 - 50, GamePanel.HEIGHT - 30);
    }
    
    private Color getColorForType(int type) {
        switch(type) {
            case 1: return Color.YELLOW; // Normal
            case 2: return Color.GREEN;  // Medium
            case 3: return Color.GRAY;   // Hard
            case 4: return Color.RED;    // Explosive
            default: return Color.WHITE;
        }
    }
    
    private String getBrickName(int type) {
        switch(type) {
            case 1: return "Normal";
            case 2: return "Medium";
            case 3: return "Hard";
            case 4: return "Explosive";
            default: return "";
        }
    }
    
    private String getBrickInitial(int type) {
        switch(type) {
            case 1: return "N";
            case 2: return "M";
            case 3: return "H";
            case 4: return "B"; // Bomb
            default: return "";
        }
    }
    
    private void showStatus(String msg) {
        this.statusMessage = msg;
        this.messageTimer = 120; // 2초간 표시
    }
    
    public GameButton getExitButton() {
        return exitButton;
    }
    
    // 저장된 맵 데이터를 Brick 리스트로 변환하여 게임에 반환
    public ArrayList<Brick> getGeneratedBricks() {
        ArrayList<Brick> list = new ArrayList<>();
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int type = mapData[r][c];
                if (type == 0) continue;
                
                int x = START_X + c * (CELL_WIDTH + PADDING);
                int y = START_Y + r * (CELL_HEIGHT + PADDING);
                
                if (type == 1) list.add(new NormalBrick(x, y, CELL_WIDTH, CELL_HEIGHT));
                else if (type == 2) list.add(new MediumBrick(x, y, CELL_WIDTH, CELL_HEIGHT));
                else if (type == 3) list.add(new HardBrick(x, y, CELL_WIDTH, CELL_HEIGHT));
                else if (type == 4) list.add(new ExplosiveBrick(x, y, CELL_WIDTH, CELL_HEIGHT));
            }
        }
        return list;
    }
}