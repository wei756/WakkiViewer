package com.wei756.ukkiukki.Network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.wei756.ukkiukki.Article;
import com.wei756.ukkiukki.ArticleList;
import com.wei756.ukkiukki.ArticleListAdapter;
import com.wei756.ukkiukki.ArticleViewerActivity;
import com.wei756.ukkiukki.ArticleViewerCommentListFragment;
import com.wei756.ukkiukki.CategoryManager;
import com.wei756.ukkiukki.Comment;
import com.wei756.ukkiukki.JsonUtil;
import com.wei756.ukkiukki.MainActivity;
import com.wei756.ukkiukki.ProfileManager;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.EntityBuilder;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.InputStreamBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.util.EntityUtils;


public class Web extends Thread {
    private static Web instance = null;

    public static final int RETURNCODE_SUCCESS = 0; // 성공
    public static final int RETURNCODE_ERROR_CONNECTION = -1; // 연결 에러
    public static final int RETURNCODE_ERROR_COMMENT_SPAM = -2; // 댓글 스팸 감지 에러
    public static final int RETURNCODE_ERROR_COMMENT_ENCODING = -3; // 댓글 인코딩 에러
    public static final int RETURNCODE_ERROR_LOGIN_REQUIRED = -4; // 로그인 요청 에러
    public static final int RETURNCODE_ERROR_COMMENT_BLANK = -5; // 내용 없는 댓글 에러

    private final String photoSessionKeyUrl = "https://m.cafe.naver.com/PhotoInfraSessionKey.nhn";

    private final String hostUrl = "https://m.cafe.naver.com/steamindiegame?",
            articleListUrl = "https://m.cafe.naver.com/ArticleListAjax.nhn?search.clubid={0}&search.menuid={1}&search.page={2}",
            popularArticleListUrl = "https://m.cafe.naver.com/PopularArticleList.nhn?cafeId={0}",
            menuListUrl = "https://m.cafe.naver.com/MenuListAjax.nhn?search.clubid={0}&search.perPage=9999",
            articleReadUrl = "https://m.cafe.naver.com/ArticleRead.nhn?clubid={0}&articleid={1}",
            articleCommentListUrl = "https://m.cafe.naver.com/CommentViewAjax.nhn?search.clubid={0}&search.articleid={1}&search.page={2}&search.orderby={3}",
            articleLikeItListUrl = "https://m.cafe.naver.com/LikeItMemberAjax.nhn?search.clubid={0}&search.articleid={1}",
            articleLikeItTokenUrl = "https://cafe.like.naver.com/v1/search/contents?suppress_response_codes=true&callback=&q=CAFE%5B{0}_steamindiegame_{1}%5D&isDuplication=true&_={2}",
            myActivityUrl = "https://cafe.naver.com/MyCafeMyActivityAjax.nhn?clubid={0}",
            profileArticleListUrl = "https://m.cafe.naver.com/CafeMemberArticleList.nhn?search.clubid={0}&search.writerid={1}&search.page={2}&search.perPage={3}",
            profileCommentListUrl = "https://m.cafe.naver.com/CafeMemberCommentList.nhn?search.clubid={0}&search.writerid={1}&search.page={2}&search.perPage={3}",
            profileCommentArticleListUrl = "https://m.cafe.naver.com/CafeMemberReplyList.nhn?search.clubid={0}&search.query={1}&search.page={2}&search.perPage={3}";

    private final String postCommentUrl = "https://m.cafe.naver.com/CommentPost.nhn?m=write",
            postCommentData = "clubid={0}&articleid={1}&content={2}&stickerId={3}&imagePath={4}&imageFileName={5}&imageWidth={6}&imageHeight={7}&showCafeHome=false",
            postCommentPhotoUrl = "https://cafe.upphoto.naver.com/{0}/simpleUpload/0?userId={1}&extractExif=false&extractAnimatedCnt=false&autorotate=true";
    private final String cafeId = "27842958";
    private final int timeout = 15000;

