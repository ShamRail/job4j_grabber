package ru.job4j.db;

import ru.job4j.model.Post;

import java.util.List;

public interface Store {
    void save(Post post);
    void saveAll(Iterable<Post> posts);
    List<Post> findAll();
    Post findById(String id);
}
