/*package it.unibs.pajc.client;

import it.unibs.pajc.game.BaseModel;
import it.unibs.pajc.game.Giocatore;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;

public class ClientCommandKeyBoard extends BaseModel implements KeyListener {
    private Giocatore localPlayer;
    private ArrayList<Integer> currentActiveKeys = new ArrayList<>();
    private ClientCommand command;
    private Client client;


    public ClientCommandKeyBoard(Giocatore p, Client client) {
        this.localPlayer = p;
        this.client = client;
    }

    public void applyControls() {
        localPlayer.setVelocita(0, localPlayer.getVelocitaY());

        for (Integer keyCode : currentActiveKeys) {
            switch (keyCode) {
                case KeyEvent.VK_RIGHT:
                    localPlayer.setVelocita(2, localPlayer.getVelocitaY());
                    break;
                case KeyEvent.VK_LEFT:
                    localPlayer.setVelocita(-2, localPlayer.getVelocitaY());
                    break;
                case KeyEvent.VK_SPACE:
                    localPlayer.jump();
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();

        if (!currentActiveKeys.contains(keyCode)) {
            currentActiveKeys.add(keyCode);
        }

        System.out.println("Tasto premuto: " + keyChar + " (Codice: " + keyCode + ")");

        String azione;
        switch (keyCode) {
            case KeyEvent.VK_RIGHT:
                command = new ClientCommand(ClientCommand.CommandType.MOVE_RIGHT, 1);
                azione = "MUOVI A DESTRA";
                break;
            case KeyEvent.VK_LEFT:
                command = new ClientCommand(ClientCommand.CommandType.MOVE_LEFT, 1);
                azione = "MUOVI A SINISTRA";
                break;
            case KeyEvent.VK_SPACE:
                command = new ClientCommand(ClientCommand.CommandType.JUMP, 1);
                azione = "SALTO";
                break;
            case KeyEvent.VK_Z:
                command = new ClientCommand(ClientCommand.CommandType.SHOOT, 1);
                azione = "TIRO";
                break;
            default:
                azione = "NESSUNA AZIONE ASSEGNATA";
        }

        System.out.println("→ Azione corrispondente: " + azione);

        if (command != null) {
            client.sendCommand(command);
        }
        applyControls();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentActiveKeys.remove(Integer.valueOf(e.getKeyCode()));
    }
}*/
