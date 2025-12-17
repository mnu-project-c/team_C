package kr.ac.mnu.c_team.breakout.engine;

/**
 * 고정 FPS 기반 게임 루프를 제공하는 클래스.
 * 
 * 별도의 스레드에서 동작하며, 지정한 FPS에 맞춰
 * Updatable 객체의 update()를 반복 호출한다.
 *
 * 예:
 *   GameLoop loop = new GameLoop(gamePanel, 60);
 *   loop.start();
 *
 * gamePanel은 Updatable을 구현하고 있어야 한다.
 * 
 * @author 조한흠
 * @version 1.0
 * @since 2025-12-16
 */
public class GameLoop implements Runnable {

    /** 루프 대상 (게임 상태를 갱신하는 객체) */
    private final Updatable updatable;

    /** 목표 FPS (초당 프레임 수) */
    private final int targetFps;

    /** 루프 스레드 */
    private Thread loopThread;

    /** 루프 실행 여부 플래그 */
    private volatile boolean running = false;

    /**
     * GameLoop 생성자.
     *
     * @param updatable 매 프레임 update()가 호출될 대상
     * @param targetFps 목표 FPS (예: 60)
     */
    public GameLoop(Updatable updatable, int targetFps) {
        if (updatable == null) {
            throw new IllegalArgumentException("updatable must not be null");
        }
        if (targetFps <= 0) {
            throw new IllegalArgumentException("targetFps must be > 0");
        }
        this.updatable = updatable;
        this.targetFps = targetFps;
    }

    /**
     * 게임 루프 시작 메소드.
     * 이미 실행 중이면 아무 것도 하지 않는다.
     */
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        loopThread = new Thread(this, "GameLoop-Thread");
        loopThread.start();
    }

    /**
     * 게임 루프 정지 메소드.
     * running 플래그를 내려서 run() 루프를 종료시킨다.
     */
    public synchronized void stop() {
        running = false;
        if (loopThread != null && loopThread.isAlive()) {
            try {
                loopThread.join();
            } catch (InterruptedException e) {
                // 인터럽트 발생 시 그냥 무시
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        // 나노초 단위로 한 프레임 간격 계산
        final double frameInterval = 1_000_000_000.0 / targetFps;
        double nextFrameTime = System.nanoTime() + frameInterval;

        while (running) {
            // 1. 게임 상태 업데이트
            try {
                updatable.update();
            } catch (Exception e) {
                // 예외가 발생해도 루프가 바로 죽지 않도록 방어
                e.printStackTrace();
            }

            // 2. 남은 시간 계산 후 sleep
            double remaining = nextFrameTime - System.nanoTime();
            remaining /= 1_000_000.0; // 나노초 → 밀리초 변환

            if (remaining > 0) {
                try {
                    Thread.sleep((long) remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // 인터럽트 시 루프 종료
                }
            } else {
                // 남은 시간이 0보다 작으면 (프레임이 밀렸을 때)
                // 그냥 다음 프레임으로 넘어가되, nextFrameTime만 뒤로 밀어준다.
            }

            nextFrameTime += frameInterval;
        }
    }
}
