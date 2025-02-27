package it.unibs.pajc.server;

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

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (!socket.isClosed()) {
                    Giocatore playerData = (Giocatore) in.readObject();
                    broadcast(playerData);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore di comunicazione con il client: " + e);
            }
        }

        private void broadcast(Giocatore playerData) {
            for (ClientHandler client : clients) {
                try {
                    client.out.writeObject(playerData);
                    client.out.reset();
                } catch (IOException e) {
                    System.err.println("Errore nell'invio dei dati: " + e);
                }
            }
        }
    }
}
