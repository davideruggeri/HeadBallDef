package it.unibs.pajc.clinet;
import it.unibs.pajc.game.Giocatore;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class ClientCommandKeyBoard implements KeyListener {
    private Giocatore localPlayer;
    private ArrayList<Integer> currentActiveKeys = new ArrayList<>();


    public ClientCommandKeyBoard(Giocatore p) {
        localPlayer = p;
    }

    public void applyControls() {
        if (localPlayer == null) return;


    }


    @Override
    public void keyTyped(KeyEvent e){}

    @Override
    public void keyPressed(KeyEvent e) {
        if(!currentActiveKeys.contains(e.getKeyCode())) {
            currentActiveKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentActiveKeys.remove(e.getKeyCode());
    }
}
