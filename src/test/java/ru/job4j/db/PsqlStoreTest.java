package ru.job4j.db;

import org.junit.Test;
import ru.job4j.model.Post;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PsqlStoreTest {

    public Connection init() {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            return DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void createItem() throws Exception {
        try (PsqlStore store = new PsqlStore(ConnectionRollback.create(this.init()))) {
            Post post = new Post("job", "link", "desc", Timestamp.valueOf(LocalDateTime.now()));
            store.save(post);
            Post out = store.findById(post.getId());
            assertEquals(post.getLinkText(), out.getLinkText());
            assertEquals(post.getLink(), out.getLink());
            assertEquals(post.getDescription(), out.getDescription());
            assertNotNull(out.getDescription());
            assertNotNull(out.getId());
        }
    }

    @Test
    public void whenFindById() throws Exception {
        try (PsqlStore store = new PsqlStore(ConnectionRollback.create(this.init()))) {
            Post post = new Post("job", "link", "desc", Timestamp.valueOf(LocalDateTime.now()));
            store.save(post);
            Post out = store.findById(post.getId());
            assertEquals(post.getLinkText(), out.getLinkText());
            assertEquals(post.getLink(), out.getLink());
            assertEquals(post.getDescription(), out.getDescription());
            assertNotNull(out.getDescription());
            assertNotNull(out.getId());
        }
    }

}