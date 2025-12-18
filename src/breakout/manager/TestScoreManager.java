package breakout.manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TestScoreManager {
    public static void main(String[] args) throws Exception {
        File f = new File("highscore.txt");
        if (f.exists()) f.renameTo(new File("highscore.txt.bak"));

        // write old integer format to test migration
        BufferedWriter w = new BufferedWriter(new FileWriter("highscore.txt"));
        w.write("12345");
        w.close();

        ScoreManager sm = new ScoreManager();
        System.out.println("High score after migration: " + sm.getHighScore());
        sm.addScore("ALICE", 2000);
        sm.addScore("BOB", 500);
        sm.addScore("CHARLIE", 15000);
        System.out.println("Top scores:");
        for (ScoreEntry e : sm.getTopScores()) {
            System.out.println(e.getName() + " - " + e.getScore() + " - " + e.getDate());
        }
    }
}
