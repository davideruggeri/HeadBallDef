package it.unibs.pajc.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Giocatore extends Oggetto {

    private boolean isJumping = false;
    private final float GRAVITA = 0.5f;
    private final float FORZASALTO = 60;
    private final double GROUNDLEVEL = 0.563;


    public Giocatore(CampoDiGioco campo, int cx, int cy, int numGiocatore) {
        super(campo);

        this.shape = creaArea(cx, cy, numGiocatore);
    }


    @Override
    public void stepNext() {
        if (isJumping || getY() < GROUNDLEVEL) {
            velocita[1] += GRAVITA;
        }


        if (getY() >= GROUNDLEVEL - getY()) {
            isJumping = false; // Resetta lo stato del salto
            setPosizione(getX(), (float) GROUNDLEVEL); // Blocca il giocatore al terreno
            velocita[1] = 0; // Ferma la velocità verticale
        }

        super.stepNext(); // Aggiorna posizione
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true; // Imposta lo stato di salto
            setVelocita(getVelocitaX(), FORZASALTO); // Imposta una velocità verso l'alto (negativa)
        }
        setVelocita(getVelocitaX(), FORZASALTO);
    }


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
