package com.wei756.ukkiukki;

public class Comment extends Item {
    private int id;
    private String parentAuthor;
    private int indentLevel;
    private String imgProfile;
    private String author;
    private String time;
    private String content;
    private String contentImage;
    private String sticker;
    private String href;
    private boolean iconNew;
    private boolean iconArticleAuthor;
    private boolean mine;

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

    public String getContentImage() {
        return contentImage;
    }

    public Comment setContentImage(String contentImage) {
        this.contentImage = contentImage;
        return this;
    }

    public String getSticker() {
        return sticker;
    }

    public Comment setSticker(String sticker) {
        this.sticker = sticker;
        return this;
    }

    public String getHref() {
        return href;
    }

    public Comment setHref(String href) {
        this.href = href;
        return this;
    }

    public String getImgProfile() {
        return imgProfile;
    }

    public Comment setImgProfile(String imgProfile) {
        this.imgProfile = imgProfile;
        return this;
    }

    public boolean isIconNew() {
        return iconNew;
    }

    public Comment setIconNew(boolean iconNew) {
        this.iconNew = iconNew;
        return this;
    }

    public boolean isIconArticleAuthor() {
        return iconArticleAuthor;
    }

    public Comment setIconArticleAuthor(boolean iconArticleAuthor) {
        this.iconArticleAuthor = iconArticleAuthor;
        return this;
    }

    public boolean isMine() {
        return mine;
    }

    public Comment setMine(boolean mine) {
        this.mine = mine;
        return this;
    }
}
