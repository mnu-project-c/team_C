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

    private static final int GRID_ROWS = 10;
    private static final int GRID_COLS = 8;
    private static final int CELL_WIDTH = 80;
    private static final int CELL_HEIGHT = 30;
    private static final int PADDING = 5;
    private static final int START_X = (800 - (GRID_COLS * (CELL_WIDTH + PADDING))) / 2; 
    private static final int START_Y = 60;

    private int[][] mapData;
    private int currentBrickType = 1; 

    private GameButton saveButton, loadButton, clearButton, exitButton;
    private GameButton[] typeButtons;
    
    private String statusMessage = "EDITOR MODE"; // 영어 상태 메시지
    private int messageTimer = 0;

    public LevelEditor() {
        mapData = new int[GRID_ROWS][GRID_COLS];
        initUI();
    }

    private void initUI() {
        int btnY = 600 - 80;
        // ★ 버튼은 한글 유지
        saveButton = new GameButton(50, btnY, 100, 40, "저장");
        loadButton = new GameButton(160, btnY, 100, 40, "불러오기");
        clearButton = new GameButton(270, btnY, 100, 40, "초기화");
        exitButton = new GameButton(800 - 150, btnY, 100, 40, "나가기");

        typeButtons = new GameButton[4];
        String[] labels = {"일반", "중간", "단단", "폭탄"}; // 버튼 라벨 한글
        for (int i = 0; i < 4; i++) {
            typeButtons[i] = new GameButton(50 + i * 110, 10, 100, 30, labels[i]);
        }
    }

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

        for (int i = 0; i < 4; i++) {
            typeButtons[i].update(mouse);
            if (typeButtons[i].isClicked(mouse)) {
                currentBrickType = i + 1;
                // 상태 메시지는 영어
                showStatus("Selected: " + getBrickName(currentBrickType));
            }
        }
    }

    private void handleGridClick(MouseHandler mouse) {
        if (mouse.clicked) {
            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    int x = START_X + c * (CELL_WIDTH + PADDING);
                    int y = START_Y + r * (CELL_HEIGHT + PADDING);
                    
                    Rectangle cellBounds = new Rectangle(x, y, CELL_WIDTH, CELL_HEIGHT);
                    if (cellBounds.contains(mouse.x, mouse.y)) {
                        mapData[r][c] = currentBrickType; 
                    }
                }
            }
        }
    }
    
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
            showStatus("Level Saved!"); // 영어
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadMap() {
        try {
            File file = new File("custom_level.txt");
            if (!file.exists()) { showStatus("No File!"); return; } // 영어
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
            showStatus("Level Loaded!"); // 영어
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void clearMap() {
        for(int r=0; r<GRID_ROWS; r++) for(int c=0; c<GRID_COLS; c++) mapData[r][c] = 0;
        showStatus("Map Cleared"); // 영어
    }

    public void draw(Graphics2D g, Font customFont) {
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, 800, 600);
        
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = START_X + c * (CELL_WIDTH + PADDING);
                int y = START_Y + r * (CELL_HEIGHT + PADDING);
                
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                
                int type = mapData[r][c];
                if (type > 0) {
                    g.setColor(getColorForType(type));
                    g.fillRect(x + 1, y + 1, CELL_WIDTH - 1, CELL_HEIGHT - 1);
                    
                    g.setColor(Color.BLACK);
                    if(customFont != null) g.setFont(customFont.deriveFont(12f));
                    else g.setFont(new Font("SansSerif", Font.BOLD, 12));
                    
                    g.drawString(getBrickInitial(type), x + 35, y + 20);
                }
            }
        }

        saveButton.draw(g, customFont);
        loadButton.draw(g, customFont);
        clearButton.draw(g, customFont);
        exitButton.draw(g, customFont);
        
        for (int i = 0; i < 4; i++) {
            if (i + 1 == currentBrickType) {
                g.setColor(Color.RED);
                g.drawRect(typeButtons[i].bounds.x-2, typeButtons[i].bounds.y-2, 
                           typeButtons[i].bounds.width+4, typeButtons[i].bounds.height+4);
            }
            typeButtons[i].draw(g, customFont);
        }

        g.setColor(Color.YELLOW);
        if(customFont != null) g.setFont(customFont.deriveFont(20f));
        g.drawString(statusMessage, 400 - 50, 570);
    }
    
    private Color getColorForType(int type) {
        switch(type) {
            case 1: return Color.YELLOW; 
            case 2: return Color.GREEN;  
            case 3: return Color.GRAY;   
            case 4: return Color.RED;    
            default: return Color.WHITE;
        }
    }
    
    private String getBrickName(int type) {
        switch(type) {
            case 1: return "Normal"; case 2: return "Medium"; case 3: return "Hard"; case 4: return "Explosive"; default: return "";
        }
    }
    
    private String getBrickInitial(int type) {
        switch(type) {
            case 1: return "N"; case 2: return "M"; case 3: return "H"; case 4: return "B"; default: return "";
        }
    }
    
    private void showStatus(String msg) {
        this.statusMessage = msg;
        this.messageTimer = 120; 
    }
    
    public GameButton getExitButton() { return exitButton; }
    
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