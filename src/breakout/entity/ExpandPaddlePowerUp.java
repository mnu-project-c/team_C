package breakout.entity;

import breakout.view.GamePanel;

public class ExpandPaddlePowerUp extends PowerUp {
    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, 1); // Type 1
    }
    
    @Override
    public void applyEffect(GamePanel game) {
        game.expandPaddle(); // 패들 늘리기
    }
}