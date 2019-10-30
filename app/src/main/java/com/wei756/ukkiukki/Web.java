package com.wei756.ukkiukki;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Web extends Thread {
    private static final Web instance = new Web();
    private static boolean logined = false;
    private final static String URL = "https://pixelnetwork.co.kr/index.php?";
    private final static int timeout = 5000;
    private final static Map<String, String> cookie = new HashMap<>(), header = new HashMap<>();
    private final static String userAgent = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.92 Mobile Safari/537.36 PIXEL_ANDROID_APP_TEST";
    private static String sessionId;

    private Web() {

    }

    /**
     * 게시판의 글목록을 불러옵니다.
     *
     * @param articleList 메소드가 호출된 articleList
     * @param mid         게시판
     * @param page        페이지 번호
     * @param reset       리사이클뷰 초기화
     * @see Web#getArticleElements(String, int)
     * @see ArticleListAdapter
     * @see MainActivity
     */
    public static void loadArticleList(final ArticleList articleList, final String mid, final int page, final boolean reset) {
        new Thread() {
            public void run() {
                ArrayList arrayList = null;
                Elements articles = null;
                try {
                    articles = getArticleElements(mid, page);

                    arrayList = new ArrayList<>();

                    if (articles.text().contains("등록된 글이 없습니다.") == false) {
                        for (Element article : articles) {
                            // 레벨 아이콘과 닉네임 분리
                            Element elementAuthor = null;

                            // for album style category

                            // author
                            if (article.selectFirst("td[class=author]") != null)
                                elementAuthor = article.selectFirst("td[class=author]");
                            else if (article.selectFirst("span[class=nickname]") != null)
                                elementAuthor = article.selectFirst("span[class=nickname]");

                            String author = elementAuthor.html(), levelIcon = "";
                            if (author.contains("<a")) {
                                if (author.contains("</span>")) { // 레벨 아이콘이 있을 경우
                                    levelIcon = String.valueOf(elementAuthor.selectFirst("span").text().charAt(0));
                                    author = author.substring(author.indexOf("</span>") + 7, author.indexOf("</a>"));
                                } else {                          // 레벨 아이콘이 없을 경우
                                    author = author.substring(author.indexOf("\">") + 2, author.indexOf("</a>"));
                                }
                            } else {
                                if (author.contains("</span>")) {
                                    levelIcon = String.valueOf(elementAuthor.selectFirst("span").text().charAt(0));
                                    author = author.substring(author.indexOf("</span>") + 7);
                                }
                            }
                            author = author.replaceAll(" ", "");
                            // 리스트 추가
                            Article article1 = new Article().setLevelIcon(levelIcon)
                                    .setAuthor(author)
                                    .setThumbnail((article.select("img[title=file]").first() != null));

                            // for album style category

                            // title
                            if (article.selectFirst("td[class=title]") != null) {
                                article1.setTitle(article.selectFirst("td[class=title]").selectFirst("a").text());
                                article1.setHref(article.selectFirst("td[class=title]").selectFirst("a").attr("href"));
                            } else if (article.selectFirst("span[class=title]") != null) {
                                article1.setTitle(article.selectFirst("span[class=title]").text());
                                article1.setHref(article.selectFirst("span[class=title]").selectFirst("a").attr("href"));
                            }

                            // thumbnail
                            if (article.selectFirst("img[class=tmb]") != null) {
                                article1.setThumbnailUrl(article.selectFirst("img[class=tmb]").attr("src"));
                            }

                            // time
                            if (article.selectFirst("td[class=time]") != null)
                                article1.setTime(article.selectFirst("td[class=time]").text());

                            // readNum
                            if (article.selectFirst("td[class=readNum]") != null)
                                article1.setView(article.selectFirst("td[class=readNum]").text());

                            // comment
                            if (article.selectFirst("td[class=mobile_comment_count]") != null)
                                article1.setComment(article.selectFirst("td[class=mobile_comment_count]").selectFirst("a[title=Replies]").selectFirst("a").text());
                            if (article.selectFirst("span[class=replies]") != null)
                                article1.setComment(article.selectFirst("span[class=replies]").selectFirst("a").text());


                            // for commawang category
                            if (article.selectFirst("td[class=voteNum]") != null)
                                article1.setVote(article.selectFirst("td[class=voteNum]").text());

                            arrayList.add(article1);
                        }

                    } else {
                        throw new NullPointerException();
                    }
                    Log.i("Web", "Article " + page + " page loaded. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");

                    // callback
                    articleList.onLoadedArticleList(mid, arrayList, reset);

                } catch (IOException e) {
                    arrayList = null;
                    Log.w("Web.err", "Connection error. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    articleList.onLoadedArticleList(mid, e, reset);

                } catch (NullPointerException e) {
                    arrayList = null;
                    Log.w("Web.err", "Error occurred. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    articleList.onLoadedArticleList(mid, e, reset);
                }

            }

        }.start();
    }

    /**
     * 실제 웹페이지로부터 글목록을 파싱합니다.
     * table 태그 안의 tr 태그들을 Elements 타입으로 반환합니다
     *
     * @param mid  게시판
     * @param page 페이지 번호
     * @return 글목록
     * @throws IOException          통신에 문제가 있는 경우
     * @throws NullPointerException 게시판에 게시글이 없는 경우
     * @see Web#loadArticleList(ArticleList, String, int, boolean)
     */
    private static Elements getArticleElements(String mid, int page) throws IOException, NullPointerException {
        // connect
        if (sessionId == null) {
            setSessionId();
        }
        Connection.Response response = null;
        Document document;

        response = Jsoup.connect(URL + "mid=" + mid + "&page=" + page)
                .userAgent(userAgent)
                .cookie("PHPSESSID", sessionId)
                .timeout(timeout)
                .method(Connection.Method.GET)
                .execute();

        document = response.parse();

        Log.v("Web", "PHPSESSID is " + sessionId + " now.");

        //parse article list
        CategoryManager category = CategoryManager.getInstance();
        int catType = CategoryManager.TYPE_BOARD;
        try {
            catType = (Integer) category.getParam(mid, CategoryManager.TYPE);
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }
        Element articleTable;
        Elements articles = null;
        if (catType == CategoryManager.TYPE_BOARD
                || catType == CategoryManager.TYPE_COMMAWANG) {
            articleTable = document.selectFirst("table[summary=List of Articles]").selectFirst("tbody");
            articles = articleTable.select("tr");
        } else if (catType == CategoryManager.TYPE_PIXEL
                || catType == CategoryManager.TYPE_CLIP
                || catType == CategoryManager.TYPE_CREATIVE) {
            articleTable = document.selectFirst("div[class=board_gallery]");
            articles = articleTable.select("div[class=gallery_item]");
        }
        Log.i("Web", "" + articles.size() + " article(s) found from " + page + " page. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.getArticleElements");

        return articles;
    }


    public static void loadArticleList(final ArticleList articleList, final String url, final int page) {
        new Thread() {
            public void run() {
                ArrayList arrayList = null;
                Elements articles = null;
                try {
                    articles = getArticleElements(url);

                    arrayList = new ArrayList<>();

                    if (articles != null) { // 게시글이 있으면
                        for (Element article : articles) {
                            // 레벨 아이콘과 닉네임 분리
                            Element elementAuthor = null;

                            // for album style category

                            // author
                            String author = article.selectFirst("span[class=nick]").text();

                            // 리스트 추가
                            Article article1 = new Article()
                                    .setAuthor(author)
                                    .setTitle(article.selectFirst("strong[class=tit]").text())
                                    .setHref(article.selectFirst("a").attr("href"))
                                    .setTime(article.selectFirst("span[class=time]").text())
                                    .setView(article.selectFirst("span[class=no]").text())
                                    .setComment(article.selectFirst("em[class=num]").text());

                            // thumbnail
                            if (article.selectFirst("div[class=thumb]") != null) {
                                article1.setThumbnailUrl(article.selectFirst("div[class=thumb]").selectFirst("img").attr("src"));
                            }

                            arrayList.add(article1);
                            Log.e("Web", "제목: " + article1.getTitle());
                        }

                    } else {
                        throw new NullPointerException();
                    }
                    //Log.i("Web", "Article " + page + " page loaded. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");

                    // callback
                    //articleList.onLoadedArticleList(mid, arrayList, reset);

                } catch (IOException e) {
                    arrayList = null;
                    //Log.w("Web.err", "Connection error. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    //articleList.onLoadedArticleList(mid, e, reset);

                } catch (NullPointerException e) {
                    arrayList = null;
                    //Log.w("Web.err", "Error occurred. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    //articleList.onLoadedArticleList(mid, e, reset);
                }

            }

        }.start();
    }

    public static Elements getArticleElements(String url) throws IOException, NullPointerException {
        // connect
        Connection.Response response = null;
        Document document;
        Elements articles = null;
        Connection connection = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .method(Connection.Method.GET);
        connection = putHeader(connection);
        response = connection.execute();

        getCookies(response);
        document = response.parse();

        Log.v("Web", "JSESSIONID is " + cookie.get("JSESSIONID") + " now.");
        //Log.e("Web", document.text()); // for debug: view loaded page with text

        //parse article list
        /*CategoryManager category = CategoryManager.getInstance();
        int catType = CategoryManager.TYPE_BOARD;
        try {
            catType = (Integer) category.getParam(mid, CategoryManager.TYPE);
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }*/
        Element articleTable;
        if (document.selectFirst("div[id=ct]").select("p[class=noti]").isEmpty()) { // 게시글이 없는지 확인
            articleTable = document.selectFirst("div[id=articleListArea]").selectFirst("ul");
            articles = articleTable.select("li"); // 게시글 추출
        }
        Log.i("Web", "" + articles.size() + " article(s) found from " + 1 + " page. (" + url + ")" + " on Web.getArticleElements");
        //Log.i("Web", "" + articles.size() + " article(s) found from " + page + " page. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.getArticleElements");

        return articles;
    }

    private static Connection putHeader(Connection connection) {
        if (!cookie.containsKey("JSESSIONID")) // 최초 접속인지 확인
            resetCookies();

        return connection.cookies(cookie).headers(getHeader());
    }

    private static void getCookies(Connection.Response response) {
        resetCookies();
        cookie.putAll(response.cookies());
        Log.i("Web", "Get cookies.");
        printMap(cookie);
    }

    private static void resetCookies() {
        cookie.clear();
    }

    private static Map getHeader() {
        if (!header.containsKey("user-agent")) {
            header.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            header.put("accept-encoding", "gzip, deflate, br");
            header.put("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            header.put("sec-fetch-mode", "navigate");
            header.put("sec-fetch-site", "none");
            header.put("sec-fetch-user", "?1");
            header.put("upgrade-insecure-requests", "1");
            header.put("user-agent", userAgent);
        }
        return header;
    }

    private static void printMap(Map<String, String> map) {
        for (Map.Entry<String, String> elem : map.entrySet()) {

            String key = elem.getKey();
            String value = elem.getValue();

            Log.i("Web", key + " : " + value);
        }
    }

    /**
     * 해당 스트리머의 트위치 방송이 켜져있는지 확인하고 그 여부를 반환합니다.
     *
     * @param mid 스트리머 게시판 코드
     * @return 뱅온 여부
     * @see StreamerList
     */
    public static boolean getTwitchLive(String mid) {
        boolean live = false;
        String twitchChannel;
        CategoryManager category = CategoryManager.getInstance();
        try {
            if ((Integer) category.getParam(mid, CategoryManager.GROUP) == CategoryManager.GROUP_STREAMER) {
                twitchChannel = (String) category.getParam(mid, CategoryManager.CHANNEL_TWITCH);

                Connection.Response twitch = Jsoup.connect("https://twitch.tv/" + twitchChannel)
                        .userAgent(userAgent)
                        .timeout(timeout)
                        .method(Connection.Method.GET)
                        .execute();

                Document twitchLive = twitch.parse();
                Log.e("Twitch Live test", "" + twitchLive.text());
                live = twitchLive.text().contains(" Playing ") && !twitchLive.text().contains(" hosting ");
            }
        } catch (InvalidCategoryException e) {
            Log.w("Web.err", "올바르지 않은 채널입니다(" + mid + ") on Web.getTwitchLive");
            e.printStackTrace();
        } catch (IOException e) {
            Log.w("Web.err", "통신 에러(" + mid + ") on Web.getTwitchLive");
            e.printStackTrace();
        }

        Log.v("Web", mid + ":" + live + " on Web.getTwitchLive");
        return live;
    }

    /**
     * 게시글을 불러옵니다.
     *
     * @param articleViewerActivity 불러온 게시글을 출력할 Activity
     * @param articleHref           불러올 게시글의 주소
     * @see ArticleViewerActivity
     */
    public static void getArticle(final ArticleViewerActivity articleViewerActivity, final String articleHref) {
        new Thread() {
            public void run() {
                int begin = articleHref.indexOf("mid="), end = articleHref.indexOf("&page=");
                String mid = articleHref.substring(begin + 4, end);

                // get html
                Document document = null;
                Connection.Response response = null;
                try {
                    if (sessionId == null) {
                        setSessionId();
                    }
                    response = Jsoup.connect(articleHref)
                            .userAgent(userAgent)
                            .cookie("PHPSESSID", sessionId)
                            .timeout(timeout)
                            .method(Connection.Method.GET)
                            .execute();
                    document = response.parse();

                    Log.v("Web", "PHPSESSID is " + sessionId + " now.");

                    Element header = document.selectFirst("div[class=read_header]");
                    Element body = document.selectFirst("div[class=read_body]");
                    Element footer = document.selectFirst("div[class=read_footer]");
                    Element feedback = document.selectFirst("div[class=feedback]");

                    // get header
                    Article data = null;
                    if (!document.text().contains("대상을 찾을 수 없습니다.")) {
                        // 레벨 아이콘과 닉네임 분리
                        Element elementAuthor = header.selectFirst("span[class=author]");
                        String author = elementAuthor.html(), levelIcon = "";
                        if (author.contains("<a")) {
                            if (author.contains("</span>")) { // 레벨 아이콘이 있을 경우
                                levelIcon = elementAuthor.selectFirst("a").selectFirst("span").text();
                                author = author.substring(author.indexOf("</span>") + 7, author.indexOf("</a>"));
                            } else {                          // 레벨 아이콘이 없을 경우
                                author = elementAuthor.selectFirst("a").text();
                            }
                        } else {
                            if (author.contains("</span>")) {
                                levelIcon = elementAuthor.selectFirst("span").text();
                                author = author.substring(author.indexOf("</span>") + 7);
                            } else {
                                author = elementAuthor.text();
                            }
                        }
                        // 데이터 입력
                        data = new Article(header.selectFirst("h1").text(),
                                mid,
                                levelIcon,
                                author,
                                header.selectFirst("span[class=time]").text(),
                                header.selectFirst("span[class=read_count]").text(),
                                header.selectFirst("span[class=vote_count]").text(),
                                articleHref,
                                body,
                                footer,
                                feedback);
                        Log.i("Web", " article(s) found from page. (" + articleHref + ")" + " on Web.getArticle");
                    }

                    // callback
                    articleViewerActivity.onLoadArticle(data);
                } catch (IOException e) {
                    Log.w("Web.err", "Connection error. (" + URL + ")" + " on Web.getArticle");
                    e.printStackTrace();

                    // callback
                    articleViewerActivity.onLoadArticle(null);
                }
            }
        }.start();
    }

    /**
     * Pixel 사이트에 접속해 phpsessid를 받아옵니다.
     *
     * @throws IOException 통신에 문제가 있는 경우
     */
    private static void setSessionId() throws IOException {
        Connection.Response response = Jsoup.connect(URL)
                .userAgent(userAgent)
                .timeout(timeout)
                .method(Connection.Method.GET)
                .execute();

        sessionId = response.cookie("PHPSESSID");
        Log.i("Web", "Set sessionId.");
        Log.i("Web", "PHPSESSID: " + sessionId);
        Log.i("Web", "User Agent: " + userAgent);
        Log.i("Web", "Android version: " + Build.MODEL + " " + Build.VERSION.RELEASE);
    }

    /**
     * Singleton
     *
     * @return
     */
    public Web getInstance() {
        return instance;
    }
}

