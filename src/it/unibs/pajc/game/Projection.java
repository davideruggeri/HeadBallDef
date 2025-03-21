package it.unibs.pajc.game;

public class Projection {
    public float min, max;

    public Projection(float min, float max) {
        this.min = min;
        this.max = max;
    }
    public static float getOverlap(Projection a, Projection b) {
        return Math.min(a.max, b.max) - Math.max(a.min, b.min);
    }
}
