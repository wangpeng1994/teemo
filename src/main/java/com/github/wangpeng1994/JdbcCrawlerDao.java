package com.github.wangpeng1994;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:" + new File(projectDir, "news"), USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_TO_BE_PROCESSED limit 1");
             ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                String link = resultSet.getString(1);
                updateLinkIntoDatabase(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
                return link;
            }
        }
        return null;
    }

    @Override
    public void updateLinkIntoDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (title, content, url, created_at, updated_at) values (?, ?, ?, now(), now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }
}
