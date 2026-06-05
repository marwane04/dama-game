package com.dama.dao;

import com.dama.controller.GameRecord;

import java.sql.SQLException;

public interface GameRecordDao {
    void createTable() throws SQLException;
    void insert(GameRecord record) throws SQLException;
    void findAll(GameRecord record) throws SQLException;
}
