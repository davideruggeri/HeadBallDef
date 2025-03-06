package it.unibs.pajc.game;

import javax.imageio.ImageIO;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Giocatore extends Oggetto {
    private boolean isJumping = false;
    private final float GRAVITA = 0.5f;
    private final float FORZASALTO = 10;
    private boolean isBot;
    private int id;

    // Forma base del giocatore, definita in coordinate locali
    private Area formaBase;

    public Giocatore(CampoDiGioco campo, int cx, int cy, int id, boolean isBot) {
        // Imposta la posizione del giocatore nel mondo
        super(campo, cx, cy);
        this.id = id;
        this.isBot = isBot;
        // Crea la forma in coordinate locali (senza offset di posizione)
        this.formaBase = creaArea(id);
   }

    @Override
    public void stepNext() {
        // Gestione della collisione con la palla
        handleCollision(campo.getBall());
        super.stepNext();
        updateJump();
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            // Imposta la velocità verticale per il salto
            setVelocita(getVelocitaX(), FORZASALTO);
        }
    }

    public void updateJump() {
        if (isJumping) {
            // Durante il salto, la gravità riduce la velocità verticale
            vy -= GRAVITA;
        }
        // Se il giocatore tocca il ground, resetta il salto
        if (getY() <= campo.getGroundY()) {
            setPosizione(getX(), campo.getGroundY());
            isJumping = false;
        }
    }

    /**
     * Gestisce la collisione con la palla.
     * Calcola la normale, l'impulso, applica lo spin e separa i due oggetti se necessario.
     */
    public void handleCollision(Ball ball) {
        if (checkCollision(ball)) {
            // 1. Calcola la normale di collisione
            float deltaX = ball.getX() - getX();
            float deltaY = ball.getY() - getY();
            float distanza = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distanza == 0) return; // Evita divisioni per zero
            float normaleX = deltaX / distanza;
            float normaleY = deltaY / distanza;

            // 2. Calcola velocità relativa lungo la normale
            float velRelX = ball.getVelocitaX() - getVelocitaX();
            float velRelY = ball.getVelocitaY() - getVelocitaY();
            float velLungoNormale = velRelX * normaleX + velRelY * normaleY;
            if (velLungoNormale > 0) return; // Se si allontanano, non intervenire

            // 3. Calcola l'impulso
            float e = 0.8f; // Coefficiente di restituzione
            float impulso = -(1 + e) * velLungoNormale;

            // 4. Applica l'impulso alla palla
            ball.setVelocita(
                    ball.getVelocitaX() + impulso * normaleX,
                    ball.getVelocitaY() + impulso * normaleY
            );

            // 5. Spin: se il giocatore è in movimento, applica uno spin laterale alla palla
            float spin = getVelocitaX() * 0.5f;
            ball.setVelocita(ball.getVelocitaX() + spin, ball.getVelocitaY());

            // 6. Separazione: evita che la palla rimanga incastrata nel giocatore
            float overlap = 20 - distanza;
            if (overlap > 0) {
                ball.setPosizione(
                        ball.getX() + normaleX * overlap,
                        ball.getY() + normaleY * overlap
                );
            }
        }
    }

    public int getId() {
        return id;
    }

    /**
     * Crea l'area (forma) del giocatore partendo da un'immagine.
     * La forma viene creata in coordinate locali, senza offset di posizione.
     */
    private Area creaArea(int numG) {
        try {
            BufferedImage image = (numG == 1) ?
                    ImageIO.read(getClass().getResourceAsStream("/images/testa1.png")) :
                    ImageIO.read(getClass().getResourceAsStream("/images/testa2.png"));

            if (image == null) return null;

            Path2D path = new Path2D.Double();
            double fScal = 1.0; // Fattore di scala

            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            // Crea la shape pixel per pixel in base all'immagine
            for (int y = 0; y < imgHeight; y++) {
                for (int x = 0; x < imgWidth; x++) {
                    int pixel = image.getRGB(x, y);
                    if ((pixel >> 24) != 0x00) { // Pixel visibile
                        double px = x * fScal;
                        double py = y * fScal;
                        path.append(new Rectangle2D.Double(px, py, fScal, fScal), false);
                    }
                }
            }
            return new Area(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Implementazione del metodo astratto getFormaBase()
    @Override
    public Shape getFormaBase() {
        return formaBase;
    }

    /**
     * Override di getShape() per centrare correttamente la forma del giocatore.
     * Qui si trasla la forma in modo che il "piede" (base della figura) coincida con la posizione (x, y).
     */
    @Override
    public Shape getShape() {
        Rectangle2D bounds = formaBase.getBounds2D();
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.scale(0.75, -0.75);
        at.translate(-bounds.getWidth() / 2, -bounds.getHeight());
        return at.createTransformedShape(formaBase);
    }

}
