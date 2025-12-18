package breakout.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class SoundManager {

    private static final String SOUND_PATH = "assets/";
    
<<<<<<< HEAD
=======
    // 기존 상수들 유지
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339
    public static final String SOUND_HIT = "hit.wav"; 
    public static final String SOUND_FAIL = "Fail.wav"; 
    public static final String SOUND_EXPLODE = "explode.wav"; 
    public static final String SOUND_CLICK = "click.wav"; 
    public static final String SOUND_VICTORY = "victory.wav";
    public static final String SOUND_POWERUP = "powerup.wav";
    public static final String SOUND_WALL    = "hit.wav";
    public static final String SOUND_BUY     = "buy.wav";
    public static final String SOUND_ERROR   = "error.wav";    
    public static final String SOUND_GAMEOVER = "GameOver.wav";
    public static final String SOUND_BOMB = "bomb.wav";

    private Map<String, Clip> clipCache;
    private Clip currentBgmClip;
<<<<<<< HEAD
    private String currentBgmFileName; 
=======
    private String currentBgmName = ""; // 현재/다음에 틀어야 할 브금 이름
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339
    private boolean isMuted = false;
    private float masterVolume = -10.0f;

    private static SoundManager INSTANCE = null;

    public static synchronized SoundManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SoundManager();
        return INSTANCE;
    }

    // GameButton 등에서 호출하는 메서드
    public static void playClick() {
        getInstance().playClickSound();
    }

    public SoundManager() {
        this.clipCache = new HashMap<>();
        preLoadSound(SOUND_HIT);
        System.out.println("[SoundManager] Audio System Initialized.");
    }
    
    private void preLoadSound(String fileName) {
        try { loadClip(fileName); } catch (Exception e) {}
    }

    // --- 효과음 메서드들 ---
    public void playHitSound() { playSound(SOUND_HIT); }
    public void playFailSound() { playSound(SOUND_FAIL); } 
    public void playExplodeSound() { playSound(SOUND_EXPLODE); } 
    public void playClickSound() { playSound(SOUND_CLICK); }
    public void playPowerupSound() { playSound(SOUND_POWERUP); }
    public void playWallSound()    { playSound(SOUND_WALL); }
    public void playBuySound()     { playSound(SOUND_BUY); }
    public void playErrorSound()   { playSound(SOUND_ERROR); }
<<<<<<< HEAD
    public void playGameOverSound() { playSound(SOUND_GAMEOVER); }
    public void playBombSound() { playSound(SOUND_BOMB);}
=======
    public void playBombSound()    { playSound(SOUND_BOMB); }

    // --- 핵심: 게임 오버/승리 후 브금 복구 로직 ---
    public void playGameOverSound() { playSpecialAndResume(SOUND_GAMEOVER); }
    public void playVictorySound()  { playSpecialAndResume(SOUND_VICTORY); }

    private void playSpecialAndResume(String fileName) {
        if (isMuted) return;
        
        stopBGM(); // 일단 지금 나오는 브금 중단

        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume);
                clip.setFramePosition(0);
                
                // [T-Point] 리스너: 특수음(게임오버 등)이 끝나면 브금 자동 재시작
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        if (!isMuted && !currentBgmName.isEmpty()) {
                            playBGM(currentBgmName);
                        }
                    }
                });
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339

    public void playSound(String fileName) {
        if (isMuted) return;
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                // 효과음 재생 시 이전 재생이 덜 끝났더라도 강제로 처음부터 다시 재생
                clip.stop(); 
                clip.setFramePosition(0);
                clip.setMicrosecondPosition(0); // 확실한 초기화
                setVolume(clip, masterVolume);
                clip.start();
            }
        } catch (Exception e) {}
    }

<<<<<<< HEAD
    // ★ 버그 수정된 BGM 재생 로직
    public void playBGM(String fileName) {
        if (isMuted) return;
        
        // 이미 해당 곡이 재생 중이라면 건너뜀 (끊김 방지)
        if (currentBgmClip != null && currentBgmClip.isRunning() && fileName.equals(currentBgmFileName)) {
             return; 
        }

        stopBGM(); // 기존 BGM 끄기
        
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                // ★ 핵심 수정: 재생 전 상태를 완전히 리셋
                clip.stop(); 
                clip.setFramePosition(0);
                clip.setMicrosecondPosition(0); // 프레임 위치와 마이크로초 위치 모두 0으로
                
                setVolume(clip, masterVolume - 5.0f);
                
                // ★ 수정: start() 대신 loop()만 호출 (loop가 재생을 시작함)
                clip.loop(Clip.LOOP_CONTINUOUSLY); 
                
=======
    // --- 브금 제어 로직 ---
    public void playBGM(String fileName) {
        this.currentBgmName = fileName;

        if (isMuted) {
            stopBGM();
            return;
        }

        // 이미 같은 곡이 나오고 있다면 무시
        if (currentBgmClip != null && currentBgmClip.isRunning() && fileName.equals(currentBgmName)) {
            return;
        }

        stopBGM();

        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume - 5.0f);
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339
                currentBgmClip = clip;
                currentBgmFileName = fileName;
            }
        } catch (Exception e) {
            System.out.println("브금 로드 실패: " + fileName);
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (currentBgmClip != null) {
            currentBgmClip.stop();
<<<<<<< HEAD
            // 멈출 때도 위치 초기화 (다음 재생을 위해)
            currentBgmClip.setFramePosition(0);
            currentBgmClip.setMicrosecondPosition(0);
            
=======
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339
            currentBgmClip = null;
            currentBgmFileName = null;
        }
    }

    // --- 설정 연동 (온오프) ---
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (isMuted) {
            stopAll(); // 모든 소리 즉시 정지
        } else {
            // 다시 켜면 아까 틀려고 했던 브금 재생
            if (!currentBgmName.isEmpty()) {
                playBGM(currentBgmName);
            }
        }
    }

    // 기존에 있던 중복 메서드 setMute는 삭제하거나 setMuted를 호출하게 함
    public void setMute(boolean muted) {
        setMuted(muted);
    }

    private Clip loadClip(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (clipCache.containsKey(fileName)) {
<<<<<<< HEAD
            Clip clip = clipCache.get(fileName);
            // 혹시 닫혀있다면 다시 열기
            if (!clip.isOpen()) {
                clip.open();
            }
            return clip;
=======
            Clip c = clipCache.get(fileName);
            if (c.isOpen()) return c;
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339
        }
        
        File file = new File(SOUND_PATH + fileName);
        if (!file.exists()) return null;
        
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        clipCache.put(fileName, clip);
        return clip;
    }

    private void setVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // 볼륨값 범위 제한 (너무 작거나 크지 않게)
            float newVolume = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), volume));
            gainControl.setValue(newVolume);
        }
    }

    public void stopAll() {
        stopBGM();
        for (Clip clip : clipCache.values()) {
            if (clip != null && clip.isRunning()) clip.stop();
        }
    }
<<<<<<< HEAD

    public void setMute(boolean muted) {
        this.isMuted = muted;
        if (muted) stopAll();
        else {
            // 뮤트 해제 시 BGM이 설정되어 있었다면 다시 재생
            if (currentBgmFileName != null) playBGM(currentBgmFileName);
        }
    }
=======
>>>>>>> 7e43b7b1f0205352241fe5564e3db6505e219339
}