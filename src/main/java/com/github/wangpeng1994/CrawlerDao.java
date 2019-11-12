package com.github.wangpeng1994;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertLinkToBeProcessed(String link) throws SQLException;

    void deleteProcessedLink(String link) throws SQLException;

    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;
}
