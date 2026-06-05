package com.dama.dao;

import com.dama.controller.GameRecord;
import com.dama.controller.GameType;
import com.dama.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteGameRecordDao implements GameRecordDao {

    @Override
    public void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS game_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    start_time TEXT NOT NULL,
                    result TEXT NOT NULL,
                    game_mode TEXT NOT NULL
                )
                """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        }
    }

    @Override
    public void insert(GameRecord record) throws SQLException {
        String sql = """
                INSERT INTO game_records (start_time, result, game_mode)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, record.getStartTime().toString());
            statement.setString(2, record.getResult());
            statement.setString(3, record.getGameMode().name());
            statement.executeUpdate();
        }
    }

    @Override
    public List<GameRecord> findAll() throws SQLException {
        String sql = """
                SELECT start_time, result, game_mode
                FROM game_records
                ORDER BY start_time DESC, id DESC
                """;

        List<GameRecord> records = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                records.add(new GameRecord(
                        LocalDateTime.parse(resultSet.getString("start_time")),
                        resultSet.getString("result"),
                        GameType.valueOf(resultSet.getString("game_mode"))
                ));
            }
        }

        return records;
    }

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }
}
