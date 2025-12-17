package breakout.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ScoreManager {

    private static final String FILE_PATH = "highscore.txt";
    private int highScore = 0;

    public ScoreManager() {
        loadHighScore();
    }

    // 파일에서 점수 읽어오기
    public void loadHighScore() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                highScore = 0;
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
            reader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // 파일에 점수 저장하기
    public void saveHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH));
                writer.write(String.valueOf(highScore));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getHighScore() {
        return highScore;
    }
}