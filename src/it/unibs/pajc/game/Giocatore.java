package it.unibs.pajc.game;

import javax.imageio.ImageIO;
import java.awt.Shape;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Giocatore extends Oggetto {
    private boolean isJumping = false;
    private final int id;

    private final Area formaBase;

    public Giocatore(CampoDiGioco campo, int cx, int cy, int id) {
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
    public boolean isJumping() {return isJumping;}
    @Override
    public Shape getFormaBase() {return formaBase;}
    public int getId() {return id;}

    /* ************************
    * Collisione con la palla *
    **************************/

    private MTV calculateMTV(Shape shapeA, Shape shapeB) {
        List<Vector2D> verticesA = getVertices(shapeA);
        List<Vector2D> verticesB = getVertices(shapeB);

        List<Vector2D> axes = new ArrayList<>();
        axes.addAll(getAxes(verticesA));
        axes.addAll(getAxes(verticesB));

        float minOverlap = Float.MAX_VALUE;
        Vector2D mtvAxis = null;

        for (Vector2D axis : axes) {
            Projection projA = projectVertices(verticesA, axis);
            Projection projB = projectVertices(verticesB, axis);
            float overlap = Projection.getOverlap(projA, projB);

            if (overlap <= 0) {
                return null;
            } else if (overlap < minOverlap) {
                minOverlap = overlap;
                mtvAxis = axis;
            }
        }
        return new MTV(mtvAxis, minOverlap);
    }

    public void handleCollision(Ball ball) {
        if (checkCollision(ball)) {
            MTV mtv = calculateMTV(this.getShape(), ball.getShape());
            if (mtv != null) {

                Rectangle2D boundsPlayer = this.getShape().getBounds2D();
                Rectangle2D boundsBall = ball.getShape().getBounds2D();
                float centerPlayerX = (float) boundsPlayer.getCenterX();
                float centerPlayerY = (float) boundsPlayer.getCenterY();
                float centerBallX = (float) boundsBall.getCenterX();
                float centerBallY = (float) boundsBall.getCenterY();
                Vector2D centerDiff = new Vector2D(centerBallX - centerPlayerX, centerBallY - centerPlayerY);

                if (centerDiff.dot(mtv.axis) < 0) {
                    mtv.axis = mtv.axis.negate();
                }

                ball.setPosizione(
                        ball.getX() + mtv.axis.x * mtv.overlap,
                        ball.getY() + mtv.axis.y * mtv.overlap
                );

                Vector2D ballVelocity = new Vector2D(ball.getVelocitaX(), ball.getVelocitaY());
                Vector2D playerVelocity = new Vector2D(this.getVelocitaX(), this.getVelocitaY());

                Vector2D relativeVelocity = new Vector2D(
                        ballVelocity.x - playerVelocity.x,
                        ballVelocity.y - playerVelocity.y
                );

                float dot = relativeVelocity.dot(mtv.axis);

                float restitution = 0.8f;

                // Calcola l'impulso
                float impulse = -(1 + restitution) * dot;

                // Aggiorna la velocità della palla applicando la riflessione e la forza extra
                Vector2D newVelocity = new Vector2D(
                        ball.getVelocitaX() + impulse * mtv.axis.x,
                        ball.getVelocitaY() + impulse * mtv.axis.y
                );

                // Imposta la nuova velocità della palla
                ball.setVelocita(newVelocity.x, newVelocity.y);
            }
        }
    }

    /* *******************************************
    * Gestione della collisione tra i due player *
    **********************************************/

    public void resolveCollision(Giocatore other) {
        if (this.checkCollision(other)) {
            Rectangle2D bounds1 = this.getShape().getBounds2D();
            Rectangle2D bounds2 = other.getShape().getBounds2D();

            Rectangle2D intersezione = bounds1.createIntersection(bounds2);

            if (intersezione.getWidth() < intersezione.getHeight()) {
                if (this.x < other.x) {
                    this.setPosizione(this.x - (float)intersezione.getWidth()/2, this.y);
                    other.setPosizione(other.x + (float)intersezione.getWidth()/2, other.y);
                } else {
                    this.setPosizione(this.x + (float)intersezione.getWidth()/2, this.y);
                    other.setPosizione(other.x - (float)intersezione.getWidth()/2, other.y);
                }
            } else {
                if (this.y < other.y) {
                    this.setPosizione(this.x, this.y - (float)intersezione.getHeight()/2);
                    other.setPosizione(other.x, other.y + (float)intersezione.getHeight()/2);
                } else {
                    this.setPosizione(this.x, this.y + (float)intersezione.getHeight()/2);
                    other.setPosizione(other.x, other.y - (float)intersezione.getHeight()/2);
                }
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
