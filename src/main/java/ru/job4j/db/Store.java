package ru.job4j.db;

import ru.job4j.model.Post;

import java.sql.Timestamp;
import java.util.List;

public interface Store {
    void save(Post post);
    void saveAll(List<Post> posts);
    List<Post> findAll();
    Post findById(String id);
    Timestamp lastItem();
}
