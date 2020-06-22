package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.model.Post;
import ru.job4j.utils.DateUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class SqlRuParse implements Parse {

    private static final int POST_LIST_TOPIC = 1;

    private static final int DATE_TIME = 5;

    private static final int LINK = 0;

    private static final int MSG_TEXT = 1;

    private static final String RESOURCE = "https://www.sql.ru/forum/job-offers";

    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class.getName());

    @Override
    public List<Post> list(String url, Predicate<Timestamp> until) {
        List<Post> posts = new ArrayList<>();
        LOG.debug("Parse: {}", url);
        try {
            Document doc = Jsoup.connect(url).get();
            Elements tableRows = doc.select(".forumTable").first().getElementsByTag("tr");
            LOG.debug("Retrieved {} rows", tableRows.size());
            for (Element row : tableRows) {
                Element postListTopic = row.child(POST_LIST_TOPIC);
                if (postListTopic.hasClass("postslisttopic")) {
                    Element date = row.child(DATE_TIME);
                    Timestamp createDate = DateUtils.parse(date.text());
                    LOG.debug("Post date: {}", createDate);
                    if (until.test(createDate)) {
                        break;
                    }
                    LOG.debug("Check passed");
                    Element linkElement = postListTopic.child(LINK);
                    String link = linkElement.attr("href");
                    String text = linkElement.text();
                    LOG.debug("Parse vacansy: {}", text);
                    posts.add(new Post(
                            text, link, "", createDate
                    ));
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
        LOG.debug("Parse resources {}", url);
        try {
            Document doc = Jsoup.connect(url).get();
            Elements comments = doc.select(".msgTable");
            String description = comments.first().select(".msgBody").get(MSG_TEXT).html();
            String name = comments.first().select(".messageHeader").text();
            String date = comments.last().select(".msgFooter").text();
            date = date.substring(0, date.indexOf('[') - 1);
            LOG.debug("Parsing completed");
            return new Post(
                    name, url, description, DateUtils.parse(date)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public List<String> resources() {
        List<String> list = new LinkedList<>();
        for (int page = 1; page <= 5; page++) {
            list.add(String.format("%s/%s", RESOURCE, page));
        }
        return list;
    }
}