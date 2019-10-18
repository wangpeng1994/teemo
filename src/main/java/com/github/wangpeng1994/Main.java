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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {

        // Links to be processed.
        List<String> linkPool = new LinkedList<>();
        // Links that have been processed.
        Set<String> processedLinks = new HashSet<>();
        // Add a first link to start.
        linkPool.add("https://sina.cn");

        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(0);

            // Avoid the same link.
            if (processedLinks.contains(link)) {
                continue;
            }

            if (link.contains("news.sina.cn") || "https://sina.cn".equals(link)) {

                if (link.startsWith("//")) {
                    link = "https:" + link;
                }
                System.out.println(link);

                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(link);
                httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");

                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    System.out.println(response1.getStatusLine());
                    HttpEntity entity1;
                    entity1 = response1.getEntity();
                    String html = EntityUtils.toString(entity1);

                    Document doc = Jsoup.parse(html);

                    // Each encountered <a> tag will be added into linkPool.
                    Elements aTags = doc.select("a");
                    for (Element aTag : aTags) {
                        linkPool.add(aTag.attr("href"));
                    }

                    // If it's a news detail page, then store into database. Otherwise, do nothing.
                    Elements articleTags = doc.select("article");
                    if (!articleTags.isEmpty()) {
                        for (Element articleTag : articleTags) {
                            String title = articleTags.get(0).child(0).text();
                            System.out.println(title);
                        }
                    }

                    processedLinks.add(link);
                }
            } else {
                // Not process the link we're not interested in.
                continue;
            }
        }
    }
}
