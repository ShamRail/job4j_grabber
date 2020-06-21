package ru.job4j.html;

import ru.job4j.model.Post;

import java.util.List;

public interface Parse {
    List<Post> list(String link);
    Post detail(String link);
}
