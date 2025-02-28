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
    private final List<ClientHandler> clients = new ArrayList<>();

    private Timer gameLoopTimer;

    public Server() {
        campoDiGioco = new CampoDiGioco(false);
    }

    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server avviato sulla porta " + PORT);
            running = true;

            campoDiGioco.setGroundY(158);
            startGameLoop();

            new Thread(this::acceptClients).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void acceptClients() {
        int playerId = 1;

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());

                if (playerId == 1) {
                    campoDiGioco.addPlayer(playerId);
                }else if (playerId == 2) {
                    campoDiGioco.addPlayer(playerId);
                }
                ClientHandler handler = new ClientHandler(clientSocket, playerId++);
                synchronized (this) {
                    clients.add(handler);
                }

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
        if (gameLoopTimer != null) {
            gameLoopTimer.cancel();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGameLoop() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                campoDiGioco.updatePhysics();
                broadcastGameState();
            }
        }, 0, 16); // circa 60 FPS
    }


    public synchronized void broadcastGameState() {
        GameState state = new GameState(campoDiGioco);
        for (ClientHandler handler : clients) {
            handler.sendGameState(state);
        }
    }

    public synchronized void processCommand(int playerId, ClientCommand command) {
        switch (command.getCommand()) {
            case MOVE_LEFT:
                campoDiGioco.movePlayer(playerId, -1);
                break;
            case MOVE_RIGHT:
                campoDiGioco.movePlayer(playerId, 1);
                break;
            case JUMP:
                campoDiGioco.jump(playerId);
                break;
            case SHOOT:
                campoDiGioco.kickBall(playerId);
                break;
            case REQUEST_INITIAL_STATE:
                sendStateToClient(playerId);
                return;
        }
        broadcastGameState();
    }

    private void sendStateToClient(int playerId) {
        GameState state = new GameState(campoDiGioco);
        for (ClientHandler handler : clients) {
            if (handler.getPlayerId() == playerId) {
                handler.sendGameState(state);
                break;
            }
        }
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

        public int getPlayerId() {
            return playerId;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Client connesso! PlayerID: " + playerId);

                synchronized (Server.this) {
                    sendGameState(new GameState(campoDiGioco));
                    broadcastGameState();
                }

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
                synchronized (Server.this) {
                    processCommand(playerId, command);
                }
            } else {
                System.err.println("Messaggio sconosciuto: " + message.getType());
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
                synchronized (Server.this) {
                    clients.remove(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
