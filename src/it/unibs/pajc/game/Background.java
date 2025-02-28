package it.unibs.pajc.game;

import it.unibs.pajc.client.Client;
import it.unibs.pajc.client.ClientCommand;
import it.unibs.pajc.network.NetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Background extends JPanel implements KeyListener {

    private final CampoDiGioco campo = new CampoDiGioco();

    private Image backgroundImage;
    private Image giocatore1;
    private Image giocatore2;

    private Client client;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Background(Client client) {
        this.client = client;

        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);

        loadImages();

        Timer animator = new Timer(16, e -> {
            applyControls();
            campo.stepNext();
            repaint();
        });

        animator.start();

        if (client != null) {
            new Thread(this::listenToServer).start();
        }
    }

    private void loadImages() {
        backgroundImage = new ImageIcon(getClass().getResource("/images/sfondo1.png")).getImage();
        giocatore1 = new ImageIcon(getClass().getResource("/images/testa1.png")).getImage();
        giocatore2 = new ImageIcon(getClass().getResource("/images/testa2.png")).getImage();
    }

    public CampoDiGioco getCampo() {
        return campo;
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

            g2d.setColor(Color.BLACK);
            g2d.drawRect(-getWidth(), 0, getWidth() * 2, 0);
        }
    }

    // Modifica il loop di ascolto nel thread di Background
    private void listenToServer() {
        while (true) {
            NetworkMessage message = client.readMessage();
            if (message.getType() == NetworkMessage.MessageType.GAME_STATE) {
                GameState gameState = (GameState) message.getPayload();
                gameState.applyToCampo(campo);
                repaint();
            } else {
                System.err.println("Messaggio non previsto: " + message.getType());
            }
        }
    }


    private final ArrayList<Integer> currentActiveKeys = new ArrayList<>();

    public void applyControls() {
        Giocatore g1 = campo.getLocalPlayer();
        if (g1 == null) return;

        g1.setVelocita(0, g1.getVelocitaY());

        if (client != null) { // multiplayer
            for (Integer key : currentActiveKeys) {
                ClientCommand command = switch (key) {
                    case KeyEvent.VK_RIGHT -> new ClientCommand(ClientCommand.CommandType.MOVE_RIGHT, 1);
                    case KeyEvent.VK_LEFT -> new ClientCommand(ClientCommand.CommandType.MOVE_LEFT, 1);
                    case KeyEvent.VK_SPACE, KeyEvent.VK_Z -> new ClientCommand(ClientCommand.CommandType.JUMP, 1);
                    default -> null;
                };
                if (command != null) {
                    client.sendCommand(command);
                }
            }
        } else { // single player
            for (Integer key : currentActiveKeys) {
                switch (key) {
                    case KeyEvent.VK_RIGHT -> g1.setVelocita(6f, g1.getVelocitaY());
                    case KeyEvent.VK_LEFT -> g1.setVelocita(-6f, g1.getVelocitaY());
                    case KeyEvent.VK_SPACE -> g1.jump();
                }
            }
        }
    }

    public void setClient(Client client) {
        this.client = client;
        this.socket = client.getSocket();
        this.out = client.getOutputStream();
        this.in = client.getInputStream();
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
