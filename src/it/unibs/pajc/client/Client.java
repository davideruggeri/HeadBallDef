package it.unibs.pajc.client;

import it.unibs.pajc.game.Background;
import it.unibs.pajc.game.GameState;
import it.unibs.pajc.game.HeadBallApp;
import it.unibs.pajc.network.NetworkMessage;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Background background;

    private final JFrame frame;

    private JDialog waitingDialog;

    public Client(JFrame frame) {
        this.frame = frame;
    }

    public boolean connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            NetworkMessage startMessage = readMessage();
            if (startMessage.getType() == NetworkMessage.MessageType.GAME_START) {
                throw new IOException("Messaggio iniziale inatteso dal server");
            }

            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Errore nella connessione al server:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void sendCommand(ClientCommand command) {
        try {
            NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.PLAYER_COMMAND, command);
            out.writeObject(message);
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Errore nell'invio del comando:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void requestInitialState() {
        sendCommand(new ClientCommand(ClientCommand.CommandType.REQUEST_INITIAL_STATE, 1));
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public void startStateReceiver() {
        Thread stateReceiver = new Thread(() -> {
            while (true) {
                NetworkMessage message = readMessage();
                if (message == null) break;

                switch (message.getType()) {
                    case GAME_STATE -> {
                        GameState state = (GameState) message.getPayload();
                        SwingUtilities.invokeLater(() -> {
                            if (background != null) {
                                background.updateGameState(state);
                            }
                        });
                    }
                    case COUNTDOWN_UPDATE -> {
                        int secondsLeft = (Integer) message.getPayload();
                        SwingUtilities.invokeLater(() -> showCountdown(secondsLeft));
                    }
                    case GAME_START -> {
                            SwingUtilities.invokeLater(this::hideWaitingDialog);
                        System.out.println("Il gioco è iniziato!");
                    }
                    case GAME_OVER -> {
                        String result = (String) message.getPayload();
                        SwingUtilities.invokeLater(() -> showEndMessage(result));
                    }
                }
            }
        });
        stateReceiver.setDaemon(true);
        stateReceiver.start();
    }


    public NetworkMessage readMessage() {
        try {
            return (NetworkMessage) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Errore nella lettura del messaggio dal server:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    private void showCountdown(int secondsLeft) {
        if (waitingDialog == null) {
            waitingDialog = new JDialog(frame, "Attesa inizio partita", true);
            waitingDialog.setSize(200, 100);
            waitingDialog.setLocationRelativeTo(frame);
            waitingDialog.setLayout(new BoxLayout(waitingDialog.getContentPane(), BoxLayout.Y_AXIS));
        }

        waitingDialog.getContentPane().removeAll();
        JLabel countdownLabel = new JLabel("Partita inizia in: " + secondsLeft + " secondi", JLabel.CENTER);
        waitingDialog.getContentPane().add(countdownLabel);
        waitingDialog.revalidate();
        waitingDialog.repaint();

        if (!waitingDialog.isVisible()) {
            SwingUtilities.invokeLater(() -> waitingDialog.setVisible(true));
        }

        if (secondsLeft == 0) {
            hideWaitingDialog();
        }
    }
    private void hideWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isVisible()) {
            waitingDialog.setVisible(false);
        }
    }
    private void showEndMessage(String result) {
        JOptionPane.showMessageDialog(null, "Partita terminata!\n Risultato: " + result, "Game Over", JOptionPane.INFORMATION_MESSAGE);

        sendCommand(new ClientCommand(ClientCommand.CommandType.DISCONNECT, 0));
        closeConnection();

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            HeadBallApp menu = new HeadBallApp();
            menu.setExistingFrame(frame);
            menu.showMenu();
        });

    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
