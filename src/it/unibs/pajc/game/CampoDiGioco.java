package it.unibs.pajc.game;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CampoDiGioco{
    protected Rectangle2D.Float bounds = new Rectangle2D.Float(-500, -300, 1000, 600);
    protected ArrayList<Oggetto> listaOggetti = new ArrayList<>();
    protected Ball ball;
    protected Giocatore g1, g2;

    public CampoDiGioco() {
        ball = new Ball(this, 0, 0);
        ball.setPosizione(0, 300);
        addOggetto(ball);

        g1 = new Giocatore(this, -350, 158, 1);
        g2 = new Giocatore(this, 250, 158, 2);
        addOggetto(g1);
        addOggetto(g2);
    }

    public void addOggetto(Oggetto oggetto) {listaOggetti.add(oggetto);}

    public Giocatore getG1() {return g1;}
    public Giocatore getG2() {return g2;}
    public Ball getBall() {return ball;}


    public void stepNext() {
        for (Oggetto o : listaOggetti) {
            o.stepNext();
            applyLimit(o);
        }

    }

    private void applyLimit(Oggetto o) {
        if (o.getX() < bounds.getMinX()) {
            o.setPosizione((float) bounds.getMinX(), o.getY());
            o.setVelocita(0, o.getVelocitaY());
        }
        if (o.getX() > bounds.getMaxX()) {
            o.setPosizione((float) bounds.getMaxX(), o.getY());
            o.setVelocita(0, o.getVelocitaY());
        }

        if (o.getY() < bounds.getMinY()) {
            o.setPosizione(o.getX(), (float) bounds.getMinY());
            o.setVelocita(o.getVelocitaX(), 0);
        }
        if (o.getY() > bounds.getMaxY()) {
            o.setPosizione(o.getX(), (float) bounds.getMaxY());
            o.setVelocita(o.getVelocitaX(), 0);
        }
    }





}
