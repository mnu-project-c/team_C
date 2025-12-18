package breakout.manager;

import breakout.entity.Achievement;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AchievementManager {
    private List<Achievement> achievements;
    private static final String FILE_PATH = "achievements.txt";

    public AchievementManager() {
        achievements = new ArrayList<>();
        initAchievements();
        loadAchievements();
    }

    private void initAchievements() {
        achievements.add(new Achievement("첫 걸음", "첫 번째 벽돌을 파괴하세요."));
        achievements.add(new Achievement("고득점자", "1,000점 이상 달성하세요."));
        achievements.add(new Achievement("생존 전문가", "목숨을 하나도 잃지 않고 스테이지 클리어."));
    }

    // 업적 달성 체크
    public void unlock(String title) {
        for (Achievement a : achievements) {
            if (a.title.equals(title) && !a.isUnlocked) {
                a.isUnlocked = true;
                System.out.println("업적 달성!: " + title);
                saveAchievements();
            }
        }
    }

    public void saveAchievements() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Achievement a : achievements) {
                writer.println(a.title + ":" + a.isUnlocked);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void loadAchievements() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                for (Achievement a : achievements) {
                    if (a.title.equals(parts[0])) {
                        a.isUnlocked = Boolean.parseBoolean(parts[1]);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<Achievement> getAchievements() { return achievements; }
}