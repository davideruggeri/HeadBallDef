package it.unibs.pajc.client;

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
        this.background = new Background();
        this.giocatoreLocale = new Giocatore(background.getCampo());
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

        ClientCommandKeyBoard keyBoard = new ClientCommandKeyBoard(giocatoreLocale);
        keyBoard.addChangeListener(this::inviaServer);
        background.addKeyListener(keyBoard);
    }

    private void ascoltaServer() {
        try {
            while (!socket.isClosed()) {
                Giocatore remoteData = (Giocatore) in.readObject();
                background.getCampo().getRemotePlayer().setVelocita(remoteData.getVelocitaX(), remoteData.getVelocitaY());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella ricezione dei dati dal server.");
        }
    }

    private void inviaServer(ChangeEvent e) {
        try {
            out.writeUnshared(giocatoreLocale);
            out.flush();
        } catch (IOException ex) {
            System.err.println("Errore nell'invio dei dati al server.");
        }
    }
    public void receiveGameState(GameState gameState) {
        campo.updateFromGameState(gameState);
    }
    public void sendCommand(ClientCommand command) throws IOException {
        out.writeObject(command);
        out.flush();
    }


}
