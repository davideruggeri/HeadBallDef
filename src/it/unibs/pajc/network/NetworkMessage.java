package it.unibs.pajc.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {

    public enum MessageType {
        GAME_STATE,
        PLAYER_COMMAND,
        ERROR,
        GAME_START
    }

    private final MessageType type;
    private final Object payload;

    public NetworkMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
