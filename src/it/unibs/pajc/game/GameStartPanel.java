package it.unibs.pajc.game;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GameStartPanel extends JPanel {

    private final Random random = new Random();

    public GameStartPanel() {
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Monospaced", Font.BOLD, 150);
        g2d.setFont(font);
        String message = "GAME START";

        FontMetrics metrics = g2d.getFontMetrics(font);
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

        g2d.setColor(Color.WHITE);
        g2d.drawString(message, x, y);
    }
}
