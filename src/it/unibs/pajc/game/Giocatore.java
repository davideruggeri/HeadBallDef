package it.unibs.pajc.game;

import javax.imageio.ImageIO;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Giocatore extends Oggetto {

    private boolean isJumping = false;
    private final float GRAVITA = 0.5f;
    private final float FORZASALTO = 60;
    private final double GROUNDLEVEL = 0.563;
    CampoDiGioco campo;

    public Giocatore(CampoDiGioco campo, int cx, int cy, int numGiocatore) {
        super(campo);
        this.campo = campo;

        this.shape = creaArea(cx, cy, numGiocatore);
    }

    @Override
    public void stepNext() {
        if (isJumping || getY() < GROUNDLEVEL) {
            velocita[1] = velocita[1] + GRAVITA;
        }
        if (getY() >= GROUNDLEVEL - getY()) {
            isJumping = false;
            setPosizione(getX(), (float) GROUNDLEVEL);
            velocita[1] = 0;
        }
        this.handleCollision(campo.getBall());
        super.stepNext();
    }

    /**
     * Controllare il salto che non funziona
     */
    public void jump() {
        if (!isJumping) {
            isJumping = true; // Imposta lo stato di salto
            velocita[1] = velocita[1] + GRAVITA; // Imposta una velocità verso l'alto (negativa)
        }
        setVelocita(getVelocitaX(), FORZASALTO);
    }

    /**
     * Sistemare la direzione con cui la palla viene rimbalzata dopo il contatto
     * (controllare se è in questo metodo o nella classe ball)
     */

    public void handleCollision(Ball ball) {
        // Usa il metodo checkCollision per verificare la collisione
        if (this.checkCollision(ball)) {
            // Calcola il centro del giocatore
            float centerXPlayer = getX() + (float) this.shape.getBounds2D().getWidth() / 2;
            float centerYPlayer = getY() + (float) this.shape.getBounds2D().getHeight() / 2;

            // Calcola il centro della palla
            float centerXBall = ball.getX() + (float) ball.getShape().getBounds2D().getWidth() / 2;
            float centerYBall = ball.getY() + (float) ball.getShape().getBounds2D().getHeight() / 2;

            // Calcola l'angolo dell'impatto usando atan2
            float deltaX = centerXBall - centerXPlayer;
            float deltaY = centerYBall - centerYPlayer;
            float angle = (float) Math.atan2(deltaY, deltaX);

            // Passa l'angolo calcolato alla palla
            ball.setAngle((float) Math.toDegrees(angle));

            // Applica un impulso alla palla (opzionale)
            float speedMultiplier = 1.2f; // Fattore per incrementare la velocità
            ball.setVelocita(
                    ball.getVelocitaX() + (float) (Math.cos(angle) * speedMultiplier),
                    ball.getVelocitaY() + (float) (Math.sin(angle) * speedMultiplier)
            );
        }
    }


    /**
     * Creare il metodo per il calcio usando quello sopra
     */

    public void normalShot(Ball ball) {
        ball = campo.getBall();
    }

    /* -----------------------------------
     * Creazione dell'area del giocatore
     */

    public Area creaArea(int cx, int cy, int numG) {
        try {
            BufferedImage image = null;
            if (numG == 1) {
                image = ImageIO.read(getClass().getResourceAsStream("/images/testa1.png"));
            } else if (numG == 2) {
                image = ImageIO.read(getClass().getResourceAsStream("/images/testa2.png"));
            }
            Path2D path = new Path2D.Double();
            double fMolt = 1.5;

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);

                    if ((pixel >> 24) != 0x00) {
                        path.moveTo((cx + x) * fMolt, (cy - y) * fMolt);
                        path.lineTo((cx + x + 1) * fMolt, (cy - y) * fMolt);
                        path.lineTo((cx + x + 1) * fMolt, (cy - y - 1) * fMolt);
                        path.lineTo((cx + x) * fMolt, (cy - y - 1) * fMolt);
                        path.closePath();
                    }
                }
            }
            return new Area(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}