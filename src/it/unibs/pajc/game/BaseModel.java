package it.unibs.pajc.game;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.EventListener;

public class BaseModel {

    protected EventListenerList listenerList = new EventListenerList();

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
}
