package com.dama.network;

public class Session {
    private final String code;
    private final ClientHandler playerOne;
    private final ClientHandler playerTwo;

    public Session(String code, ClientHandler playerOne, ClientHandler playerTwo) {
        this.code = code;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
    }

    public String getCode() {
        return code;
    }

    public void start() {
        playerOne.sendStartMessage(true);
        playerTwo.sendStartMessage(false);
    }

    public ClientHandler getOpponent(ClientHandler client) {
        if (client == playerOne) {
            return playerTwo;
        }
        if (client == playerTwo) {
            return playerOne;
        }
        return null;
    }

    public void forwardMove(ClientHandler from, Move move) {
        ClientHandler opponent = getOpponent(from);
        if (opponent != null) {
            opponent.sendLine(move.toString());
        }
    }
}
