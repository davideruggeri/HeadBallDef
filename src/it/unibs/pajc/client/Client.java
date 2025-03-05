package it.unibs.pajc.client;

import it.unibs.pajc.game.Background;
import it.unibs.pajc.game.GameState;
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
            in = new ObjectInputStream(socket.getInputStream());

            SwingUtilities.invokeLater(this::showWaitingPopUp);

            requestInitialState();

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

                if (message.getType() == NetworkMessage.MessageType.GAME_STATE) {
                    GameState state = (GameState) message.getPayload();
                    SwingUtilities.invokeLater(() -> {
                        if (background != null) {
                            background.updateGameState(state);
                        }
                    });
                } else if (message.getType() == NetworkMessage.MessageType.GAME_START) {
                    SwingUtilities.invokeLater(this::closeWaitingPopup);
                    break;
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

    private void showWaitingPopUp() {
        waitingDialog = new JDialog(frame, "Attesa Giocatore", true);
        waitingDialog.setSize(300, 150);
        waitingDialog.setLocationRelativeTo(frame);
        waitingDialog.add(new JLabel("In attesa di un altro giocatore...", SwingConstants.CENTER));
        waitingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        waitingDialog.setVisible(true);
    }

    public void closeWaitingPopup() {
        if (waitingDialog != null) {
            waitingDialog.dispose();
        }
    }
}
