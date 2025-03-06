package it.unibs.pajc.game;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CampoDiGioco extends BaseModel {
    public static final int CAMPO_WIDTH = 1000;
    public static final int CAMPO_HEIGHT = 600;
    // Definizione dei bounds in coordinate mondo: (0,0) fino a (1000,600)
    protected Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, CAMPO_WIDTH, CAMPO_HEIGHT);

    protected ArrayList<Oggetto> listaOggetti = new ArrayList<>();
    protected Ball ball;
    protected Giocatore localPlayer, remotePlayer;
    private boolean singlePlayer;
    private int groundY = 80;
    private int gameTime;
    private int player1Score, player2Score;
    private long lastCollisionTime = 0;
    private static final long COLLISION_COOLDOWN = 50; // ms

    public CampoDiGioco(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
        this.gameTime = 90;

        // Crea la palla al centro del campo (500,300)
        ball = new Ball(this, 500, 300);
        addOggetto(ball);

        // Crea il giocatore locale, posizionato sul lato sinistro (es. x = 150) e al ground
        localPlayer = new Giocatore(this, 0, 0, 1, false);
        localPlayer.setPosizione(150, groundY);
        addOggetto(localPlayer);

        // Se necessario, aggiungi anche il giocatore remoto (es. posizionato sul lato destro)

        /*remotePlayer = new Giocatore(this, 850, groundY, 2, singlePlayer);
        addOggetto(remotePlayer);*/

    }

    public void addPlayer(int playerId) {
        if (playerId == 1 && localPlayer == null) {
            localPlayer = new Giocatore(this, 150, groundY, playerId, false);
            addOggetto(localPlayer);
            System.out.println("Assegnato Player " + playerId + " come localPlayer.");
        } else if (playerId == 2 && remotePlayer == null) {
            remotePlayer = new Giocatore(this, 850, groundY, playerId, false);
            addOggetto(remotePlayer);
            System.out.println("Assegnato Player " + playerId + " come remotePlayer.");
        }
    }

    public void addOggetto(Oggetto oggetto) {listaOggetti.add(oggetto);}
    public ArrayList<Oggetto> getOggetti() {return listaOggetti;}
    public Giocatore getLocalPlayer() {return localPlayer;}
    public Giocatore getRemotePlayer() {return remotePlayer;}
    public Ball getBall() {return ball;}
    public float getGroundY() {return groundY;}
    public void setGroundY(int groundY) {this.groundY = groundY;}
    public int getGameTime() {return gameTime;}
    public void setGameTime(int gameTime) {this.gameTime = gameTime;}
    public int getPlayer1Score() {return player1Score;}
    public void setPlayer1Score(int player1Score) {this.player1Score = player1Score;}
    public int getPlayer2Score() {return player2Score;}
    public void setPlayer2Score(int player2Score) {this.player2Score = player2Score;}

    public void stepNext() {
        for (Oggetto o : listaOggetti) {
            o.stepNext();
            applyLimit(o);
        }
    }

    private void applyLimit(Oggetto o) {
        if (o instanceof Giocatore) {
            if (o.getX() < 0) {
                o.setPosizione(0, o.getY());
                o.setVelocita(0, o.getVelocitaY());
            }
            if (o.getX() > CAMPO_WIDTH) {
                o.setPosizione(CAMPO_WIDTH, o.getY());
                o.setVelocita(0, o.getVelocitaY());
            }
        }
        if (o instanceof Ball) {
            if (o.getX() < 20) {
                o.setPosizione(20, o.getY());
                o.setVelocita(-o.getVelocitaX() * 0.7f, o.getVelocitaY() * 0.9f);
            }
            if (o.getX() > CAMPO_WIDTH - 20) {
                o.setPosizione(CAMPO_WIDTH - 20, o.getY());
                o.setVelocita(-o.getVelocitaX() * 0.7f, o.getVelocitaY() * 0.9f);
            }
            if (o.getY() > CAMPO_HEIGHT) {
                o.setPosizione(o.getX(), CAMPO_HEIGHT);
                o.setVelocita(o.getVelocitaX() * 0.7f, -o.getVelocitaY() * 0.9f);
            }
        }
    }

    public void movePlayer(int playerId, int direction) {
        if (localPlayer != null && playerId == localPlayer.getId()) {
            localPlayer.setVelocita(direction == 1 ? 8f : -8f, localPlayer.getVelocitaY());
        } else if (remotePlayer != null && playerId == remotePlayer.getId()) {
            remotePlayer.setVelocita(direction == 1 ? 8f : -8f, remotePlayer.getVelocitaY());
        }
    }

    public void jump(int playerId) {
        if (remotePlayer != null && playerId == remotePlayer.getId()) {
            remotePlayer.jump();
        } else if (localPlayer != null && playerId == localPlayer.getId()) {
            localPlayer.jump();
        }
    }

    public void kickBall(int playerId) {
        // Qui puoi implementare la logica per "calciare" la palla se necessario
    }

    public void updatePhysics() {
        for (Oggetto o : listaOggetti) {
            if (o instanceof Ball) {
                Ball palla = (Ball) o;
                palla.applyGravity();  // Applica la gravità
                if (palla.getY() <= 0) {
                    palla.applyFriction();
                }
                palla.stepNext();
            }
            if (o instanceof Giocatore) {
                Giocatore giocatore = (Giocatore) o;
                giocatore.stepNext();
                giocatore.applyFriction();
                if (giocatore.getY() < groundY) {
                    giocatore.setPosizione(giocatore.getX(), groundY);
                    giocatore.setVelocita(giocatore.getVelocitaX(), 0);
                }
            } else {
                o.setPosizione(o.getX(), o.getY() + o.getVelocitaY());
            }
            applyLimit(o);
        }
        checkCollisions();
    }

    private void checkCollisions() {
        long now = System.currentTimeMillis();
        if (now - lastCollisionTime < COLLISION_COOLDOWN) {
            return;
        }
        if (localPlayer != null && localPlayer.checkCollision(ball)) {
            ball.bounceOffPlayer(localPlayer);
            lastCollisionTime = now;
        }
        if (remotePlayer != null && remotePlayer.checkCollision(ball)) {
            ball.bounceOffPlayer(remotePlayer);
            lastCollisionTime = now;
        }
        // Qui puoi aggiungere altre logiche di collisione (ad esempio, contro ostacoli o pareti)
    }
}
