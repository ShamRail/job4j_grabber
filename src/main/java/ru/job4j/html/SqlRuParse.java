package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.selectFirst(".forumTable").select(".altCol");
        for (int i = 1; i < row.size(); i += 2) {
            System.out.println(row.get(i).text());
        }
    }
}