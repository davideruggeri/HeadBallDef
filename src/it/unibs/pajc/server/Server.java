package it.unibs.pajc.server;

import it.unibs.pajc.client.ClientCommand;
import it.unibs.pajc.game.CampoDiGioco;
import it.unibs.pajc.game.GameState;
import it.unibs.pajc.network.NetworkMessage;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    public static final int PORT = 1234;
    private ServerSocket serverSocket;
    private boolean running = false;

    private final CampoDiGioco campoDiGioco;
    private final List<ClientHandler> clients = new ArrayList<>(); // Lista dei client connessi

    public Server() {
        campoDiGioco = new CampoDiGioco();
    }

    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server avviato sulla porta " + PORT);
            running = true;

            new Thread(this::acceptClients).start();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void acceptClients() {
        int playerId = 1;  // Assegnamo un ID univoco a ogni client che si connette

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, playerId++);
                clients.add(handler);

                new Thread(handler).start();
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcastGameState() {
        GameState state = new GameState(campoDiGioco);

        for (ClientHandler handler : clients) {
            handler.sendGameState(state);
        }
    }

    public synchronized void movePlayer(int playerId, int direction) {
        campoDiGioco.movePlayer(playerId, direction);
    }

    public synchronized void jumpPlayer(int playerId) {
        campoDiGioco.jump(playerId);
    }

    public synchronized void kickBall(int playerId) {
        campoDiGioco.kickBall(playerId);
    }

    public CampoDiGioco getCampo() {
        return campoDiGioco;
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private final int playerId;

        public ClientHandler(Socket socket, int playerId) {
            this.clientSocket = socket;
            this.playerId = playerId;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Client connesso! PlayerID: " + playerId);

                // Invia subito lo stato iniziale
                sendGameState(new GameState(campoDiGioco));

                while (true) {
                    NetworkMessage message = (NetworkMessage) in.readObject();
                    handleMessage(message);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnesso. PlayerID: " + playerId);
            } finally {
                disconnect();
            }
        }

        private void handleMessage(NetworkMessage message) {
            if (message.getType() == NetworkMessage.MessageType.PLAYER_COMMAND) {
                ClientCommand command = (ClientCommand) message.getPayload();
                processCommand(command);
            } else {
                System.err.println("Messaggio sconosciuto: " + message.getType());
            }
        }

        private void processCommand(ClientCommand command) {
            switch (command.getCommand()) {
                case REQUEST_INITIAL_STATE:
                    sendGameState(new GameState(campoDiGioco));
                    break;
                case MOVE_LEFT:
                    movePlayer(playerId, -1);
                    broadcastGameState();
                    break;
                case MOVE_RIGHT:
                    movePlayer(playerId, 1);
                    broadcastGameState();
                    break;
                case JUMP:
                    jumpPlayer(playerId);
                    broadcastGameState();
                    break;
                case SHOOT:
                    kickBall(playerId);
                    broadcastGameState();
                    break;
                default:
                    System.err.println("Comando sconosciuto: " + command.getCommand());
            }
        }

        public void sendGameState(GameState state) {
            try {
                out.writeObject(new NetworkMessage(NetworkMessage.MessageType.GAME_STATE, state));
                out.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnect() {
            try {
                clientSocket.close();
                clients.remove(this); // Rimuoviamo il client dalla lista
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
