package it.unibs.pajc.server;

import it.unibs.pajc.game.Background;
import it.unibs.pajc.game.CampoDiGioco;
import it.unibs.pajc.game.Giocatore;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public final static int PORT = 1234;

    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private CampoDiGioco campoDiGioco;
    private Background background;
    private JFrame frame;
    private ExecutorService executor;

    public Server(JFrame frame) {
        this.frame = frame;
    }

    public void start() {

        frame.getContentPane().removeAll();
        background = new Background();
        campoDiGioco = background.getCampo();
        campoDiGioco.setMultiPlayer();
        campoDiGioco.addChangeListener(this::modelUpdate);

        int x = background.getX(), y = background.getY();
        frame.dispose();
        frame = new JFrame("Server");
        frame.setBounds(x, y, CampoDiGioco.CAMPO_WIDTH, CampoDiGioco.CAMPO_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);

        frame.getContentPane().add(background, BorderLayout.CENTER);

        executor = Executors.newFixedThreadPool(2);
        executor.execute(this::ascoltaClient);
    }

    public boolean startServer() {
        boolean connesso = false;

        try (ServerSocket server = new ServerSocket(PORT)) {

            client = server.accept();
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());

            connesso = true;
            start();

        } catch (IOException e) {
            System.err.println("Errore di comunicazione" + e);
            throw new RuntimeException(e);
        }
        return connesso;
    }

    private void ascoltaClient(){
        try {
            while(client.isClosed() == false) {

                Giocatore tmpGiocatore = (Giocatore) in.readObject();
                Giocatore remoteGiocatore = campoDiGioco.getRemotePlayer();

                remoteGiocatore.setVelocita(tmpGiocatore.getVelocitaX(), tmpGiocatore.getVelocitaY());

                if(tmpGiocatore.isShooting()) {
                    remoteGiocatore.shoot();
                }

            }
        } catch (IOException e) {
            System.err.println("Errore di comunicazione" + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void inviaAlClient(Serializable obj) {
        try {
            out.writeObject(obj);
            out.reset();
        } catch (IOException e) {
            System.err.println("Errore di comunicazione" + e);
        }
    }

    private void modelUpdate(ChangeEvent e) {
        inviaAlClient(campoDiGioco.getListaOggetti());
        background.repaint();
    }
}
