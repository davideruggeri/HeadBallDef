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

    public Client(JFrame frame) {
        this.frame = frame;
    }

    public boolean connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Richiede subito lo stato iniziale (può essere anche chiamato dall'esterno se serve)
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

    public ObjectInputStream getInputStream() {
        return in;
    }

    public ObjectOutputStream getOutputStream() {
        return out;
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


    public Socket getSocket() {
        return socket;
    }
}
