package it.unibs.pajc.game;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CampoDiGioco extends BaseModel{
    public static final int CAMPO_WIDTH = 1000;
    public static final int CAMPO_HEIGHT = 600;
    protected Rectangle2D.Float bounds = new Rectangle2D.Float(-500, -300, CAMPO_WIDTH, CAMPO_HEIGHT);
    protected ArrayList<Oggetto> listaOggetti = new ArrayList<>();
    protected Ball ball;
    protected Giocatore localPlayer, remotePlayer;

    public CampoDiGioco() {
        ball = new Ball(this, 0, 0);
        ball.setPosizione(0, 300);
        addOggetto(ball);

        localPlayer = new Giocatore(this, -350, 158, 1, false);
        remotePlayer = new Giocatore(this, 250, 158, 2, true);
        addOggetto(localPlayer);
        addOggetto(remotePlayer);
    }
    public void setMultiPlayer() {
        remotePlayer.setIsBot(true);
    }
    public void setSinglePlayer() {remotePlayer.setIsBot(false);}

    public void setListaOggetti(ArrayList<Oggetto> listaOggetti) {this.listaOggetti = listaOggetti;}
    public ArrayList<Oggetto> getListaOggetti() {return listaOggetti;}
    public void addOggetto(Oggetto oggetto) {listaOggetti.add(oggetto);}
    public void setLocalPlayer(Giocatore localPlayer) {this.localPlayer = localPlayer;}


    public Giocatore getLocalPlayer() {return localPlayer;}
    public Giocatore getRemotePlayer() {return remotePlayer;}
    public Ball getBall() {return ball;}


    public void stepNext() {
        for (Oggetto o : listaOggetti) {
            o.stepNext();
            applyLimit(o);
        }

    }

    private void applyLimit(Oggetto o) {
        if (o.getX() < -400) {
            o.setPosizione(-400, o.getY());
            o.setVelocita(0, o.getVelocitaY());
        }
        if (o.getX() > 1250) {
            o.setPosizione(1250, o.getY());
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
