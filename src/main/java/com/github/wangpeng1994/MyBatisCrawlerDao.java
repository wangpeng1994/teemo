package com.github.wangpeng1994;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            String link = session.selectOne("com.github.wangpeng1994.MyMapper.selectNextAvailableLink");
            if (link != null) {
                deleteProcessedLink(link);
            }
            return link;
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.wangpeng1994.MyMapper.countProcessedLink", link);
            return count != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) throws SQLException {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "links_already_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.wangpeng1994.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String link) throws SQLException {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "links_to_be_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.wangpeng1994.MyMapper.insertLink", param);
        }
    }

    @Override
    public void deleteProcessedLink(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.wangpeng1994.MyMapper.deleteLink", link);
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.wangpeng1994.MyMapper.insertNews", new News(title, content, link));
        }
    }
}
