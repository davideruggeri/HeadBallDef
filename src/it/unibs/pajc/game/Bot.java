package it.unibs.pajc.game;

import javax.swing.*;

public class Bot extends Giocatore {

    private static final float MAX_SPEED = 8.0f;
    private static final float ACCELERATION = 0.5f;
    private static final float REACTION_TIME = 0.1f; // Tempo di reazione (simula ritardo umano)
    private static final float GOAL_X = 50.0f;  // Posizione della porta

    private float targetVelocityX = 0.0f;
    private float reactionTimer = 0.0f;

    public Bot(CampoDiGioco campo, int cx, int cy, int id) {
        super(campo, cx, cy, id);
    }

    @Override
    public void stepNext() {
        Ball ball = campo.getBall();
        float ballX = ball.getX();
        float ballY = ball.getY();
        float botX = getX();
        float botY = getY();

        if (checkCollision(ball)) {
            handleCollision(ball);
        } else {
            // Simula un ritardo nella reazione
            if (reactionTimer <= 0) {
                float targetX = (ballX > botX) ? ballX + 10 : ballX - 10;
                if (Math.abs(ballX - botX) > 10) {
                    targetVelocityX = (targetX < botX) ? -MAX_SPEED : MAX_SPEED;
                } else {
                    targetVelocityX = 0;
                }
                reactionTimer = REACTION_TIME; // Reset del timer di reazione
            }
        }

        // Simula accelerazione
        float currentVelX = getVelocitaX();
        if (currentVelX < targetVelocityX) {
            setVelocita(Math.min(currentVelX + ACCELERATION, targetVelocityX), getVelocitaY());
        } else if (currentVelX > targetVelocityX) {
            setVelocita(Math.max(currentVelX - ACCELERATION, targetVelocityX), getVelocitaY());
        }

        if ((ballY < botY || Math.abs(ballX - botX) < 40) && !isJumping()) {
            jump();
        }

        reactionTimer -= 0.016; // Supponendo un frame rate di circa 60 FPS

        super.stepNext();
    }
}
