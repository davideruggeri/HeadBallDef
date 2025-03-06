package it.unibs.pajc.game;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;

public abstract class Oggetto {
    protected float x, y;   // Posizione nel mondo
    protected float vx, vy; // Velocità
    protected CampoDiGioco campo;

    public Oggetto(CampoDiGioco campo, float x, float y) {
        this.campo = campo;
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelocitaX() { return vx; }
    public float getVelocitaY() { return vy; }

    public void setPosizione(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void setVelocita(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void stepNext() {
        x += vx;
        y += vy;
    }

    public void applyGravity() {
        vy -= 0.5f; // La gravità va applicata secondo le unità del tuo mondo
    }

    public void applyBounce() {
        vy -= vy * 0.8f;
    }

    public void applyFriction() {
        vx *= 0.9f;
    }

    // Ogni sottoclasse deve definire la propria forma di base,
    // già impostata in coordinate locali
    public abstract Shape getFormaBase();

    // Restituisce la shape traslata in base alla posizione corrente
    public Shape getShape() {
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        return at.createTransformedShape(getFormaBase());
    }

    public boolean checkCollision(Oggetto o) {
        Area area1 = new Area(this.getShape());
        Area area2 = new Area(o.getShape());
        area1.intersect(area2);
        return !area1.isEmpty();
    }
}
