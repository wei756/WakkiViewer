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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Web extends Thread {
    private static final Web instance = new Web();
    private static boolean logined = false;
    private final static String URL = "https://m.cafe.naver.com/steamindiegame?",
            articleListUrl = "https://m.cafe.naver.com/ArticleListAjax.nhn?",
            popularArticleListUrl = "https://m.cafe.naver.com/PopularArticleList.nhn?",
            menuListUrl = "https://m.cafe.naver.com/MenuListAjax.nhn?";
    private final static String cafeId = "27842958";
    private final static int timeout = 5000;
    private final static Map<String, String> cookie = new HashMap<>(), header = new HashMap<>();
    private final static String userAgentMobile = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.92 Mobile Safari/537.36";
    private final static String userAgentPC = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36";
    private static String sessionId;

    private Web() {

    }

    /**
     * 해당 URL로 http request을 합니다.
     *
     * @param url
     * @param method
     * @return Response
     * @throws IOException
     */
    public static Connection.Response httpRequest(String url, Connection.Method method, boolean connectByPC, boolean usingCookie) throws IOException {
        Connection.Response response = null;
        Connection connection = Jsoup.connect(url)
                .userAgent(connectByPC ? userAgentPC : userAgentMobile)
                .timeout(timeout)
                .method(method);
        if (usingCookie)
            connection = putHeader(connection);
        response = connection.execute();

        if (usingCookie) {
            getCookies(response);
            Log.v("Web", "JSESSIONID is " + cookie.get("JSESSIONID") + " now.");
        }

        //Log.e("Web", response.parse().text()); // for debug: view loaded page with text

        return response;
    }

    /**
     * 게시판 목록을 불러옵니다.
     *
     * @see CategoryManager
     */
    public static ArrayList<Map> loadCategoryList() {
        ArrayList arrayList = null;
        String url = menuListUrl + "search.clubid=" + cafeId + "&search.perPage=9999";
        try {
            Elements categories = null;

            Document document;
            document = httpRequest(url, Connection.Method.GET, true, true).parse();

            categories = document.selectFirst("ul[id=allMenuList]").select("li"); // 카테고리 추출
            Log.i("Web", "" + categories.size() + " category block(s) found. on Web.loadCategoryList");


            if (categories != null) { // 카테고리가 있으면
                arrayList = new ArrayList<>();

                for (Element category : categories) {
                    CategoryManager.CategoryBuilder category1 = new CategoryManager.CategoryBuilder();
                    if (category.selectFirst("h4") != null) { // 그룹

                        String name = category.selectFirst("h4").text();
                        category1.setType(CategoryManager.TYPE_GROUP)
                                .setName(name);

                        Log.v("Web", "카테고리: 그룹: " + name);

                    } else if (category.selectFirst("div") != null) { // 카테고리
                        Element a = category.selectFirst("a[class=tit  ]");
                        String name = a.text();
                        category1.setName(name);

                        // 카테고리 타입
                        switch (name) {
                            case "전체글보기":
                                category1.setType(CategoryManager.TYPE_MAINPAGE);
                                break;
                            case "인기글":
                                category1.setType(CategoryManager.TYPE_BEST);
                                break;
                            case "▩ 표 지 왕 ▩":
                                category1.setType(CategoryManager.TYPE_THUMBKING);
                                break;
                            default:
                                /*
                                switch (category.selectFirst("img").className()) {
                                    case "ico-photo":
                                        category1.setType(CategoryManager.TYPE_ALBUM);
                                        break;
                                    case "ico-memo":
                                        category1.setType(CategoryManager.TYPE_REPORT);
                                        break;
                                    default:
                                        category1.setType(CategoryManager.TYPE_BOARD);
                                        break;
                                }
                                */
                                category1.setType(CategoryManager.TYPE_BOARD);
                                break;
                        }

                        String menuId = a.attr("href");
                        if (menuId.contains("PopularArticleList.nhn")) {
                            category1.setMenuId(CategoryManager.CATEGORY_POPULAR_ARTICLE);
                        } else if (menuId.contains("PopularMemberList.nhn")) {
                            category1.setMenuId(CategoryManager.CATEGORY_POPULAR_MEMBER);
                        } else if (menuId.contains("WeeklyPopularTagList.nhn")) {
                            category1.setMenuId(CategoryManager.CATEGORY_TAG);
                        } else {
                            Pattern p= Pattern.compile("search.menuid=([0-9]*)");
                            Matcher m = p.matcher(menuId);
                            while(m.find()){
                                category1.setMenuId(Integer.parseInt(m.group(1)));
                            }
                        }

                        Log.v("Web", "카테고리: 게시판: " + name + "(id: " + menuId + ")");

                    } else if (category.selectFirst("span[class=blind]") != null) { // 구분선

                        category1.setName("구분선");

                        // 카테고리 타입
                        category1.setType(CategoryManager.TYPE_CONTOUR);
                        Log.v("Web", "카테고리: 구분선");
                    } else {
                        continue;
                    }

                    // 리스트 추가
                    arrayList.add(category1.build());
                }

            } else {
                throw new NullPointerException();
            }

        } catch (IOException e) {
            arrayList = null;
            Log.w("Web.err", "Connection error. (" + url + ")" + " on Web.loadCategoryList");
            e.printStackTrace();

        } catch (NullPointerException e) {
            arrayList = null;
            Log.w("Web.err", "Error occurred. (" + url + ")" + " on Web.loadCategoryList");
            e.printStackTrace();
        }

        Log.i("Web", "" + arrayList.size() + " category(s) found. (" + url + ")" + " on Web.loadCategoryList");
        return arrayList;
    }

    /**
     * 게시판의 글목록을 불러옵니다.
     *
     * @param articleList 메소드가 호출된 articleList
     * @param mid         게시판
     * @param page        페이지
     * @param reset       리사이클뷰 초기화
     * @see Web#getArticleElements(int, int)
     * @see ArticleListAdapter
     * @see MainActivity
     */
    public static void loadArticleList(final ArticleList articleList, final int mid, final int page, final boolean reset) {
        new Thread() {
            public void run() {
                ArrayList arrayList = null;
                Elements articles = null;
                try {
                    articles = getArticleElements(mid, page);

                    arrayList = new ArrayList<>();

                    if (!articles.isEmpty()) { // 게시글이 있으면
                        for (Element article : articles) {

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
                    Log.i("Web", "Article " + page + " page loaded. (mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");

                    // callback
                    articleList.onLoadedArticleList(mid, arrayList, reset);

                } catch (IOException e) {
                    arrayList = null;
                    //Log.w("Web.err", "Connection error. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    articleList.onLoadedArticleList(mid, e, reset);

                } catch (NullPointerException e) {
                    arrayList = null;
                    //Log.w("Web.err", "Error occurred. (" + URL + "mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    articleList.onLoadedArticleList(mid, e, reset);
                }

            }

        }.start();
    }


    /**
     * 실제 웹페이지로부터 글목록을 파싱합니다.
     * div 태그 안의 li 태그들을 Elements 타입으로 반환합니다
     *
     * @param mid  게시판
     * @param page 페이지
     * @return 글목록
     * @throws IOException          통신에 문제가 있는 경우
     * @throws NullPointerException 게시판에 게시글이 없는 경우
     * @see Web#loadArticleList(ArticleList, int, int, boolean)
     */
    public static Elements getArticleElements(int mid, int page) throws IOException, NullPointerException {
        Elements articles = null;

        // connect
        Document document;
        String url;
        if (mid == 0)
            url = articleListUrl + "search.clubid=" + cafeId + "&search.menuid=" + "&search.page=" + page;
        else if (mid == -1)
            url = popularArticleListUrl + "cafeId=" + cafeId;
        else
            url = articleListUrl + "search.clubid=" + cafeId + "&search.menuid=" + mid + "&search.page=" + page;

        document = httpRequest(url, Connection.Method.GET, false, true).parse();

        //parse article list
        /*CategoryManager category = CategoryManager.getInstance();
        int catType = CategoryManager.TYPE_BOARD;
        try {
            catType = (Integer) category.getParam(mid, CategoryManager.TYPE);
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }*/
        if (mid == -1) { // 인기글
            //TODO:인기글 파싱
        } else { // 전체글보기, 일반게시판
            Element articleTable = document.getElementsByClass("list_area").first();
            articles = articleTable.getElementsByClass("board_box"); // 게시글 추출

            Log.i("Web", "" + articles.size() + " article(s) found from " + page + " page. (" + url + ")" + " on Web.getArticleElements");
        }
        return articles;
    }

    /**
     * http request로 전송할 cookie를 설정합니다.
     *
     * @return Connection
     */
    private static Connection putHeader(Connection connection) {
        if (!cookie.containsKey("JSESSIONID")) // 최초 접속인지 확인
            resetCookies();

        return connection.cookies(cookie).headers(getHeader());
    }

    /**
     * http response로부터 전달받은 cookie를 저장합니다.
     */
    private static void getCookies(Connection.Response response) {
        resetCookies();
        cookie.putAll(response.cookies());
        Log.i("Web", "Get cookies.");
        printMap(cookie);
    }

    /**
     * cookie를 초기화합니다.
     */
    private static void resetCookies() {
        cookie.clear();
    }

    /**
     * http request에 필요한 header와 cookie를 반환합니다.
     *
     * @return header
     */
    private static Map getHeader() {
        if (!header.containsKey("user-agent")) {
            header.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            header.put("accept-encoding", "gzip, deflate, br");
            header.put("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            header.put("sec-fetch-mode", "navigate");
            header.put("sec-fetch-site", "none");
            header.put("sec-fetch-user", "?1");
            header.put("upgrade-insecure-requests", "1");
        }
        return header;
    }

    /**
     * Map 의 데이터를 모두 디버그 메세지로 표시합니다.
     *
     * @param map 표시할 Map
     */
    private static void printMap(Map<String, String> map) {
        for (Map.Entry<String, String> elem : map.entrySet()) {

            String key = elem.getKey();
            String value = elem.getValue();

            Log.i("Web", key + " : " + value);
        }
    }

    /**
     * 왁구텽의 트위치 방송이 켜져있는지 확인하고 그 여부를 반환합니다.
     *
     * @return 뱅온 여부
     */
    public static boolean getTwitchLive() {
        boolean live = false;
        String twitchChannel;
        CategoryManager category = CategoryManager.getInstance();
        try {
            twitchChannel = "woowakgood";
            Document twitchLive = httpRequest("https://twitch.tv/" + twitchChannel, Connection.Method.GET, false, false).parse();
            Log.v("Twitch Live test", "" + twitchLive.text());

            live = twitchLive.text().contains(" Playing ") && !twitchLive.text().contains(" hosting ");
        } catch (IOException e) {
            Log.w("Web.err", "통신 에러(" + "woowakgood" + ") on Web.getTwitchLive");
            e.printStackTrace();
        }

        Log.i("Web", "woowakgood" + ":" + live + " on Web.getTwitchLive");
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
                try {
                    document = httpRequest(articleHref, Connection.Method.GET, false, true).parse();

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
     * Singleton
     *
     * @return
     */
    public Web getInstance() {
        return instance;
    }
}

