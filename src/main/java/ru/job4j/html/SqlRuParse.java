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

public class SqlRuParse implements Parse {

    private static final int POST_LIST_TOPIC = 1;

    private static final int DATE_TIME = 5;

    private static final int LINK = 0;

    private static final int MSG_TEXT = 1;

    @Override
    public List<Post> list(String url) {
        List<Post> posts = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements tableRows = doc.select(".forumTable").first().getElementsByTag("tr");
            for (Element row : tableRows) {
                Element postListTopic = row.child(POST_LIST_TOPIC);
                if (postListTopic.hasClass("postslisttopic")) {
                    Element date = row.child(DATE_TIME);
                    Element linkElement = postListTopic.child(LINK);
                    String link = linkElement.attr("href");
                    String text = linkElement.text();
                    posts.add(new Post(
                            text, link, "", DateUtils.parse(date.text())
                    ));
                    System.out.println(String.format("%s %s %s", link, text, date.text()));
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return posts;
    }

    @Override
    public Post detail(String url) {
        Post post = new Post();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements comments = doc.select(".msgTable");
            String description = comments.first().select(".msgBody").get(MSG_TEXT).text();
            String name = comments.first().select(".messageHeader").text();
            String date = comments.last().select(".msgFooter").text();
            date = date.substring(0, date.indexOf('[') - 1);
            return new Post(
                    name, url, description, DateUtils.parse(date)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return post;
    }
}