package it.unibs.pajc.game;

import java.util.ArrayList;

public class CampoDiGioco {
    public static final int CAMPO_WIDTH = 1000;
    public static final int CAMPO_HEIGHT = 600;

    protected ArrayList<Oggetto> listaOggetti = new ArrayList<>();
    protected Ball ball;
    protected Giocatore localPlayer, remotePlayer;
    private int groundY = 80;
    private int gameTime;
    private int player1Score = 0, player2Score = 0;
    private long lastCollisionTime = 0;
    private static final long COLLISION_COOLDOWN = 50; // ms


    public CampoDiGioco(boolean singlePlayer) {
        this.gameTime = 90;

        ball = new Ball(this, 510, 300);
        addOggetto(ball);

        localPlayer = new Giocatore(this, 0, 0, 1);
        localPlayer.setPosizione(250, groundY);
        addOggetto(localPlayer);
        if (singlePlayer) {
            remotePlayer = new Bot(this, 0, 0, 2);
            remotePlayer.setPosizione(750, groundY);
        } else {
            remotePlayer = new Giocatore(this, 0, 0, 2);
            remotePlayer.setPosizione(750, groundY);
        }
        addOggetto(remotePlayer);
    }

    public void addPlayer(int playerId) {
        if (playerId == 1 && localPlayer == null) {
            localPlayer = new Giocatore(this, 150, groundY, playerId);
            addOggetto(localPlayer);
            System.out.println("Assegnato Player " + playerId + " come localPlayer.");
        } else if (playerId == 2 && remotePlayer == null) {
            remotePlayer = new Giocatore(this, 750, groundY, playerId);
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
        updatePhysics();
    }

    private void applyLimit(Oggetto o) {
        if (o instanceof Giocatore giocatore) {
            if (giocatore.getId() == 1) {
                if (o.getX() < 30) {
                    o.setPosizione(30, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
                if (o.getX() > CAMPO_WIDTH - 45) {
                    o.setPosizione(CAMPO_WIDTH - 45, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
            } else if (giocatore.getId() == 2) {
                if (o.getX() < 0) {
                    o.setPosizione(0, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
                if (o.getX() > CAMPO_WIDTH - 70) {
                    o.setPosizione(CAMPO_WIDTH - 70, o.getY());
                    o.setVelocita(0, o.getVelocitaY());
                }
            }
        }
        if (o instanceof Ball) {
            if (o.getX() < 20) {
                o.setPosizione(20, o.getY());
                o.setVelocita(-o.getVelocitaX() * 0.7f, o.getVelocitaY() * 0.7f);
            }
            if (o.getX() > CAMPO_WIDTH - 20) {
                o.setPosizione(CAMPO_WIDTH - 20, o.getY());
                o.setVelocita(-o.getVelocitaX() * 0.7f, o.getVelocitaY() * 0.7f);
            }
            if (o.getY() > CAMPO_HEIGHT) {
                o.setPosizione(o.getX(), CAMPO_HEIGHT);
                o.setVelocita(o.getVelocitaX() * 0.7f, -o.getVelocitaY() * 0.5f);
            }
            if (o.getY() > 233 && o.getY() < 246 && o.getX() < 75) { // Rimbalzo sulla traversa sinistra
                o.setPosizione(o.getX(), 246);
                o.setVelocita(o.getVelocitaX() * 0.9f, -o.getVelocitaY() * 0.5f);
            }

            if (o.getY() > 235 && o.getY() < 248 && o.getX() > 925) { // Rimbalzo sulla traversa destra
                o.setPosizione(o.getX(), 248);
                o.setVelocita(o.getVelocitaX() * 0.9f, -o.getVelocitaY() * 0.5f);
            }
        }
    }

    public void goal() {
        if (ball.getY() < 232 && ball.getX() < 70) {
            setPlayer2Score(++player2Score);
            ball.reset();
        } else if (ball.getY() < 234 && ball.getX() > 930) {
            setPlayer1Score(++player1Score);
            ball.reset();
        }
    }


    public void movePlayer(int playerId, int direction) {
        if (localPlayer != null && playerId == localPlayer.getId()) {
            localPlayer.setVelocita(direction == 1 ? 4f : -4f, localPlayer.getVelocitaY());
        } else if (remotePlayer != null && playerId == remotePlayer.getId()) {
            remotePlayer.setVelocita(direction == 1 ? 4f : -4f, remotePlayer.getVelocitaY());
        }
    }

    public void jump(int playerId) {
        if (remotePlayer != null && playerId == remotePlayer.getId()) {
            remotePlayer.jump();
        } else if (localPlayer != null && playerId == localPlayer.getId()) {
            localPlayer.jump();
        }
    }

    public void shot(int id) {
        if (localPlayer != null && id == localPlayer.getId()) {
            localPlayer.shot(localPlayer.getId());
        }
        if (remotePlayer!= null && id == remotePlayer.getId()) {
            remotePlayer.shot(remotePlayer.getId());
        }
    }

    public void updatePhysics() {
        for (Oggetto o : listaOggetti) {
            if (o instanceof Ball palla) {
                palla.stepNext();
            }
            if (o instanceof Giocatore giocatore) {
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
            goal();
        }

        // Evitare la collisione tra i due giocatori
        if (localPlayer != null && remotePlayer != null) {
            if (localPlayer.getId() < remotePlayer.getId() && localPlayer.checkCollision(remotePlayer)) {
                localPlayer.resolveCollision(remotePlayer);
            }
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
    }
}
