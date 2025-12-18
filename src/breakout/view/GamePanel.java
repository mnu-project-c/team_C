package breakout.view;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import breakout.entity.Achievement;
import breakout.manager.AchievementManager;
import breakout.engine.Vector2D;

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
    public static final int STATE_LEADERBOARD = 8;
    public static final int STATE_ACHIEVEMENTS = 9;
    public static final int STATE_USER_CUSTOM = 10;
    public static final int STATE_NAME_INPUT = 11;
    
    private Thread gameThread;
    private boolean running = false;
    private final int FPS = 60;
    private final double SLOW_FACTOR = 0.7;
    private final int SLOW_DURATION = FPS * 10;
    private final int PIERCE_DURATION = FPS * 10;
    private final int DOUBLE_SCORE_DURATION = FPS * 15;
    
    private ShopOverlayPanel shopOverlay;
    private Runnable shopOpener;

    private InputManager inputManager;
    private MouseHandler mouseHandler;
    private EffectManager effectManager;
    private ScoreManager scoreManager;
    private PowerUpManager powerUpManager;
    private AchievementManager achievementManager;
    private SoundManager soundManager;
    private LevelEditor levelEditor;
    private NotificationPopup notificationPopup;
    
    private Paddle paddle;
    private final ArrayList<Ball> balls = new ArrayList<>();
    private MapGenerator mapGenerator;
    
    // 버튼 객체들
    private GameButton startButton, settingsButton, exitButton, userCustomButton;
    private GameButton leaderboardButton, achievementButton, editorButton, ucBackButton;
    private GameButton restartButton, menuButton, victoryLevelButton, achBackButton;
    
    private LeaderboardPanel leaderboardPanel;
    private SettingsPanel settingsPanel;
    private LevelSelectPanel levelSelectPanel;
    private PausePanel pausePanel;
    private NameInputModal nameModal;
    
    private Image[] ballSkins;
    private int currentSkinIndex = -1; 

    private int gameState = STATE_MENU;
    private int previousState = STATE_MENU; 
    
    private int score = 0;
    private int lives = 3; 
    private boolean isSoundOn = true;
    private int shakeTimer = 0;
    private boolean wasEscPressed = false;
    
    private BufferedImage[] backgrounds;
    private int currentBgIndex = 0;
    private Image menuGifImage;
    private Font customFont;
    
    private int comboCount = 0;       
    private float comboScale = 1.0f;  
    
    private final Color[] colorList = { Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.WHITE, Color.CYAN };
    private final String[] colorNames = { "빨강", "주황", "노랑", "초록", "파랑", "보라", "흰색", "하늘" };
    private final String[] shapeNames = { "기본", "알약", "샤프", "파도" };
    
    private int ballColorIndex = 0; 
    private int brickColorIndex = 2;
    private int paddleColorIndex = 7; 
    private int paddleShapeIndex = 0; 
    private final Random rng = new Random();
    
    private int currentLevel = 1;

    // 아이템 상태
    private boolean doubleScoreActive = false;
    private int doubleScoreTimer = 0;
    private boolean piercingActive = false;
    private int piercingTimer = 0;
    private boolean slowBallActive = false;
    private int slowBallTimer = 0;
    private int bombBallCharges = 0;

    // 럭키 드로우용 Enum (클래스 내부에 한 번만 선언)
    private enum LuckyPrize {
        EXTRA_LIFE, WIDE_PADDLE, SLOW_BALL, PIERCING_BALL, DOUBLE_SCORE
    }
    
    private float fadeAlpha = 0.0f;
    private boolean isFading = false;
    private boolean isFadeOut = false;
    private int nextGameState = -1;
    private final float FADE_SPEED = 0.05f;
    private boolean isCRTFilterOn = false;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDoubleBuffered(true);
        setFocusable(true);
        requestFocus();
        
        inputManager = new InputManager();
        addKeyListener(inputManager);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameState == STATE_NAME_INPUT && nameModal != null) {
                    nameModal.handleKeyPress(e);
                    repaint();
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
                if (gameState == STATE_NAME_INPUT && nameModal != null) {
                    nameModal.handleKeyTyped(e);
                    repaint();
                }
            }
        });

        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        
        effectManager = new EffectManager();
        scoreManager = new ScoreManager();
        powerUpManager = new PowerUpManager();
        achievementManager = new AchievementManager();
        soundManager = SoundManager.getInstance(); 
        
        notificationPopup = new NotificationPopup();
        achievementManager.setPopup(notificationPopup);

        soundManager.playBGM("Bgm.wav"); 
        levelEditor = new LevelEditor();
        
        loadResources();
        initGameObjects(); 
        initUI();          
        leaderboardPanel = new LeaderboardPanel(scoreManager);
        settingsPanel = new SettingsPanel(this);
        levelSelectPanel = new LevelSelectPanel(this);
        pausePanel = new PausePanel(this);
        applyCustomColors();
    }

    private void loadResources() {
        backgrounds = new BufferedImage[6];
        for(int i=0; i<6; i++) {
            File file = new File("assets/bg" + (i+1) + ".jpg");
            if (file.exists()) {
                try { backgrounds[i] = ImageIO.read(file); } catch (Exception e) {}
            }
        }
        ballSkins = new Image[4];
        for (int i = 0; i < 4; i++) {
            File file = new File("assets/skin" + (i+1) + ".jpg");
            if (file.exists()) {
                try { ballSkins[i] = ImageIO.read(file); } catch (Exception e) {}
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
                customFont = baseFont.deriveFont(Font.BOLD, 12f);
            } else { useDefaultFonts(); }
        } catch (Exception e) { useDefaultFonts(); }
    }
    
    private void useDefaultFonts() { customFont = new Font("SansSerif", Font.BOLD, 12); }
    
    private void initUI() {
        int startY = 270;
        int gap = 60;
        startButton = createCenteredButton(startY, 200, 50, "게임 시작");
        userCustomButton = createCenteredButton(startY + gap, 200, 50, "커스텀 메뉴");
        settingsButton = createCenteredButton(startY + gap * 2, 200, 50, "설정");
        exitButton = createCenteredButton(startY + gap * 3, 200, 50, "게임 종료");
        
        leaderboardButton = createCenteredButton(startY, 200, 50, "랭킹");
        achievementButton = createCenteredButton(startY + gap, 200, 50, "업적");
        editorButton = createCenteredButton(startY + gap * 2, 200, 50, "레벨 에디터");
        ucBackButton = createCenteredButton(startY + gap * 3, 200, 50, "뒤로가기");
        
        restartButton = createCenteredButton(340, 200, 50, "다시 시작");
        victoryLevelButton = createCenteredButton(400, 200, 50, "레벨 선택"); 
        menuButton = createCenteredButton(460, 200, 50, "메인 메뉴");
        achBackButton = new GameButton(WIDTH / 2 - 100, 500 , 200 , 50, "돌아가기");
    }
    
    private GameButton createCenteredButton(int y, int width, int height, String text) {
        return new GameButton(WIDTH/2 - width/2, y, width, height, text);
    }

    private void initGameObjects() {
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        paddle.setColor(colorList[paddleColorIndex]); 
        paddle.setShapeType(paddleShapeIndex);
        balls.clear();
        balls.add(createBall(WIDTH / 2 - 10, HEIGHT - 100));
        mapGenerator = new MapGenerator();
        scoreManager.load();
    }

    public void transitionTo(int nextState) {
        if (isFading) return;
        this.nextGameState = nextState;
        this.isFading = true;
        this.isFadeOut = true; 
        this.fadeAlpha = 0.0f;
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        running = true;
        while(running) {
            update();
            repaint(); 
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000;
                Thread.sleep(Math.max(0, (long)remainingTime));
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void update() {
        if (isFading) {
            if (isFadeOut) {
                fadeAlpha += FADE_SPEED;
                if (fadeAlpha >= 1.0f) {
                    fadeAlpha = 1.0f;
                    if (nextGameState != -1) gameState = nextGameState;
                    isFadeOut = false; 
                }
            } else {
                fadeAlpha -= FADE_SPEED;
                if (fadeAlpha <= 0.0f) { fadeAlpha = 0.0f; isFading = false; }
            }
        }

        if (notificationPopup != null) notificationPopup.update();
        if (shopOverlay != null && shopOverlay.isVisible()) {
            shopOverlay.updateOverlay(mouseHandler); return; 
        }

        if (shakeTimer > 0) shakeTimer--;
        if (comboScale > 1.0f) comboScale -= 0.05f;

        inputManager.update();
        effectManager.update(); 
        if (!inputManager.escape) wasEscPressed = false;

        if (!isFading || !isFadeOut) {
            switch (gameState) {
                case STATE_MENU: updateMenu(); break;
                case STATE_USER_CUSTOM: updateUserCustom(); break;
                case STATE_LEVEL_SELECT: 
                    if (levelSelectPanel != null && levelSelectPanel.update(mouseHandler)) transitionTo(STATE_MENU);
                    break;
                case STATE_PLAY: updatePlay(); break;
                case STATE_PAUSED: 
                    if (pausePanel != null) pausePanel.update(mouseHandler); 
                    if (inputManager.escape && !wasEscPressed) {
                        soundManager.playClickSound(); resumeFromPause(); wasEscPressed = true;
                    }
                    break;
                case STATE_GAME_OVER:
                case STATE_VICTORY: updateResult(); break;
                case STATE_SETTINGS: 
                    if (settingsPanel != null && settingsPanel.update(mouseHandler)) transitionTo(previousState);
                    break;
                case STATE_EDITOR: updateEditor(); break;
                case STATE_LEADERBOARD:
                    if (leaderboardPanel != null && leaderboardPanel.update(mouseHandler)) transitionTo(STATE_USER_CUSTOM);
                    break;
                case STATE_ACHIEVEMENTS:
                    achBackButton.update(mouseHandler);
                    if(achBackButton.isClicked(mouseHandler)) transitionTo(STATE_USER_CUSTOM);
                    break;
                case STATE_NAME_INPUT:
                    if (nameModal != null) {
                        nameModal.update(mouseHandler, soundManager);
                        if (nameModal.isFinished()) {
                            if (!nameModal.isCancelled()) scoreManager.addScore(nameModal.getInputName(), score);
                            transitionTo(STATE_LEADERBOARD);
                            nameModal = null;
                        }
                    }
                    break;
            }
        }
    }

    private void updateMenu() {
        startButton.update(mouseHandler); userCustomButton.update(mouseHandler);
        settingsButton.update(mouseHandler); exitButton.update(mouseHandler);
        if (startButton.isClicked(mouseHandler)) transitionTo(STATE_LEVEL_SELECT);
        if (userCustomButton.isClicked(mouseHandler)) transitionTo(STATE_USER_CUSTOM);
        if (settingsButton.isClicked(mouseHandler)) { previousState = STATE_MENU; transitionTo(STATE_SETTINGS); }
        if (exitButton.isClicked(mouseHandler)) System.exit(0);
    }

    private void updateUserCustom() {
        leaderboardButton.update(mouseHandler); achievementButton.update(mouseHandler);
        editorButton.update(mouseHandler); ucBackButton.update(mouseHandler);
        if (leaderboardButton.isClicked(mouseHandler)) { previousState = STATE_USER_CUSTOM; transitionTo(STATE_LEADERBOARD); }
        if (achievementButton.isClicked(mouseHandler)) transitionTo(STATE_ACHIEVEMENTS);
        if (editorButton.isClicked(mouseHandler)) transitionTo(STATE_EDITOR);
        if (ucBackButton.isClicked(mouseHandler)) transitionTo(STATE_MENU);
    }

    private void updateEditor() {
        levelEditor.update(mouseHandler);
        if (levelEditor.getExitButton().isClicked(mouseHandler)) transitionTo(STATE_USER_CUSTOM);
    }

    private void updatePlay() {
        tickPowerTimers();
        paddle.update();
        if (inputManager.escape && !wasEscPressed) {
            soundManager.playClickSound(); gameState = STATE_PAUSED; wasEscPressed = true; return;
        }
        powerUpManager.update(this, paddle);

        for (int i = balls.size() - 1; i >= 0; i--) {
            Ball b = balls.get(i);
            b.update();
            CollisionDetector.handleWallCollision(b, 0, 0, WIDTH, HEIGHT, soundManager);

            if (CollisionDetector.isColliding(b, paddle)) {
                CollisionDetector.handlePaddleCollision(b, paddle);
                if (b.getVelocity().y < 0) { startShake(5); soundManager.playHitSound(); comboCount = 0; }
            }

            for (Brick brick : mapGenerator.bricks) {
                if (!brick.isDestroyed) {
                    brick.update();
                    if (b.getBounds().intersects(brick.getBounds())) {
                        if (!piercingActive) CollisionDetector.resolveBallVsRect(b, brick);
                        brick.hit();
                        
                        double cx = brick.getPosition().x + brick.getWidth()/2;
                        double cy = brick.getPosition().y + brick.getHeight()/2;
                        
                        if (bombBallCharges > 0) { bombBallCharges--; triggerExplosion(brick); effectManager.createExplosion(cx, cy, brick.color); }

                        comboCount++;
                        comboScale = Math.min(3.0f, 2.0f + (comboCount * 0.1f));
                        addScoreWithMultiplier(brick.scoreValue + (comboCount > 1 ? comboCount * 10 : 0));
                        
                        achievementManager.unlock("첫 걸음");
                        if (score >= 10000) achievementManager.unlock("고득점자");

                        if (brick.isDestroyed) {
                            soundManager.playExplodeSound();
                            effectManager.createExplosion(cx, cy, brick.color);
                            if (brick instanceof breakout.entity.ExplosiveBrick) triggerExplosion(brick);
                            powerUpManager.maybeSpawn(cx, cy);
                            startShake(15 + Math.min(comboCount, 10)); 
                        } else { soundManager.playHitSound(); startShake(5); }
                        if (!piercingActive) break;
                    }
                }
            }
            if (b.getPosition().y > HEIGHT) balls.remove(i);
        }

        if (balls.isEmpty()) {
            lives--; startShake(20);
            if (lives > 0) { soundManager.playFailSound(); resetRound(); }
            else { 
                gameState = STATE_GAME_OVER; 
                promptAndAddScore(score); 
                soundManager.stopBGM();
                soundManager.playGameOverSound();
            }
        }

        if (mapGenerator.bricks.stream().noneMatch(br -> !br.isDestroyed)) {
            if (lives == 3) achievementManager.unlock("생존 전문가");
            gameState = STATE_VICTORY;
            achievementManager.addClearCount();
            promptAndAddScore(score);
            soundManager.stopBGM();
            soundManager.playVictorySound();
        }
    }

    private void updateResult() {
        restartButton.update(mouseHandler); 
        menuButton.update(mouseHandler);
        victoryLevelButton.update(mouseHandler); 
        
        if (victoryLevelButton.isClicked(mouseHandler)) {
            soundManager.playBGM("Bgm.wav");
            transitionTo(STATE_LEVEL_SELECT);
        }
        if (restartButton.isClicked(mouseHandler)) {
            soundManager.playBGM("Bgm.wav");
            startGameWithLevel(currentLevel);
        }
        if (menuButton.isClicked(mouseHandler)) {
            soundManager.playBGM("Bgm.wav");
            transitionTo(STATE_MENU);
        }
    }

    private void resetGame() {
        clearPowerStates();
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        paddle.setColor(colorList[paddleColorIndex]);
        paddle.setShapeType(paddleShapeIndex);
        balls.clear(); 
        balls.add(createBall(WIDTH / 2 - 10, HEIGHT - 100));
        reapplySlowIfNeeded();
        if (currentLevel != 0) mapGenerator.loadLevel(currentLevel);
        else mapGenerator.bricks = levelEditor.getGeneratedBricks();
        powerUpManager.clear(); 
        applyCustomColors();
        score = 0; lives = 3; shakeTimer = 0; comboCount = 0; gameState = STATE_PLAY;
    }

    private void resetRound() {
        paddle.resetWidth();
        paddle.getPosition().x = WIDTH / 2 - 50;
        paddle.getPosition().y = HEIGHT - 60;
        balls.clear(); 
        balls.add(createBall(WIDTH / 2 - 10, HEIGHT - 100));
        reapplySlowIfNeeded();
        powerUpManager.clear(); comboCount = 0;
        try { Thread.sleep(500); } catch (Exception e) {}
    }

    private void promptAndAddScore(int score) {
        if (!scoreManager.isHighScore(score)) return;
        nameModal = new NameInputModal(WIDTH/2 - 200, HEIGHT/2 - 100, 400, 200);
        gameState = STATE_NAME_INPUT;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D dbg = (Graphics2D) g;
        dbg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (gameState == STATE_MENU || gameState == STATE_USER_CUSTOM) {
             if (menuGifImage != null) dbg.drawImage(menuGifImage, 0, 0, WIDTH, HEIGHT, this);
             else { dbg.setColor(Color.BLACK); dbg.fillRect(0, 0, WIDTH, HEIGHT); }
        } else {
            if (backgrounds != null && backgrounds[currentBgIndex] != null) dbg.drawImage(backgrounds[currentBgIndex], 0, 0, WIDTH, HEIGHT, null);
            else { dbg.setColor(Color.BLACK); dbg.fillRect(0, 0, WIDTH, HEIGHT); }
        }
        
        int sx = 0, sy = 0;
        if (shakeTimer > 0) { 
            int intensity = 5 + (comboCount > 2 ? 5 : 0); 
            sx = rng.nextInt(intensity*2)-intensity;
            sy = rng.nextInt(intensity*2)-intensity;
            dbg.translate(sx, sy);
        }
        
        switch (gameState) {
            case STATE_MENU: drawMenu(dbg); break;
            case STATE_USER_CUSTOM: drawUserCustom(dbg); break;
            case STATE_LEVEL_SELECT: if (levelSelectPanel != null) levelSelectPanel.draw(dbg, customFont); break;
            case STATE_PLAY: drawPlay(dbg); break;
            case STATE_PAUSED: drawPaused(dbg); break;
            case STATE_GAME_OVER: drawPlay(dbg); drawResult(dbg, "GAME OVER", Color.RED); break;
            case STATE_VICTORY: drawPlay(dbg); drawResult(dbg, "STAGE CLEAR!", Color.GREEN); break;
            case STATE_SETTINGS: if (settingsPanel != null) settingsPanel.draw(dbg, customFont); break;
            case STATE_EDITOR: levelEditor.draw(dbg, customFont); break;
            case STATE_LEADERBOARD: if (leaderboardPanel != null) leaderboardPanel.draw(dbg, customFont); break;
            case STATE_ACHIEVEMENTS: drawAchievements(dbg); break;
            case STATE_NAME_INPUT: if (nameModal != null) nameModal.draw(dbg, customFont); break;
        }

        if (sx != 0 || sy != 0) dbg.translate(-sx, -sy);
        if (isFading && fadeAlpha > 0.01f) {
            dbg.setColor(new Color(0, 0, 0, Math.max(0.0f, Math.min(1.0f, fadeAlpha)))); 
            dbg.fillRect(0, 0, WIDTH, HEIGHT);
        }
        if (isCRTFilterOn) drawCRT(dbg);
        if (notificationPopup != null) notificationPopup.draw(dbg, WIDTH);
        if (gameState == STATE_MENU || gameState == STATE_USER_CUSTOM) Toolkit.getDefaultToolkit().sync(); 
    }

    private void drawPlay(Graphics2D dbg) {
        mapGenerator.draw(dbg); paddle.draw(dbg); 
        dbg.setColor(colorList[ballColorIndex]);
        for (Ball b : balls) b.draw(dbg);
        effectManager.draw(dbg); powerUpManager.draw(dbg); drawHUD(dbg);
    }

    private void drawPaused(Graphics2D dbg) {
        mapGenerator.draw(dbg); paddle.draw(dbg); 
        for (Ball b : balls) b.draw(dbg);
        if (pausePanel != null) pausePanel.draw(dbg, customFont);
    }

    private void drawCRT(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 50));
        for (int y = 0; y < HEIGHT; y += 4) g.fillRect(0, y, WIDTH, 2);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 100)); g2.fillRect(0, 0, WIDTH, 40);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 24));
        g2.drawString("SCORE: " + score, 20, 28);
        for (int i = 0; i < lives; i++) drawHeart(g2, WIDTH - 120 + (i * 30), 10);
        drawActiveBuffs(g2);
        if (comboCount >= 2) drawCombo(g2);
    }

    private void drawCombo(Graphics2D g2) {
        int fontSize = (int)(40 * comboScale); 
        g2.setFont(new Font("Consolas", Font.BOLD, fontSize));
        Color[] flash = { Color.RED, Color.ORANGE, Color.YELLOW, Color.WHITE, Color.MAGENTA, Color.CYAN };
        Color mainColor = flash[(int)((System.currentTimeMillis() / 50) % flash.length)];
        String text = comboCount + " COMBO!";
        int tw = g2.getFontMetrics().stringWidth(text);
        int drawX = WIDTH/2 - tw/2;
        int drawY = 80;
        g2.setColor(Color.DARK_GRAY);
        for (int i = 1; i <= 8; i++) g2.drawString(text, drawX + i, drawY + i); 
        g2.setColor(mainColor);
        g2.drawString(text, drawX, drawY);
    }

    private void drawActiveBuffs(Graphics2D g2) {
        List<String> s = new ArrayList<>();
        if (doubleScoreActive) s.add("2x SCORE " + (doubleScoreTimer/60) + "s");
        if (piercingActive) s.add("PIERCE " + (piercingTimer/60) + "s");
        if (slowBallActive) s.add("SLOW " + (slowBallTimer/60) + "s");
        if (bombBallCharges > 0) s.add("BOMB x" + bombBallCharges);
        if (!s.isEmpty()) {
            g2.setFont(new Font("Consolas", Font.BOLD, 16)); g2.setColor(Color.WHITE);
            g2.drawString(String.join(" | ", s), 10, 55);
        }
    }

    private void drawHeart(Graphics2D g2, int x, int y) {
        g2.setColor(Color.RED); g2.fillOval(x, y, 10, 10); g2.fillOval(x + 10, y, 10, 10); 
        g2.fillPolygon(new int[]{x, x+10, x+20}, new int[]{y+5, y+20, y+5}, 3);
    }

    private void drawMenu(Graphics2D g2) {
        drawCentered3DText(g2, "샤갈적인 벽돌깨기", 150, Color.YELLOW, Color.DARK_GRAY, 70f);
        drawCentered3DText(g2, "⚜️태풍을 부르는 학생회의 반란⚜️", 210, Color.WHITE, Color.BLACK, 30f);
        g2.setColor(Color.CYAN); g2.setFont(new Font("Consolas", Font.BOLD, 20));
        drawCenteredString(g2, "HIGH SCORE: " + scoreManager.getHighScore(), WIDTH/2, 550);
        startButton.draw(g2, customFont); userCustomButton.draw(g2, customFont);
        settingsButton.draw(g2, customFont); exitButton.draw(g2, customFont);
    }

    private void drawUserCustom(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        drawCentered3DText(g2, "커스텀 메뉴", 180, Color.GREEN, Color.DARK_GRAY, 50f);
        leaderboardButton.draw(g2, customFont); achievementButton.draw(g2, customFont);
        editorButton.draw(g2, customFont); ucBackButton.draw(g2, customFont);
    }

    private void drawResult(Graphics2D g2, String title, Color color) {
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        drawCentered3DText(g2, title, 200, color, Color.BLACK, 50f);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 30));
        drawCenteredString(g2, "Final Score: " + score, WIDTH/2, 280); 

        restartButton.draw(g2, customFont); 
        victoryLevelButton.draw(g2, customFont);
        menuButton.draw(g2, customFont);
    }

    private void drawAchievements(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        drawCentered3DText(g2, "ACHIEVEMENTS", 80, Color.YELLOW, Color.BLACK, 40f);
        List<Achievement> list = achievementManager.getAchievements(); 
        for (int i = 0; i < list.size(); i++) {
            Achievement a = list.get(i); int y = 150 + (i * 60);
            g2.setColor(a.isUnlocked ? Color.GREEN : Color.GRAY);
            g2.drawString((a.isUnlocked ? "✔ " : "🔒 ") + a.title, 150, y);
        }
        achBackButton.draw(g2, customFont); 
    }

    private void drawCentered3DText(Graphics2D g2, String text, int y, Color c1, Color c2, float s) {
        if (customFont != null) g2.setFont(customFont.deriveFont(Font.BOLD, s));
        int x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.setColor(c2); g2.drawString(text, x+3, y+3); g2.setColor(c1); g2.drawString(text, x, y);
    }

    public void drawCenteredString(Graphics2D g, String text, int x, int y) {
        int tw = g.getFontMetrics().stringWidth(text); g.drawString(text, x - tw / 2, y);
    }

    // --- 유틸리티 및 시스템 로직 ---

    private void triggerExplosion(Brick b) {
        soundManager.playBombSound();
        Rectangle r = new Rectangle((int)b.getPosition().x-80, (int)b.getPosition().y-30, 240, 90);
        for (Brick target : mapGenerator.bricks) {
            if (!target.isDestroyed && target != b && r.intersects(target.getBounds())) {
                target.hit(); if (target.isDestroyed) addScoreWithMultiplier(target.scoreValue);
            }
        }
        startShake(20);
    }

    private void tickPowerTimers() {
        if (doubleScoreActive && --doubleScoreTimer <= 0) doubleScoreActive = false;
        if (piercingActive && --piercingTimer <= 0) piercingActive = false;
        if (slowBallActive && --slowBallTimer <= 0) disableSlowBall();
    }

    private void disableSlowBall() {
        for (Ball b : balls) { b.getVelocity().x /= SLOW_FACTOR; b.getVelocity().y /= SLOW_FACTOR; }
        slowBallActive = false;
    }

    private void reapplySlowIfNeeded() {
        if (slowBallActive) {
            for (Ball b : balls) { b.getVelocity().x *= SLOW_FACTOR; b.getVelocity().y *= SLOW_FACTOR; }
        }
    }

    private void clearPowerStates() { doubleScoreActive = piercingActive = slowBallActive = false; bombBallCharges = 0; }
    private void addScoreWithMultiplier(int amount) { score += doubleScoreActive ? amount * 2 : amount; }

    public void startShake(int d) { this.shakeTimer = d; }
    public void transitionToPlay() { gameState = STATE_PLAY; }
    public void startGame() { if (gameThread == null) { running = true; gameThread = new Thread(this); gameThread.start(); } }
    public void startGameWithLevel(int lv) { currentLevel = lv; resetGame(); }

    // --- 설정 및 인게임 연동 메서드 ---
    public void addLife() { lives++; }
    public void expandPaddle() { if (paddle != null) paddle.expand(); }

    public void activateDoubleScore() { doubleScoreActive = true; doubleScoreTimer = DOUBLE_SCORE_DURATION; }
    public void activatePiercingBall() { piercingActive = true; piercingTimer = PIERCE_DURATION; }
    public void activateSlowBall() { 
        if(!slowBallActive){ for(Ball b:balls){ b.getVelocity().x*=SLOW_FACTOR; b.getVelocity().y*=SLOW_FACTOR; } } 
        slowBallActive = true; slowBallTimer = SLOW_DURATION; 
    }

    public void cycleBallColor() { ballColorIndex = (ballColorIndex + 1) % colorList.length; }
    public Color getCurrentBallColor() { return colorList[ballColorIndex]; }
    public void cycleBallSkin() {
        currentSkinIndex++;
        if (currentSkinIndex >= 4) currentSkinIndex = -1;
        applyBallSkin();
    }
    public void applyBallSkin() { for (Ball b : balls) { applyBallSkinToBall(b); } }
    public String getBallSkinName() { return (currentSkinIndex == -1) ? "없음" : "학생회 " + (currentSkinIndex + 1); }
    public void cyclePaddleColor() {
        paddleColorIndex = (paddleColorIndex + 1) % colorList.length;
        if (paddle != null) paddle.setColor(colorList[paddleColorIndex]);
    }
    public void cyclePaddleShape() {
        paddleShapeIndex = (paddleShapeIndex + 1) % 4;
        if (paddle != null) paddle.setShapeType(paddleShapeIndex);
    }
    public void cycleBrickColor() {
        brickColorIndex = (brickColorIndex + 1) % colorList.length;
        applyCustomColors();
    }
    public void applyCustomColors() { Color target = colorList[brickColorIndex]; for (Brick b : mapGenerator.bricks) if (b instanceof breakout.entity.NormalBrick) b.color = target; }
    
    public void toggleSound() { 
        isSoundOn = !isSoundOn; 
        soundManager.setMute(!isSoundOn); 
        if (isSoundOn) {
            soundManager.playBGM("Bgm.wav");
    } else {
         soundManager.stopBGM();
    }   
    }
    public boolean isSoundOn() { return isSoundOn; }
    public void toggleCRTFilter() { isCRTFilterOn = !isCRTFilterOn; }
    public boolean isCRTFilterOn() { return isCRTFilterOn; }
    public void nextBackground() { currentBgIndex = (currentBgIndex+1)% backgrounds.length; }
    public void prevBackground() { currentBgIndex = (currentBgIndex-1+backgrounds.length)% backgrounds.length; }
    
    public String getBallColorName() { return colorNames[ballColorIndex]; }
    public String getPaddleColorName() { return colorNames[paddleColorIndex]; }
    public String getBrickColorName() { return colorNames[brickColorIndex]; }
    public String getPaddleShapeName() { return shapeNames[paddleShapeIndex]; }

    public void resumeFromPause() { gameState = STATE_PLAY; wasEscPressed = false; }
    public void openSettingsFromPause() { previousState = STATE_PAUSED; transitionTo(STATE_SETTINGS); }
    public void gotoMenuFromPause() { transitionTo(STATE_MENU); }

    public void setShopOverlay(ShopOverlayPanel shopOverlay) { this.shopOverlay = shopOverlay; }
    public void setShopOpener(Runnable r) { this.shopOpener = r; }
    public void openShop() { if (shopOpener != null) shopOpener.run(); }
    public void pauseForOverlay() { this.gameState = STATE_PAUSED; }
    public void resumeFromOverlay() { this.gameState = STATE_PLAY; this.wasEscPressed = true; }

    public void applyLongPaddleFromShop() { expandPaddle(); }
    public void applySlowBallFromShop() { activateSlowBall(); }
    public void addLifeFromShop() { addLife(); }
    public void applyPierceFromShop() { activatePiercingBall(); }
    public void applyDoubleScoreFromShop() { activateDoubleScore(); }
    public void applyBombBallFromShop() { bombBallCharges++; }
    public void applyMultiBallFromShop() { spawnMultiBall(2); }

    public String applyLuckyDrawFromShop() {
        LuckyPrize prize = rollLuckyPrize();
        switch (prize) {
            case EXTRA_LIFE: addLife(); return "행운! 체력 +1";
            case WIDE_PADDLE: expandPaddle(); return "패들 확장!";
            case SLOW_BALL: activateSlowBall(); return "볼 슬로우 10초";
            case PIERCING_BALL: activatePiercingBall(); return "관통 볼 10초";
            case DOUBLE_SCORE: activateDoubleScore(); return "더블 스코어 15초";
            default: return "행운 실패..?";
        }
    }

    private LuckyPrize rollLuckyPrize() {
        int roll = rng.nextInt(100);
        if (roll < 20) return LuckyPrize.EXTRA_LIFE;
        if (roll < 40) return LuckyPrize.WIDE_PADDLE;
        if (roll < 60) return LuckyPrize.SLOW_BALL;
        if (roll < 80) return LuckyPrize.PIERCING_BALL;
        return LuckyPrize.DOUBLE_SCORE;
    }

    public int getScore() { return score; }
    public void spendScore(int a) { score = Math.max(0, score - a); }
    public Paddle getPaddle() { return paddle; }
    public SoundManager getSoundManager() { return soundManager; }
    public MouseHandler getMouseHandler() { return mouseHandler; }

    public boolean isSkinUnlocked(int skinIndex) {
        if (skinIndex == -1) return true;
        return achievementManager.isUnlocked("학생회의 자격-" + (skinIndex + 1));
    }

    private Ball createBall(double x, double y) {
        Ball newBall = new Ball(x, y);
        applyBallSkinToBall(newBall); 
        if (slowBallActive) {
            newBall.getVelocity().x *= SLOW_FACTOR;
            newBall.getVelocity().y *= SLOW_FACTOR;
        }
        return newBall;
    }
    
    private Ball createBall(double x, double y, double vx, double vy) {
        Ball newBall = new Ball(x, y);
        newBall.setVelocity(new Vector2D(vx, vy));
        applyBallSkinToBall(newBall);
        return newBall;
    }
    
    private void applyBallSkinToBall(Ball target) {
        if (target == null) return;
        if (currentSkinIndex != -1 && isSkinUnlocked(currentSkinIndex)) {
            if (currentSkinIndex < ballSkins.length && ballSkins[currentSkinIndex] != null) {
                target.setSkin(ballSkins[currentSkinIndex]);
                return;
            }
        }
        target.setSkin(null); 
    }

    public void spawnMultiBall(int extraCount) {
        if (balls.isEmpty()) return;
        Ball base = balls.get(0);
        Vector2D baseVel = base.getVelocity();
        double speed = baseVel.magnitude();
        if (speed < 1.0) speed = 5.0; 
        double baseAngle = Math.atan2(baseVel.y, baseVel.x);
        for (int i = 0; i < extraCount; i++) {
            double newAngle = baseAngle + Math.toRadians((i + 1) * 20);
            double vx = speed * Math.cos(newAngle);
            double vy = speed * Math.sin(newAngle);
            balls.add(createBall(base.getPosition().x, base.getPosition().y, vx, vy));
        }
    }
}