package com.github.wangpeng1994;

public class Main {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();

        for (int i = 0; i < 4; i++) {
            new Crawler(dao).start();
        }
    }
}