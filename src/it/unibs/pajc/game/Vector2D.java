package it.unibs.pajc.game;

public class Vector2D {
    float x, y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public float dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    public float length() {
        return (float) Math.sqrt(x*x+y*y);
    }
    public Vector2D normalize() {
        float len = length();
        return len != 0 ? new Vector2D(x / len, y / len) : new Vector2D(0,0);
    }
    public Vector2D scale(float scale) {
        return new Vector2D(x * scale, y * scale);
    }
    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }
}
