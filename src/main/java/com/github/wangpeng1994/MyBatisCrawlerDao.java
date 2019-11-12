package com.github.wangpeng1994;

import java.sql.SQLException;

public class MyBatisCrawlerDao implements CrawlerDao {

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        return null;
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        return false;
    }

    @Override
    public void updateLinkIntoDatabase(String link, String sql) throws SQLException {

    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {

    }
}
