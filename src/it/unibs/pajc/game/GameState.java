package it.unibs.pajc.game;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private float ballX, ballY, ballVelX, ballVelY;
    private float player1X, player1Y, player2X, player2Y;
    private int timeRemaining;
    private int player1Score, player2Score;

    public GameState() {}

    public GameState(CampoDiGioco campo) {
        this.ballX = campo.getBall().getX();
        this.ballY = campo.getBall().getY();
        this.ballVelX = campo.getBall().getVelocitaX();
        this.ballVelY = campo.getBall().getVelocitaY();

        this.player1X = campo.getLocalPlayer().getX();
        this.player1Y = campo.getLocalPlayer().getY();
        this.player2X = campo.getRemotePlayer().getX();
        this.player2Y = campo.getRemotePlayer().getY();

        this.player1Score = campo.getPlayer1Score();
        this.player2Score = campo.getPlayer2Score();
    }

    public static GameState fromCampoDiGioco(CampoDiGioco campo) {
        return new GameState(campo);
    }

    public float getBallX() { return ballX; }
    public float getBallY() { return ballY; }
    public float getBallVelX() { return ballVelX; }
    public float getBallVelY() { return ballVelY; }

    public float getPlayer1X() { return player1X; }
    public float getPlayer1Y() { return player1Y; }
    public float getPlayer2X() { return player2X; }
    public float getPlayer2Y() { return player2Y; }

    public int getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(int timeRemaining) { this.timeRemaining = timeRemaining; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }
    public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }

    public void applyToCampo(CampoDiGioco campo) {
        campo.getBall().setPosizione(ballX, ballY);
        campo.getBall().setVelocita(ballVelX, ballVelY);
        campo.getLocalPlayer().setPosizione(player1X, player1Y);
        campo.getRemotePlayer().setPosizione(player2X, player2Y);
        campo.setGameTime(timeRemaining);
        campo.setPlayer1Score(player1Score);
        campo.setPlayer2Score(player2Score);
    }
}
