package it.unibs.pajc.game;

import it.unibs.pajc.client.Client;
import it.unibs.pajc.client.ClientCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Objects;

public class Background extends JPanel implements KeyListener {
    private final CampoDiGioco campo;

    private Image backgroundImage;
    private Image giocatore1;
    private Image giocatore2;
    private Client client;
    private Timer t;
    private final Timer animator;

    private final JFrame frame;

    private final ArrayList<Integer> currentActiveKeys = new ArrayList<>();

    public Background(JFrame frame, Client client, boolean singlePlayer) {
        this.frame = frame;
        setLayout(null);
        campo = new CampoDiGioco(singlePlayer);
        this.client = client;

        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);

        loadImages();

        animator = new Timer(16, _ -> {
            applyControls();
            if (client == null) {
                campo.stepNext();
            }
            repaint();
        });
        animator.start();

        if (client == null) {
            aggiornaTimer();
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void aggiornaTimer() {
        t = new Timer(1000, _ -> {
            if (campo.getGameTime() > 0) {
                campo.setGameTime(campo.getGameTime() - 1);
                repaint();
            } else {
                t.stop();
                animator.stop();
                endGame();
            }
        });
        t.start();
    }

    public void endGame() {
        removeKeyListener(this);

        String message = "Partita terminata!!!\n "
                + campo.getPlayer1Score() + " - " + campo.getPlayer2Score();
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);

        frame.dispose();
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            HeadBallApp menu = new HeadBallApp();
            menu.setExistingFrame(frame);
            menu.showMenu();
        });
    }

    private void loadImages() {
        ImageIcon bgIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/sfondo1.png")));
        backgroundImage = bgIcon.getImage();
        ImageIcon icon1 = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/testa1.png")));
        giocatore1 = icon1.getImage();
        ImageIcon icon2 = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/testa2.png")));
        giocatore2 = icon2.getImage();
    }

    /* *************************
    * Creazione campo di gioco *
    ****************************/

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            int imgWidth = backgroundImage.getWidth(this);
            int imgHeight = backgroundImage.getHeight(this);

            double scaleX = (double) getWidth() / imgWidth;
            double scaleY = (double) getHeight() / imgHeight;

            AffineTransform t = new AffineTransform();
            t.translate(0, getHeight());
            t.scale(scaleX, -scaleY);

            g2d.drawImage(backgroundImage, t, this);
        }

        double worldWidth = CampoDiGioco.CAMPO_WIDTH;   // 1000
        double worldHeight = CampoDiGioco.CAMPO_HEIGHT;   // 600

        double scale = getWidth() / worldWidth;
        double extraY = (getHeight() / scale - worldHeight) / 2;

        AffineTransform worldToScreen = new AffineTransform();
        worldToScreen.translate(0, extraY + worldHeight);
        worldToScreen.scale(scale, -scale);

        AffineTransform original = g2d.getTransform();
        g2d.transform(worldToScreen);

        for (Oggetto o : campo.getOggetti()) {
            if (o instanceof Giocatore giocatore) {
                Image img = (giocatore == campo.getLocalPlayer()) ? giocatore1 : giocatore2;

                Rectangle2D bounds = giocatore.getFormaBase().getBounds2D();

                AffineTransform at = new AffineTransform();

                at.translate(giocatore.getX(), giocatore.getY());

                double scaleFactor = 0.75;

                at.scale(scaleFactor, -scaleFactor);

                at.translate(-bounds.getWidth() / 2, -bounds.getHeight());
                if (giocatore.getId() == 1) {
                    at.translate(bounds.getX() - 7, bounds.getY() - 13);
                    g2d.drawImage(img, at, this);
                } else {
                    at.translate(bounds.getX() - 52, bounds.getY() - 13);
                    g2d.drawImage(img, at, this);

                }

                g2d.setColor(Color.BLACK);
                //g2d.draw(giocatore.getShape()); //Disegno della sagoma del giocatore

            } else if (o instanceof Ball) {
                drawBall(g2d, (Ball) o);
            }
        }

        g2d.setColor(Color.BLACK);

        //porta dx
        //g2d.drawRect(925, 240, (int) worldWidth, 0);
        //g2d.drawRect(925, 60, 0,181);

        //porta sx
        //g2d.drawRect(0, 238, 75, 0);
        //g2d.drawRect(75, 60,0,179);

        g2d.setTransform(original);

        if (campo.getGameTime() <= 15) {
            g2d.setColor(Color.RED);
            drawTimer(g2d);
        } else {
            g2d.setColor(Color.BLACK);
            drawTimer(g2d);
        }

        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        String player1ScoreTxt = String.valueOf(campo.getPlayer1Score());
        String player2ScoreTxt = String.valueOf(campo.getPlayer2Score());
        int player1x = getWidth() / 4;
        int player1y = 50;
        int player2x = (3 * getWidth() / 4) - fm.stringWidth(player2ScoreTxt);
        int player2y = 50;
        g2d.drawString(player1ScoreTxt, player1x, player1y);
        g2d.drawString(player2ScoreTxt, player2x, player2y);
    }

    private void drawBall(Graphics2D g2d, Ball ball) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape ballShape = ball.getShape();

        g2d.setColor(Color.WHITE);
        g2d.fill(ballShape);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(ballShape);

        Rectangle bounds = ballShape.getBounds();
        int diameter = Math.min(bounds.width, bounds.height);
        int centerX = bounds.x + diameter / 2;
        int centerY = bounds.y + diameter / 2;

        drawSoccerPattern(g2d, centerX, centerY, diameter);
    }

    private void drawSoccerPattern(Graphics2D g2d, int centerX, int centerY, int diameter) {
        int pentagonRadius = diameter / 4;
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];

        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(90 + i * 72);
            xPoints[i] = (int) (centerX + pentagonRadius * Math.cos(angle));
            yPoints[i] = (int) (centerY - pentagonRadius * Math.sin(angle));
        }
        Polygon pentagon = new Polygon(xPoints, yPoints, 5);
        g2d.setColor(Color.BLACK);
        g2d.fillPolygon(pentagon);
        g2d.drawPolygon(pentagon);

    }

    private void drawTimer(Graphics2D g2d) {
        g2d.setFont(new Font("Courier New", Font.BOLD, 50));
        String timerTxt = String.format("%02d:%02d", campo.getGameTime() / 60, campo.getGameTime() % 60);
        FontMetrics fm = g2d.getFontMetrics();
        int timerX = (getWidth() - fm.stringWidth(timerTxt)) / 2;
        int timerY = 50;
        g2d.drawString(timerTxt, timerX, timerY);
    }

    public void updateGameState(GameState state) {
        if (state != null) {
            state.applyToCampo(campo);
            repaint();
        } else {
            System.err.println("Ricevuto GameState nullo");
        }
    }

    /* ********************
     * Controlli di gioco *
     **********************/

    public void applyControls() {
        Giocatore g1 = campo.getLocalPlayer();
        if (g1 == null) return;

        g1.setVelocita(0, g1.getVelocitaY());

        if (client != null) {
            for (Integer key : currentActiveKeys) {
                ClientCommand command = switch (key) {
                    case KeyEvent.VK_RIGHT -> new ClientCommand(ClientCommand.CommandType.MOVE_RIGHT, 1);
                    case KeyEvent.VK_LEFT  -> new ClientCommand(ClientCommand.CommandType.MOVE_LEFT, 1);
                    case KeyEvent.VK_SPACE -> new ClientCommand(ClientCommand.CommandType.JUMP, 1);
                    case KeyEvent.VK_Z -> new ClientCommand(ClientCommand.CommandType.SHOOT, 1);
                    default -> null;
                };
                if (command != null) client.sendCommand(command);
            }
        } else {
            for (Integer key : currentActiveKeys) {
                switch (key) {
                    case KeyEvent.VK_RIGHT -> g1.setVelocita(8f, g1.getVelocitaY());
                    case KeyEvent.VK_LEFT  -> g1.setVelocita(-8f, g1.getVelocitaY());
                    case KeyEvent.VK_SPACE -> g1.jump();
                    case KeyEvent.VK_Z -> g1.shot(1);
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!currentActiveKeys.contains(e.getKeyCode())) {
            currentActiveKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {currentActiveKeys.remove((Integer) e.getKeyCode());}
}