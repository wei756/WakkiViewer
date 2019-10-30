package com.wei756.ukkiukki;

public class Comment extends Item {
    private int id;
    private String parentAuthor;
    private int indentLevel;
    private String levelIcon;
    private String author;
    private String time;
    private String content;
    private String href;

    public Comment(String levelIcon, String author, String time, String content, String href) {
        this.levelIcon = levelIcon;
        this.author = author;
        this.time = time;
        this.content = content;
        this.href = href;
    }

    public Comment() {

    }

    public int getId() {
        return id;
    }

    public Comment setId(int id) {
        this.id = id;
        return this;
    }

    public String getParentAuthor() {
        return parentAuthor;
    }

    public Comment setParentAuthor(String parentAuthor) {
        this.parentAuthor = parentAuthor;
        return this;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public Comment setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Comment setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Comment setTime(String time) {
        this.time = time;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Comment setContent(String content) {
        this.content = content;
        return this;
    }

    public String getHref() {
        return href;
    }

    public Comment setHref(String href) {
        this.href = href;
        return this;
    }

    public String getLevelIcon() {
        return levelIcon;
    }

    public Comment setLevelIcon(String levelIcon) {
        this.levelIcon = levelIcon;
        return this;
    }
}
