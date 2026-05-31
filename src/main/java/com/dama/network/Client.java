package com.dama.network;

import java.io.*;
import java.net.Socket;

public class Client {
    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket socket;
    private final String sessionCode;

    public Client() {
        this(null);
    }

    public Client(String sessionCode) {
        this.sessionCode = sessionCode;
        try {
            this.socket = new Socket("localhost", 1234);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            sendSessionRequest();
        } catch (IOException e) {
            closeConnection();
        }
    }

    public interface MoveListener {
        void onMoveReceived(Move move);
    }

    public interface StartListener {
        void onStartAssigned(boolean isLocalTurn);
    }

    public interface WaitingListener {
        void onWaitingForOpponent();
    }

    public interface SessionCodeListener {
        void onSessionCodeAssigned(String code);
    }

    public interface SessionErrorListener {
        void onSessionError(String message);
    }

    private MoveListener moveListener;
    private StartListener startListener;
    private WaitingListener waitingListener;
    private SessionCodeListener sessionCodeListener;
    private SessionErrorListener sessionErrorListener;

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public void setStartListener(StartListener startListener) {
        this.startListener = startListener;
    }

    public void setWaitingListener(WaitingListener waitingListener) {
        this.waitingListener = waitingListener;
    }

    public void setSessionCodeListener(SessionCodeListener sessionCodeListener) {
        this.sessionCodeListener = sessionCodeListener;
    }

    public void setSessionErrorListener(SessionErrorListener sessionErrorListener) {
        this.sessionErrorListener = sessionErrorListener;
    }

    public void sendMove(Move move) {
        if (writer == null || socket == null || socket.isClosed()) {
            closeConnection();
            return;
        }
        try {
            writer.write(move.toString());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeConnection();
        }
    }

    public void listenForMoves() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket != null && socket.isConnected() && !socket.isClosed()) {
                    try {
                        String receivedMove = reader.readLine();
                        if (receivedMove == null) {
                            closeConnection();
                            break;
                        }
                        if (receivedMove.startsWith("SESSION:CODE:")) {
                            handleSessionCode(receivedMove);
                            continue;
                        }
                        if (receivedMove.startsWith("SESSION:INVALID")) {
                            handleSessionError(receivedMove);
                            continue;
                        }
                        if (receivedMove.startsWith("START:")) {
                            handleStartMessage(receivedMove);
                            continue;
                        }
                        if (receivedMove.startsWith("WAITING")) {
                            handleWaitingMessage();
                            continue;
                        }
                        if (moveListener != null) {
                            moveListener.onMoveReceived(Move.stringToMove(receivedMove));
                        }
                    } catch (IOException e) {
                        closeConnection();
                        break;
                    } catch (IllegalArgumentException e) {
                        // Ignore malformed messages and continue listening.
                    }
                }
            }
        }).start();
    }

    private void handleStartMessage(String message) {
        if (startListener == null) {
            return;
        }
        String token = message.substring("START:".length()).trim();
        boolean isLocalTurn = "YOU".equalsIgnoreCase(token);
        startListener.onStartAssigned(isLocalTurn);
    }

    private void handleWaitingMessage() {
        if (waitingListener != null) {
            waitingListener.onWaitingForOpponent();
        }
    }

    private void handleSessionCode(String message) {
        if (sessionCodeListener == null) {
            return;
        }
        String code = message.substring("SESSION:CODE:".length()).trim();
        sessionCodeListener.onSessionCodeAssigned(code);
    }

    private void handleSessionError(String message) {
        if (sessionErrorListener == null) {
            return;
        }
        sessionErrorListener.onSessionError(message);
    }

    private void sendSessionRequest() {
        if (writer == null) {
            return;
        }
        try {
            if (sessionCode == null || sessionCode.isBlank()) {
                writer.write("SESSION:NEW");
            } else {
                writer.write("SESSION:JOIN:" + sessionCode.trim());
            }
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeConnection();
        }
    }

    public void closeConnection() {
        System.out.println("connection closed");
        try {
            if (writer != null) {
                writer.close();
            }

            if (reader != null) {
                reader.close();
            }

            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}