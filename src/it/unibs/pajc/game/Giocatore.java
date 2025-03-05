package it.unibs.pajc.game;

import javax.imageio.ImageIO;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Giocatore extends Oggetto {

    private boolean isJumping = false;
    private final float GRAVITA = 0.5f;
    private final float FORZASALTO = 10;
    CampoDiGioco campo;
    private boolean isBot = true;
    private int id;

    public Giocatore(CampoDiGioco campo, int cx, int cy, int id, boolean isBot) {
        super(campo);
        this.id = id;
        this.campo = campo;
        this.isBot = isBot;

        this.shape = creaArea(cx, cy, id);
    }

    @Override
    public void stepNext() {
        this.handleCollision(campo.getBall());
        super.stepNext();
        updateJump();
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            velocita[1] += GRAVITA;
            setVelocita(getVelocitaX(), FORZASALTO);
        }
    }

    public void updateJump() {
        if (isJumping) {
            velocita[1] -= GRAVITA;
        }
        if (getY() <= 0 ) {
            setPosizione(getX(), 0);
            isJumping = false;
        }
    }

    /**
     * Sistemare la direzione con cui la palla viene rimbalzata dopo il contatto
     * gestire correttamente la fisica del rimbalzo tra giocatore e palla
     */

    public void handleCollision(Ball ball) {
        if (checkCollision(ball)) {
            // 1. Calcola normale di collisione
            float deltaX = ball.getX() - getX();
            float deltaY = ball.getY() - getY();
            float distanza = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            float normaleX = deltaX / distanza;
            float normaleY = deltaY / distanza;

            // 2. Calcola velocità relativa lungo la normale
            float velRelX = ball.getVelocitaX() - getVelocitaX();
            float velRelY = ball.getVelocitaY() - getVelocitaY();
            float velLungoNormale = velRelX * normaleX + velRelY * normaleY;

            if (velLungoNormale > 0) return; // Le due cose si allontanano, niente collisione da gestire.

            // 3. Calcola impulso
            float e = 0.8f; // coefficiente di restituzione
            float impulso = -(1 + e) * velLungoNormale;

            // 4. Applica impulso alla palla
            ball.setVelocita(
                    ball.getVelocitaX() + impulso * normaleX,
                    ball.getVelocitaY() + impulso * normaleY
            );

            // 5. Spin: se il giocatore è in movimento, "spinna" la palla lateralmente
            float spin = getVelocitaX() * 0.5f;
            ball.setVelocita(ball.getVelocitaX() + spin, ball.getVelocitaY());

            // 6. Separazione: evita che la palla resti incastrata
            float overlap = 20 - distanza;
            if (overlap > 0) {
                ball.setPosizione(
                        ball.getX() + normaleX * overlap,
                        ball.getY() + normaleY * overlap
                );
            }
        }
    }

    public int getId() {return id;}

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