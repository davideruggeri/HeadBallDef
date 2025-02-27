package it.unibs.pajc.game;

import java.io.Serializable;

public class GameState implements Serializable {
    private float ballX, ballY, ballVelX, ballVelY;
    private float player1X, player1Y, player2X, player2Y;

    public GameState(CampoDiGioco campo) {
        this.ballX = campo.getBall().getX();
        this.ballY = campo.getBall().getY();
        this.ballVelX = campo.getBall().getVelocitaX();
        this.ballVelY = campo.getBall().getVelocitaY();

        this.player1X = campo.getLocalPlayer().getX();
        this.player1Y = campo.getLocalPlayer().getY();
        this.player2X = campo.getRemotePlayer().getX();
        this.player2Y = campo.getRemotePlayer().getY();
    }

    public float getBallX() { return ballX; }
    public float getBallY() { return ballY; }
    public float getBallVelX() { return ballVelX; }
    public float getBallVelY() { return ballVelY; }

    public float getPlayer1X() { return player1X; }
    public float getPlayer1Y() { return player1Y; }
    public float getPlayer2X() { return player2X; }
    public float getPlayer2Y() { return player2Y; }

    public void applyToCampo(CampoDiGioco campo) {
        campo.getBall().setPosizione(ballX, ballY);
        campo.getBall().setVelocita(ballVelX, ballVelY);
        campo.getLocalPlayer().setPosizione(player1X, player1Y);
        campo.getRemotePlayer().setPosizione(player2X, player2Y);
    }
}
