package breakout.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class SoundManager {

    private static final String SOUND_PATH = "assets/";

    public static final String SOUND_HIT = "hit.wav";
    public static final String SOUND_FAIL = "Fail.wav";
    public static final String SOUND_EXPLODE = "explode.wav";
    public static final String SOUND_CLICK = "click.wav";
    public static final String SOUND_VICTORY = "victory.wav";
    public static final String SOUND_POWERUP = "powerup.wav";
    public static final String SOUND_WALL = "hit.wav";
    public static final String SOUND_BUY = "buy.wav";
    public static final String SOUND_ERROR = "error.wav";
    public static final String SOUND_GAMEOVER = "GameOver.wav";
    public static final String SOUND_BOMB = "bomb.wav";

    private Map<String, Clip> clipCache;
    private Clip currentBgmClip;
    private String currentBgmName = "";
    private boolean isMuted = false;
    private float masterVolume = -10.0f;

    private static SoundManager INSTANCE = null;

    public static synchronized SoundManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SoundManager();
        }
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
        try {
            loadClip(fileName);
        } catch (Exception e) {
            // 로드 실패 시 무시
        }
    }

    public void playHitSound() {
        playSound(SOUND_HIT);
    }

    public void playFailSound() {
        playSound(SOUND_FAIL);
    }

    public void playExplodeSound() {
        playSound(SOUND_EXPLODE);
    }

    public void playClickSound() {
        playSound(SOUND_CLICK);
    }

    public void playPowerupSound() {
        playSound(SOUND_POWERUP);
    }

    public void playWallSound() {
        playSound(SOUND_WALL);
    }

    public void playBuySound() {
        playSound(SOUND_BUY);
    }

    public void playErrorSound() {
        playSound(SOUND_ERROR);
    }

    public void playBombSound() {
        playSound(SOUND_BOMB);
    }

    public void playGameOverSound() {
        playSpecialAndResume(SOUND_GAMEOVER);
    }

    public void playVictorySound() {
        playSpecialAndResume(SOUND_VICTORY);
    }

    // 게임 오버나 승리 효과음 재생 후 기존 BGM으로 복구하는 로직
    private void playSpecialAndResume(String fileName) {
        if (isMuted) {
            return;
        }

        stopBGM();

        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume);
                clip.setFramePosition(0);

                // 효과음 재생이 끝나면 BGM을 다시 시작하는 리스너 등록
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

    public void playSound(String fileName) {
        if (isMuted) {
            return;
        }
        
        try {
            Clip clip = loadClip(fileName);
            if (clip != null) {
                setVolume(clip, masterVolume);
                clip.setFramePosition(0);
                clip.start();
            }
        } catch (Exception e) {
            // 재생 실패 시 무시
        }
    }

    public void playBGM(String fileName) {
        this.currentBgmName = fileName;

        if (isMuted) {
            stopBGM();
            return;
        }

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
                currentBgmClip = clip;
            }
        } catch (Exception e) {
            System.out.println("브금 로드 실패: " + fileName);
        }
    }

    public void stopBGM() {
        if (currentBgmClip != null) {
            currentBgmClip.stop();
            currentBgmClip = null;
        }
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        
        if (isMuted) {
            stopAll();
        } else {
            if (!currentBgmName.isEmpty()) {
                playBGM(currentBgmName);
            }
        }
    }

    public void setMute(boolean muted) {
        setMuted(muted);
    }

    private Clip loadClip(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (clipCache.containsKey(fileName)) {
            Clip c = clipCache.get(fileName);
            if (c.isOpen()) {
                return c;
            }
        }

        File file = new File(SOUND_PATH + fileName);
        if (!file.exists()) {
            return null;
        }

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
        
        for (Clip clip : clipCache.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }
}