package breakout.entity;

import java.io.Serializable;

public class Achievement implements Serializable {
    public String title;       // 업적 이름
    public String description; // 업적 설명
    public boolean isUnlocked; // 달성 여부

    public Achievement(String title, String description) {
        this.title = title;
        this.description = description;
        this.isUnlocked = false;
    }
}