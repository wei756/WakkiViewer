package com.wei756.ukkiukki;

import org.jsoup.nodes.Element;

public class Article extends Item{
    private String title;
    private String mid;
    private String levelIcon;
    private String author;
    private String time;
    private String view;
    private String comment;
    private String likeIt;
    private String thumbnailUrl;
    private String href;

    private Element body;
    private Element footer;
    private Element feedback;

    // on article viewer
    public Article(String title, String mid, String levelIcon, String author, String time, String view, String likeIt, String href, Element body, Element footer, Element feedback) {
        this.title = title;
        this.mid = mid;
        this.levelIcon = levelIcon;
        this.author = author;
        this.time = time;
        this.view = view;
        //this.comment = comment;
        this.likeIt = likeIt;
        this.href = href;
        this.body = body;
        this.footer = footer;
        this.feedback = feedback;
    }

    public Article(){

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

    public Article setLikeIt(String vote) {
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
}
