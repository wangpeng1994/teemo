package com.github.wangpeng1994;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class Main {

    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws SQLException {
        File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        Connection connection = DriverManager.getConnection("jdbc:h2:file:" + new File(projectDir, "news"), USERNAME, PASSWORD);
        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            // 确保当前链接没有被处理
            if (!isLinkProcessed(connection, link)) {
                System.out.println(link);
                Document doc = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                storeIntoDatabaseIfItIsNewsPage(connection, doc, link);
                updateLinkIntoDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
            }
        }
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_TO_BE_PROCESSED limit 1");
             ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                String link = resultSet.getString(1);
                updateLinkIntoDatabase(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
                return link;
            }
        }
        return null;
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String link = aTag.attr("href");
            // 只处理感兴趣的链接
            if (isInterestingLink(link)) {
                if (link.startsWith("//")) {
                    link = "https:" + link;
                }
                updateLinkIntoDatabase(connection, link, "insert into LINKS_TO_BE_PROCESSED (link) values (?)");
            }
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static void updateLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Connection connection, Document doc, String link) throws SQLException {
        Elements articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));

                try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (title, content, url, created_at, updated_at) values (?, ?, ?, now(), now())")) {
                    statement.setString(1, title);
                    statement.setString(2, content);
                    statement.setString(3, link);
                    statement.executeUpdate();
                }
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
        String html = "";
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1;
            entity1 = response1.getEntity();
            html = EntityUtils.toString(entity1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Jsoup.parse(html);
    }

    private static boolean isInterestingLink(String link) {
        return (isNewsPage(link) || isIndexPage(link)) && isNotLoginPage(link) && isLegalUrl(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isLegalUrl(String link) {
        return link.startsWith("https://") || link.startsWith("http://") || link.startsWith("//");
    }
}