    private final WebClientManager webClientManager = WebClientManager.getInstance();


    private Web() {

    }

    /**
     * 프로필 정보를 불러옵니다.
     */
    public void loadMyInfomation() {
        new Thread() {
            public void run() {
                String url = MessageFormat.format(myActivityUrl, cafeId);
                Document document;
                ProfileManager profileManager = ProfileManager.getInstance();
                if (webClientManager.getLogined()) {
                    try {
                        document = WebRequestBuilder.create()
                                .url(url)
                                .method(WebRequestBuilder.METHOD_GET)
                                .userAgent(WebRequestBuilder.USER_AGENT_PC)
                                .useCookie(true)
                                .build();//httpRequest(url, METHOD_GET, true, true, null);

                        String id, nickname, profile;
                        String date, grade;
                        int visit, article, comment;

                        id = getStringValue(document.selectFirst("li[class=info2]").selectFirst("a[class=gm-tcol-c]").attr("href"), "memberid");
                        nickname = document.selectFirst("div[class=prfl_info]").text();
                        profile = document.selectFirst("img").attr("src");

                        date = document.selectFirst("li[class=date gm-tcol-c]").selectFirst("em").text();
                        grade = document.selectFirst("li[class=grade gm-tcol-c]").attr("title");

                        visit = Integer.parseInt(document.selectFirst("li[class=info]").selectFirst("em").text().replaceAll("[,회]", ""));
                        article = Integer.parseInt(document.selectFirst("li[class=info2]").selectFirst("em").selectFirst("a").text());
                        comment = Integer.parseInt(document.selectFirst("li[class=info3]").selectFirst("em").selectFirst("a").text());

                        Log.e("Web", "Profile-------------------");
                        Log.e("Web", "id: " + id + " 닉네임: " + nickname + " 프로필 사진 url: " + profile +
                                "\n 가입날짜: " + date + " 등급: " + grade +
                                "\n 방문수: " + visit + " 글: " + article + " 댓글: " + comment);


                        profileManager.setId(id);
                        profileManager.setNickname(nickname);
                        profileManager.setProfile(profile);
                        profileManager.setDate(date);
                        profileManager.setGrade(grade);

                        profileManager.setArticle(article);
                        profileManager.setComment(comment);
                        profileManager.setVisit(visit);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //TODO: 로그인 안되어있을 시
                }
                profileManager.setLogined(webClientManager.getLogined());
            }

        }.start();
    }

    /**
     * 게시판 목록을 불러옵니다.
     *
     * @see CategoryManager
     */
    public ArrayList<Map> loadCategoryList() {
        ArrayList arrayList = null;
        String url = MessageFormat.format(menuListUrl, cafeId);
        try {
            Elements categories = null;

            Document document;
            document = WebRequestBuilder.create()
                    .url(url)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_PC)
                    .useCookie(true)
                    .build();//httpRequest(url, METHOD_GET, true, true, null);

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

                        //Log.v("Web", "카테고리: 그룹: " + name);

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
                            try {
                                category1.setMenuId(getIntegerValue(menuId, "search.menuid"));
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }

                        //Log.v("Web", "카테고리: 게시판: " + name + "(id: " + menuId + ")");

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
        if (arrayList != null)
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
    public void loadArticleList(final ArticleList articleList, final int mid, final int page, final boolean reset) {
        new Thread() {
            public void run() {
                ArrayList arrayList = null;
                Elements articles = null;
                try {
                    articles = getArticleElements(mid, page);

                    arrayList = new ArrayList<>();

                    if (!articles.isEmpty()) { // 게시글이 있으면
                        for (Element article : articles) {

                            // 리스트 추가
                            Article article1 = new Article();
                            if (mid == CategoryManager.CATEGORY_POPULAR_ARTICLE) { // 인기글
                                // author
                                String author = article.selectFirst("div[class=user]").text();

                                article1.setAuthor(author)
                                        .setTitle(article.selectFirst("strong[class=tit ellip]").text())
                                        .setHref("" + getIntegerValue(article.selectFirst("a").attr("href"), "articleid"))
                                        .setLikeIt(article.selectFirst("em[class=u_cnt _count]").text())
                                        .setComment(article.selectFirst("span[class=coment_btn]").text());

                                // thumbnail
                                Element thumbnail = article.selectFirst("img[class=lazy]");
                                if (thumbnail != null) {
                                    article1.setThumbnailUrl(thumbnail.attr("data-original"));
                                }
                            } else if (mid == CategoryManager.CATEGORY_PROFILE_COMMENT) { // 프로필 작성댓글
                                // content
                                String time = article.selectFirst("span[class=info_item]").ownText();
                                String articleTitle = article.selectFirst("span[class=desc]").ownText();
                                String comment = "";
                                if (!articleTitle.equals("원글이 삭제된 게시글입니다."))
                                    comment = article.selectFirst("span[class=num]").text();

                                // content
                                Element contentElement = article.selectFirst("strong[class=txt]");
                                String content;
                                if (contentElement == null)
                                    content = "";
                                else {
                                    if (Build.VERSION.SDK_INT < 24)
                                        content = Html.fromHtml(contentElement.html()).toString();
                                    else
                                        content = Html.fromHtml(contentElement.html(), Html.FROM_HTML_MODE_COMPACT).toString();
                                }

                                article1.setContent(content)
                                        .setTime(time)
                                        .setArticleTitle(articleTitle)
                                        .setComment(comment)
                                        .setHref("" + getIntegerValue(article.selectFirst("a").attr("href"), "articleid"));

                            } else { // 전체글보기, 일반게시판
                                // author
                                String author = article.selectFirst("span[class=nick]").text();

                                article1.setAuthor(author)
                                        .setNewArticle(article.selectFirst("span[class=icon_new_txt]") != null)
                                        .setTitle(article.selectFirst("strong[class=tit]").text())
                                        .setHref("" + getIntegerValue(article.selectFirst("a").attr("href"), "articleid"))
                                        .setTime(article.selectFirst("span[class=time]").text())
                                        .setView(article.selectFirst("span[class=no]").text())
                                        .setComment(article.selectFirst("em[class=num]").text());

                                // thumbnail
                                if (article.selectFirst("div[class=thumb]") != null) {
                                    article1.setThumbnailUrl(article.selectFirst("div[class=thumb]").selectFirst("img").attr("src"));
                                }
                            }

                            // add
                            arrayList.add(article1);
                            //Log.e("Web", "제목: " + article1.getTitle());
                        }

                    } else {
                        throw new NullPointerException();
                    }
                    Log.i("Web", "Article " + page + " page loaded. (mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");

                    // callback
                    articleList.onLoadedArticleList(mid, arrayList, reset);

                } catch (IOException e) {
                    Log.w("Web.err", "Connection error. (mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
                    e.printStackTrace();

                    // callback with exception
                    articleList.onLoadedArticleList(mid, e, reset);

                } catch (NullPointerException e) {
                    Log.w("Web.err", "Error occurred. (mid=" + mid + "&page=" + page + ")" + " on Web.loadArticleList");
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
    public Elements getArticleElements(int mid, int page) throws IOException, NullPointerException {
        Elements articles = null;

        // connect
        Document document;
        String url;
        if (mid == CategoryManager.CATEGORY_POPULAR_ARTICLE)
            url = MessageFormat.format(popularArticleListUrl, cafeId);
        else if (mid == CategoryManager.CATEGORY_PROFILE_ARTICLE)
            url = MessageFormat.format(profileArticleListUrl, cafeId, ProfileManager.getInstance().getId(), page, 30);
        else if (mid == CategoryManager.CATEGORY_PROFILE_COMMENT)
            url = MessageFormat.format(profileCommentListUrl, cafeId, ProfileManager.getInstance().getId(), page, 30);
        else if (mid == CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE)
            url = MessageFormat.format(profileCommentArticleListUrl, cafeId, ProfileManager.getInstance().getId(), page, 30);
        else
            url = MessageFormat.format(articleListUrl, cafeId, mid != 0 ? mid : "", page);

        document = WebRequestBuilder.create()
                .url(url)
                .method(WebRequestBuilder.METHOD_GET)
                .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                .useCookie(true)
                .build();//httpRequest(url, METHOD_GET, false, true, null);

        //parse article list
        /*CategoryManager category = CategoryManager.getInstance();
        int catType = CategoryManager.TYPE_BOARD;
        try {
            catType = (Integer) category.getParam(mid, CategoryManager.TYPE);
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }*/
        if (mid == CategoryManager.CATEGORY_POPULAR_ARTICLE) { // 인기글
            //TODO:인기글 파싱
            Element articleTable = document.getElementsByClass("list_area").first();
            articles = articleTable.selectFirst("ul[id=listArea]").getElementsByClass("popularItem"); // 게시글 추출
        } else if (mid == CategoryManager.CATEGORY_PROFILE_ARTICLE
                || mid == CategoryManager.CATEGORY_PROFILE_COMMENT
                || mid == CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE) { // 프로필 작성글, 작성댓글, 댓글단 글
            articles = document.select("li"); // 게시글 추출
        } else { // 전체글보기, 일반게시판
            Element articleTable = document.getElementsByClass("list_area").first();
            articles = articleTable.getElementsByClass("board_box"); // 게시글 추출

            Log.i("Web", "" + articles.size() + " article(s) found from " + page + " page. (" + url + ")" + " on Web.getArticleElements");
        }
        return articles;
    }

    /**
     * Map 의 데이터를 모두 로그캣으로 표시합니다.
     *
     * @param map 표시할 Map
     */
    public void printMap(Map<String, String> map) {
        if (map != null)
            for (Map.Entry elem : map.entrySet()) {

                String key = "" + elem.getKey();
                String value = "" + elem.getValue();

                Log.d("Web", key + " : " + value);
            }
    }

    /**
     * 왁구텽의 트위치 방송이 켜져있는지 확인하고 그 여부를 반환합니다.
     *
     * @return 뱅온 여부
     */
    public boolean getTwitchLive() {
        boolean live = false;
        String twitchChannel;
        CategoryManager category = CategoryManager.getInstance();
        try {
            twitchChannel = "woowakgood";
            Document twitchLive = WebRequestBuilder.create()
                    .url("https://twitch.tv/" + twitchChannel)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(false)
                    .build();//httpRequest("https://twitch.tv/" + twitchChannel, METHOD_GET, false, false, null);
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
     * @param articleId             불러올 게시글의 id
     * @see ArticleViewerActivity
     */
    public void getArticle(final ArticleViewerActivity articleViewerActivity, final String articleId) {
        new Thread() {
            public void run() {
                String articleUrl = MessageFormat.format(articleReadUrl, cafeId, articleId);

                // get html
                Document document = null;
                try {
                    document = WebRequestBuilder.create()
                            .url(articleUrl)
                            .method(WebRequestBuilder.METHOD_GET)
                            .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                            .useCookie(true)
                            .build();//httpRequest(articleUrl, METHOD_GET, false, true, null);
                    Element error = document.selectFirst("div[class=error_content_body]");
                    boolean noerror = true;
                    if (error != null) {
                        String errorCode = error.selectFirst("h2").text();
                        Log.w("Web.err", "게시글을 불러오는 중 에러가 발생했습니다.(errorcode:" + errorCode + ")");

                        if (errorCode.equals("카페 멤버만 볼 수 있습니다.")) // 로그인 필요
                            throw new RequiresLoginException();
                        if (errorCode.equals("게시글이 존재하지 않거나 삭제되었습니다.")) // 존재하지 않는 게시글
                            noerror = false;
                    }

                    Article data = new Article();
                    if (noerror) { // 존재하는 게시글

                        // 컴포넌트 로드
                        Element header = document.selectFirst("div[class=post_title]");
                        Element body = document.selectFirst("div[id=postContent]");
                        Element comment = document.selectFirst("div[id=commentArea]");
                        Element footer = document.selectFirst("div[class=footer_fix]");

                        // mid
                        String mid = "" + getIntegerValue(header.selectFirst("a[class=border_name]").attr("href"), "search.menuid");


                        String title = header.selectFirst("h2[class=tit]").text();
                        String author = header.selectFirst("span[class=end_user_nick]").text();
                        String time = header.selectFirst("span[class=date font_l]").text();
                        String view = header.selectFirst("span[class=no font_l]").selectFirst("em").text();
                        String likeIt = footer.selectFirst("em[class=u_cnt _count]").text();

                        Elements elementsA = header.selectFirst("div[class=user_wrap]").select("a");
                        Element elementProfile = null;
                        for (Element a : elementsA) {
                            for (String className : a.classNames()) {
                                if (className.equals("thumb"))
                                    elementProfile = a;
                            }
                        }
                        String authorId = null, authorProfile = null;
                        if (elementProfile != null) {
                            authorProfile = elementProfile.selectFirst("img").attr("src");

                            for (String className : elementProfile.classNames())
                                if (className.contains("_click")) {
                                    Pattern p = Pattern.compile("MoveMemberProfile\\|([\\w_\\-]*)");
                                    Matcher m = p.matcher(className);
                                    while (m.find()) {
                                        authorId = m.group(1);
                                    }
                                }
                        }
                        //Log.e("Web", "authorid: " + authorId + "\n" +
                        //        "authorProfile: " + authorProfile);

                        // 데이터 입력
                        data.setTitle(title)
                                .setMid(mid)
                                .setAuthor(author)
                                .setAuthorProfile(authorProfile)
                                .setAuthorId(authorId)
                                .setTime(time)
                                .setView(view)
                                .setLikeIt(likeIt)
                                .setHref(articleId)
                                .setBody(body)
                                .setFeedback(comment);

                        Log.i("Web", " article(s) found from page. (" + articleId + ")" + " on Web.getArticle");

                    } else
                        data = null; // null 처리
                    // callback
                    articleViewerActivity.onLoadArticle(data);
                } catch (IOException e) {
                    Log.w("Web.err", "Connection error. (" + articleUrl + ")" + " on Web.getArticle");
                    e.printStackTrace();

                    // callback
                    articleViewerActivity.onLoadArticle(null);
                } catch (RequiresLoginException e) {
                    Log.w("Web.err", "Require login. (" + articleUrl + ")" + " on Web.getArticle");
                    e.printStackTrace();

                    // callback
                    articleViewerActivity.onLoadArticle(null);
                }
            }
        }.start();
    }

    /**
     * 좋아요 리스트를 불러옵니다.
     *
     * @param articleId 불러올 좋아요 리스트의 게시글 id
     * @see ArticleViewerActivity
     */
    public Map getArticleLikeItList(final String articleId) {

        String articleUrl = MessageFormat.format(articleLikeItListUrl, cafeId, articleId);

        // get html
        Document document = null;
        Map likeItMap;
        try {
            document = WebRequestBuilder.create()
                    .url(articleUrl)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .build();//httpRequest(articleUrl, METHOD_GET, false, true, null);

            // String to JSON
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            JSONObject jsonObj = (JSONObject) parser.parse(document.text());

            likeItMap = JsonUtil.getMapFromJsonObject(jsonObj);

            // callback
            return likeItMap;
        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (" + articleUrl + ")" + " on Web.getArticleLikeItList");
            e.printStackTrace();

            // callback
            return null;
        } catch (ParseException e) {
            Log.w("Web.err", "JSON Parse error. (" + articleUrl + ")" + " on Web.getArticleLikeItList");
            e.printStackTrace();

            // callback
            return null;
        }
    }

    /**
     * 댓글 리스트를 불러옵니다.
     *
     * @param articleId 불러올 댓글 리스트의 게시글 id
     * @see ArticleViewerActivity
     */
    @SuppressWarnings("deprecation")
    public ArrayList<Comment> getArticleCommentList(final String articleId, final int page, final String orderby) {

        String articleUrl = MessageFormat.format(articleCommentListUrl, cafeId, articleId, page, orderby);
        Log.e("Web.eeeeeeeeeeee", articleUrl);

        // get html
        Document document = null;
        ArrayList<Comment> arrayList = null;
        try {
            document = WebRequestBuilder.create()
                    .url(articleUrl)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .build();//httpRequest(articleUrl, METHOD_GET, false, true, null);

            arrayList = new ArrayList<>();

            Elements comments = document.select("li");
            for (Element comment : comments) {
                String author = "", time = "", imgProfile = "",
                        contentText = "", contentImage = "", sticker = "";
                boolean iconNew = false, iconArticleAuthor = false;
                if (comment.classNames().contains("u_cbox_comment")) { // 댓글인 경우에만
                    Comment comment1 = new Comment();

                    imgProfile = comment.selectFirst("span[class=u_cbox_thumb]").selectFirst("img").attr("src");
                    author = comment.selectFirst("span[class=u_cbox_info_main]").selectFirst("a").text();
                    time = comment.selectFirst("span[class=u_cbox_date]").text();

                    // 댓글 내용
                    Element elementContentText = comment.selectFirst("div[class=u_cbox_text_wrap]");
                    if (Build.VERSION.SDK_INT < 24)
                        contentText = Html.fromHtml(elementContentText.html()).toString();
                    else
                        contentText = Html.fromHtml(elementContentText.html(), Html.FROM_HTML_MODE_COMPACT).toString();

                    // 댓글 이미지/스티커
                    Element elementSticker = comment.selectFirst("div[class=u_cbox_sticker_section]"),
                            elementImage = comment.selectFirst("div[class=u_cbox_image_section]");
                    if (elementSticker != null) // 스티커
                        sticker = elementSticker.selectFirst("img").attr("src");
                    if (elementImage != null) // 이미지
                        contentImage = elementImage.selectFirst("img").attr("src");

                    iconNew = comment.selectFirst("span[class=u_cbox_ico_new]") != null;
                    iconArticleAuthor = comment.selectFirst("span[class=u_cbox_ico_author]") != null;

                    // 데이터 입력
                    comment1.setAuthor(author)
                            .setTime(time)
                            .setContent(contentText)
                            .setContentImage(contentImage)
                            .setSticker(sticker)
                            .setImgProfile(imgProfile)
                            .setIconNew(iconNew)
                            .setIconArticleAuthor(iconArticleAuthor);

                    // 대댓글 관련
                    int indent = 0;
                    if (comment.classNames().contains("re")) // 대댓글이면
                        indent = 1;
                    comment1.setIndentLevel(indent);
                    Element targetName = comment.selectFirst("span[class=u_cbox_target_name]");// 대상 닉네임
                    if (targetName != null)
                        comment1.setParentAuthor(targetName.text());

                    Log.e("Web", comment.className() + " " + indent);
                    // 본인댓글 여부
                    boolean mine = false;
                    mine = comment.child(0).className().contains("u_cbox_mine");
                    comment1.setMine(mine);

                    // 데이터 입력
                    arrayList.add(comment1);
                }
            }

            // 마지막 페이지인지 확인
            if (document.selectFirst("div[class=inner]") == null) {
                Log.e("Web", "마지막 페이지");
                arrayList.add(new Comment()
                        .setImgProfile(ArticleViewerCommentListFragment.LAST_PAGE));
            }

            // callback
            return arrayList;
        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (" + articleUrl + ")" + " on Web.getArticleCommentList");
            e.printStackTrace();

            // callback
            return null;
        }
    }

    /**
     * 댓글을 작성합니다.
     *
     * @param articleId 댓글 달 게시글 id
     * @param commentId 대댓글 달 댓글 id
     * @param content   댓글 내용
     * @see ArticleViewerActivity
     */
    public int postComment(final String articleId, final String commentId, final String content, @Nullable Map<String, String> photo) {
        String commentData = "";
        try {
            String strContent = URLEncoder.encode(content, "UTF-8");

            String imagePath = "", imageFileName = "", imageWidth = "", imageHeight = "";
            if (photo != null) { // 사진댓글
                imagePath = URLEncoder.encode(photo.get("path") + "/", "UTF-8");
                imageFileName = URLEncoder.encode(photo.get("fileName"), "UTF-8");
                imageWidth = photo.get("width");
                imageHeight = photo.get("height");
            }
            commentData = MessageFormat.format(postCommentData, cafeId, articleId, strContent, "", imagePath, imageFileName, imageWidth, imageHeight);
            Log.e("Web.eeeeeeeeeeee", commentData);

            // get html
            Document document = null;
            document = WebRequestBuilder.create()
                    .url(postCommentUrl)
                    .method(WebRequestBuilder.METHOD_POST)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .dataFormUrlencoded(commentData)
                    .build();//httpRequest(postCommentUrl, METHOD_POST, false, true, commentData);
            Log.e("Web.debug", document.html());

            Element elementError = document.selectFirst("div[class=error_content_body]");
            if (elementError != null) { // 에러 발생
                if (elementError.selectFirst("h2").text().contains("해당 댓글은 도배글로 판단되어 등록되지 않습니다.")) { // 도배 감지
                    Log.w("Web.err", "댓글 작성 중 에러 발생.(errorcode: 해당 댓글은 도배글로 판단되어 등록되지 않습니다.)");
                    return RETURNCODE_ERROR_COMMENT_SPAM;
                }
                if (elementError.selectFirst("h2").text().contains("내용이 없는 댓글 작성 시도입니다.")) { // 도배 감지
                    Log.w("Web.err", "댓글 작성 중 에러 발생.(errorcode: 내용이 없는 댓글 작성 시도입니다.)");
                    return RETURNCODE_ERROR_COMMENT_BLANK;
                }
            }

            // callback
            Log.i("Web", "댓글을 작성하였습니다.");
            return RETURNCODE_SUCCESS;
        } catch (UnsupportedEncodingException e) {
            Log.w("Web.err", "Encoding error on Web.postComment.");
            e.printStackTrace();

            // callback
            return RETURNCODE_ERROR_COMMENT_ENCODING;
        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (" + commentData + ")" + " on Web.postComment");
            e.printStackTrace();

            // callback
            return RETURNCODE_ERROR_CONNECTION;
        }
    }

    /**
     * 사진 업로드에 필요한 세션키를 받아옵니다.
     *
     * @return 세션키
     * @see Web#postCommentPhoto(Context, Uri)
     */
    public String getPhotoSessionKey() {
        try {
            Log.e("Web.eeeeeeeeeeee", photoSessionKeyUrl);

            // get html
            Document document = null;
            document = WebRequestBuilder.create()
                    .url(photoSessionKeyUrl)
                    .method(WebRequestBuilder.METHOD_POST)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .build();//httpRequest(photoSessionKeyUrl, METHOD_POST, false, true, null);

            // String to JSON
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            JSONObject jsonObj = (JSONObject) parser.parse(document.text());
            Map sessionMap = JsonUtil.getMapFromJsonObject(jsonObj);

            String sessionKey = null;
            if (sessionMap.containsKey("message"))
                return (String) ((Map) sessionMap.get("message")).get("result");
            else
                return "" + RETURNCODE_ERROR_CONNECTION;

        } catch (IOException e) {
            Log.w("Web.err", "Connection error on Web.getPhotoSessionKey.");
            e.printStackTrace();

            // callback
            return "" + RETURNCODE_ERROR_CONNECTION;
        } catch (ParseException e) {
            Log.w("Web.err", "JSON Parse error on Web.getPhotoSessionKey.");
            e.printStackTrace();

            // callback
            return "" + RETURNCODE_ERROR_CONNECTION;
        }
    }

    /**
     * 사진을 네이버 카페 서버에 업로드합니다.
     *
     * @return returncode
     */
    public Map<String, String> postCommentPhoto(Context context, Uri uri) {
        Map<String, String> map = new HashMap<>();
        try {
            if (!webClientManager.getLogined()) { // 로그인이 안 되어 있을 시
                map.put("returncode", "" + RETURNCODE_ERROR_LOGIN_REQUIRED);
                return map;
            }

            String sessionKey = getPhotoSessionKey();
            Log.e("Web.debug", "SessionKey: " + sessionKey);
            String userId = ProfileManager.getInstance().getId();

            if (!sessionKey.equals("" + RETURNCODE_ERROR_CONNECTION)) { // 세션 키를 불러오는 데 성공했다면

                String postPhotoUrl = MessageFormat.format(postCommentPhotoUrl, sessionKey, userId);
                Log.e("Web.debug", "Request URL: " + postPhotoUrl);

                // get html
                Document document = null;
                document = WebRequestBuilder.create()
                        .url(postPhotoUrl)
                        .method(WebRequestBuilder.METHOD_POST)
                        .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                        .useCookie(true)
                        .dataMultipartUri(context, uri)
                        .build();//postPhotoToNaver(context, postPhotoUrl, uri);
                Log.e("Web.debug", "Response: " + document.html());

                map.put("returncode", "" + RETURNCODE_SUCCESS);
                // put response
                if (document.selectFirst("item") != null) { // success
                    map.put("isSuccess", "true");
                    map.put("url", document.selectFirst("url").text());
                    map.put("path", document.selectFirst("path").text());
                    map.put("fileName", document.selectFirst("fileName").text());
                    map.put("width", document.selectFirst("width").text());
                    map.put("height", document.selectFirst("height").text());
                    map.put("fileSize", document.selectFirst("fileSize").text());
                    map.put("thumbnail", document.selectFirst("thumbnail").text());


                }
                if (document.selectFirst("result") != null) { // fail
                    map.put("isSuccess", "false");
                    map.put("code", document.selectFirst("code").text());
                    map.put("cause", document.selectFirst("cause").text());

                }
            } else {
                map.put("returncode", "" + RETURNCODE_ERROR_CONNECTION);
            }

        } catch (IOException e) {
            Log.w("Web.err", "Connection error on Web.postCommentPhoto.");
            e.printStackTrace();

            map.put("returncode", "" + RETURNCODE_ERROR_CONNECTION);
        } finally {
            // callback
            return map;
        }
    }


    /**
     * 로그인 세션을 cookie에 저장합니다
     *
     * @param cookies
     */
    public void applyLoginSession(Map<String, String> cookies) {
        webClientManager.setCookiesMap(cookies);
    }

    /**
     * 로그아웃시켜 세션을 만료합니다.
     */
    public int logoutLoginSession() {
        try {
            Document document = WebRequestBuilder.create()
                    .url("https://nid.naver.com/nidlogin.logout")
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .build();//httpRequest("https://nid.naver.com/nidlogin.logout", METHOD_GET, false, true, null);
            webClientManager.setLogined(false);
            ProfileManager.getInstance().setLogined(false);
            return RETURNCODE_SUCCESS;

        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (logout)" + " on Web.logoutLoginSession");
            e.printStackTrace();
            return RETURNCODE_ERROR_CONNECTION;
        }
    }

    /**
     * url 끝에 포함된 paramter값을 추출합니다
     */
    private static Integer getIntegerValue(String str, String valName) throws NullPointerException {
        Pattern p = Pattern.compile(valName + "=([0-9]*)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    /**
     * url 끝에 포함된 paramter값을 추출합니다
     */
    private static String getStringValue(String str, String valName) throws NullPointerException {
        Pattern p = Pattern.compile(valName + "=([\\w-]*)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Singleton
     */
    public static Web getInstance() {
        if (instance == null) {
            instance = new Web();
        }
        return instance;
    }
}

class RequiresLoginException extends Exception {
    RequiresLoginException() {
        super();
    }
}

