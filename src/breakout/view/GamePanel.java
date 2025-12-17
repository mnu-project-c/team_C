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
    
    // Resources
    private BufferedImage[] backgrounds;
    private int currentBgIndex = 0;
    private Image menuGifImage; 
    
    // ★ 최적화: 폰트 객체 캐싱 (매 프레임 생성 방지)
    private Font mainFont;      // 기본 폰트
    private Font titleFont;     // 큰 제목용 (70px)
    private Font subTitleFont;  // 부제목용 (30px)
    
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
        
        // 1. 배경 로드
        for(int i=0; i<6; i++) {
            File file = new File("assets/bg" + (i+1) + ".jpg");
            if (file.exists()) {
                try { backgrounds[i] = ImageIO.read(file); } 
                catch (Exception e) { System.out.println("배경 로드 실패: " + file.getName()); }
            }
        }
        
        // 2. GIF 로드
        try {
            File gifFile = new File("assets/main_bg.gif");
            if (gifFile.exists()) {
                menuGifImage = Toolkit.getDefaultToolkit().createImage(gifFile.getAbsolutePath());
            }
        } catch (Exception e) {}
        
        // 3. 폰트 로드 및 캐싱 (★ 최적화 핵심)
        try {
            File fontFile = new File("assets/DungGeunMo.ttf");
            if (fontFile.exists()) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
                
                // 미리 파생 폰트 생성
                mainFont = baseFont.deriveFont(Font.BOLD, 12f);
                titleFont = baseFont.deriveFont(Font.BOLD, 70f);
                subTitleFont = baseFont.deriveFont(Font.BOLD, 30f);
            } else {
                useDefaultFonts();
            }
        } catch (Exception e) {
            useDefaultFonts();
        }
    }
    
    private void useDefaultFonts() {
        mainFont = new Font("SansSerif", Font.BOLD, 12);
        titleFont = new Font("SansSerif", Font.BOLD, 70);
        subTitleFont = new Font("SansSerif", Font.BOLD, 30);
    }
    
    private void initUI() {
        int centerX = WIDTH / 2 - 100;
        
        // 메인 메뉴
        startButton = new GameButton(centerX, 250, 200, 50, "GAME START");
        settingsButton = new GameButton(centerX, 320, 200, 50, "SETTINGS");
        editorButton = new GameButton(centerX, 390, 200, 50, "LEVEL EDITOR");
        exitButton = new GameButton(centerX, 460, 200, 50, "EXIT GAME");

        // 일시정지 및 결과 화면
        restartButton = new GameButton(centerX, 350, 200, 50, "TRY AGAIN");
        menuButton = new GameButton(centerX, 420, 200, 50, "MAIN MENU");
        resumeButton = new GameButton(centerX, 300, 200, 50, "RESUME");
        
        // 설정
        soundButton = new GameButton(centerX, 150, 200, 50, "SOUND: ON");
        prevBgButton = new GameButton(centerX - 110, 230, 100, 50, "<< BG");
        nextBgButton = new GameButton(centerX + 210, 230, 100, 50, "BG >>");
        ballColorButton = new GameButton(centerX, 310, 200, 50, "BALL: RED");
        brickColorButton = new GameButton(centerX, 390, 200, 50, "BRICK: YELLOW");
        backButton = new GameButton(centerX, 500, 200, 50, "BACK");
        
        // 레벨 선택
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
        
        if (currentLevel != 0) {
            mapGenerator.loadLevel(currentLevel);
        } else {
            mapGenerator.bricks = levelEditor.getGeneratedBricks();
        }
        
        powerUpManager.clear();
        applyCustomColors();
        score = 0; lives = 3; shakeTimer = 0; gameState = STATE_PLAY;
    }
    
    private void resetRound() {
        paddle.resetWidth();
        paddle.getPosition().x = WIDTH / 2 - 50; 
        paddle.getPosition().y = HEIGHT - 60;
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        powerUpManager.clear();
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
        startButton.update(mouseHandler);
        settingsButton.update(mouseHandler);
        exitButton.update(mouseHandler);
        editorButton.update(mouseHandler);
        
        if (startButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_LEVEL_SELECT; }
        if (settingsButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_SETTINGS; }
        if (editorButton.isClicked(mouseHandler)) { soundManager.playClickSound(); gameState = STATE_EDITOR; }
        if (exitButton.isClicked(mouseHandler)) { soundManager.playClickSound(); System.exit(0); }
    }
    
    private void updateLevelSelect() {
        // ★ [버그 수정] 중복된 코드 제거 및 정리
        lvl1Button.update(mouseHandler);
        lvl2Button.update(mouseHandler);
        lvl3Button.update(mouseHandler);
        customPlayButton.update(mouseHandler);
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
            soundManager.playClickSound();
            gameState = STATE_MENU;
        }
    }

    private void updatePlay() {
        if (inputManager.escape && !wasEscPressed) {
            soundManager.playClickSound();
            gameState = STATE_PAUSED;
            wasEscPressed = true;
            return;
        }
        if (!inputManager.escape) wasEscPressed = false;
        
        paddle.update();
        ball.update();
        powerUpManager.update(this, paddle);
        
        // 패들 충돌
        if (CollisionDetector.isColliding(ball, paddle)) {
            CollisionDetector.handlePaddleCollision(ball, paddle);
            if (ball.getVelocity().y < 0) { 
                startShake(5);
                soundManager.playHitSound();
            }
        }
        
        // 벽돌 충돌
        for (Brick brick : mapGenerator.bricks) {
            if (!brick.isDestroyed) {
                if (ball.getBounds().intersects(brick.getBounds())) {
                    CollisionDetector.resolveBallVsRect(ball, brick);
                    brick.hit();
                    score += brick.scoreValue;
                    
                    if (brick.isDestroyed) {
                        soundManager.playExplodeSound();
                        if (brick instanceof breakout.entity.ExplosiveBrick) triggerExplosion(brick);
                        
                        double cx = brick.getPosition().x + brick.getWidth()/2;
                        double cy = brick.getPosition().y + brick.getHeight()/2;
                        effectManager.createExplosion(cx, cy, brick.color);
                        powerUpManager.maybeSpawn(cx, cy);
                        startShake(5); 
                    } else {
                        soundManager.playHitSound();
                    }
                    break; // 한 프레임에 하나의 벽돌만 충돌 처리 (뚫고 지나감 방지)
                }
            }
        }
        
        // 공 떨어짐 처리
        if (ball.getPosition().y > HEIGHT) {
            lives--;
            startShake(20);
            if (lives > 0) {
                soundManager.playFailSound();
                resetRound();
            } else {
                soundManager.playFailSound();
                gameState = STATE_GAME_OVER;
                scoreManager.saveHighScore(score);
            }
        }
        
        // 승리 조건
        long remainingBricks = mapGenerator.bricks.stream().filter(b -> !b.isDestroyed).count();
        if (remainingBricks == 0) {
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
            soundManager.playClickSound();
            isSoundOn = !isSoundOn;
            soundManager.setMute(!isSoundOn);
            soundButton = new GameButton(WIDTH/2 - 100, 150, 200, 50, "SOUND: " + (isSoundOn ? "ON" : "OFF"));
        }
        if (prevBgButton.isClicked(mouseHandler)) { 
            soundManager.playClickSound(); 
            currentBgIndex = (currentBgIndex-1+6)%6; 
            changeBackgroundBGM(); 
        }
        if (nextBgButton.isClicked(mouseHandler)) { 
            soundManager.playClickSound(); 
            currentBgIndex = (currentBgIndex+1)%6; 
            changeBackgroundBGM(); 
        }
        if (ballColorButton.isClicked(mouseHandler)) { 
            soundManager.playClickSound(); 
            ballColorIndex = (ballColorIndex+1)%colorList.length; 
            ballColorButton = new GameButton(WIDTH/2-100, 310, 200, 50, "BALL: " + colorNames[ballColorIndex]); 
        }
        if (brickColorButton.isClicked(mouseHandler)) { 
            soundManager.playClickSound(); 
            brickColorIndex = (brickColorIndex+1)%colorList.length; 
            brickColorButton = new GameButton(WIDTH/2-100, 390, 200, 50, "BRICK: " + colorNames[brickColorIndex]); 
            applyCustomColors(); 
        }
        if (backButton.isClicked(mouseHandler)) { 
            soundManager.playClickSound(); 
            gameState = STATE_MENU; 
        }
    }
    
    private void changeBackgroundBGM() {
        if (isSoundOn) {
            String bgmName = "bgm" + (currentBgIndex + 1) + ".wav";
            soundManager.playBGM(bgmName);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D dbg = (Graphics2D) g;
        dbg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. 배경 그리기
        if (gameState != STATE_MENU) {
            if (backgrounds != null && backgrounds[currentBgIndex] != null) {
                dbg.drawImage(backgrounds[currentBgIndex], 0, 0, WIDTH, HEIGHT, null);
            } else {
                dbg.setColor(Color.BLACK);
                dbg.fillRect(0, 0, WIDTH, HEIGHT);
            }
        }
        
        // 2. 쉐이크 효과 적용
        int sx = 0, sy = 0;
        if (shakeTimer > 0) {
            sx = (int)(Math.random()*10-5);
            sy = (int)(Math.random()*10-5);
            dbg.translate(sx, sy);
        }
        
        // 3. 상태별 화면 그리기
        switch (gameState) {
            case STATE_MENU: drawMenu(dbg); break;
            case STATE_LEVEL_SELECT: drawLevelSelect(dbg); break;
            case STATE_PLAY:
                mapGenerator.draw(dbg);
                paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]);
                ball.draw(dbg);
                effectManager.draw(dbg);
                powerUpManager.draw(dbg);
                drawHUD(dbg);
                break;
            case STATE_PAUSED:
                mapGenerator.draw(dbg);
                paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]);
                ball.draw(dbg);
                drawPause(dbg);
                break;
            case STATE_GAME_OVER:
                mapGenerator.draw(dbg);
                paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]);
                ball.draw(dbg);
                effectManager.draw(dbg);
                drawResult(dbg, "GAME OVER", Color.RED);
                break;
            case STATE_VICTORY:
                mapGenerator.draw(dbg);
                paddle.draw(dbg);
                dbg.setColor(colorList[ballColorIndex]);
                ball.draw(dbg);
                effectManager.draw(dbg);
                drawResult(dbg, "STAGE CLEAR!", Color.GREEN);
                break;
            case STATE_SETTINGS: drawSettings(dbg); break;
            case STATE_EDITOR: levelEditor.draw(dbg); break;
        }
        
        // 쉐이크 복구
        if (sx != 0 || sy != 0) dbg.translate(-sx, -sy);
        
        // ★ 최적화: 그래픽 버퍼 동기화 (Linux/Mac 깜빡임 방지)
        Toolkit.getDefaultToolkit().sync(); 
    }
    
    // ★ 최적화: 미리 캐싱된 Font 객체 사용
    private void draw3DText(Graphics2D g2, String text, int x, int y, Color mainColor, Color shadowColor, Font font) {
        g2.setFont(font);
        g2.setColor(shadowColor);
        for(int i = 5; i > 0; i--) g2.drawString(text, x + i, y + i);
        g2.setColor(mainColor);
        g2.drawString(text, x, y);
    }
    
    private void drawMenu(Graphics2D g2) {
        if (menuGifImage != null) {
            g2.drawImage(menuGifImage, 0, 0, WIDTH, HEIGHT, this);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }
        
        // ★ 캐싱된 폰트 사용
        draw3DText(g2, "샤갈적인 벽돌깨기", WIDTH/2 - 280, 150, Color.YELLOW, Color.DARK_GRAY, titleFont);
        draw3DText(g2, "[학생회의 반란]", WIDTH/2 - 130, 210, Color.WHITE, Color.BLACK, subTitleFont);
        
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Consolas", Font.BOLD, 20)); // Score 폰트는 단순하므로 유지하거나 이것도 캐싱 가능
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
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, WIDTH, 40);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 24));
        g2.drawString("SCORE: " + score, 20, 28);
        g2.drawString("LIVES:", WIDTH - 180, 28);
        for (int i = 0; i < lives; i++) {
            drawHeart(g2, WIDTH - 100 + (i * 30), 10);
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
                    effectManager.createExplosion(b.getPosition().x + b.getWidth()/2, b.getPosition().y + b.getHeight()/2, b.color);
                }
            }
        }
        startShake(20); 
    }
}