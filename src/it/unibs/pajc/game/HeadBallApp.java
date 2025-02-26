package it.unibs.pajc.game;

import it.unibs.pajc.clinet.Client;
import it.unibs.pajc.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HeadBallApp {
    private JFrame frame;

    Server server;
    Client client;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    HeadBallApp window = new HeadBallApp();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public HeadBallApp() {
        startGame();
    }

    public void startGame() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Menu");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);

        JPanel menuPanel = new JPanel();
        frame.getContentPane().add(menuPanel, BorderLayout.CENTER);
        menuPanel.setBackground(Color.GRAY);
        menuPanel.setLayout(null);

        JLabel titleLabel = new JLabel("HEAD BALL");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Showcard Gothic", Font.BOLD, 80));
        titleLabel.setBounds(175, 47, 673, 173);
        menuPanel.add(titleLabel);

        JButton btnSinglePlayer = new JButton("SINGLE PLAYER");
        btnSinglePlayer.setFont(new Font("Arial Black", Font.PLAIN, 40));
        btnSinglePlayer.setBounds(150, 244, 728, 96);
        menuPanel.add(btnSinglePlayer);
        btnSinglePlayer.addActionListener(this::startLocalGame);

        JButton btnHostGame = new JButton("HOST GAME");
        btnHostGame.setFont(new Font("Arial Black", Font.PLAIN, 40));
        btnHostGame.setBounds(150, 340, 364, 96);
        menuPanel.add(btnHostGame);
        btnHostGame.addActionListener(this::hostGame);

        JButton btnJoinGame = new JButton("JOIN GAME");
        btnJoinGame.setFont(new Font("Arial Black", Font.PLAIN, 40));
        btnJoinGame.setBounds(514, 340, 364, 96);
        menuPanel.add(btnJoinGame);
        btnJoinGame.addActionListener(this::joinGame);

    }

    private void hostGame(ActionEvent e) {
        server = new Server(frame);
        boolean success = server.startServer();

        if (!success) {
            JOptionPane.showMessageDialog(null, "Errore nell'avvio del server.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, "I tasti disponibili per la partita sono:\n" +
                "- (<- e ->) per muoversi di lato.\n" +
                "- (space) per saltare.\n" +
                "- (z) calcio.", "Mosse disponibili", JOptionPane.PLAIN_MESSAGE);

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
    }



    private void startLocalGame(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "I tasti disponibili per la partita sono:\n" +
                "- (<- e ->) per muoversi di lato.\n" +
                "- (space) per saltare.\n" +
                "- (z) calcio.", "Mosse disponibili", JOptionPane.PLAIN_MESSAGE);

        frame.getContentPane().removeAll();
        Background sv = new Background();
        frame.getContentPane().add(sv, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();

    }
    private void joinGame(ActionEvent e) {
        String port;
        do {
            port = JOptionPane.showInputDialog(null, "Inserire il numero della porta: ", null, JOptionPane.PLAIN_MESSAGE);
            if (port == null) {
                return;
            }
            if (!port.equals("1234")) {
                JOptionPane.showMessageDialog(null, "Porta errata, Riprovare", null, JOptionPane.ERROR_MESSAGE);
            }
        } while (!port.equals("1234"));

        client = new Client(frame);
        boolean connected = client.connectToServer("localhost", Integer.parseInt(port));

        if (!connected) {
            JOptionPane.showMessageDialog(null, "Connessione al server fallita.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, "Connessione avvenuta con successo", null, JOptionPane.PLAIN_MESSAGE);
        JOptionPane.showMessageDialog(null, "I tasti disponibili per la partita sono:\n" +
                "- (<- e ->) per muoversi di lato.\n" +
                "- (space) per saltare.\n" +
                "- (z) calcio.", "Mosse disponibili", JOptionPane.PLAIN_MESSAGE);

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
    }

}
