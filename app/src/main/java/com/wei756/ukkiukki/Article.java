package com.wei756.ukkiukki;

import com.wei756.ukkiukki.Network.Web;

import java.util.ArrayList;
import java.util.Map;

public class Article extends Item {
    private int errorcode = Web.RETURNCODE_SUCCESS;

    private String title;
    private String head;
    private String mid;
    private String levelIcon;
    private String author;
    private String authorId;
    private String authorProfile;
    private String time;
    private String timestamp;
    private String view;
    private String comment;
    private String likeIt;
    private String thumbnailUrl;
    private String href;

    // profile comment list
    private String content;
    private String articleTitle;

    private boolean newArticle = true, readArticle = false, newComment = false;

    private Map article, comments, advert, authority;
    private ArrayList<Map> attaches;

    public Article() { }

    public int getErrorcode() {
        return errorcode;
    }

    public Article setErrorcode(int errorcode) {
        this.errorcode = errorcode;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getHead() {
        return head;
    }

    public Article setHead(String head) {
        this.head = head;
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

    public String getTimestamp() {
        return timestamp;
    }

    public Article setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getMid() {
        return mid;
    }

    public Article setMid(String mid) {
        this.mid = mid;
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

    public String getContent() {
        return content;
    }

    public Article setContent(String content) {
        this.content = content;
        return this;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public Article setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
        return this;
    }

    public Map getArticle() {
        return article;
    }

    public Article setArticle(Map article) {
        this.article = article;
        return this;
    }

    public ArrayList<Map> getAttaches() {
        return attaches;
    }

    public Article setAttaches(ArrayList attaches) {
        this.attaches = attaches;
        return this;
    }

    public Map getComments() {
        return comments;
    }

    public Article setComments(Map comments) {
        this.comments = comments;
        return this;
    }

    public Map getAdvert() {
        return advert;
    }

    public Article setAdvert(Map advert) {
        this.advert = advert;
        return this;
    }

    public Map getAuthority() {
        return authority;
    }

    public Article setAuthority(Map authority) {
        this.authority = authority;
        return this;
    }
}
