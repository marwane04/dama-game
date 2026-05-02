package com.dama.network;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>(2);

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientHandlers.add(this);
            System.out.println("client added to the list");
        } catch (Exception e) {
            closeConnection();
        }
    }

    public void closeConnection() {
        removeClient();
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
        while (socket != null && socket.isConnected()) {
            try {
                String receivedString = reader.readLine();
                if (receivedString == null) {
                    closeConnection();
                    break;
                }
                Move receivedMove = Move.stringToMove(receivedString);
                sendMoveToOpponent(receivedMove);
            } catch (IOException e) {
                closeConnection();
                break;
            }
        }
    }

    private void sendMoveToOpponent(Move move) {
        if (move == null) {
            return;
        }

        ClientHandler otherClient = null;
        if (clientHandlers.size() > 1) {
            otherClient = clientHandlers.get(0) != this ? clientHandlers.get(0) : clientHandlers.get(1);
        }
        try {
            if (otherClient != null) {
                otherClient.writer.write(move.toString());
                otherClient.writer.newLine();
                otherClient.writer.flush();
            }
        } catch (IOException e) {
            closeConnection();
        }

    }

    private void removeClient() {
        clientHandlers.remove(this);
    }
}
