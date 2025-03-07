package it.unibs.pajc.server;

import it.unibs.pajc.client.ClientCommand;
import it.unibs.pajc.game.CampoDiGioco;
import it.unibs.pajc.game.GameState;
import it.unibs.pajc.network.NetworkMessage;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

public class Server {

    public static final int PORT = 1234;
    private ServerSocket serverSocket;
    private boolean running = false;

    private final CampoDiGioco campoDiGioco;
    private final List<ClientHandler> clients = new ArrayList<>();

    private Timer gameLoopTimer;
    private int seconds = 90;
    private Timer gameTimer;
    private Timer loopTimer;
    private int readyPlayers = 0;


    public Server() {
        campoDiGioco = new CampoDiGioco(false);
    }

    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);

            System.out.println("Server avviato sulla porta " + PORT);
            running = true;

            campoDiGioco.setGroundY(80);

            new Thread(this::acceptClients).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void acceptClients() {
        int playerId = 1;

        while (running && clients.size() < 2) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());

                campoDiGioco.addPlayer(playerId);

                synchronized (this) {
                    ClientHandler handler = new ClientHandler(clientSocket, playerId);
                    clients.add(handler);
                    new Thread(handler).start();
                    playerId++;

                    if (clients.size() == 2) {
                        System.out.println("Entrambi i giocatori connessi!");
                    } else {
                        System.out.println("In attesa del secondo giocatore...");
                    }
                }

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
        loopTimer = new Timer();
        loopTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                campoDiGioco.updatePhysics();
                broadcastGameState();
            }
        }, 0, 33); // circa 30 FPS
    }

    private synchronized void checkAndStartGame() {
            System.out.println("Entrambi i giocatori sono connessi, il gioco inizierà tra 5 secondi...");

            Timer countdownTimer = new Timer();
            int[] secondsLeft = {3};

            countdownTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    broadcastCountdownUpdate(secondsLeft[0]);  // manda il countdown ai client

                    if (secondsLeft[0] == 0) {
                        countdownTimer.cancel();

                        broadcastGameState();

                        for (ClientHandler client : clients) {
                            client.sendGameStart();
                        }
                        startGameLoop();
                        startGameTimer();
                    } else {
                        secondsLeft[0]--;
                    }
                }
            }, 1000, 1000); // ogni secondo

    }

    private void broadcastCountdownUpdate(int secondsLeft) {
        NetworkMessage countdownMessage = new NetworkMessage(NetworkMessage.MessageType.COUNTDOWN_UPDATE, secondsLeft);
        for (ClientHandler client : clients) {
            client.sendMessage(countdownMessage);
        }
    }

    public synchronized void broadcastGameState() {
        campoDiGioco.goal();

        GameState state = new GameState(campoDiGioco);
        state.setTimeRemaining(seconds);

        state.setPlayer1Score(campoDiGioco.getPlayer1Score());
        state.setPlayer2Score(campoDiGioco.getPlayer2Score());

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
                break;
            case PLAYER_READY:
                readyPlayers++;
                if (readyPlayers == 2) {
                    checkAndStartGame();
                }
                break;
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

    private void startGameTimer() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (seconds > 0) {
                    seconds--;
                    broadcastGameState();
                } else {
                    gameTimer.cancel();
                    loopTimer.cancel();
                    System.out.println("Tempo scaduto!");
                    endGame();
                }
            }
        }, 0, 1000);
    }

    public void endGame() {
        String message = campoDiGioco.getPlayer1Score() + " - " + campoDiGioco.getPlayer2Score();
        NetworkMessage gameOverMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_OVER, message);

        for (ClientHandler handler : new ArrayList<>(clients)) {
            handler.sendMessage(gameOverMessage);

        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (ClientHandler handler : new ArrayList<>(clients)) {
                    handler.disconnect();
                }
                clients.clear();
                readyPlayers = 0;
                stopServer();
            }
        }, 5000);
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

                out.writeObject(new NetworkMessage(NetworkMessage.MessageType.PLAYER_ID_ASSIGNED, playerId));
                out.reset();
                System.out.println("Client connesso! PlayerID: " + playerId);

                while (true) {
                    NetworkMessage message = (NetworkMessage) in.readObject();
                    if (message.getType() == NetworkMessage.MessageType.PLAYER_COMMAND) {
                        ClientCommand command = (ClientCommand) message.getPayload();
                        if (command.getCommand() == ClientCommand.CommandType.REQUEST_INITIAL_STATE) {
                            sendGameState(new GameState(campoDiGioco));
                            continue;
                        }
                    }

                    handleMessage(message);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnesso. PlayerID: " + playerId);
            } finally {
                disconnect();
            }
        }

        public void sendMessage(NetworkMessage message) {
            try {
                out.writeObject(message);
                out.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendGameStart() {
            try {
                out.writeObject(new NetworkMessage(NetworkMessage.MessageType.GAME_START, null));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleMessage(NetworkMessage message) {
            if (message.getType() == NetworkMessage.MessageType.PLAYER_COMMAND) {
                ClientCommand command = (ClientCommand) message.getPayload();
                if (command.getCommand() == ClientCommand.CommandType.DISCONNECT) {
                    System.out.println("Il player " + playerId + " si è disconnesso!");
                    disconnect();
                } else {
                    synchronized (Server.this) {
                        processCommand(playerId, command);
                    }
                }
            } else {
                System.err.println("Messaggio sconosciuto: " + message.getType());
            }
        }

        public void sendGameState(GameState state) {
            try {
                out.writeObject(new NetworkMessage(NetworkMessage.MessageType.GAME_STATE, state));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnect() {
            try {
                if (!clientSocket.isClosed()) {
                    sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, null));
                    clientSocket.close();
                }
                synchronized (Server.this) {
                    clients.remove(this);
                    if (clients.size() < 2) {
                        System.out.println("Un giocatore si è disconnesso. Fine partita!");
                        endGame();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}