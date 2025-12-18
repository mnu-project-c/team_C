package breakout.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter; // 추가됨
import java.awt.event.KeyEvent;   // 추가됨
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

public class GamePanel extends JPanel implements Runnable {
    
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
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
<<<<<<< HEAD
=======
    public static final int STATE_NAME_INPUT = 11; // 이름 입력 상태
>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
    
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
    
    private Paddle paddle;
    private Ball ball;
    private MapGenerator mapGenerator;
    
    private GameButton startButton, settingsButton, exitButton;
    private GameButton userCustomButton;

    private GameButton leaderboardButton;
    private GameButton achievementButton;
    private GameButton editorButton;
    private GameButton ucBackButton;
    
    private LeaderboardPanel leaderboardPanel;
    private SettingsPanel settingsPanel;
    private LevelSelectPanel levelSelectPanel;
    private PausePanel pausePanel;
    private GameButton restartButton, menuButton;
    private GameButton victoryLevelButton;    
    private GameButton achBackButton;
    
    private NameInputModal nameModal; // 모달 객체
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
    private boolean doubleScoreActive = false;
    private int doubleScoreTimer = 0;
    private boolean piercingActive = false;
    private int piercingTimer = 0;
    private boolean slowBallActive = false;
    private int slowBallTimer = 0;
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
        
<<<<<<< HEAD
=======
        // ★ 이름 입력을 위한 전용 키 리스너
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

>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        
        effectManager = new EffectManager();
        scoreManager = new ScoreManager();
        powerUpManager = new PowerUpManager();
        achievementManager = new AchievementManager();
        soundManager = new SoundManager(); 

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
                try { backgrounds[i] = ImageIO.read(file); } 
                catch (Exception e) {}
            }
        }
        
        ballSkins = new Image[4];
        for (int i = 0; i < 4; i++) {
            File file = new File("assets/skin" + (i+1) + ".jpg");
            if (file.exists()) {
                try { ballSkins[i] = ImageIO.read(file); }
                catch (Exception e) { e.printStackTrace(); }
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
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
                customFont = baseFont.deriveFont(Font.BOLD, 12f);
            } else {
                useDefaultFonts();
            }
        } catch (Exception e) { useDefaultFonts(); }
    }
    
    private void useDefaultFonts() {
        customFont = new Font("SansSerif", Font.BOLD, 12);
    }
    
    private void initUI() {
        int centerX = WIDTH / 2 - 100;
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
<<<<<<< HEAD
        achBackButton = new GameButton(WIDTH / 2 - 100, 500 , 200 , 50, "돌아가기");
=======
        achBackButton = new GameButton(centerX, 500 , 200 , 50, "돌아가기");
>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
    }
    
    private GameButton createCenteredButton(int y, int width, int height, String text) {
        return new GameButton(WIDTH/2 - width/2, y, width, height, text);
    }

    private void initGameObjects() {
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        paddle.setColor(colorList[paddleColorIndex]); 
        paddle.setShapeType(paddleShapeIndex);
        
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        applyBallSkin(); 
        mapGenerator = new MapGenerator();
        scoreManager.load();
    }
    
    public void applyCustomColors() {
        Color targetBrickColor = colorList[brickColorIndex];
        for (Brick b : mapGenerator.bricks) {
            if (b instanceof breakout.entity.NormalBrick) b.color = targetBrickColor;
        }
    }
    
    public void applyBallSkin() {
        if (ball == null) return;
        if (currentSkinIndex != -1 && currentSkinIndex < ballSkins.length && ballSkins[currentSkinIndex] != null) {
            ball.setSkin(ballSkins[currentSkinIndex]);
        } else {
            ball.setSkin(null); 
        }
    }
    
    public void startGameWithLevel(int level) {
        currentLevel = level;
        resetGame();
    }

    private void resetGame() {
        clearPowerStates();
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        paddle.setColor(colorList[paddleColorIndex]);
        paddle.setShapeType(paddleShapeIndex);
        
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        applyBallSkin(); 
        reapplySlowIfNeeded();
        
        if (currentLevel != 0) mapGenerator.loadLevel(currentLevel);
        else mapGenerator.bricks = levelEditor.getGeneratedBricks();
        
        powerUpManager.clear();
        applyCustomColors();
        score = 0; lives = 3; shakeTimer = 0; 
        comboCount = 0; 
        gameState = STATE_PLAY;
    }
    
    private void resetRound() {
        paddle.resetWidth();
        paddle.getPosition().x = WIDTH / 2 - 50; 
        paddle.getPosition().y = HEIGHT - 60;
        
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        applyBallSkin(); 
        reapplySlowIfNeeded();
        
        powerUpManager.clear();
        comboCount = 0; 
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

    public void setShopOverlay(ShopOverlayPanel shopOverlay) { this.shopOverlay = shopOverlay; }
    public void setShopOpener(Runnable shopOpener) { this.shopOpener = shopOpener; }
    public void openShop() { if (shopOpener != null) shopOpener.run(); }
    public void pauseForOverlay() { gameState = STATE_PAUSED; }
    public void resumeFromOverlay() { gameState = STATE_PAUSED; }

    public void resumeFromPause() { gameState = STATE_PLAY; wasEscPressed = false; }
    public void openSettingsFromPause() { previousState = STATE_PAUSED; transitionTo(STATE_SETTINGS); }
    public void gotoMenuFromPause() { transitionTo(STATE_MENU); }

    public int getScore() { return score; }
    public void spendScore(int amount) { score -= amount; if (score < 0) score = 0; }
    public void applyLongPaddleFromShop() { paddle.expand(); }
    public void applySlowBallFromShop() { activateSlowBall(); }
    public void addLifeFromShop() { lives++; }
    public void applyPierceFromShop() { activatePiercingBall(); }
    public void applyDoubleScoreFromShop() { activateDoubleScore(); }
    public MouseHandler getMouseHandler() { return mouseHandler; }
    public SoundManager getSoundManager() { return soundManager; }
<<<<<<< HEAD

=======
>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
    public String applyLuckyDrawFromShop() {
        LuckyPrize prize = rollLuckyPrize();
        switch (prize) {
            case EXTRA_LIFE:
                addLife();
                return "행운! 체력 +1";
            case WIDE_PADDLE:
                paddle.expand();
                return "패들 확장!";
            case SLOW_BALL:
                activateSlowBall();
                return "볼 슬로우 10초";
            case PIERCING_BALL:
                activatePiercingBall();
                return "관통 볼 10초";
            case DOUBLE_SCORE:
                activateDoubleScore();
                return "더블 스코어 15초";
            default:
                return "행운 실패..?";
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
    
    private void activateDoubleScore() {
        doubleScoreActive = true;
        doubleScoreTimer = DOUBLE_SCORE_DURATION;
    }
    
    private void activatePiercingBall() {
        piercingActive = true;
        piercingTimer = PIERCE_DURATION;
    }
    
    private void activateSlowBall() {
        if (ball != null && !slowBallActive) {
            ball.getVelocity().x *= SLOW_FACTOR;
            ball.getVelocity().y *= SLOW_FACTOR;
        }
        slowBallActive = true;
        slowBallTimer = SLOW_DURATION;
    }
    
    private void disableSlowBall() {
        if (ball != null) {
            ball.getVelocity().x /= SLOW_FACTOR;
            ball.getVelocity().y /= SLOW_FACTOR;
        }
        slowBallActive = false;
        slowBallTimer = 0;
    }
    
    private void reapplySlowIfNeeded() {
        if (slowBallActive && ball != null) {
            ball.getVelocity().x *= SLOW_FACTOR;
            ball.getVelocity().y *= SLOW_FACTOR;
        }
    }
    
    private void clearPowerStates() {
        doubleScoreActive = false;
        doubleScoreTimer = 0;
        piercingActive = false;
        piercingTimer = 0;
        if (slowBallActive) {
            disableSlowBall();
        } else {
            slowBallTimer = 0;
        }
    }
    
    private void tickPowerTimers() {
        if (doubleScoreActive) {
            doubleScoreTimer--;
            if (doubleScoreTimer <= 0) doubleScoreActive = false;
        }
        if (piercingActive) {
            piercingTimer--;
            if (piercingTimer <= 0) piercingActive = false;
        }
        if (slowBallActive) {
            slowBallTimer--;
            if (slowBallTimer <= 0) disableSlowBall();
        }
    }
    
    private void addScoreWithMultiplier(int amount) {
        score += doubleScoreActive ? amount * 2 : amount;
    }

    public void transitionTo(int nextState) {
        if (isFading) return;
        this.nextGameState = nextState;
        this.isFading = true;
        this.isFadeOut = true; 
        this.fadeAlpha = 0.0f;
    }
    
    public boolean isCRTFilterOn() { return isCRTFilterOn; }
    public void toggleCRTFilter() { isCRTFilterOn = !isCRTFilterOn; }

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
        if (isFading) {
            if (isFadeOut) {
                fadeAlpha += FADE_SPEED;
                if (fadeAlpha >= 1.0f) {
                    fadeAlpha = 1.0f;
                    if (nextGameState != -1) {
                        gameState = nextGameState;
                    }
                    isFadeOut = false; 
                }
            } else {
                fadeAlpha -= FADE_SPEED;
                if (fadeAlpha <= 0.0f) {
                    fadeAlpha = 0.0f;
                    isFading = false; 
                }
            }
        }

        if (shopOverlay != null && shopOverlay.isVisible()) {
            shopOverlay.updateOverlay(mouseHandler);
            return; 
        }

        if (shakeTimer > 0) shakeTimer--;
        if (comboScale > 1.0f) comboScale -= 0.05f;

        inputManager.update();
        effectManager.update(); 
        
        if (!inputManager.escape) {
            wasEscPressed = false;
        }
        
        if (!isFading || !isFadeOut) { 
            switch (gameState) {
                case STATE_MENU: updateMenu(); break;
                case STATE_USER_CUSTOM: updateUserCustom(); break;
                case STATE_LEVEL_SELECT: 
                    if (levelSelectPanel != null && levelSelectPanel.update(mouseHandler)) { 
                        transitionTo(STATE_MENU); 
                    } 
                    break;
                case STATE_PLAY: updatePlay(); break;
                case STATE_PAUSED: 
                    if (pausePanel != null) pausePanel.update(mouseHandler); 
                    if (inputManager.escape && !wasEscPressed) {
                        soundManager.playClickSound();
                        resumeFromPause(); 
                        wasEscPressed = true; 
                    }
                    break;
                case STATE_GAME_OVER:
                case STATE_VICTORY: updateResult(); break;
                case STATE_SETTINGS: 
                    if (settingsPanel != null && settingsPanel.update(mouseHandler)) { 
                        transitionTo(previousState); 
                    } 
                    break;
                case STATE_EDITOR: 
                    updateEditor(); 
                    break;
                case STATE_LEADERBOARD:
                    if (leaderboardPanel != null && leaderboardPanel.update(mouseHandler)) {
                        transitionTo(STATE_USER_CUSTOM);
                    }
                    break;
                case STATE_ACHIEVEMENTS:
                    achBackButton.update(mouseHandler);
                    if(achBackButton.isClicked(mouseHandler)) transitionTo(STATE_USER_CUSTOM);
<<<<<<< HEAD
=======
                    break;
                // ★ 이름 입력 상태 업데이트 로직 추가
                case STATE_NAME_INPUT:
                    if (nameModal != null) {
                        nameModal.update(mouseHandler, soundManager);
                        if (nameModal.isFinished()) {
                            if (!nameModal.isCancelled()) {
                                scoreManager.addScore(nameModal.getInputName(), score);
                            }
                            transitionTo(STATE_LEADERBOARD); // 입력 완료 후 랭킹으로 이동
                            nameModal = null;
                        }
                    }
>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
                    break;
            }
        }
    }
    
    private void updateMenu() {
        startButton.update(mouseHandler); 
        userCustomButton.update(mouseHandler);
        settingsButton.update(mouseHandler); 
        exitButton.update(mouseHandler);
        
        if (startButton.isClicked(mouseHandler)) { transitionTo(STATE_LEVEL_SELECT); }
        if (userCustomButton.isClicked(mouseHandler)) { transitionTo(STATE_USER_CUSTOM); }
        if (settingsButton.isClicked(mouseHandler)) { 
            previousState = STATE_MENU; 
            transitionTo(STATE_SETTINGS); 
        }
        if (exitButton.isClicked(mouseHandler)) { System.exit(0); }
    }

    private void updateUserCustom() {
        leaderboardButton.update(mouseHandler);
        achievementButton.update(mouseHandler);
        editorButton.update(mouseHandler);
        ucBackButton.update(mouseHandler);
        
        if (leaderboardButton.isClicked(mouseHandler)) { previousState = STATE_USER_CUSTOM; transitionTo(STATE_LEADERBOARD); }
        if (achievementButton.isClicked(mouseHandler)) { transitionTo(STATE_ACHIEVEMENTS); }
        if (editorButton.isClicked(mouseHandler)) { transitionTo(STATE_EDITOR); }
        if (ucBackButton.isClicked(mouseHandler)) { transitionTo(STATE_MENU); }
    }
    
    private void updateEditor() {
        levelEditor.update(mouseHandler);
        if (levelEditor.getExitButton().isClicked(mouseHandler)) {
            transitionTo(STATE_USER_CUSTOM);
        }
    }
    
    private void updatePlay() {
        tickPowerTimers();
        paddle.update();
        ball.update();

        CollisionDetector.handleWallCollision(ball, 0, 0, WIDTH, HEIGHT, soundManager);
        
        if (inputManager.escape && !wasEscPressed) {
            soundManager.playClickSound(); gameState = STATE_PAUSED; wasEscPressed = true; return;
        }
        
        powerUpManager.update(this, paddle);
        
        if (CollisionDetector.isColliding(ball, paddle)) {
            CollisionDetector.handlePaddleCollision(ball, paddle);

            if (ball.getVelocity().y < 0) { 
                startShake(5);
                soundManager.playHitSound();
                comboCount = 0; 
            }
        }
        
        for (Brick brick : mapGenerator.bricks) {
            if (!brick.isDestroyed) {
                if (ball.getBounds().intersects(brick.getBounds())) {
                    if (!piercingActive) {
                        CollisionDetector.resolveBallVsRect(ball, brick);
                    }
                    brick.hit();
                    
                    comboCount++;
                    comboScale = 2.0f + (comboCount * 0.1f); 
                    if (comboScale > 3.0f) comboScale = 3.0f;
                    
                    int bonus = (comboCount > 1) ? (comboCount * 10) : 0;
                    addScoreWithMultiplier(brick.scoreValue + bonus);
                    
                    achievementManager.unlock("첫 걸음");

                    if (score >= 10000){
                        achievementManager.unlock("고득점자");
                    }

                    if (brick.isDestroyed) {
                        soundManager.playExplodeSound();
                        if (brick instanceof breakout.entity.ExplosiveBrick) triggerExplosion(brick);
                        effectManager.createExplosion(brick.getPosition().x+40, brick.getPosition().y+15, brick.color);
                        powerUpManager.maybeSpawn(brick.getPosition().x+40, brick.getPosition().y+15);
                        startShake(15 + Math.min(comboCount, 10)); 
                    } else {
                        soundManager.playHitSound();
                        startShake(5);
                    }
                    if (!piercingActive) {
                        break; 
                    }
                }
            }
        }

        if (ball.getPosition().y > HEIGHT) {
            lives--;
            startShake(20);
            if (lives > 0) {
                soundManager.playFailSound();
                resetRound(); 
            } else {
                soundManager.playFailSound();
                gameState = STATE_GAME_OVER;
                promptAndAddScore(score);
            }
        }
        long remainingBricks = mapGenerator.bricks.stream().filter(b -> !b.isDestroyed).count();
        if (remainingBricks == 0) {
            if (lives == 3) {
                achievementManager.unlock("생존 전문가");
            }
        }
        if (mapGenerator.bricks.stream().noneMatch(b -> !b.isDestroyed)) {
            gameState = STATE_VICTORY;
            promptAndAddScore(score);
        }
    }
    
    // ★ JOptionPane 대체 메서드 수정
    private void promptAndAddScore(int score) {
        if (!scoreManager.isHighScore(score)) return;
<<<<<<< HEAD
        String name = JOptionPane.showInputDialog(null, "랭킹 등록! 이름을 입력하세요 (최대 10자):", "새로운 기록!", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) name = "익명";
        if (name.length() > 10) name = name.substring(0,10);
        scoreManager.addScore(name, score);
=======
        
        // 커스텀 모달 생성
        nameModal = new NameInputModal(WIDTH/2 - 200, HEIGHT/2 - 100, 400, 200);
        gameState = STATE_NAME_INPUT; // 상태 변경
>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
    }
    
    private void updateResult() {
        if (isFading) return; 

        restartButton.update(mouseHandler);
        menuButton.update(mouseHandler);
        
        if (gameState == STATE_VICTORY) {
            victoryLevelButton.update(mouseHandler); 
            if (victoryLevelButton.isClicked(mouseHandler)) {
                transitionTo(STATE_LEVEL_SELECT); 
            }
        }
        
        if (restartButton.isClicked(mouseHandler)) { 
            startGameWithLevel(currentLevel); 
        }
        if (menuButton.isClicked(mouseHandler)) { transitionTo(STATE_MENU); }
    }
    
    public boolean isSoundOn() { return isSoundOn; }
    public void toggleSound() { isSoundOn = !isSoundOn; soundManager.setMute(!isSoundOn); }
    public void prevBackground() { currentBgIndex = (currentBgIndex-1+6)%6; changeBackgroundBGM(); }
    public void nextBackground() { currentBgIndex = (currentBgIndex+1)%6; changeBackgroundBGM(); }

    public int getBallColorIndex() { return ballColorIndex; }
    public void setBallColorIndex(int idx) { ballColorIndex = idx; }
    public void cycleBallColor() { ballColorIndex = (ballColorIndex+1)%colorList.length; }
    public String getBallColorName() { return colorNames[ballColorIndex]; }

    public int getBallSkinIndex() { return currentSkinIndex; }
    public void setBallSkinIndex(int idx) { currentSkinIndex = idx; }
    public void cycleBallSkin() { currentSkinIndex++; if (currentSkinIndex >= 4) currentSkinIndex = -1; applyBallSkin(); }
    public String getBallSkinName() { return (currentSkinIndex == -1) ? "없음" : "학생회 " + (currentSkinIndex + 1); }

    public int getPaddleColorIndex() { return paddleColorIndex; }
    public void setPaddleColorIndex(int idx) { paddleColorIndex = idx; if (paddle != null) paddle.setColor(colorList[paddleColorIndex]); }
    public void cyclePaddleColor() { paddleColorIndex = (paddleColorIndex + 1) % colorList.length; if (paddle != null) paddle.setColor(colorList[paddleColorIndex]); }
    public String getPaddleColorName() { return colorNames[paddleColorIndex]; }

    public int getPaddleShapeIndex() { return paddleShapeIndex; }
    public void setPaddleShapeIndex(int idx) { paddleShapeIndex = idx; if (paddle != null) paddle.setShapeType(paddleShapeIndex); }
    public void cyclePaddleShape() { paddleShapeIndex = (paddleShapeIndex + 1) % 4; if (paddle != null) paddle.setShapeType(paddleShapeIndex); }
    public String getPaddleShapeName() { return shapeNames[paddleShapeIndex]; }

    public int getBrickColorIndex() { return brickColorIndex; }
    public void setBrickColorIndex(int idx) { brickColorIndex = idx; applyCustomColors(); }
    public void cycleBrickColor() { brickColorIndex = (brickColorIndex+1)%colorList.length; applyCustomColors(); }
    public String getBrickColorName() { return colorNames[brickColorIndex]; }

    public Paddle getPaddle() { return paddle; }
    
    private void changeBackgroundBGM() {
        if (isSoundOn) {
            soundManager.playBGM("Bgm.wav");
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D dbg = (Graphics2D) g;
        dbg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (gameState != STATE_MENU && gameState != STATE_USER_CUSTOM) {
            if (backgrounds != null && backgrounds[currentBgIndex] != null) dbg.drawImage(backgrounds[currentBgIndex], 0, 0, WIDTH, HEIGHT, null);
            else { dbg.setColor(Color.BLACK); dbg.fillRect(0, 0, WIDTH, HEIGHT); }
        } else if (gameState == STATE_MENU || gameState == STATE_USER_CUSTOM) {
             if (menuGifImage != null) dbg.drawImage(menuGifImage, 0, 0, WIDTH, HEIGHT, this);
             else { dbg.setColor(Color.BLACK); dbg.fillRect(0, 0, WIDTH, HEIGHT); }
        }
        
        int sx = 0, sy = 0;
        if (shakeTimer > 0) { 
            int intensity = 5 + (comboCount > 2 ? 5 : 0); 
            sx = (int)(Math.random() * intensity * 2 - intensity); 
            sy = (int)(Math.random() * intensity * 2 - intensity); 
            dbg.translate(sx, sy); 
        }
        
        switch (gameState) {
            case STATE_MENU: drawMenu(dbg); break;
            case STATE_USER_CUSTOM: drawUserCustom(dbg); break;
            case STATE_LEVEL_SELECT: 
                if (levelSelectPanel != null) levelSelectPanel.draw(dbg, customFont);
                break;
            case STATE_PLAY:
                mapGenerator.draw(dbg); paddle.draw(dbg); 
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg); 
                effectManager.draw(dbg); powerUpManager.draw(dbg); drawHUD(dbg); break;
            case STATE_PAUSED:
                mapGenerator.draw(dbg); paddle.draw(dbg); 
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg); 
                if (pausePanel != null) pausePanel.draw(dbg, customFont); break;
            case STATE_GAME_OVER:
                mapGenerator.draw(dbg); paddle.draw(dbg); 
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg); 
                effectManager.draw(dbg); drawResult(dbg, "GAME OVER", Color.RED); break;
            case STATE_VICTORY:
                mapGenerator.draw(dbg); paddle.draw(dbg); 
                dbg.setColor(colorList[ballColorIndex]); ball.draw(dbg); 
                effectManager.draw(dbg); drawResult(dbg, "STAGE CLEAR!", Color.GREEN); break;
            case STATE_SETTINGS: 
                if (settingsPanel != null) settingsPanel.draw(dbg, customFont); 
                break;
            case STATE_EDITOR: levelEditor.draw(dbg, customFont); break;
            case STATE_LEADERBOARD:
                if (leaderboardPanel != null) leaderboardPanel.draw(dbg, customFont);
                break;
            case STATE_ACHIEVEMENTS:
                drawAchievements(dbg);
                break;
<<<<<<< HEAD
=======
            // ★ 이름 입력 화면 그리기 로직 추가
            case STATE_NAME_INPUT: 
                if (nameModal != null) nameModal.draw(dbg, customFont); 
                break;
>>>>>>> 60c52665f5022d594caa88e988f44ff0f292a8c2
        }
        
        if (sx != 0 || sy != 0) dbg.translate(-sx, -sy);
        
        if (isFading && fadeAlpha > 0.01f) {
            float alpha = Math.max(0.0f, Math.min(1.0f, fadeAlpha));
            dbg.setColor(new Color(0, 0, 0, alpha)); 
            dbg.fillRect(0, 0, WIDTH, HEIGHT);
        }

        if (isCRTFilterOn) {
            dbg.setColor(new Color(0, 0, 0, 50)); 
            for (int y = 0; y < HEIGHT; y += 4) { 
                dbg.fillRect(0, y, WIDTH, 2);
            }
            
            java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(WIDTH / 2, HEIGHT / 2);
            float radius = WIDTH;
            float[] dist = {0.0f, 0.5f, 1.0f};
            Color[] colors = {
                new Color(0, 0, 0, 0),    
                new Color(0, 0, 0, 0),    
                new Color(0, 0, 0, 150)   
            };
            java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(center, radius, dist, colors);
            dbg.setPaint(p);
            dbg.fillRect(0, 0, WIDTH, HEIGHT);
        }

        if (gameState == STATE_MENU || gameState == STATE_USER_CUSTOM) {
            Toolkit.getDefaultToolkit().sync(); 
        }
    }
    
    private void drawCentered3DText(Graphics2D g2, String text, int y, Color mainColor, Color shadowColor, float size) {
        if (customFont != null) g2.setFont(customFont.deriveFont(Font.BOLD, size));
        else g2.setFont(new Font("SansSerif", Font.BOLD, (int)size));
        
        FontMetrics fm = g2.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        
        g2.setColor(shadowColor);
        for(int i = 5; i > 0; i--) g2.drawString(text, x + i, y + i);
        g2.setColor(mainColor);
        g2.drawString(text, x, y);
    }
    
    private void drawMenu(Graphics2D g2) {
        drawCentered3DText(g2, "샤갈적인 벽돌깨기", 150, Color.YELLOW, Color.DARK_GRAY, 70f);
        drawCentered3DText(g2, "⚜️태풍을 부르는 학생회의 반란⚜️", 210, Color.WHITE, Color.BLACK, 30f);
        
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        drawCenteredString(g2, "HIGH SCORE: " + scoreManager.getHighScore(), WIDTH/2, 550);
        
        startButton.draw(g2, customFont); 
        userCustomButton.draw(g2, customFont);
        settingsButton.draw(g2, customFont); 
        exitButton.draw(g2, customFont);
    }

    private void drawUserCustom(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawCentered3DText(g2, "커스텀 메뉴", 180, Color.GREEN, Color.DARK_GRAY, 50f);

        leaderboardButton.draw(g2, customFont);
        achievementButton.draw(g2, customFont);
        editorButton.draw(g2, customFont);
        ucBackButton.draw(g2, customFont);
    }
    
    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, WIDTH, 40);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 24));
        
        g2.drawString("SCORE: " + score, 20, 28);
        g2.drawString("LIVES:", WIDTH - 200, 28); 

        int maxHearts = 3;
        int heartsToDraw = Math.min(lives, maxHearts);
        int extraLives = lives - maxHearts;

        for (int i = 0; i < heartsToDraw; i++) {
            drawHeart(g2, WIDTH - 120 + (i * 30), 10);
        }

        if (extraLives > 0) {
            g2.setColor(Color.yellow);
            g2.setFont(new Font("Consolas", Font.BOLD, 20));
            g2.drawString("+" + extraLives, WIDTH - 25, 28);
        }
        
        drawActiveBuffs(g2);
        
        if (comboCount >= 2) {
            int fontSize = (int)(40 * comboScale); 
            g2.setFont(new Font("Consolas", Font.BOLD, fontSize));
            
            long time = System.currentTimeMillis();
            Color[] flashingColors = { Color.RED, Color.ORANGE, Color.YELLOW, Color.WHITE, Color.MAGENTA, Color.CYAN };
            int colorIndex = (int)((time / 50) % flashingColors.length); 
            Color mainColor = flashingColors[colorIndex];
            
            String comboText = comboCount + " COMBO!";
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(comboText);
            
            int jitterX = 0, jitterY = 0;
            if (comboScale > 1.2f) { 
                jitterX = (int)(Math.random() * 10 - 5);
                jitterY = (int)(Math.random() * 10 - 5);
            }
            
            int drawX = WIDTH/2 - tw/2 + jitterX;
            int drawY = 80 + jitterY;

            g2.setColor(Color.DARK_GRAY);
            for (int i = 1; i <= 8; i++) {
                g2.drawString(comboText, drawX + i, drawY + i);
            }
            g2.setColor(mainColor);
            g2.drawString(comboText, drawX, drawY);
        }
    }
    
    private void drawHeart(Graphics2D g2, int x, int y) {
        g2.setColor(Color.RED); g2.fillOval(x, y, 10, 10); g2.fillOval(x + 10, y, 10, 10); 
        int[] xp = {x, x + 10, x + 20}; int[] yp = {y + 5, y + 20, y + 5}; g2.fillPolygon(xp, yp, 3);
    }
    
    private void drawActiveBuffs(Graphics2D g2) {
        ArrayList<String> status = new ArrayList<>();
        if (doubleScoreActive) status.add("2x SCORE " + formatTimer(doubleScoreTimer));
        if (piercingActive) status.add("PIERCE " + formatTimer(piercingTimer));
        if (slowBallActive) status.add("SLOW " + formatTimer(slowBallTimer));
        if (status.isEmpty()) return;
        
        String text = String.join("  |  ", status);
        g2.setFont(new Font("Consolas", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int boxW = fm.stringWidth(text) + 20;
        int boxH = 22;
        int x = 10;
        int y = 45;
        
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(x, y - boxH + 6, boxW, boxH, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 10, y - 6);
    }
    
    private String formatTimer(int ticks) {
        int sec = (int)Math.ceil(ticks / (double)FPS);
        return sec + "s";
    }

    private void drawAchievements(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));
        drawCenteredString(g2, "ACHIEVEMENTS", WIDTH / 2, 80);
        
        g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
        List<Achievement> list = achievementManager.getAchievements(); 
        
        for (int i = 0; i < list.size(); i++) {
            Achievement a = list.get(i);
            int y = 150 + (i * 60);
            
            g2.setColor(a.isUnlocked ? Color.GREEN : Color.GRAY);
            g2.drawString(a.isUnlocked ? "✔ " + a.title : "🔒 " + a.title, 150, y);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Malgun Gothic", Font.ITALIC, 14));
            g2.drawString(a.description, 150, y + 20);
            g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
        }
    
        achBackButton.draw(g2, customFont); 
    }
    
    private void drawResult(Graphics2D g2, String title, Color color) {
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(color); g2.setFont(new Font("Arial", Font.BOLD, 50));
        drawCenteredString(g2, title, WIDTH/2, 200);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Consolas", Font.BOLD, 30));
        
        drawCenteredString(g2, "Final Score: " + score, WIDTH/2, 280); 
        
        restartButton.draw(g2, customFont);
        
        if (gameState == STATE_VICTORY) {
            victoryLevelButton.draw(g2, customFont);
        }
        
        menuButton.draw(g2, customFont);
    }
    
    public void drawCenteredString(Graphics2D g, String text, int x, int y) {
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
                    addScoreWithMultiplier(b.scoreValue);
                    effectManager.createExplosion(b.getPosition().x+40, b.getPosition().y+15, b.color);
                }
            }
        }
        startShake(20); 
    }
    
    public Color getCurrentBallColor() {
        return colorList[ballColorIndex];
    }
}