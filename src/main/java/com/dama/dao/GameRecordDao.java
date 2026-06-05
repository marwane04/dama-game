package com.dama.dao;

import com.dama.controller.GameRecord;

import java.sql.SQLException;
import java.util.List;

public interface GameRecordDao {
    void createTable() throws SQLException;
    void insert(GameRecord record) throws SQLException;
    List<GameRecord> findAll() throws SQLException;
}
