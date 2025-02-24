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
       /* if (isJumping) {
            setPosizione((getX() + getVelocitaX()), (getY() + getVelocitaY()));
            setVelocita(getVelocitaX(), getVelocitaY() + GRAVITA);

            if (getY() >= GROUNDLEVEL) {
                setPosizione(getX(), (float) GROUNDLEVEL);
                setVelocita(0, 0);
                isJumping = false;
            }
        }*/
        this.handleCollision(campo.getBall());
        super.stepNext();
    }

    /**
     * Controllare il salto che non funziona
     */


    public void jump() {
        if (!isJumping) {
            isJumping = true;
            velocita[1] = velocita[1] + GRAVITA;
        }
        //setVelocita(getVelocitaX(), FORZASALTO);
    }


    /**
     * Sistemare la direzione con cui la palla viene rimbalzata dopo il contatto
     * (controllare se è in questo metodo o nella classe ball)
     * gestire correttamente la fisica del rimbalzo tra giocatore e palla
     */
    
    public void handleCollision(Ball ball) {
        // Verifica la collisione
        if (this.checkCollision(ball)) {
            // Calcola l'area di intersezione tra il giocatore e la palla
            Area intersezione = new Area(this.getShape());
            intersezione.intersect(new Area(ball.getShape()));

            // Ottieni il centro dell'area di intersezione come punto di contatto
            double puntoContattoX = intersezione.getBounds2D().getCenterX();
            double puntoContattoY = intersezione.getBounds2D().getCenterY();

            // Calcola il vettore dalla palla al punto di contatto
            float deltaX = (float) (puntoContattoX - ball.getX());
            float deltaY = (float) (puntoContattoY - ball.getY());

            // Trova la normale alla superficie d'impatto (direzione della forza di rimbalzo)
            float normaleAngle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));

            // Calcola l'angolo di riflessione
            float incomingAngle = (float) Math.toDegrees(Math.atan2(ball.getVelocitaY(), ball.getVelocitaX()));
            float reflectionAngle = 2 * normaleAngle - incomingAngle;

            // Imposta il nuovo angolo della palla
            ball.setAngle(reflectionAngle);

            // (Opzionale) Aggiungi un incremento di velocità per simulare l'impatto
            float speedMultiplier = 1.1f; // Fattore di velocità
            ball.setVelocita(
                    ball.getVelocitaX() * speedMultiplier,
                    ball.getVelocitaY() * speedMultiplier
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