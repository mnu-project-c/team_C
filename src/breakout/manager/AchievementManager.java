package breakout.manager;

import breakout.entity.Achievement;
import breakout.view.NotificationPopup;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    private List<Achievement> achievements;
    private static final String FILE_PATH = "achievements.txt";
    private int totalClears = 0;
    private NotificationPopup popup;

    public AchievementManager() {
        achievements = new ArrayList<>();
        initAchievements();
        loadAchievements();
    }

    public void setPopup(NotificationPopup popup) {
        this.popup = popup;
    }

    private void initAchievements() {
        achievements.add(new Achievement("첫 걸음", "첫 번째 벽돌을 파괴하세요."));
        achievements.add(new Achievement("고득점자", "10,000점 이상 달성하세요."));
        achievements.add(new Achievement("생존 전문가", "목숨을 하나도 잃지 않고 스테이지 클리어."));
        achievements.add(new Achievement("학생회의 자격-1", "게임 클리어 10회"));
        achievements.add(new Achievement("학생회의 자격-2", "게임 클리어 20회"));
        achievements.add(new Achievement("학생회의 자격-3", "게임 클리어 30회"));
        achievements.add(new Achievement("학생회의 자격-4", "게임 클리어 40회"));
    }

    // 업적 해제 및 알림 출력 로직
    public void unlock(String title) {
        for (Achievement a : achievements) {
            if (a.title.equals(title)) {
                if (!a.isUnlocked) {
                    a.isUnlocked = true;
                    saveAchievements();

                    System.out.println("신규 업적 달성!: " + title);

                    if (this.popup != null) {
                        this.popup.show(title);
                    }
                }
            }
        }
    }

    public void addClearCount() {
        this.totalClears++;
        checkClearAchievements();
        saveAchievements();
    }

    private void checkClearAchievements() {
        if (totalClears >= 10) {
            unlock("학생회의 자격-1");
        }
        if (totalClears >= 20) {
            unlock("학생회의 자격-2");
        }
        if (totalClears >= 30) {
            unlock("학생회의 자격-3");
        }
        if (totalClears >= 40) {
            unlock("학생회의 자격-4");
        }
    }

    // 파일 저장 로직
    public void saveAchievements() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            writer.println("TotalClears:" + totalClears);
            
            for (Achievement a : achievements) {
                writer.println(a.title + ":" + a.isUnlocked);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 파일 로드 로직
    public void loadAchievements() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TotalClears:")) {
                    this.totalClears = Integer.parseInt(line.split(":")[1]);
                } else {
                    String[] parts = line.split(":");
                    if (parts.length < 2) {
                        continue;
                    }
                    
                    for (Achievement a : achievements) {
                        if (a.title.equals(parts[0])) {
                            a.isUnlocked = Boolean.parseBoolean(parts[1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("업적 로드 중 오류 발생: " + e.getMessage());
        }
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public int getTotalClears() {
        return totalClears;
    }

    public boolean isUnlocked(String title) {
        for (Achievement a : achievements) {
            if (a.title.equals(title)) {
                return a.isUnlocked;
            }
        }
        return false;
    }
}