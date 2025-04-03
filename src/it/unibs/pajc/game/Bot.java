package it.unibs.pajc.game;

public class Bot extends Giocatore {

    private static final float MAX_SPEED = 8.0f;
    private static final float ACCELERATION = 3f;
    private static final float REACTION_TIME = 0.4f;

    private float targetVelocityX = 0.0f;
    private float reactionTimer = 0.0f;

    public Bot(CampoDiGioco campo, int cx, int cy, int id) {
        super(campo, cx, cy, id);
    }

    @Override
    public void stepNext() {
        Ball ball = campo.getBall();
        float ballX = ball.getX();
        float ballY = ball.getY();
        float botX = getX();
        float botY = getY();

        if (checkCollision(ball)) {
            handleCollision(ball);
        } else {
            if (reactionTimer <= 0) {
                float targetX = (ballX > botX) ? ballX + 10 : ballX - 10;
                if (Math.abs(ballX - botX) > 10) {
                    targetVelocityX = (targetX < botX) ? -MAX_SPEED : MAX_SPEED;
                } else {
                    targetVelocityX = 0;
                }
                reactionTimer = REACTION_TIME;
            }
        }

        float currentVelX = getVelocitaX();
        if (currentVelX < targetVelocityX) {
            setVelocita(Math.min(currentVelX + ACCELERATION, targetVelocityX), getVelocitaY());
        } else if (currentVelX > targetVelocityX) {
            setVelocita(Math.max(currentVelX - ACCELERATION, targetVelocityX), getVelocitaY());
        }

        if ((ballY < botY || Math.abs(ballX - botX) < 20) && !isJumping()) {
            jump();
        }

        reactionTimer -= 0.016f;

        super.stepNext();
    }
}
