package it.unibs.pajc.server;

import it.unibs.pajc.client.ClientCommand;
import it.unibs.pajc.game.CampoDiGioco;
import it.unibs.pajc.game.GameState;
import it.unibs.pajc.game.Giocatore;
import it.unibs.pajc.game.Oggetto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public final static int PORT = 1234;
    private ServerSocket serverSocket;
    private boolean running = false;
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private CampoDiGioco campo = new CampoDiGioco();

    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server avviato sulla porta " + PORT);

            executor.execute(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());
                        ClientHandler handler = new ClientHandler(clientSocket);
                        clients.add(handler);
                        executor.execute(handler);
                    } catch (IOException e) {
                        if (running) e.printStackTrace();
                    }
                }
            });

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void broadcastGameState() {
        GameState state = new GameState(campo);
        for (ClientHandler client : clients) {
            client.sendData(state);
        }
    }


    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.out.println("Server chiuso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        public void sendData(Object data) {
            try {
                out.writeObject(data);
                out.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Appena il client è collegato, gli invio lo stato iniziale
                sendData(new GameState(campo));

                while (!socket.isClosed()) {
                    Object received = in.readObject();

                    if (received instanceof Giocatore) {
                        Giocatore playerData = (Giocatore) received;
                        broadcast(playerData);
                    }
                    else if (received instanceof ClientCommand) {
                        ClientCommand command = (ClientCommand) received;
                        handleCommand(command);
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore di comunicazione con il client: " + e);
            }
        }
        private void broadcast(Object data) {
            for (ClientHandler client : clients) {
                if (!client.socket.isClosed()) { // Verifica che il client sia ancora connesso
                    try {
                        client.out.writeObject(data);
                        client.out.reset(); // Resetta lo stream per evitare problemi di caching
                    } catch (IOException e) {
                        System.err.println("Errore nell'invio dei dati al client: " + e);
                    }
                }
            }
        }


        public void gameLoop() {
            while (true) {
                campo.stepNext(); // Aggiorna la fisica
                GameState state = new GameState(campo);
                broadcast(state); // Invia lo stato ai client
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void handleCommand(ClientCommand command) {
            Giocatore player = (command.getPlayerId() == 1) ? campo.getLocalPlayer() : campo.getRemotePlayer();
            switch (command.getCommand()) {
                case MOVE_LEFT:
                    player.setVelocita(-5, player.getVelocitaY());
                    break;
                case MOVE_RIGHT:
                    player.setVelocita(5, player.getVelocitaY());
                    break;
                case JUMP:
                    player.jump();
                    break;
                case SHOOT:
                    player.shoot();
                    break;
                default:
                    System.out.println("Comando sconosciuto: " + command.getCommand());
                    break;
            }
            broadcastGameState();
        }
    }
}
