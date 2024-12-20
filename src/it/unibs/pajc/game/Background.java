package it.unibs.pajc.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class Background extends JPanel implements KeyListener {

    CampoDiGioco campo = new CampoDiGioco();

    private Image backgroundImage;
    private Image giocatore1;
    private Image giocatore2;

    public Background() {
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);

        Timer animator = new Timer(16, e -> {
            applyControls();
            campo.stepNext();
            repaint();
        });

        animator.start();

        String sfondo = "/images/sfondo1.png";
        backgroundImage = new ImageIcon(getClass().getResource(sfondo)).getImage();
        String g1 = "/images/testa1.png";
        giocatore1 = new ImageIcon(getClass().getResource(g1)).getImage();
        String g2 = "/images/testa2.png";
        giocatore2 = new ImageIcon(getClass().getResource(g2)).getImage();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;


        double s = (double) Math.min(getWidth(), getHeight()) / 1000.;

        g2d.scale(s, -s);
        g2d.translate(getWidth() / (2 * s), (-getHeight() / s) + (132 * s));
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, (int) (-493 / s), (int) (-40 / s), (int) (getWidth() / s), (int) (getHeight() / s), this);
        }


        for (Oggetto o : campo.listaOggetti) {
            if (o instanceof Giocatore) {
                Giocatore giocatore = (Giocatore) o;

                Image img = (giocatore == campo.getG1()) ? giocatore1 : giocatore2;
/*
                int cx = giocatore.getCx();
                int cy = giocatore.getCy();

                int imgX = cx - img.getWidth(null) / 2;
                int imgY = cy - img.getHeight(null) / 2;

                AffineTransform at = new AffineTransform();
                at.scale(1.5, 1.5);  // Aumentiamo le dimensioni dell'immagine di 1.5 volte
                at.translate(imgX + 150, imgY + 78);
                at.scale(1, -1);  // Capovolgiamo l'immagine lungo l'asse X

                // Applicare la trasformazione affine
                g2d.drawImage(img, at, this);
*/
                g2d.fill(giocatore.getShape());


            } else {
                g2d.fill(o.getShape());
            }

            g2d.drawRect(-getWidth(), 0, getWidth() * 2, 0);

        }
    }


    private ArrayList<Integer> currentActiveKeys = new ArrayList<>();

    public void applyControls() {
        Giocatore g1 = campo.getG1();
        if (g1 == null) return;

        g1.setVelocita(0, g1.getVelocitaY()); // Resetta la velocità orizzontale a ogni chiamata

        for (Integer key : currentActiveKeys) {
            switch (key) {
                case KeyEvent.VK_RIGHT:
                    g1.setVelocita(2, g1.getVelocitaY());
                    break;
                case KeyEvent.VK_LEFT:
                    g1.setVelocita(-2, g1.getVelocitaY());
                    break;
                case KeyEvent.VK_SPACE:
                    g1.jump(); // Salto
                    break;
            }
        }
    }




    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if(!currentActiveKeys.contains(e.getKeyCode())) {
            currentActiveKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentActiveKeys.remove((Integer) e.getKeyCode());
    }

}