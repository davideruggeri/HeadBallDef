package it.unibs.pajc.game;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class Ball extends Oggetto {
    private float gravita = 0.5f;
    private float fattoreRimbalzo = 0.8f;

    public Ball(CampoDiGioco campo, int xc, int yc) {
        super(campo);

        this.shape = new Area(new Ellipse2D.Double(
                xc,
                yc,
                40,
                40
        ));


    }

    public void stepNext() {
        // Applica gravità
        velocita[1] += gravita;

        // Aggiorna posizione
        super.stepNext();

        // Gestisci rimbalzi verticali
        if (getY() >= campo.bounds.getMaxY()) {
            velocita[1] = -Math.abs(velocita[1]) * fattoreRimbalzo;
            setPosizione(getX(), (float) campo.bounds.getMaxY());
        }
        if (getY() <= campo.bounds.getMinY()) {
            velocita[1] = Math.abs(velocita[1]);
            setPosizione(getX(), (float) campo.bounds.getMinY());
        }
    }
}

