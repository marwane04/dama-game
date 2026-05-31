package com.dama.network;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final int CODE_LENGTH = 6;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();
    private final Map<String, ClientHandler> waitingByCode = new ConcurrentHashMap<>();
    private final Map<String, Session> sessionsByCode = new ConcurrentHashMap<>();
    private final Map<ClientHandler, String> codeByClient = new ConcurrentHashMap<>();

    public String registerNew(ClientHandler client) {
        String code = generateUniqueCode();
        waitingByCode.put(code, client);
        codeByClient.put(client, code);
        client.sendSessionCode(code);
        client.sendWaitingMessage();
        return code;
    }

    public boolean registerJoin(ClientHandler client, String code) {
        if (code == null || code.isBlank()) {
            client.sendLine("SESSION:INVALID");
            client.closeConnection();
            return false;
        }
        ClientHandler waiting = waitingByCode.remove(code);
        if (waiting == null) {
            client.sendLine("SESSION:INVALID");
            client.closeConnection();
            return false;
        }
        Session session = new Session(code, waiting, client);
        sessionsByCode.put(code, session);
        codeByClient.put(waiting, code);
        codeByClient.put(client, code);
        session.start();
        return true;
    }

    public void forwardMove(ClientHandler from, Move move) {
        if (from == null || move == null) {
            return;
        }
        String code = codeByClient.get(from);
        if (code == null) {
            return;
        }
        Session session = sessionsByCode.get(code);
        if (session != null) {
            session.forwardMove(from, move);
        }
    }

    public void removeClient(ClientHandler client) {
        if (client == null) {
            return;
        }
        String code = codeByClient.remove(client);
        if (code == null) {
            return;
        }
        ClientHandler waiting = waitingByCode.get(code);
        if (waiting == client) {
            waitingByCode.remove(code);
            return;
        }
        Session session = sessionsByCode.remove(code);
        if (session == null) {
            return;
        }
        ClientHandler opponent = session.getOpponent(client);
        if (opponent != null) {
            waitingByCode.put(code, opponent);
            opponent.sendWaitingMessage();
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (waitingByCode.containsKey(code) || sessionsByCode.containsKey(code));
        return code;
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CODE_CHARS.length());
            builder.append(CODE_CHARS.charAt(index));
        }
        return builder.toString();
    }
}
