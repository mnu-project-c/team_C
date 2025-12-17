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
    
    // 네 assets 폴더에 실제 존재하는 파일들만 등록
    public static final String SOUND_HIT = "hit.wav"; 
    public static final String SOUND_FAIL = "Fail.wav";       // 대문자 F 주의
    public static final String SOUND_EXPLODE = "explode.wav"; // 깨질 때 소리
    public static final String SOUND_CLICK = "click.wav";     // 버튼 소리

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

    public void playHitSound() { playSound(SOUND_HIT); }
    public void playFailSound() { playSound(SOUND_FAIL); } 
    public void playExplodeSound() { playSound(SOUND_EXPLODE); } 
    public void playClickSound() { playSound(SOUND_CLICK); }

    public void playSound(String fileName) {
        if (isMuted) return;
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume);
                clip.setFramePosition(0); 
                clip.start();
            }
        } catch (Exception e) {
            // 파일이 없어도 에러 안 뜨게 조용히 넘어감
        }
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