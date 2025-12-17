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
    
    // 파일명 상수
    public static final String SOUND_HIT = "hit.wav"; 
    public static final String SOUND_BREAK = "break.wav";   
    public static final String SOUND_FAIL = "fail.wav";       // [NEW] 하트 1개 잃었을 때
    public static final String SOUND_GAMEOVER = "gameover.wav"; // [NEW] 완전 게임 오버
    public static final String SOUND_VICTORY = "victory.wav";

    private Map<String, Clip> clipCache;
    private Clip currentBgmClip;
    private boolean isMuted = false;
    private float masterVolume = -10.0f;

    public SoundManager() {
        this.clipCache = new HashMap<>();
        preLoadSound(SOUND_HIT);
        System.out.println("[SoundManager] Audio System Initialized.");
    }
    
    private void preLoadSound(String fileName) {
        try { loadClip(fileName); } catch (Exception e) {}
    }

    // 편의 메소드들
    public void playHitSound() { playSound(SOUND_HIT); }
    public void playBreakSound() { playSound(SOUND_BREAK); }
    
    // ★ [핵심] 하트 잃었을 때 소리
    public void playFailSound() { playSound(SOUND_FAIL); } 
    
    // ★ [핵심] 게임 오버 소리
    public void playGameOverSound() { playSound(SOUND_GAMEOVER); }

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

    public void playBGM(String fileName) {
        if (isMuted) return;
        stopBGM();
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume - 5.0f);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                currentBgmClip = clip;
            }
        } catch (Exception e) {}
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
            if (volume > 6.0f) volume = 6.0f;
            if (volume < -80.0f) volume = -80.0f;
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
    }
}