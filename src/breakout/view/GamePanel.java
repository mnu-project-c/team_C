package breakout.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import breakout.engine.CollisionDetector;
import breakout.entity.Ball;
import breakout.entity.Brick;
import breakout.entity.Paddle;
import breakout.manager.EffectManager;
import breakout.manager.InputManager;
import breakout.manager.LevelEditor;
import breakout.manager.MapGenerator;
import breakout.manager.MouseHandler;
import breakout.manager.PowerUpManager;
import breakout.manager.ScoreManager;
import breakout.manager.SoundManager;

public class GamePanel extends JPanel implements Runnable {
    
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    // 상태 상수
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAY = 1;
    public static final int STATE_GAME_OVER = 2;
    public static final int STATE_VICTORY = 3;
    public static final int STATE_SETTINGS = 4;
    public static final int STATE_PAUSED = 5;
    public static final int STATE_LEVEL_SELECT = 6;
    public static final int STATE_EDITOR = 7;
    
    private Thread gameThread;
    private boolean running = false;
    private final int FPS = 60;
    
    // Managers
    private InputManager inputManager;
    private MouseHandler mouseHandler;
    private EffectManager effectManager;
    private ScoreManager scoreManager;
    private PowerUpManager powerUpManager;
    private SoundManager soundManager;
    private LevelEditor levelEditor;
    private MapGenerator mapGenerator;
    
    // Entities
    private Paddle paddle;
    private Ball ball;
    
    // UI Buttons
    private GameButton startButton, settingsButton, exitButton;
    private GameButton restartButton, menuButton;
    private GameButton soundButton, backButton;
    private GameButton resumeButton;
    private GameButton prevBgButton, nextBgButton;
    private GameButton ballColorButton, brickColorButton;
    private GameButton lvl1Button, lvl2Button, lvl3Button, lvlBackButton;
    private GameButton editorButton;
    private GameButton customPlayButton;
    
    // Game State Variables
    private int gameState = STATE_MENU;
    private int score = 0;
    private int lives = 3; 
    private boolean isSoundOn = true;
    private int shakeTimer = 0;
    private boolean wasEscPressed = false;
    
    // ★ 콤보 시스템 변수
    private int comboCount = 0;       // 현재 콤보
    private float comboScale = 1.0f;  // 콤보 텍스트 크기 애니메이션용
    
    // Resources
    private BufferedImage[] backgrounds;
    private int currentBgIndex = 0;
    private Image menuGifImage; 
    
    // Fonts
    private Font mainFont;      
    private Font titleFont;     
    private Font subTitleFont;  
    
    private final Color[] colorList = { Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.WHITE };
    private final String[] colorNames = { "RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "PURPLE", "WHITE" };
    private int ballColorIndex = 0; 
    private int brickColorIndex = 2;
    
    private int currentLevel = 1;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDoubleBuffered(true);
        setFocusable(true);
        requestFocus();
        
        inputManager = new InputManager();
        addKeyListener(inputManager);
        
        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        
        effectManager = new EffectManager();
        scoreManager = new ScoreManager();
        powerUpManager = new PowerUpManager();
        soundManager = new SoundManager(); 
        soundManager.playBGM("bgm1.wav"); 
        
        loadResources();
        
        initGameObjects(); 
        initUI();          
        applyCustomColors();
    }
    
