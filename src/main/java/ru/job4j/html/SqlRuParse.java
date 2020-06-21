package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.model.Post;
import ru.job4j.utils.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlRuParse {

    private static final int POST_LIST_TOPIC = 1;

    private static final int DATE_TIME = 5;

    private static final int LINK = 0;
    public static final int MSG_TEXT = 1;

    public static void main(String[] args) throws Exception {
        for (int page = 1; page <= 5; page++) {
            String url = String.format("https://www.sql.ru/forum/job-offers/%d", page);
            parsePage(url);
        }
    }

    private static List<Post> parsePage(String url) throws IOException {
        List<Post> posts = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements tableRows = doc.select(".forumTable").first().getElementsByTag("tr");
        for (Element row : tableRows) {
            Element postListTopic = row.child(POST_LIST_TOPIC);
            if (postListTopic.hasClass("postslisttopic")) {
                Element date = row.child(DATE_TIME);
                Element linkElement = postListTopic.child(LINK);
                String link = linkElement.attr("href");
                String text = linkElement.text();
                String description = parseDescription(link);
                posts.add(new Post(
                        text, link, description, DateUtils.parse(date.text())
                ));
                System.out.println(String.format("%s %s %s", link, text, date.text()));
                System.out.println(description);
                System.out.println();
            }
        }
        return posts;
    }

    private static String parseDescription(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc
                .select(".msgTable").first()
                .select(".msgBody").get(MSG_TEXT)
                .text();
    }

}