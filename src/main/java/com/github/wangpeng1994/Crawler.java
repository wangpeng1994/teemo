package com.github.wangpeng1994;

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

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class Crawler {
    private CrawlerDao dao = new MyBatisCrawlerDao();

    public static void main(String[] args) throws SQLException {
        new Crawler().run();
    }

    private void run() throws SQLException {
        String link;

        while ((link = dao.getNextLinkThenDelete()) != null) {
            if (!dao.isLinkProcessed(link)) {
                System.out.println(link);
                Document doc = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(doc);
                storeIntoDatabaseIfItIsNewsPage(doc, link);
                dao.insertProcessedLink(link);
            }
        }
    }

    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String link = aTag.attr("href");
            if (isInterestingLink(link)) {
                if (link.startsWith("//")) {
                    link = "https:" + link;
                }
                dao.insertLinkToBeProcessed(link);
            }
        }
    }

    private void storeIntoDatabaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        Elements articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(title, content, link);
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