    private void loadResources() {
        backgrounds = new BufferedImage[6];
        for(int i=0; i<6; i++) {
            File file = new File("assets/bg" + (i+1) + ".jpg");
            if (file.exists()) {
                try { backgrounds[i] = ImageIO.read(file); } 
                catch (Exception e) { System.out.println("배경 로드 실패: " + file.getName()); }
            }
        }
        
        try {
            File gifFile = new File("assets/main_bg.gif");
            if (gifFile.exists()) menuGifImage = Toolkit.getDefaultToolkit().createImage(gifFile.getAbsolutePath());
        } catch (Exception e) {}
        
        try {
            File fontFile = new File("assets/DungGeunMo.ttf");
            if (fontFile.exists()) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
                mainFont = baseFont.deriveFont(Font.BOLD, 12f);
                titleFont = baseFont.deriveFont(Font.BOLD, 70f);
                subTitleFont = baseFont.deriveFont(Font.BOLD, 30f);
            } else {
                useDefaultFonts();
            }
        } catch (Exception e) { useDefaultFonts(); }
    }
    
    private void useDefaultFonts() {
        mainFont = new Font("SansSerif", Font.BOLD, 12);
        titleFont = new Font("SansSerif", Font.BOLD, 70);
        subTitleFont = new Font("SansSerif", Font.BOLD, 30);
    }
    
    private void initUI() {
        int centerX = WIDTH / 2 - 100;
        startButton = new GameButton(centerX, 250, 200, 50, "GAME START");
        settingsButton = new GameButton(centerX, 320, 200, 50, "SETTINGS");
        editorButton = new GameButton(centerX, 390, 200, 50, "LEVEL EDITOR");
        exitButton = new GameButton(centerX, 460, 200, 50, "EXIT GAME");

        restartButton = new GameButton(centerX, 350, 200, 50, "TRY AGAIN");
        menuButton = new GameButton(centerX, 420, 200, 50, "MAIN MENU");
        resumeButton = new GameButton(centerX, 300, 200, 50, "RESUME");
        
        soundButton = new GameButton(centerX, 150, 200, 50, "SOUND: ON");
        prevBgButton = new GameButton(centerX - 110, 230, 100, 50, "<< BG");
        nextBgButton = new GameButton(centerX + 210, 230, 100, 50, "BG >>");
        ballColorButton = new GameButton(centerX, 310, 200, 50, "BALL: RED");
        brickColorButton = new GameButton(centerX, 390, 200, 50, "BRICK: YELLOW");
        backButton = new GameButton(centerX, 500, 200, 50, "BACK");
        
        lvl1Button = new GameButton(centerX, 200, 200, 50, "LEVEL 1");
        lvl2Button = new GameButton(centerX, 270, 200, 50, "LEVEL 2");
        lvl3Button = new GameButton(centerX, 340, 200, 50, "LEVEL 3");
        customPlayButton = new GameButton(centerX, 410, 200, 50, "CUSTOM MAP");
        lvlBackButton = new GameButton(centerX, 480, 200, 50, "BACK");
        
        levelEditor = new LevelEditor();
    }
    
    private void initGameObjects() {
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        mapGenerator = new MapGenerator();
        scoreManager.loadHighScore();
    }
    
    private void applyCustomColors() {
        Color targetBrickColor = colorList[brickColorIndex];
        for (Brick b : mapGenerator.bricks) {
            if (b instanceof breakout.entity.NormalBrick) b.color = targetBrickColor;
        }
    }
    
    private void startGameWithLevel(int level) {
        currentLevel = level;
        resetGame();
    }

    private void resetGame() {
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        if (currentLevel != 0) mapGenerator.loadLevel(currentLevel);
        else mapGenerator.bricks = levelEditor.getGeneratedBricks();
        
        powerUpManager.clear();
        applyCustomColors();
        score = 0; lives = 3; shakeTimer = 0; 
        comboCount = 0; // ★ 게임 시작 시 콤보 초기화
        gameState = STATE_PLAY;
    }
    
    private void resetRound() {
        paddle.resetWidth();
        paddle.getPosition().x = WIDTH / 2 - 50; 
        paddle.getPosition().y = HEIGHT - 60;
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        powerUpManager.clear();
        comboCount = 0; // ★ 라운드 리셋(죽음) 시 콤보 초기화
        try { Thread.sleep(500); } catch (Exception e) {}
    }
    
    public void addLife() { lives++; }
    public void expandPaddle() { paddle.expand(); }
    public void startShake(int duration) { this.shakeTimer = duration; }
    
    public void startGame() {
        if (gameThread == null) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while(running) {
            update(); 
            repaint(); 
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000;
                if(remainingTime < 0) remainingTime = 0;
                Thread.sleep((long)remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
    
    private void update() {
        if (shakeTimer > 0) shakeTimer--;
        
        // ★ 콤보 애니메이션: 글자 크기가 서서히 원래대로(1.0) 돌아옴
        if (comboScale > 1.0f) comboScale -= 0.05f;

        inputManager.update();
        effectManager.update(); 
        
        switch (gameState) {
            case STATE_MENU: updateMenu(); break;
            case STATE_LEVEL_SELECT: updateLevelSelect(); break;
            case STATE_PLAY: updatePlay(); break;
            case STATE_PAUSED: updatePaused(); break;
            case STATE_GAME_OVER:
            case STATE_VICTORY: updateResult(); break;
            case STATE_SETTINGS: updateSettings(); break;
            case STATE_EDITOR: updateEditor(); break;
        }
    }
    
    private void updateMenu() {
        startButton.update(mouseHandler); settingsButton.update(mouseHandler);
        exitButton.update(mouseHandler); editorButton.update(mouseHandler);
        if (startButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_LEVEL_SELECT; }
        if (settingsButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_SETTINGS; }
        if (editorButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_EDITOR; }
        if (exitButton.isClicked(mouseHandler)) { soundManager.playClickSound(); System.exit(0); }
    }
    
    private void updateLevelSelect() {
        lvl1Button.update(mouseHandler); lvl2Button.update(mouseHandler);
        lvl3Button.update(mouseHandler); customPlayButton.update(mouseHandler);
        lvlBackButton.update(mouseHandler);
        if (lvl1Button.isClicked(mouseHandler)) { soundManager.playClickSound(); startGameWithLevel(1); }
        if (lvl2Button.isClicked(mouseHandler)) { soundManager.playClickSound(); startGameWithLevel(2); }
        if (lvl3Button.isClicked(mouseHandler)) { soundManager.playClickSound(); startGameWithLevel(3); }
        if (customPlayButton.isClicked(mouseHandler)) { soundManager.playClickSound(); startGameWithLevel(0); }
        if (lvlBackButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_MENU; }
    }
    
    private void updateEditor() {
        levelEditor.update(mouseHandler);
        if (levelEditor.getExitButton().isClicked(mouseHandler)) {
            soundManager.playClickSound(); gameState = STATE_MENU;
        }
    }

    private void updatePlay() {
        if (inputManager.escape && !wasEscPressed) {
            soundManager.playClickSound(); gameState = STATE_PAUSED; wasEscPressed = true; return;
        }
        if (!inputManager.escape) wasEscPressed = false;
        
        paddle.update();
        ball.update();
        powerUpManager.update(this, paddle);
        
        // 1. 패들 충돌
        if (CollisionDetector.isColliding(ball, paddle)) {
            CollisionDetector.handlePaddleCollision(ball, paddle);
            if (ball.getVelocity().y < 0) { 
                startShake(5);
                soundManager.playHitSound();
                comboCount = 0; // ★ 패들에 닿으면 콤보 끊김!
            }
        }
        
        // 2. 벽돌 충돌
        for (Brick brick : mapGenerator.bricks) {
            if (!brick.isDestroyed) {
                if (ball.getBounds().intersects(brick.getBounds())) {
                    CollisionDetector.resolveBallVsRect(ball, brick);
                    brick.hit();
                    
                    // ★ 콤보 증가 및 애니메이션 트리거
                    comboCount++;
                    comboScale = 1.8f; // 글자를 1.8배로 키움 (Pop 효과)
                    
                    // 점수에 콤보 보너스 추가 (콤보당 +10점)
                    int bonus = (comboCount > 1) ? (comboCount * 10) : 0;
                    score += (brick.scoreValue + bonus);
                    
                    if (brick.isDestroyed) {
                        soundManager.playExplodeSound();
                        if (brick instanceof breakout.entity.ExplosiveBrick) triggerExplosion(brick);
                        effectManager.createExplosion(brick.getPosition().x+40, brick.getPosition().y+15, brick.color);
                        powerUpManager.maybeSpawn(brick.getPosition().x+40, brick.getPosition().y+15);
                        startShake(5); 
                    } else {
                        soundManager.playHitSound();
                    }
                    break; 
                }
            }
        }
        
        // 3. 공 떨어짐
        if (ball.getPosition().y > HEIGHT) {
            lives--;
            startShake(20);
            if (lives > 0) {
                soundManager.playFailSound();
                resetRound(); // 여기서 comboCount = 0 됨
            } else {
                soundManager.playFailSound();
                gameState = STATE_GAME_OVER;
                scoreManager.saveHighScore(score);
            }
        }
        
        if (mapGenerator.bricks.stream().noneMatch(b -> !b.isDestroyed)) {
            gameState = STATE_VICTORY;
            scoreManager.saveHighScore(score);
        }
    }
    
    private void updatePaused() {
        resumeButton.update(mouseHandler); menuButton.update(mouseHandler);
        if (resumeButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_PLAY; }
        if (menuButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_MENU; }
    }
    
    private void updateResult() {
        restartButton.update(mouseHandler); menuButton.update(mouseHandler);
        if (restartButton.isClicked(mouseHandler)) { soundManager.playClickSound(); startGameWithLevel(currentLevel); }
        if (menuButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_MENU; }
    }
    
    private void updateSettings() {
        soundButton.update(mouseHandler); prevBgButton.update(mouseHandler);
        nextBgButton.update(mouseHandler); ballColorButton.update(mouseHandler);
        brickColorButton.update(mouseHandler); backButton.update(mouseHandler);
        
        if (soundButton.isClicked(mouseHandler)) {
            soundManager.playClickSound(); isSoundOn = !isSoundOn; soundManager.setMute(!isSoundOn);
            soundButton = new GameButton(WIDTH/2-100, 150, 200, 50, "SOUND: " + (isSoundOn?"ON":"OFF"));
        }
        if (prevBgButton.isClicked(mouseHandler)) { soundManager.playClickSound(); currentBgIndex=(currentBgIndex-1+6)%6; changeBackgroundBGM(); }
        if (nextBgButton.isClicked(mouseHandler)) { soundManager.playClickSound(); currentBgIndex=(currentBgIndex+1)%6; changeBackgroundBGM(); }
        if (ballColorButton.isClicked(mouseHandler)) { soundManager.playClickSound(); ballColorIndex=(ballColorIndex+1)%colorList.length; ballColorButton=new GameButton(WIDTH/2-100,310,200,50,"BALL: "+colorNames[ballColorIndex]); }
        if (brickColorButton.isClicked(mouseHandler)) { soundManager.playClickSound(); brickColorIndex=(brickColorIndex+1)%colorList.length; brickColorButton=new GameButton(WIDTH/2-100,390,200,50,"BRICK: "+colorNames[brickColorIndex]); applyCustomColors(); }
        if (backButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_MENU; }
    }
    
    private void changeBackgroundBGM() {
        if (isSoundOn) soundManager.playBGM("bgm" + (currentBgIndex + 1) + ".wav");
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D dbg = (Graphics2D) g;
        dbg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (gameState != STATE_MENU) {
            if (backgrounds != null && backgrounds[currentBgIndex] != null) dbg.drawImage(backgrounds[currentBgIndex], 0, 0, WIDTH, HEIGHT, null);
            else { dbg.setColor(Color.BLACK); dbg.fillRect(0, 0, WIDTH, HEIGHT); }
        }
        
        int sx = 0, sy = 0;
        if (shakeTimer > 0) { sx=(int)(Math.random()*10-5); sy=(int)(Math.random()*10-5); dbg.translate(sx, sy); }
        
        switch (gameState) {
            case STATE_MENU: drawMenu(dbg); break;
            case STATE_LEVEL_SELECT: drawLevelSelect(dbg); break;
            case STATE_PLAY:
                mapGenerator.draw(dbg); paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg);
                effectManager.draw(dbg); powerUpManager.draw(dbg);
                drawHUD(dbg);
                break;
            case STATE_PAUSED:
                mapGenerator.draw(dbg); paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg);
                drawPause(dbg);
                break;
            case STATE_GAME_OVER:
                mapGenerator.draw(dbg); paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg);
                effectManager.draw(dbg); drawResult(dbg, "GAME OVER", Color.RED);
                break;
            case STATE_VICTORY:
                mapGenerator.draw(dbg); paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg);
                effectManager.draw(dbg); drawResult(dbg, "STAGE CLEAR!", Color.GREEN);
                break;
            case STATE_SETTINGS: drawSettings(dbg); break;
            case STATE_EDITOR: levelEditor.draw(dbg); break;
        }
        
        if (sx != 0 || sy != 0) dbg.translate(-sx, -sy);
        if (gameState == STATE_MENU) Toolkit.getDefaultToolkit().sync(); 
    }
    
    private void draw3DText(Graphics2D g2, String text, int x, int y, Color mainColor, Color shadowColor, Font font) {
        g2.setFont(font); g2.setColor(shadowColor);
        for(int i=5; i>0; i--) g2.drawString(text, x+i, y+i);
        g2.setColor(mainColor); g2.drawString(text, x, y);
    }
    
    private void drawMenu(Graphics2D g2) {
        if (menuGifImage != null) g2.drawImage(menuGifImage, 0, 0, WIDTH, HEIGHT, this);
        else { g2.setColor(Color.BLACK); g2.fillRect(0, 0, WIDTH, HEIGHT); }
        
        draw3DText(g2, "샤갈적인 벽돌깨기", WIDTH/2 - 280, 150, Color.YELLOW, Color.DARK_GRAY, titleFont);
        draw3DText(g2, "[학생회의 반란]", WIDTH/2 - 130, 210, Color.WHITE, Color.BLACK, subTitleFont);
        g2.setColor(Color.CYAN); g2.setFont(new Font("Consolas", Font.BOLD, 20));
        drawCenteredString(g2, "HIGH SCORE: " + scoreManager.getHighScore(), WIDTH/2, 550);
        startButton.draw(g2); settingsButton.draw(g2); exitButton.draw(g2); editorButton.draw(g2);
    }
    
    private void drawLevelSelect(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 40));
        drawCenteredString(g2, "SELECT LEVEL", WIDTH/2, 120);
        lvl1Button.draw(g2); lvl2Button.draw(g2); lvl3Button.draw(g2); customPlayButton.draw(g2); lvlBackButton.draw(g2);
    }
    
    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 100)); g2.fillRect(0, 0, WIDTH, 40);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 24));
        g2.drawString("SCORE: " + score, 20, 28);
        g2.drawString("LIVES:", WIDTH - 180, 28);
        for (int i = 0; i < lives; i++) drawHeart(g2, WIDTH - 100 + (i * 30), 10);
        
        // ★ 콤보 텍스트 그리기 (2콤보 이상일 때)
        if (comboCount >= 2) {
            int fontSize = (int)(40 * comboScale); // 콤보 높을수록, 방금 터졌을수록 커짐
            g2.setFont(new Font("Consolas", Font.BOLD, fontSize));
            
            // 콤보 색상 변화 (노랑 -> 주황 -> 빨강)
            if (comboCount < 5) g2.setColor(Color.YELLOW);
            else if (comboCount < 10) g2.setColor(Color.ORANGE);
            else g2.setColor(Color.RED);
            
            String comboText = comboCount + " COMBO!";
            int tw = g2.getFontMetrics().stringWidth(comboText);
            // 화면 중앙 상단 (점수 아래쪽)에 표시
            g2.drawString(comboText, WIDTH/2 - tw/2, 80);
        }
    }
    
    private void drawHeart(Graphics2D g2, int x, int y) {
        g2.setColor(Color.RED); g2.fillOval(x, y, 10, 10); g2.fillOval(x + 10, y, 10, 10); 
        int[] xp = {x, x + 10, x + 20}; int[] yp = {y + 5, y + 20, y + 5}; g2.fillPolygon(xp, yp, 3);
    }
    
    private void drawSettings(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 40));
        drawCenteredString(g2, "SETTINGS", WIDTH/2, 100);
        soundButton.draw(g2); g2.setFont(new Font("Arial", Font.BOLD, 15)); g2.setColor(Color.LIGHT_GRAY);
        drawCenteredString(g2, "Select Background (" + (currentBgIndex + 1) + "/6)", WIDTH/2, 220);
        prevBgButton.draw(g2); nextBgButton.draw(g2); ballColorButton.draw(g2); brickColorButton.draw(g2); backButton.draw(g2);
    }
    
    private void drawPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.ORANGE); g2.setFont(new Font("Arial", Font.BOLD, 50));
        drawCenteredString(g2, "PAUSED", WIDTH/2, 200);
        resumeButton.draw(g2); menuButton.draw(g2);
    }
    
    private void drawResult(Graphics2D g2, String title, Color color) {
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(color); g2.setFont(new Font("Arial", Font.BOLD, 50));
        drawCenteredString(g2, title, WIDTH/2, 200);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 30));
        drawCenteredString(g2, "Final Score: " + score, WIDTH/2, 280);
        restartButton.draw(g2); menuButton.draw(g2);
    }
    
    private void drawCenteredString(Graphics2D g, String text, int x, int y) {
        int tw = g.getFontMetrics().stringWidth(text); g.drawString(text, x - tw / 2, y);
    }
    
    private void triggerExplosion(Brick centerBrick) {
        int ex = (int) (centerBrick.getPosition().x - centerBrick.getWidth());
        int ey = (int) (centerBrick.getPosition().y - centerBrick.getHeight());
        Rectangle area = new Rectangle(ex, ey, (int)centerBrick.getWidth()*3, (int)centerBrick.getHeight()*3);
        for (Brick b : mapGenerator.bricks) {
            if (!b.isDestroyed && b != centerBrick && area.intersects(b.getBounds())) {
                b.hit(); 
                if (b.isDestroyed) {
                    score += b.scoreValue;
                    effectManager.createExplosion(b.getPosition().x+40, b.getPosition().y+15, b.color);
                }
            }
        }
        startShake(20); 
    }
}