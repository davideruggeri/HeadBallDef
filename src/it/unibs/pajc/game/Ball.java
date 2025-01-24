package it.unibs.pajc.game;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class Ball extends Oggetto {
    private float gravita = 0.5f;
    private float fattoreRimbalzo = 0.8f;
    private final double GROUNDLEVEL = 0.563;

    public Ball(CampoDiGioco campo, int xc, int yc) {
        super(campo);

        this.shape = new Area(new Ellipse2D.Double(
                xc,
                yc,
                40,
                40
        ));
    }
    @Override
    public void stepNext() {
        super.stepNext();
        this.velocita[1] -= gravita;

        if (getY() <= GROUNDLEVEL + 20) {
            setPosizione(getX(), (float) GROUNDLEVEL + 20);
            setVelocita(getVelocitaX(), - getVelocitaY() * fattoreRimbalzo);
        }
    }
    public void setAngle(float angle) {
        float speed = (float) Math.hypot(velocita[0], velocita[1]); // Modulo della velocità

        // Aggiorna le componenti di velocità in base all'angolo
        velocita[0] = (float) (speed * Math.cos(Math.toRadians(angle)));
        velocita[1] = (float) (speed * Math.sin(Math.toRadians(angle)));
    }

}

