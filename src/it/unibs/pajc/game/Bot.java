package it.unibs.pajc.game;

public class Bot extends Giocatore {

    public Bot(CampoDiGioco campo, int cx, int cy, int id) {
        super(campo, cx, cy, id);
    }

    @Override
    public void stepNext() {
        Ball ball = campo.getBall();
        float ballX = ball.getX();
        float ballY = ball.getY();
        float botX = getX() +52;
        float botY = getY() -13;

        if (Math.abs(ballX - botX) < 15) {
            setVelocita(-10.0f, getVelocitaY());
        } else {
            setVelocita((ballX < botX) ? -8.0f : 8.0f, getVelocitaY());
        }
        if ((ballY < botY || Math.abs(ballX - botX) < 25) && !isJumping()) {
            jump();
        }
        super.stepNext();
    }
}