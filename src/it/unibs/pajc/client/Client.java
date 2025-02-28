package it.unibs.pajc.client;

import it.unibs.pajc.game.Background;
import it.unibs.pajc.game.GameState;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final JFrame frame;
    private Background background; // il pannello di gioco, serve per applicare lo stato ricevuto dal server

    public Client(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Connessione al server.
     *
     * @param serverAddress IP o hostname
     * @param port porta del server
     * @return true se la connessione ha successo
     */
    public boolean connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Manda un comando iniziale per registrarsi
            out.writeObject(new ClientCommand(ClientCommand.CommandType.JOIN_GAME, 1));
            out.flush();

            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Errore nella connessione al server:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Richiede lo stato iniziale al server.
     */
    public void requestInitialState() {
        try {
            sendCommand(new ClientCommand(ClientCommand.CommandType.REQUEST_INITIAL_STATE, 1));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Errore nella richiesta dello stato iniziale:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Invia un comando al server.
     *
     * @param command il comando da inviare
     */
    public void sendCommand(ClientCommand command) {
        try {
            out.writeObject(command);
            out.reset(); // pulisce la cache interna dello stream
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Errore nell'invio del comando:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Avvia un thread che riceve lo stato dal server e lo applica al Background.
     */
    public void startStateReceiver() {
        new Thread(() -> {
            try {
                while (true) {
                    Object received = in.readObject();
                    if (received instanceof GameState) {
                        GameState gameState = (GameState) received;
                        if (background != null) {
                            gameState.applyToCampo(background.getCampo());
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(frame, "Connessione persa:\n" + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                disconnect();
            }
        }).start();
    }

    /**
     * Collega il pannello di gioco al client, in modo da poter aggiornare lo stato.
     */
    public void setBackground(Background background) {
        this.background = background;
    }

    /**
     * Fornisce lo stream di input al gioco (se necessario).
     */
    public ObjectInputStream getInputStream() {
        return in;
    }

    public ObjectOutputStream getOutputStream() {
        return out;
    }

    /**
     * Disconnessione e chiusura socket.
     */
    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}












/*package it.unibs.pajc.client;

import it.unibs.pajc.game.*;
import it.unibs.pajc.server.Server;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Background background;
    private Giocatore giocatoreLocale;
    private ExecutorService executor;
    private JFrame frame;
    private CampoDiGioco campo;

    public Client(JFrame frame) {
        this.frame = frame;
        this.background = new Background(campo, out, in);
        this.giocatoreLocale = new Giocatore(background.getCampo());
    }

    public void connettiAlServer() {
        try {
            socket = new Socket("localhost", 1234);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Ricevi subito lo stato iniziale
            Object firstData = in.readObject();
            if (firstData instanceof GameState) {
                GameState initialState = (GameState) firstData;
                campo = new CampoDiGioco();
                campo.updateFromGameState(initialState);
            } else {
                throw new IOException("Errore: il server non ha inviato lo stato iniziale");
            }

            // Avvia la finestra grafica con il campo ricevuto
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Gioco Multiplayer");
                Background background = new Background(campo, out, in);
                frame.setContentPane(background);
                frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            });

            // Avvia thread che ascolta continuamente dal server
            new Thread(this::ascoltaServer).start();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public boolean connectToServer(String host, int port) {
        boolean connesso = false;
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            connesso = true;
            inizializzaGioco();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Errore nella connessione al server.");
        }
        return connesso;
    }

    private void inizializzaGioco() {
        this.campo = background.getCampo();
        frame.getContentPane().removeAll();
        frame.setTitle("Client");
        frame.setSize(CampoDiGioco.CAMPO_WIDTH, CampoDiGioco.CAMPO_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);

        frame.getContentPane().add(background, BorderLayout.CENTER);
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                background.requestFocusInWindow();
            }
        });

        executor = Executors.newFixedThreadPool(2);
        executor.execute(this::ascoltaServer);

        ClientCommandKeyBoard keyBoard = new ClientCommandKeyBoard(giocatoreLocale, this);
        keyBoard.addChangeListener(this::inviaServer);
        background.addKeyListener(keyBoard);
    }

    private void ascoltaServer() {
        try {
            while (!socket.isClosed()) {
                Object received = in.readObject(); // Legge l'oggetto dal server

                if (received instanceof GameState) {
                    GameState gameState = (GameState) received;
                    receiveGameState(gameState); // Metodo che aggiorna il campo di gioco
                } else {
                    System.err.println("Ricevuto oggetto inaspettato: " + received.getClass());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella ricezione dei dati dal server: " + e.getMessage());
        }
    }


    private void inviaServer(ChangeEvent e) {
        if (e.getSource() instanceof ClientCommand) {
            try {
                out.writeObject(e.getSource()); // Invia il comando al server
                out.flush();
            } catch (IOException ex) {
                System.err.println("Errore nell'invio del comando al server.");
            }
        }
    }

    public void receiveGameState(GameState gameState) {
        campo.updateFromGameState(gameState);
    }
    public void sendCommand(ClientCommand command) throws IOException {
        out.writeObject(command);
        out.flush();
    }


}*/
