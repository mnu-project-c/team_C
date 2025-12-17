package kr.ac.mnu.c_team.breakout.entity;

import kr.ac.mnu.c_team.breakout.view.GamePanel;

public class ExtraLifePowerUp extends PowerUp {
    public ExtraLifePowerUp(double x, double y) {
        super(x, y, 0); // Type 0
    }
    
    @Override
    public void applyEffect(GamePanel game) {
        game.addLife(); // 생명 추가
    }
}