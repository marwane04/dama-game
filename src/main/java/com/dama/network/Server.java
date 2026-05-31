package com.dama.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final ServerSocket socket;
    private final SessionManager sessionManager;

    public Server(ServerSocket socket, SessionManager sessionManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;
    }

    private void startServer() {
        try {
            while (!socket.isClosed()) {
                Socket clientSocket = socket.accept();
                System.out.println("client connected");

                Thread thread = new Thread(new ClientHandler(clientSocket, sessionManager));
                thread.start();
            }
        } catch (IOException e) {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket, new SessionManager());
        server.startServer();
    }

}
