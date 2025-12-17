package kr.ac.mnu.c_team.breakout.entity;

import kr.ac.mnu.c_team.breakout.view.GamePanel;

public class ExpandPaddlePowerUp extends PowerUp {
    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, 1); // Type 1
    }
    
    @Override
    public void applyEffect(GamePanel game) {
        game.expandPaddle(); // 패들 늘리기
    }
}