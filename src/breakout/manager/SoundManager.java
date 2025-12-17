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

/**
 * 게임의 모든 오디오 리소스를 관리하는 사운드 매니저 클래스.
 * <p>
 * 효과음(.wav)의 로딩, 재생, 볼륨 조절 및 메모리 관리를 담당한다.
 * 배경음악(BGM) 전환 기능을 포함한다.
 * </p>
 * @author 박한결
 * @version 1.5
 * @since 2025-12-17
 */
public class SoundManager {

    // 사운드 파일이 위치한 기본 경로 (프로젝트 최상위 assets 폴더)
    private static final String SOUND_PATH = "assets/";
    
    // 파일명 상수 (assets 폴더에 이 이름대로 파일이 있어야 함!)
    public static final String SOUND_HIT = "hit.wav"; 
    public static final String SOUND_BREAK = "break.wav";   
    public static final String SOUND_GAMEOVER = "gameover.wav"; 
    public static final String SOUND_VICTORY = "victory.wav";

    // 오디오 클립 캐시
    private Map<String, Clip> clipCache;
    
    // 현재 재생 중인 BGM 클립 (제어를 위해 따로 저장)
    private Clip currentBgmClip;

    private boolean isMuted = false;
    private float masterVolume = -10.0f;

    public SoundManager() {
        this.clipCache = new HashMap<>();
        // 버벅임 방지를 위해 주요 사운드 미리 로딩
        preLoadSound(SOUND_HIT);
        System.out.println("[SoundManager] Audio System Initialized.");
    }
    
    private void preLoadSound(String fileName) {
        try {
            loadClip(fileName);
        } catch (Exception e) {
            System.out.println("[SoundManager] Warning: " + fileName + " not found.");
        }
    }

    // 편의 메소드들 (GamePanel에서 이것만 부르면 됨)
    public void playHitSound() { playSound(SOUND_HIT); }
    public void playBreakSound() { playSound(SOUND_BREAK); }

    /**
     * 효과음 재생 (중첩 가능)
     */
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
            // 효과음 에러는 무시 (게임 진행 방해 X)
        }
    }

    /**
     * 배경 음악 재생 (기존 음악은 정지됨)
     */
    public void playBGM(String fileName) {
        if (isMuted) return;

        // 1. 기존에 재생 중인 BGM이 있다면 끈다.
        stopBGM();

        try {
            // 2. 새 BGM 파일 로드
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume - 5.0f); // 배경음은 조금 작게
                clip.loop(Clip.LOOP_CONTINUOUSLY);    // 무한 반복
                clip.start();
                
                // 3. 현재 BGM으로 등록
                currentBgmClip = clip;
            }
        } catch (Exception e) {
            System.err.println("[SoundManager] Failed to play BGM: " + fileName);
        }
    }
    
    /**
     * 현재 재생 중인 BGM을 정지한다.
     */
    public void stopBGM() {
        if (currentBgmClip != null) {
            if (currentBgmClip.isRunning()) {
                currentBgmClip.stop();
            }
            currentBgmClip = null;
        }
    }

    private Clip loadClip(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // 캐시에 있으면 반환
        if (clipCache.containsKey(fileName)) {
            return clipCache.get(fileName);
        }

        File file = new File(SOUND_PATH + fileName);
        if (!file.exists()) {
            return null;
        }

        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        
        clipCache.put(fileName, clip);
        System.out.println("[SoundManager] Loaded new sound: " + fileName);
        
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
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public void setMute(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            stopAll();
        } else {
            // 음소거 해제 시 처리는 필요하면 추가
        }
    }
}