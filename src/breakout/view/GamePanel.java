package kr.ac.mnu.c_team.breakout.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import kr.ac.mnu.c_team.breakout.engine.CollisionDetector;
import kr.ac.mnu.c_team.breakout.entity.Ball;
import kr.ac.mnu.c_team.breakout.entity.Brick;
import kr.ac.mnu.c_team.breakout.entity.Paddle;
import kr.ac.mnu.c_team.breakout.entity.ExplosiveBrick;
import kr.ac.mnu.c_team.breakout.manager.EffectManager;
import kr.ac.mnu.c_team.breakout.manager.InputManager;
import kr.ac.mnu.c_team.breakout.manager.MapGenerator;
import kr.ac.mnu.c_team.breakout.manager.MouseHandler;
import kr.ac.mnu.c_team.breakout.manager.PowerUpManager;
import kr.ac.mnu.c_team.breakout.manager.ScoreManager;


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
    
    private BufferedImage dbImage;
    private Graphics2D dbg;
    private Thread gameThread;
    private boolean running = false;
    private final int FPS = 60;
    
    private InputManager inputManager;
    private MouseHandler mouseHandler;
    private EffectManager effectManager;
    private ScoreManager scoreManager;
    private PowerUpManager powerUpManager;
    
    private Paddle paddle;
    private Ball ball;
    private MapGenerator mapGenerator;
    
    // UI 버튼들
    private GameButton startButton, settingsButton, exitButton;
    private GameButton restartButton, menuButton;
    private GameButton soundButton, backButton;
    private GameButton resumeButton;
    private GameButton prevBgButton, nextBgButton;
    private GameButton ballColorButton, brickColorButton;
    private GameButton lvl1Button, lvl2Button, lvl3Button, lvlBackButton;
    
    private int gameState = STATE_MENU;
    private int score = 0;
    private int lives = 3; 
    private boolean isSoundOn = true;
    
    private int shakeTimer = 0;
    private boolean wasEscPressed = false;
    
    private BufferedImage[] backgrounds;
    private int currentBgIndex = 0;
    
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
        
        loadBackgrounds();
        initGameObjects(); 
        initUI();          
        applyCustomColors();
    }
    
    private void loadBackgrounds() {
        backgrounds = new BufferedImage[6];
        try {
            for(int i=0; i<6; i++) {
                backgrounds[i] = ImageIO.read(new File("assets/bg" + (i+1) + ".jpg"));
            }
        } catch (IOException e) { }
    }
    
    private void initUI() {
        int centerX = WIDTH / 2 - 100;
        
        startButton = new GameButton(centerX, 250, 200, 50, "GAME START");
        settingsButton = new GameButton(centerX, 320, 200, 50, "SETTINGS");
        exitButton = new GameButton(centerX, 390, 200, 50, "EXIT GAME");
        
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
        lvlBackButton = new GameButton(centerX, 450, 200, 50, "BACK");
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
            if (b instanceof kr.ac.mnu.c_team.breakout.entity.NormalBrick) {
            	b.color = targetBrickColor;
            }
        }
    }
    
    private void startGameWithLevel(int level) {
        currentLevel = level;
        resetGame();
    }

    private void resetGame() {
        paddle = new Paddle(WIDTH / 2 - 50, HEIGHT - 60, inputManager);
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        mapGenerator.loadLevel(currentLevel);
        
        powerUpManager.clear();
        
        applyCustomColors();
        score = 0;
        lives = 3;
        shakeTimer = 0;
        gameState = STATE_PLAY;
    }
    
    private void resetRound() {
        paddle.resetWidth();
        paddle.getPosition().x = WIDTH / 2 - 50; // GameObject의 position 직접 접근 대신 getter 사용
        paddle.getPosition().y = HEIGHT - 60;
        ball = new Ball(WIDTH / 2 - 10, HEIGHT - 100);
        powerUpManager.clear();
        try { Thread.sleep(500); } catch (Exception e) {}
    }
    
    // 아이템 효과
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
            render();
            draw();
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1000000;
                if(remainingTime < 0) remainingTime = 0;
                Thread.sleep((long)remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
    
    private void update() {
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
        }
    }
    
    private void updateMenu() {
        startButton.update(mouseHandler);
        settingsButton.update(mouseHandler);
        exitButton.update(mouseHandler);
        if (startButton.isClicked(mouseHandler)) gameState = STATE_LEVEL_SELECT;
        if (settingsButton.isClicked(mouseHandler)) gameState = STATE_SETTINGS;
        if (exitButton.isClicked(mouseHandler)) System.exit(0);
    }
    
    private void updateLevelSelect() {
        lvl1Button.update(mouseHandler);
        lvl2Button.update(mouseHandler);
        lvl3Button.update(mouseHandler);
        lvlBackButton.update(mouseHandler);
        if (lvl1Button.isClicked(mouseHandler)) startGameWithLevel(1);
        if (lvl2Button.isClicked(mouseHandler)) startGameWithLevel(2);
        if (lvl3Button.isClicked(mouseHandler)) startGameWithLevel(3);
        if (lvlBackButton.isClicked(mouseHandler)) gameState = STATE_MENU;
    }
    
    private void updatePlay() {
        if (inputManager.escape && !wasEscPressed) {
            gameState = STATE_PAUSED;
            wasEscPressed = true;
            return;
        }
        if (!inputManager.escape) wasEscPressed = false;
        
        paddle.update();
        ball.update();
        powerUpManager.update(this, paddle);
        
        if (CollisionDetector.isColliding(ball, paddle)) {
            
            // 1. 패들 전용 충돌 처리
            CollisionDetector.handlePaddleCollision(ball, paddle);
            // 2. 이펙트 (공이 튕겨 나갔을 때만 흔들기)
            if (ball.getVelocity().y < 0) { 
                startShake(5);
            }
        }
        
        for (Brick brick : mapGenerator.bricks) {
            if (!brick.isDestroyed) {
                // 충돌 감지 (CollisionDetector 사용 권장)
                if (ball.getBounds().intersects(brick.getBounds())) {
                    
                    // 1. 물리 반사 (앞서 적용한 코드)
                    kr.ac.mnu.c_team.breakout.engine.CollisionDetector.resolveBallVsRect(ball, brick);

                    // 2. 벽돌 타격
                    brick.hit();
                    score += brick.scoreValue;
                    
                    // 3. ★ 폭발 벽돌인지 확인 후 폭발 실행
                    if (brick instanceof kr.ac.mnu.c_team.breakout.entity.ExplosiveBrick) {
                        triggerExplosion(brick);
                    }

                    // 4. 이펙트 생성
                    double cx = brick.getPosition().x + brick.getWidth()/2;
                    double cy = brick.getPosition().y + brick.getHeight()/2;
                    effectManager.createExplosion(cx, cy, brick.color);
                    powerUpManager.maybeSpawn(cx, cy);
                    
                    if (!(brick instanceof kr.ac.mnu.c_team.breakout.entity.ExplosiveBrick)) {
                         startShake(5); // 일반 벽돌은 살짝만 흔들림
                    }
                    
                    break;
                }
            }
        }
        
        if (ball.getPosition().y > HEIGHT) {
            lives--;
            startShake(20);
            if (lives > 0) resetRound();
            else {
                gameState = STATE_GAME_OVER;
                scoreManager.saveHighScore(score);
            }
        }
        
        long remainingBricks = mapGenerator.bricks.stream().filter(b -> !b.isDestroyed).count();
        if (remainingBricks == 0) {
            gameState = STATE_VICTORY;
            scoreManager.saveHighScore(score);
        }
    }
    
    private void updatePaused() {
        resumeButton.update(mouseHandler);
        menuButton.update(mouseHandler);
        if (resumeButton.isClicked(mouseHandler)) gameState = STATE_PLAY;
        if (menuButton.isClicked(mouseHandler)) gameState = STATE_MENU;
        if (inputManager.escape && !wasEscPressed) {
            gameState = STATE_PLAY;
            wasEscPressed = true;
        }
        if (!inputManager.escape) wasEscPressed = false;
    }
    
    private void updateResult() {
        restartButton.update(mouseHandler);
        menuButton.update(mouseHandler);
        if (restartButton.isClicked(mouseHandler)) startGameWithLevel(currentLevel);
        if (menuButton.isClicked(mouseHandler)) gameState = STATE_MENU;
    }
    
    private void updateSettings() {
        soundButton.update(mouseHandler);
        prevBgButton.update(mouseHandler);
        nextBgButton.update(mouseHandler);
        ballColorButton.update(mouseHandler);
        brickColorButton.update(mouseHandler);
        backButton.update(mouseHandler);
        
        if (soundButton.isClicked(mouseHandler)) {
            isSoundOn = !isSoundOn;
            soundButton = new GameButton(WIDTH/2 - 100, 150, 200, 50, "SOUND: " + (isSoundOn ? "ON" : "OFF"));
        }
        if (backgrounds != null) {
            if (prevBgButton.isClicked(mouseHandler)) {
                currentBgIndex--;
                if (currentBgIndex < 0) currentBgIndex = backgrounds.length - 1;
            }
            if (nextBgButton.isClicked(mouseHandler)) {
                currentBgIndex++;
                if (currentBgIndex >= backgrounds.length) currentBgIndex = 0;
            }
        }
        if (ballColorButton.isClicked(mouseHandler)) {
            ballColorIndex++;
            if (ballColorIndex >= colorList.length) ballColorIndex = 0;
            ballColorButton = new GameButton(WIDTH/2 - 100, 310, 200, 50, "BALL: " + colorNames[ballColorIndex]);
        }
        if (brickColorButton.isClicked(mouseHandler)) {
            brickColorIndex++;
            if (brickColorIndex >= colorList.length) brickColorIndex = 0;
            brickColorButton = new GameButton(WIDTH/2 - 100, 390, 200, 50, "BRICK: " + colorNames[brickColorIndex]);
            applyCustomColors();
        }
        if (backButton.isClicked(mouseHandler)) gameState = STATE_MENU;
    }
    
    private void render() {
        if (dbImage == null) {
            dbImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            dbg = (Graphics2D) dbImage.getGraphics();
            dbg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        if (backgrounds != null && backgrounds[currentBgIndex] != null) {
            dbg.drawImage(backgrounds[currentBgIndex], 0, 0, WIDTH, HEIGHT, null);
        } else {
            dbg.setColor(Color.BLACK);
            dbg.fillRect(0, 0, WIDTH, HEIGHT);
        }
        
        int shakeX = 0, shakeY = 0;
        if (shakeTimer > 0) {
            shakeX = (int)(Math.random() * 10 - 5);
            shakeY = (int)(Math.random() * 10 - 5);
            dbg.translate(shakeX, shakeY);
            shakeTimer--;
        }
        
        switch (gameState) {
            case STATE_MENU:
                drawMenu(dbg);
                break;
            case STATE_LEVEL_SELECT:
                drawLevelSelect(dbg);
                break;
            case STATE_PLAY:
                mapGenerator.draw(dbg);
                paddle.draw(dbg);
                
                // ★ 3D 공 효과 적용: 색상만 지정하고 그리기 명령은 Ball에게 위임
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
            case STATE_SETTINGS:
                drawSettings(dbg);
                break;
        }
        
        if (shakeTimer > 0 || (shakeX != 0 || shakeY != 0)) {
            dbg.translate(-shakeX, -shakeY);
        }
        
        draw();
    }
    
    // UI 드로잉 메소드들 (변경 없음, 코드 길이상 생략하나 필요 시 이전 답변 참고하여 그대로 사용)
    private void drawLevelSelect(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));
        drawCenteredString(g2, "SELECT LEVEL", WIDTH/2, 120);
        lvl1Button.draw(g2);
        lvl2Button.draw(g2);
        lvl3Button.draw(g2);
        lvlBackButton.draw(g2);
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
        g2.setColor(Color.RED);
        g2.fillOval(x, y, 10, 10);      
        g2.fillOval(x + 10, y, 10, 10); 
        int[] xPoints = {x, x + 10, x + 20};
        int[] yPoints = {y + 5, y + 20, y + 5};
        g2.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void drawMenu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Consolas", Font.BOLD, 60));
        drawCenteredString(g2, "BREAKOUT", WIDTH/2, 120);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        drawCenteredString(g2, "Team C Project - 2025", WIDTH/2, 170);
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        drawCenteredString(g2, "HIGH SCORE: " + scoreManager.getHighScore(), WIDTH/2, 530);
        startButton.draw(g2);
        settingsButton.draw(g2);
        exitButton.draw(g2);
    }
    
    private void drawSettings(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));
        drawCenteredString(g2, "SETTINGS", WIDTH/2, 100);
        soundButton.draw(g2);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(Color.LIGHT_GRAY);
        drawCenteredString(g2, "Select Background (" + (currentBgIndex + 1) + "/6)", WIDTH/2, 220);
        prevBgButton.draw(g2);
        nextBgButton.draw(g2);
        ballColorButton.draw(g2);
        brickColorButton.draw(g2);
        backButton.draw(g2);
    }
    
    private void drawPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.ORANGE);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        drawCenteredString(g2, "PAUSED", WIDTH/2, 200);
        resumeButton.draw(g2);
        menuButton.draw(g2);
    }
    
    private void drawResult(Graphics2D g2, String title, Color color) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(color);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        drawCenteredString(g2, title, WIDTH/2, 200);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 30));
        drawCenteredString(g2, "Final Score: " + score, WIDTH/2, 280);
        if (score >= scoreManager.getHighScore() && score > 0) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            drawCenteredString(g2, "NEW HIGH SCORE!", WIDTH/2, 320);
        } else {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            drawCenteredString(g2, "High Score: " + scoreManager.getHighScore(), WIDTH/2, 320);
        }
        restartButton.draw(g2);
        menuButton.draw(g2);
    }
    
    private void drawCenteredString(Graphics2D g, String text, int x, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x - textWidth / 2, y);
    }
    
    private void draw() {
        Graphics g = getGraphics();
        if (g != null) {
            g.drawImage(dbImage, 0, 0, null);
            g.dispose();
        }
    }
    
    private void triggerExplosion(Brick centerBrick) {
        // 1. 폭발 범위 설정 (자신을 중심으로 3x3 크기 영역)
        int exX = (int) (centerBrick.getPosition().x - centerBrick.getWidth());
        int exY = (int) (centerBrick.getPosition().y - centerBrick.getHeight());
        int exW = (int) (centerBrick.getWidth() * 3);
        int exH = (int) (centerBrick.getHeight() * 3);
        
        Rectangle explosionArea = new Rectangle(exX, exY, exW, exH);

        // 2. 모든 벽돌을 검사하여 범위 안에 있으면 데미지 입힘
        for (Brick b : mapGenerator.bricks) {
            if (!b.isDestroyed && b != centerBrick) {
                if (explosionArea.intersects(b.getBounds())) {
                    b.hit(); // 주변 벽돌 데미지
                    
                    // 주변 벽돌도 깨지는 효과 연출
                    if (b.isDestroyed) {
                        score += b.scoreValue;
                        effectManager.createExplosion(
                            b.getPosition().x + b.getWidth()/2, 
                            b.getPosition().y + b.getHeight()/2, 
                            b.color
                        );
                    }
                }
            }
        }
        
        // 3. 화면을 강하게 흔들어 폭발 느낌 강조
        startShake(20); 
    }
}