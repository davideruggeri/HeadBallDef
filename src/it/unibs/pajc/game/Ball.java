package it.unibs.pajc.game;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Ball extends Oggetto {
    private final float gravita = 0.5f;
    public final float FATTORE_RIMBALZO = 0.8f;
    public final double GROUNDLEVEL = 80;

    public Ball(CampoDiGioco campo, int xc, int yc) {
        super(campo, xc, yc);
    }

    @Override
    public Shape getFormaBase() {
        // Crea un'ellisse centrata in (0,0) (raggio 20 → diametro 40)
        return new Ellipse2D.Double(-20, -20, 20, 20);
    }

    @Override
    public void stepNext() {
        super.stepNext();
        // Applica la gravità
        vy -= gravita;

        // Se la palla scende sotto il livello di terra (con un offset di 20)
        if (y < GROUNDLEVEL + 20) {
            setPosizione(x, (float) (GROUNDLEVEL + 20));
            vx *= FATTORE_RIMBALZO;
            vy = -vy * FATTORE_RIMBALZO;
        }
    }

    /**
     * Calcola il rimbalzo della palla rispetto al giocatore.
     * La palla viene spinta lontano e verso l'alto.
     */
    public void bounceOffPlayer(Giocatore player) {
        float bounceDirection = (this.getX() < player.getX()) ? 1 : -1;
        setVelocita(bounceDirection * 5, -5);
    }

    /**
     * Imposta un nuovo angolo per la velocità mantenendo invariato lo speed.
     */
    public void setAngle(float angle) {
        float speed = (float) Math.hypot(vx, vy);
        vx = (float) (speed * Math.cos(Math.toRadians(angle)));
        vy = (float) (speed * Math.sin(Math.toRadians(angle)));
    }
}
