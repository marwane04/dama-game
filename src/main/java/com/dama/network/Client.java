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
                        System.out.println(receivedMove);
                    } catch (IOException e) {
                        closeConnection();
                        break;
                    }
                }
            }
        }).start();
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
