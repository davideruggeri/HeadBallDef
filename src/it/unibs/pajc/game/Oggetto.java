package it.unibs.pajc.game;

import it.unibs.pajc.game.physic.Projection;
import it.unibs.pajc.game.physic.Vector2D;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

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

    public void applyFriction() {
        vx *= 0.7f;
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

    public static List<Vector2D> getVertices(Shape s) {
        List<Vector2D> vertices = new ArrayList<>();
        PathIterator iterator = s.getPathIterator(null);
        float[] coords = new float[6];
        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            if (type != PathIterator.SEG_CLOSE) {
                vertices.add(new Vector2D(coords[0], coords[1]));
            }
            iterator.next();
        }
        return vertices;
    }


    public static List<Vector2D> getAxes(List<Vector2D> vertices) {
        List<Vector2D> axes = new ArrayList<>();
        int count = vertices.size();
        for (int i = 0; i < count; i++) {
            Vector2D p1 = vertices.get(i);
            Vector2D p2 = vertices.get((i + 1) % count);
            Vector2D edge = new Vector2D(p2.x - p1.x, p2.y - p1.y);
            Vector2D normal = new Vector2D(-edge.y, edge.x).normalize();
            axes.add(normal);
        }
        return axes;
    }

    public static Projection projectVertices(List<Vector2D> vertices, Vector2D axis) {
        float min = vertices.get(0).dot(axis);
        float max = min;
        for (Vector2D vertex : vertices) {
            float projection = vertex.dot(axis);
            if (projection < min) {
                min = projection;
            }
            if (projection > max) {
                max = projection;
            }
        }
        return new Projection(min, max);
    }
}
