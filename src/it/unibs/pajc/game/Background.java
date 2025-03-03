package it.unibs.pajc.game;

import it.unibs.pajc.client.Client;
import it.unibs.pajc.client.ClientCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class Background extends JPanel implements KeyListener {

    private final CampoDiGioco campo;

    private Image backgroundImage;
    private Image giocatore1;
    private Image giocatore2;

    private Client client;

    private JLabel gameTimer;
    private Timer t;
    private int seconds;

    public Background(Client client, boolean singlePlayer) {
        setLayout(null);
        campo = new CampoDiGioco(singlePlayer);
        this.client = client;

        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);

        gameTimer = new JLabel("01:30", SwingConstants.CENTER);
        gameTimer.setFont(new Font("Courier New", Font.BOLD, 40));

        add(gameTimer);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                gameTimer.setBounds(-95, 800, 200, 50);
            }
        });


        loadImages();

        Timer animator = new Timer(16, e -> {
            applyControls();
            if (client == null) {
                campo.stepNext();
            }
            repaint();
        });

        animator.start();
        aggiornaTimer();
    }

    private void aggiornaTimer() {
        seconds = 90;
        gameTimer.setText("01:30");

        t = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds--;
                int minutes = seconds / 60;
                int sec = seconds % 60;

                gameTimer.setText(String.format("%02d:%02d", minutes, sec));
            }
        });

        t.start();
    }


    private void loadImages() {
        backgroundImage = new ImageIcon(getClass().getResource("/images/sfondo1.png")).getImage();
        giocatore1 = new ImageIcon(getClass().getResource("/images/testa1.png")).getImage();
        giocatore2 = new ImageIcon(getClass().getResource("/images/testa2.png")).getImage();
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
            if (o instanceof Giocatore giocatore) {
                Image img = (giocatore == campo.getLocalPlayer()) ? giocatore1 : giocatore2;

                float imgX = giocatore.getX();
                float imgY = giocatore.getY();

                AffineTransform at = new AffineTransform();
                if (giocatore == campo.getLocalPlayer()) {
                    at.translate(imgX - (296) / s, imgY + (133) / s);
                } else if (giocatore == campo.getRemotePlayer()) {
                    at.translate(imgX + (212) / s, imgY + (133) / s);
                }
                at.scale(1.5, 1.5);
                at.scale(1, -1);

                g2d.drawImage(img, at, this);
                g2d.draw(giocatore.getShape());

            } else if (o instanceof Ball) {
                g2d.setColor(Color.YELLOW);
                AffineTransform at = new AffineTransform();
                at.translate(o.getX() - o.getShape().getBounds2D().getWidth() / 2,
                        o.getY() - o.getShape().getBounds2D().getHeight() / 2);

                g2d.fill(at.createTransformedShape(o.getShape()));
            }
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRect(-getWidth(), 0, getWidth()*2, 0);
        g2d.drawRect(-getWidth() + 245, 0, 0, 300);
        g2d.drawRect(getWidth() - 245, 0, 0, 300);
        g2d.drawRect(-getWidth(), 300, 245, 0);
        g2d.drawRect(getWidth() -245, 300, 245, 0);
    }

    private final ArrayList<Integer> currentActiveKeys = new ArrayList<>();

    public void applyControls() {
        Giocatore g1 = campo.getLocalPlayer();
        if (g1 == null) return;

        g1.setVelocita(0, g1.getVelocitaY());

        if (client != null) {
            for (Integer key : currentActiveKeys) {
                ClientCommand command = switch (key) {
                    case KeyEvent.VK_RIGHT -> new ClientCommand(ClientCommand.CommandType.MOVE_RIGHT, 1);
                    case KeyEvent.VK_LEFT -> new ClientCommand(ClientCommand.CommandType.MOVE_LEFT, 1);
                    case KeyEvent.VK_SPACE -> new ClientCommand(ClientCommand.CommandType.JUMP, 1);
                    default -> null;
                };
                if (command != null) client.sendCommand(command);
            }
        } else {
            for (Integer key : currentActiveKeys) {
                switch (key) {
                    case KeyEvent.VK_RIGHT -> g1.setVelocita(6f, g1.getVelocitaY());
                    case KeyEvent.VK_LEFT -> g1.setVelocita(-6f, g1.getVelocitaY());
                    case KeyEvent.VK_SPACE -> g1.jump();
                }
            }
        }
    }

    public void updateGameState(GameState state) {
        if (state != null) {
            state.applyToCampo(campo);
            repaint();
        } else {
            System.err.println("Ricevuto GameState nullo");
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (!currentActiveKeys.contains(e.getKeyCode())) {
            currentActiveKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentActiveKeys.remove((Integer) e.getKeyCode());
    }
}
