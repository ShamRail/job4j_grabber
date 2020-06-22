package ru.job4j.html;

import ru.job4j.model.Post;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Predicate;

public interface Parse {
    List<Post> list(String link, Predicate<Timestamp> until);
    Post detail(String link);
    List<String> resources();
}
