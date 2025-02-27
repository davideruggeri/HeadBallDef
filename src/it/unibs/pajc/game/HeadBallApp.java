package it.unibs.pajc.game;

import it.unibs.pajc.client.Client;
import it.unibs.pajc.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HeadBallApp {
    private JFrame frame;
    private Server server;
    private Client client;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                HeadBallApp window = new HeadBallApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
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
       if (server == null) {
           server = new Server();
           boolean success = server.startServer();

           if (!success) {
               JOptionPane.showMessageDialog(frame, "Errore nell'avvio del server.", "Errore", JOptionPane.ERROR_MESSAGE);
               return;
           }

           JOptionPane.showMessageDialog(frame, "Server avviato! Ora puoi connetterti.", "Successo", JOptionPane.PLAIN_MESSAGE);
       }

       if (client == null) {
           client = new Client(frame);
           boolean connected = client.connectToServer("localhost", Server.PORT);

           if (!connected) {
               JOptionPane.showMessageDialog(frame, "Errore nella connessione al server.", "Errore", JOptionPane.ERROR_MESSAGE);
           }
       }
   }

    private void startLocalGame(ActionEvent e) {
        showControls();

        frame.getContentPane().removeAll();
        Background sv = new Background();
        sv.getCampo().setSinglePlayer(); // Imposta la modalità single-player
        frame.getContentPane().add(sv, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void joinGame(ActionEvent e) {
        String port;
        while (true) {
            port = JOptionPane.showInputDialog(frame, "Inserire il numero della porta:", "Connessione", JOptionPane.PLAIN_MESSAGE);
            if (port == null) return;

            try {
                int portNumber = Integer.parseInt(port);
                if (portNumber > 1024 && portNumber < 65535) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(frame, "Porta non valida! Inserire un numero tra 1025 e 65534.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Inserire un numero valido!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }

        client = new Client(frame);
        boolean connected = client.connectToServer("localhost", Server.PORT);

        if (!connected) {
            JOptionPane.showMessageDialog(frame, "Connessione al server fallita.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(frame, "Connessione avvenuta con successo", "Successo", JOptionPane.PLAIN_MESSAGE);
        showControls();
    }

    private void showControls() {
        JOptionPane.showMessageDialog(frame, "I tasti disponibili per la partita sono:\n" +
                "- (<- e ->) per muoversi di lato.\n" +
                "- (SPACE) per saltare.\n" +
                "- (Z) calcio.", "Mosse disponibili", JOptionPane.PLAIN_MESSAGE);
    }
}
