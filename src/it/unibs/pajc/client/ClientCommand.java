package it.unibs.pajc.client;

import java.io.Serializable;

public class ClientCommand implements Serializable {
    public enum CommandType { MOVE_LEFT, MOVE_RIGHT, JUMP, SHOOT, REQUEST_INITIAL_STATE, JOIN_GAME, PLAYER_READY, DISCONNECT};

    private CommandType command;
    private int playerId;

    public ClientCommand(CommandType command, int playerId) {
        this.command = command;
        this.playerId = playerId;
    }

    public CommandType getCommand() { return command; }
    public int getPlayerId() { return playerId; }
}
