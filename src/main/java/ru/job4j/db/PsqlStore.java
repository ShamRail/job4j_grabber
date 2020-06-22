package ru.job4j.db;

import ru.job4j.model.Post;
import ru.job4j.quartz.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PsqlStore implements Store, AutoCloseable {

    private ConfigManager configManager;

    private Connection connection;

    public PsqlStore(ConfigManager configManager) throws IOException {
        this.configManager = configManager;
        this.initConnection();
        this.initTable();
    }

    public PsqlStore(Connection connection) {
        this.connection = connection;
    }

    private void initConnection() {
        try {
            Class.forName(configManager.get("driver-class-name"));
            connection = DriverManager.getConnection(
                    configManager.get("url"),
                    configManager.get("username"),
                    configManager.get("password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTable() throws IOException {
        String sql = String.join("", Files.readAllLines(Path.of("./db", "create_table.sql")));
        try (Statement stm = connection.createStatement()) {
            stm.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps
                     = connection.prepareStatement("insert into post(name, link, description, create_date) values (?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getLinkText());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, post.getCreateDate());
            ps.execute();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    post.setId(rs.getString(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(Iterable<Post> posts) {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps
                         = connection.prepareStatement("insert into post(name, link, description, create_date) values (?, ?, ?, ?);")) {
                for (Post post : posts) {
                    ps.setString(1, post.getLinkText());
                    ps.setString(2, post.getLink());
                    ps.setString(3, post.getDescription());
                    ps.setTimestamp(4, post.getCreateDate());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> findAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("select * from post;")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post p = new Post(
                            rs.getString("name"),
                            rs.getString("link"),
                            rs.getString("description"),
                            rs.getTimestamp("create_date")
                    );
                    p.setId(rs.getString("id"));
                    posts.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(String id) {
        Post post = null;
        try (PreparedStatement ps = connection.prepareStatement("select * from post p where p.id = ?;")) {
            ps.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Post p = new Post(
                            rs.getString("name"),
                            rs.getString("link"),
                            rs.getString("description"),
                            rs.getTimestamp("create_date")
                    );
                    p.setId(rs.getString("id"));
                    post = p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
