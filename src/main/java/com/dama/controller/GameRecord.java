package com.dama.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameRecord {
    private LocalDateTime startTime;
    private String result;
    private GameType gameMode;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GameRecord(LocalDateTime startTime, String result, GameType gameMode) {
        this.startTime = startTime;
        this.result = result;
        this.gameMode = gameMode;
    }

    public GameRecord(String result, GameType gameMode) {
        this(LocalDateTime.now(), result, gameMode);
    }



    public LocalDateTime getStartTime() { return startTime; }
    public String getResult() { return result; }
    public GameType getGameMode() { return gameMode; }

    // Formatted string for the TableView
    public String getFormattedStartTime() {
        return startTime.format(FORMATTER);
    }

    public String getGameModeString() {
        return gameMode.name();
    }
}