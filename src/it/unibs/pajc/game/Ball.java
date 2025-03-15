package it.unibs.pajc.game;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Ball extends Oggetto {
    public final float FATTORE_RIMBALZO = 0.8f;
    public final double GROUNDLEVEL = 80;

    public Ball(CampoDiGioco campo, int xc, int yc) {
        super(campo, xc, yc);
    }

    @Override
    public Shape getFormaBase() {
        return new Ellipse2D.Double(-20, -20, 20, 20);
    }

    @Override
    public void stepNext() {
        super.stepNext();

        if (Math.abs(vx) > 7) {
            vx = 7;
        }

        float gravita = 0.5f;
        vy -= gravita;

        if (y < GROUNDLEVEL + 20) {
            setPosizione(x, (float) (GROUNDLEVEL + 20));
            vx *= FATTORE_RIMBALZO;
            vy = -vy * FATTORE_RIMBALZO;
        }
    }

    public void reset(int id) {
        /*if (id == 1) {
            setVelocita(4,-4);
            setAngle(0, 500, 300);
        } else if (id == 2) {
            setVelocita(-4,-4);
            setAngle(180, 500, 300);
        }*/
        setPosizione(510, 300);
    }

    public void bounceOffPlayer(Giocatore player) {
        float bounceDirection = (this.getX() < player.getX()) ? 1 : -1;
        setVelocita(bounceDirection * 5, -5);
    }

    public void setAngle(float angle, int x, int y) {
        setPosizione(x, y);
        float speed = (float) Math.hypot(vx, vy);
        vx = (float) (speed * Math.cos(Math.toRadians(angle)));
        vy = (float) (speed * Math.sin(Math.toRadians(angle)));
    }
}