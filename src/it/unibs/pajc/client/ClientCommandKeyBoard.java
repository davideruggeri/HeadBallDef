package it.unibs.pajc.client;
import it.unibs.pajc.game.BaseModel;
import it.unibs.pajc.game.Giocatore;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class ClientCommandKeyBoard extends BaseModel implements KeyListener {
    private Giocatore localPlayer;
    private ArrayList<Integer> currentActiveKeys = new ArrayList<>();


    public ClientCommandKeyBoard(Giocatore p) {
        localPlayer = p;
        applyControls();
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
