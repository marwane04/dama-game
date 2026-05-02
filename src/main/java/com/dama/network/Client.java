package com.dama.network;

import java.io.*;
import java.net.Socket;

public class Client {
    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket socket;

    public Client() {
        try {
            this.socket = new Socket("localhost", 1234);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

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

    private MoveListener moveListener;
    private StartListener startListener;
    private WaitingListener waitingListener;

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public void setStartListener(StartListener startListener) {
        this.startListener = startListener;
    }

    public void setWaitingListener(WaitingListener waitingListener) {
        this.waitingListener = waitingListener;
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