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
    private boolean singlePlayer;
    private float groundY = 158;

    public CampoDiGioco(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
        ball = new Ball(this, 0, 0);
        ball.setPosizione(0, 300);
        addOggetto(ball);

        // Assegna il primo giocatore sempre
        localPlayer = new Giocatore(this, -350, (int) groundY, 1, false);
        addOggetto(localPlayer);

        if (singlePlayer) {
            // Il secondo giocatore sarà un bot
            remotePlayer = new Giocatore(this, 250, (int) groundY, 2, true);
        } else {
            // Il secondo giocatore sarà aggiunto successivamente (ma lo prepariamo)
            remotePlayer = new Giocatore(this, 250, (int) groundY, 2, false);
        }
        addOggetto(remotePlayer);
    }

    public void addPlayer(int playerId) {
        if (playerId == 1 && localPlayer == null) {
            localPlayer = new Giocatore(this, -350, (int) groundY, playerId, false);
            addOggetto(localPlayer);
            System.out.println("Assegnato Player " + playerId + " come localPlayer.");
        } else if (playerId == 2 && remotePlayer == null) {
            remotePlayer = new Giocatore(this, 250, (int) groundY, playerId, false);
            addOggetto(remotePlayer);
            System.out.println("Assegnato Player " + playerId + " come remotePlayer.");
        }
    }

    public void addOggetto(Oggetto oggetto) {listaOggetti.add(oggetto);}
    public Giocatore getLocalPlayer() {return localPlayer;}
    public Giocatore getRemotePlayer() {return remotePlayer;}
    public Ball getBall() {return ball;}
    public Float getGroundY() {return groundY;}



    public void stepNext() {
        for (Oggetto o : listaOggetti) {
            o.stepNext();
            applyLimit(o);
        }

    }

    private void applyLimit(Oggetto o) {
        if(o instanceof Giocatore giocatore) {
            if (giocatore.getId() == 1) {
                if (o.getX() < -400) {
                    o.setPosizione(-400, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
                if (o.getX() > 1250) {
                    o.setPosizione(1250, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
            } else if (giocatore.getId() == 2) {
                if (o.getX() < -1350) {
                    o.setPosizione(-1350, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
                if (o.getX() > 320) {
                    o.setPosizione(320, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
            }
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

    public void movePlayer(int playerId, int direction) {
        if (playerId == localPlayer.getId()) {
            localPlayer.setVelocita(direction == 1 ? 4 : -4, 0);
        } else if (playerId == remotePlayer.getId()) {
            remotePlayer.setVelocita(direction == 1 ? 4 : -4, 0);
        }
    }


    public void jump(int playerId) {
        if (playerId == remotePlayer.getId()) {
            remotePlayer.jump();
        } else if (playerId == localPlayer.getId()) {
            localPlayer.jump();
        }
    }

    public void kickBall(int playerId) {
    }

    public void updatePhysics() {
        for (Oggetto o : listaOggetti) {
            if (!(o instanceof Giocatore)) {
                o.applyGravity();    // Applica la gravità
                o.applyFriction();   // Applica la frizione
                o.stepNext();
            }

            if (o instanceof Giocatore giocatore) {
                giocatore.stepNext();
                giocatore.applyFriction();
                float newY = giocatore.getY() + giocatore.getVelocitaY();

                if (newY >= groundY) {
                    giocatore.setPosizioneY(groundY);
                    giocatore.setVelocitaY(0);
                } else {
                    giocatore.setPosizioneY(newY);
                }
            } else {
                o.setPosizioneY(o.getY() + o.getVelocitaY());
            }
            applyLimit(o);
        }
        checkCollisions();
    }


    private void checkCollisions() {
        if (localPlayer != null && localPlayer.checkCollision(ball)) {
            ball.bounceOffPlayer(localPlayer);
        }
        if (remotePlayer != null && remotePlayer.checkCollision(ball)) {
            ball.bounceOffPlayer(remotePlayer);
        }

        // Aggiungi eventuali altre collisioni qui (es. giocatore vs muro o rete)
    }

    public void setGroundY(float groundY) {
        this.groundY = groundY;
    }
}
