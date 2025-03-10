package it.unibs.pajc.game;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;

public abstract class Oggetto {
    protected float x, y;
    protected float vx, vy;
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
        vy -= 0.5f;
    }

    public void applyFriction() {
        vx *= 0.9f;
    }

    public abstract Shape getFormaBase();

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
