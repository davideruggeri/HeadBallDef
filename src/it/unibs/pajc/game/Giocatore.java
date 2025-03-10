package it.unibs.pajc.game;

import javax.imageio.ImageIO;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Giocatore extends Oggetto {
    private boolean isJumping = false;
    private final int id;

    private final Area formaBase;

    public Giocatore(CampoDiGioco campo, int cx, int cy, int id, boolean isBot) {
        super(campo, cx, cy);
        this.id = id;
        this.formaBase = creaArea(id);
   }

    @Override
    public void stepNext() {
        handleCollision(campo.getBall());
        super.stepNext();
        updateJump();
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            float FORZASALTO = 10;
            setVelocita(getVelocitaX(), FORZASALTO);
        }
    }

    public void updateJump() {
        if (isJumping) {
            float GRAVITA = 0.5f;
            vy -= GRAVITA;
        }
        if (getY() <= campo.getGroundY()) {
            setPosizione(getX(), campo.getGroundY());
            isJumping = false;
        }
    }

    @Override
    public Shape getFormaBase() {return formaBase;}
    public int getId() {return id;}

    /* **************************************************
    * Collisione con la palla con calcolo della normale *
    *****************************************************/

    public void handleCollision(Ball ball) {
        if (checkCollision(ball)) {

            float deltaX = ball.getX() - getX();
            float deltaY = ball.getY() - getY();
            float distanza = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distanza == 0) return;

            if(Math.abs(deltaY) < 0.1f) {
                deltaY = 0.1f * Math.signum(deltaY);
            }

            float normaleX = deltaX / distanza;
            float normaleY = deltaY / distanza;

            float velRelX = ball.getVelocitaX() - getVelocitaX();
            float velRelY = ball.getVelocitaY() - getVelocitaY();
            float velLungoNormale = velRelX * normaleX + velRelY * normaleY;

            if (velLungoNormale > -0.1f) return;

            float e = 1.2f;
            float impulso = -(1 + e) * velLungoNormale;

            ball.setVelocita(
                    ball.getVelocitaX() + impulso * normaleX,
                    ball.getVelocitaY() + impulso * normaleY
            );

            float minVelocita = 2.0f;
            if (Math.abs(ball.getVelocitaX()) < minVelocita) {
                ball.setVelocita(Math.signum(ball.getVelocitaX()) * minVelocita, ball.getVelocitaY());
            }
            if (Math.abs(ball.getVelocitaY()) < minVelocita) {
                ball.setVelocita(ball.getVelocitaX(), Math.signum(ball.getVelocitaY()) * minVelocita);
            }

            float spin = (getVelocitaX() - ball.getVelocitaX()) * 0.5f;
            ball.setVelocita(ball.getVelocitaX() + spin, ball.getVelocitaY());

            float minDistanza = 30;
            float overlap = Math.max(0 , minDistanza - distanza) * 1.2f;
            if (overlap > 0) {
                ball.setPosizione(
                        ball.getX() + normaleX * overlap,
                        ball.getY() + normaleY * overlap
                );
            }
        }
    }

    /* **********************
    * Grafica del giocatore *
    *************************/

    private Area creaArea(int numG) {
        try {
            BufferedImage image = (numG == 1) ?
                    ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/testa1.png"))) :
                    ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/testa2.png")));

            if (image == null) return null;

            Path2D path = new Path2D.Double();
            double fScal = 1.0;

            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            for (int y = 0; y < imgHeight; y++) {
                for (int x = 0; x < imgWidth; x++) {
                    int pixel = image.getRGB(x, y);
                    if ((pixel >> 24) != 0x00) {
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
