package com.wei756.ukkiukki;

import org.jsoup.nodes.Element;

public class Article extends Item {
    private String title;
    private String mid;
    private String levelIcon;
    private String author;
    private String authorId;
    private String authorProfile;
    private String time;
    private String view;
    private String comment;
    private String likeIt;
    private String thumbnailUrl;
    private String href;

    private boolean newArticle = true, readArticle = false, newComment = false;

    private Element body;
    private Element footer;
    private Element feedback;

    public Article() {

    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Article setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Article setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public String getAuthorProfile() {
        return authorProfile;
    }

    public Article setAuthorProfile(String authorProfile) {
        this.authorProfile = authorProfile;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Article setTime(String time) {
        this.time = time;
        return this;
    }

    public String getView() {
        return view;
    }

    public Article setView(String view) {
        this.view = view;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Article setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getLikeIt() {
        return likeIt;
    }

    public Article setLikeIt(String likeIt) {
        this.likeIt = likeIt;
        return this;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Article setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public String getHref() {
        return href;
    }

    public Article setHref(String href) {
        this.href = href;
        return this;
    }

    public Element getBody() {
        return body;
    }

    public Article setBody(Element body) {
        this.body = body;
        return this;
    }

    public Element getFooter() {
        return footer;
    }

    public Article setFooter(Element footer) {
        this.footer = footer;
        return this;
    }

    public String getMid() {
        return mid;
    }

    public Article setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public Element getFeedback() {
        return feedback;
    }

    public Article setFeedback(Element feedback) {
        this.feedback = feedback;
        return this;
    }

    public String getLevelIcon() {
        return levelIcon;
    }

    public Article setLevelIcon(String levelIcon) {
        this.levelIcon = levelIcon;
        return this;
    }

    public boolean isNewArticle() {
        return newArticle;
    }

    public Article setNewArticle(boolean newArticle) {
        this.newArticle = newArticle;
        return this;
    }

    public boolean isReadArticle() {
        return readArticle;
    }

    public Article setReadArticle(boolean readArticle) {
        this.readArticle = readArticle;
        return this;
    }

    public boolean isNewComment() {
        return newComment;
    }

    public Article setNewComment(boolean newComment) {
        this.newComment = newComment;
        return this;
    }
}
