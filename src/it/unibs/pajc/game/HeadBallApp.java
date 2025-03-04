package it.unibs.pajc.game;

import it.unibs.pajc.client.Client;
import it.unibs.pajc.game.Background;
import it.unibs.pajc.server.Server;

import javax.swing.*;
import java.awt.*;

public class HeadBallApp {
    private JFrame frame;
    private Server server;
    private Client client;
    //private String host = "10.243.3.116"; //ip studenti
    //private String host = "192.168.1.14"; //ip casa Fritz
    //private String host = "192.168.1.101"; //ip casa P
    private String host = "localhost";


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new HeadBallApp().showMenu());
    }

    public void showMenu() {
        frame = new JFrame("Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel menuPanel = new JPanel(null);
        menuPanel.setBackground(Color.GRAY);

        JLabel titleLabel = new JLabel("HEAD BALL", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Showcard Gothic", Font.BOLD, 80));
        titleLabel.setBounds(175, 47, 673, 173);
        menuPanel.add(titleLabel);

        JButton btnSinglePlayer = new JButton("SINGLE PLAYER");
        btnSinglePlayer.setFont(new Font("Arial Black", Font.PLAIN, 40));
        btnSinglePlayer.setBounds(150, 244, 728, 96);
        btnSinglePlayer.addActionListener(e -> startLocalGame());
        menuPanel.add(btnSinglePlayer);

        JButton btnHostGame = new JButton("HOST GAME");
        btnHostGame.setFont(new Font("Arial Black", Font.PLAIN, 40));
        btnHostGame.setBounds(150, 340, 364, 96);
        btnHostGame.addActionListener(e -> hostGame());
        menuPanel.add(btnHostGame);

        JButton btnJoinGame = new JButton("JOIN GAME");
        btnJoinGame.setFont(new Font("Arial Black", Font.PLAIN, 40));
        btnJoinGame.setBounds(514, 340, 364, 96);
        btnJoinGame.addActionListener(e -> joinGame());
        menuPanel.add(btnJoinGame);

        frame.getContentPane().add(menuPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void startLocalGame() {
        showControls();
        loadGamePanel(null, true);  // Single player, nessun client
    }

    private void hostGame() {
        if (server == null) {
            server = new Server();
            if (!server.startServer()) {
                JOptionPane.showMessageDialog(frame, "Errore nell'avvio del server.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (connectClient(host, Server.PORT)) {
            showControls();
            loadGamePanel(client, false);
        }
    }

    private void joinGame() {
        String address = JOptionPane.showInputDialog(frame, "Inserisci IP server:");
        if (address == null || address.isBlank()) return;

        int port = getPortFromUser();
        if (port == -1) return;

        if (connectClient(address, port)) {
            showControls();
            loadGamePanel(client, false);
        }
    }

    private boolean connectClient(String address, int port) {
        client = new Client(frame);
        if (!client.connectToServer(address, port)) {
            JOptionPane.showMessageDialog(frame, "Connessione fallita.", "Errore", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void loadGamePanel(Client client, boolean singlePlayer) {
        frame.getContentPane().removeAll();
        Background gamePanel = new Background(client, singlePlayer);

        frame.getContentPane().add(gamePanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();


        if (client != null) {
            gamePanel.setClient(client);
            client.setBackground(gamePanel);
            client.startStateReceiver();

        }
    }

    private int getPortFromUser() {
        while (true) {
            String portStr = JOptionPane.showInputDialog(frame, "Porta:");
            if (portStr == null) return -1;
            try {
                int port = Integer.parseInt(portStr);
                if (port > 1024 && port < 65535) return port;
                JOptionPane.showMessageDialog(frame, "Porta non valida!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Inserisci un numero valido!");
            }
        }
    }

    private void showControls() {
        JOptionPane.showMessageDialog(frame, "Comandi:\n- Frecce: Muovi\n- SPACE: Salta\n- Z: Calcia");
    }
}
