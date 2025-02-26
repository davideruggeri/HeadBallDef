package it.unibs.pajc.clinet;

import it.unibs.pajc.game.Background;
import it.unibs.pajc.game.CampoDiGioco;
import it.unibs.pajc.game.Giocatore;
import it.unibs.pajc.game.Oggetto;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    String host = "10.277.219.170";
    Socket socket;

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Background background;
    private Giocatore giocatoreLocale = new Giocatore(background.getCampo());
    private ExecutorService executor;
    private JFrame frame;
    private ArrayList<Oggetto> oggetti = new ArrayList<>();

    public Client(JFrame frame) {
        this.frame = frame;
    }

    public void inizializzaGioco() {
        int x = frame.getX(), y = frame.getY();
        frame.dispose();
        frame = new JFrame("Clinet");
        frame.setBounds(x, y, CampoDiGioco.CAMPO_WIDTH, CampoDiGioco.CAMPO_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);

        background = new Background();
        background.getCampo().setListaOggetti(oggetti);

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

    public boolean connectToServer(String host, int port) {
        boolean connesso = false;

        try {
            socket = new Socket(host, Server.PORT);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            connesso = true;
            inizializzaGioco();


        } catch (UnknownHostException e){
            JOptionPane.showMessageDialog(null, "Errore nella connessione al server");
        } catch (IOException e ){
            JOptionPane.showMessageDialog(null, "Errore nella connessione al server");
        }

        return connesso;
    }

    private void ascoltaServer() {
        try {
            while(socket.isConnected() == false) {

                ArrayList<Oggetto> tmp = (ArrayList<Oggetto>) in.readObject();
                oggetti.clear();
                oggetti.addAll(tmp);

                background.revalidate();
                background.repaint();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void inviaServer(ChangeEvent c) {
        try {
            out.writeUnshared(giocatoreLocale);
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
