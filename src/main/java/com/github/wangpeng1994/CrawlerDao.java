package com.github.wangpeng1994;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void updateLinkIntoDatabase(String link, String sql) throws SQLException;

    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;
}
