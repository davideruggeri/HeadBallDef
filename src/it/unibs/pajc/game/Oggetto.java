package it.unibs.pajc.game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class Oggetto {
    private float[] posizione = {0, 0}; // x, y
    protected float[] velocita = {0, 0}; // vx, vy
    protected CampoDiGioco campo;

    public Oggetto (CampoDiGioco campo){this.campo = campo;}

    public float getX(){return posizione[0];}
    public float getY(){return posizione[1];}
    public float getVelocitaX(){return velocita[0];}
    public float getVelocitaY(){return velocita[1];}
    public void setPosizione(float x, float y){posizione[0] = x; posizione[1] = y;}
    public void setVelocita(float x, float y){velocita[0] = x; velocita[1] = y;}
    public void setPosizioneY(float y){posizione[1] = y;}
    public void setVelocitaY(float y){velocita[1] = y;}


    public void stepNext() {
        posizione[0] += velocita[0];
        posizione[1] += velocita[1];
    }

    public void applyGravity() {
        velocita[1] -= 0.5f; // gravità verso il basso
    }

    public void applyBounce() {
        velocita[1] -= velocita[1] * 0.8f;
    }

    public void applyFriction() {
        velocita[0] *= 0.9f;
    }

    /*----------------------------------------------------------
     * Creazione della Shape
     */

    protected Shape shape;

        public Shape getShape() {
            AffineTransform t = new AffineTransform();
            t.translate(getX(), getY());

            return t.createTransformedShape(shape);
        }

    public boolean checkCollision(Oggetto o){
        Area o1 = new Area(this.getShape());
        Area o2 = new Area(o.getShape());
        o1.intersect(o2);

        return !o1.isEmpty();
    }
}
