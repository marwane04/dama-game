package com.dama.network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final SessionManager sessionManager;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientHandler(Socket socket, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("client added to the list");
        } catch (Exception e) {
            closeConnection();
        }
    }

    public void closeConnection() {
        sessionManager.removeClient(this);
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

    @Override
    public void run() {
        if (!registerSession()) {
            return;
        }
        while (socket != null && socket.isConnected()) {
            try {
                String receivedString = reader.readLine();
                if (receivedString == null) {
                    closeConnection();
                    break;
                }
                if (receivedString.startsWith("SESSION:")) {
                    continue;
                }
                Move receivedMove = Move.stringToMove(receivedString);
                sessionManager.forwardMove(this, receivedMove);
            } catch (IOException e) {
                closeConnection();
                break;
            } catch (IllegalArgumentException e) {
                // Ignore malformed messages and continue listening.
            }
        }
    }

    private boolean registerSession() {
        try {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                closeConnection();
                return false;
            }
            if (firstLine.startsWith("SESSION:JOIN:")) {
                String code = firstLine.substring("SESSION:JOIN:".length()).trim();
                return sessionManager.registerJoin(this, code);
            }
            if (firstLine.startsWith("SESSION:NEW")) {
                sessionManager.registerNew(this);
                return true;
            }
            sessionManager.registerNew(this);
            Move initialMove = Move.stringToMove(firstLine);
            sessionManager.forwardMove(this, initialMove);
            return true;
        } catch (IOException e) {
            closeConnection();
            return false;
        } catch (IllegalArgumentException e) {
            sessionManager.registerNew(this);
            return true;
        }
    }

    public void sendWaitingMessage() {
        sendLine("WAITING");
    }

    public void sendStartMessage(boolean isLocalTurn) {
        sendLine("START:" + (isLocalTurn ? "YOU" : "OPPONENT"));
    }

    public void sendSessionCode(String code) {
        sendLine("SESSION:CODE:" + code);
    }

    public void sendLine(String line) {
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            closeConnection();
        }
    }
}
