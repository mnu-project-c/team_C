package breakout.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundManager {

    private static final String SOUND_PATH = "assets/";
    
    
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
    private boolean isMuted = false;
    private float masterVolume = -10.0f;

    private static SoundManager INSTANCE = null;

    public static synchronized SoundManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SoundManager();
        return INSTANCE;
    }

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

    public void playHitSound() { playSound(SOUND_HIT); }
    public void playFailSound() { playSound(SOUND_FAIL); } 
    public void playExplodeSound() { playSound(SOUND_EXPLODE); } 
    public void playClickSound() { playSound(SOUND_CLICK); }
    public void playVictorySound() { playSound(SOUND_VICTORY); }
    public void playPowerupSound() { playSound(SOUND_POWERUP); }
    public void playWallSound()    { playSound(SOUND_WALL); }
    public void playBuySound()     { playSound(SOUND_BUY); }
    public void playErrorSound()   { playSound(SOUND_ERROR); }
    public void playGameOverSound() { playSound(SOUND_GAMEOVER); }
    public void playBombSound() { playSound(SOUND_BOMB);}
    public void playSound(String fileName) {
        if (isMuted) return;
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume);
                clip.setFramePosition(0); 
                clip.start();
            }
        } catch (Exception e) {}
    }

    // ★ 브금 재생 핵심 로직
    public void playBGM(String fileName) {
        if (isMuted) return;
        
        // 현재 재생 중인 브금이 새로 부르려는 브금과 같으면 무시 (끊김 방지)
        if (currentBgmClip != null && currentBgmClip.isRunning()) {
             // 같은 파일을 다시 재생하라고 하면 그냥 둠
             return; 
        }

        stopBGM(); // 다른 브금이 나오고 있었다면 정지
        
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume - 5.0f); // 브금은 효과음보다 조금 작게
                clip.loop(Clip.LOOP_CONTINUOUSLY);   // 무한 반복
                clip.start();
                currentBgmClip = clip;
            }
        } catch (Exception e) {
            System.out.println("브금 로드 실패: " + fileName);
        }
    }
    
    public void stopBGM() {
        if (currentBgmClip != null) {
            if (currentBgmClip.isRunning()) currentBgmClip.stop();
            currentBgmClip = null;
        }
    }

    private Clip loadClip(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (clipCache.containsKey(fileName)) return clipCache.get(fileName);
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
            gainControl.setValue(volume);
        }
    }

    public void stopAll() {
        stopBGM();
        for (String key : clipCache.keySet()) {
            Clip clip = clipCache.get(key);
            if (clip != null && clip.isRunning()) clip.stop();
        }
    }

    public void setMute(boolean muted) {
        this.isMuted = muted;
        if (muted) stopAll();
        // 뮤트 해제 시 다시 브금 재생하고 싶으면 여기서 playBGM 호출 가능
    }
}