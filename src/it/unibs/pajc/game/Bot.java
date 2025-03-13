package it.unibs.pajc.game;

public class Bot extends Giocatore {

    public Bot(CampoDiGioco campo, int cx, int cy, int id, boolean isBot) {
        super(campo, cx, cy, id, isBot);
    }

    @Override
    public void stepNext() {
        // Ottieni la palla dal campo di gioco
        Ball ball = campo.getBall();

        // Movimento orizzontale: sposta il bot verso la palla
        if (ball.getX() < getX()) {
            setVelocita(-8.0f, getVelocitaY());
        } else if (ball.getX() > getX()) {
            setVelocita(8.0f, getVelocitaY());
        }

        if (ball.getY() < getY() && !isJumping()) {
            jump();
        }

        super.stepNext();
    }
}
