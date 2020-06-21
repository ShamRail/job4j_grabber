package ru.job4j.model;

import java.sql.Timestamp;
import java.util.Objects;

public class Post {

    private String lineText;

    private String link;

    private String description;

    private Timestamp createDate;

    public Post() {

    }

    public Post(String lineText, String link, String description, Timestamp createDate) {
        this.lineText = lineText;
        this.link = link;
        this.description = description;
        this.createDate = createDate;
    }

    public String getLineText() {
        return lineText;
    }

    public void setLineText(String lineText) {
        this.lineText = lineText;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
