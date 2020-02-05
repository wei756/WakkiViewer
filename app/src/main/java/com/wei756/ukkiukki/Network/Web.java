package com.wei756.ukkiukki.Network;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wei756.ukkiukki.Article;
import com.wei756.ukkiukki.ArticleList;
import com.wei756.ukkiukki.ArticleListAdapter;
import com.wei756.ukkiukki.ArticleViewerActivity;
import com.wei756.ukkiukki.ArticleViewerCommentListFragment;
import com.wei756.ukkiukki.CategoryManager;
import com.wei756.ukkiukki.Comment;
import com.wei756.ukkiukki.Item;
import com.wei756.ukkiukki.JsonUtil;
import com.wei756.ukkiukki.MainActivity;
import com.wei756.ukkiukki.Preference.DBHandler;
import com.wei756.ukkiukki.ProfileManager;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Web extends Thread {
    private static Web instance = null;

    public static final int RETURNCODE_SUCCESS = 0; // 성공
    public static final int RETURNCODE_FAILED = -100; // 실패

    public static final int RETURNCODE_ERROR_CONNECTION = -1; // 연결 에러
    public static final int RETURNCODE_ERROR_COMMENT_SPAM = -2; // 댓글 스팸 감지 에러
    public static final int RETURNCODE_ERROR_COMMENT_ENCODING = -3; // 댓글 인코딩 에러
    public static final int RETURNCODE_ERROR_LOGIN_REQUIRED = -4; // 로그인 요청 에러
    public static final int RETURNCODE_ERROR_COMMENT_BLANK = -5; // 내용 없는 댓글 에러

    public static final int RETURNCODE_ERROR_LIKEIT_TOKEN_EXPIRED = -4012; // 토큰 만료
    public static final int RETURNCODE_ERROR_ALREADY_LIKED = -4091; // 이미 좋아요 한 게시글
    public static final int RETURNCODE_ERROR_NOT_LIKED = -4091; // 좋아요하지 않은= 게시글

    public final static String photoSessionKeyUrl = "https://m.cafe.naver.com/PhotoInfraSessionKey.nhn";

    public final static String hostUrl = "https://m.cafe.naver.com/steamindiegame?",
            articleListUrl = "https://m.cafe.naver.com/ArticleListAjax.nhn?search.clubid={0}&search.menuid={1}&search.page={2}",
            popularArticleListUrl = "https://apis.naver.com/cafe-web/cafe2/WeeklyPopularArticleList.json?cafeId={0}",
            menuListUrl = "https://m.cafe.naver.com/MenuListAjax.nhn?search.clubid={0}&search.perPage=9999",
            articleReadUrl = "https://apis.naver.com/cafe-web/cafe-articleapi/cafes/{0}/articles/{1}",
            articleCommentListUrl = "https://m.cafe.naver.com/CommentViewAjax.nhn?search.clubid={0}&search.articleid={1}&search.page={2}&search.orderby={3}",
            articleLikeItListUrl = "https://m.cafe.naver.com/LikeItMemberAjax.nhn?search.clubid={0}&search.articleid={1}",
            articleLikeItTokenUrl = "https://cafe.like.naver.com/v1/search/contents?suppress_response_codes=true&q=CAFE%5B{0}_steamindiegame_{1}%5D&isDuplication=true&_={2}",
            myActivityUrl = "https://cafe.naver.com/MyCafeMyActivityAjax.nhn?clubid={0}",
            profileUrl = "https://m.cafe.naver.com/CafeMemberProfile.nhn?cafeId={0}&memberId={1}",
            profileArticleListUrl = "https://m.cafe.naver.com/CafeMemberArticleList.nhn?search.clubid={0}&search.writerid={1}&search.page={2}&search.perPage={3}",
            profileCommentListUrl = "https://m.cafe.naver.com/CafeMemberCommentList.nhn?search.clubid={0}&search.writerid={1}&search.page={2}&search.perPage={3}",
            profileCommentArticleListUrl = "https://m.cafe.naver.com/CafeMemberReplyList.nhn?search.clubid={0}&search.query={1}&search.page={2}&search.perPage={3}",
            profileLikeItArticleListUrl = "https://m.cafe.naver.com/CafeMemberLikeItList.nhn?search.cafeId={0}&search.memberId={1}&search.likeItTimestamp={2}&search.count={3}";



    public final static String postCommentUrl = "https://m.cafe.naver.com/CommentPost.nhn?m=write",
            postCommentData = "clubid={0}&articleid={1}&content={2}&stickerId={3}&imagePath={4}&imageFileName={5}&imageWidth={6}&imageHeight={7}&showCafeHome=false",
            postCommentPhotoUrl = "https://cafe.upphoto.naver.com/{0}/simpleUpload/0?userId={1}&extractExif=false&extractAnimatedCnt=false&autorotate=true",
            likeItUrl = "https://cafe.like.naver.com/v1/services/CAFE/contents/{1}_steamindiegame_{2}?suppress_response_codes=true&_method={0}&displayId=CAFE&reactionType=like&categoryId={1}&guestToken={3}&timestamp={4}&_ch=mbw&isDuplication=true&lang=ko";
    public final static String LIKE = "POST",
            UNLIKE = "DELETE";
    public final static String cafeId = "27842958";
    public final static int timeout = 15000;

    private final WebClientManager webClientManager = WebClientManager.getInstance();


    private Web() {

    }

    /**
     * 프로필 정보를 불러옵니다.
     */
    public void loadMyInfomation() {
        new Thread(() -> {
            ProfileManager profileManager = ProfileManager.getInstance();
            String url = MessageFormat.format(myActivityUrl, cafeId);
            Document document;
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

                    id = ParseUtils.getStringValue(document.selectFirst("li[class=info2]").selectFirst("a[class=gm-tcol-c]").attr("href"), "memberid");
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

        }).start();
    }

    public Map getProfile(String userId) {
        Map mapUser = new HashMap();

        ProfileManager profileManager = ProfileManager.getInstance();
        String url = MessageFormat.format(profileUrl, cafeId, userId);
        Document document;
        if (profileManager.isLogined()) {
            try {
                document = WebRequestBuilder.create()
                        .url(url)
                        .method(WebRequestBuilder.METHOD_GET)
                        .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                        .useCookie(true)
                        .build();

                String id, nickname, profile;
                String date, grade;
                int visit, article, comment;
                Element elementProfile = document.selectFirst("div[class=profile_head]");
                if (elementProfile != null) {

                    id = userId;
                    nickname = elementProfile.selectFirst("strong[class=nick]").selectFirst("span").text();
                    profile = document.selectFirst("img").attr("src");

                    date = "";
                    grade = elementProfile.selectFirst("div[class=desc]").textNodes().get(0).text();

                    visit = Integer.parseInt(elementProfile.selectFirst("div[class=desc]").textNodes().get(1).text().replaceAll("[^\\d]", ""));
                    article = -1;
                    comment = -1;

                    Log.e("Web", "Profile-------------------");
                    Log.e("Web", "id: " + id + " 닉네임: " + nickname + " 프로필 사진 url: " + profile +
                            "\n 가입날짜: " + date + " 등급: " + grade +
                            "\n 방문수: " + visit + " 글: " + article + " 댓글: " + comment);


                    mapUser.put("id", id);
                    mapUser.put("nickname", nickname);
                    mapUser.put("profile", profile);
                    mapUser.put("date", date);
                    mapUser.put("grade", grade);

                    mapUser.put("article", article);
                    mapUser.put("comment", comment);
                    mapUser.put("visit", visit);
                    mapUser.put("status", RETURNCODE_SUCCESS);
                }

            } catch (IOException e) {
                e.printStackTrace();
                mapUser.put("status", RETURNCODE_FAILED);
            }
        } else {
            mapUser.put("status", RETURNCODE_ERROR_LOGIN_REQUIRED);
        }
        return mapUser;
    }

    /**
     * 게시판 목록을 불러옵니다.
     *
     * @see CategoryManager
     */
    public ArrayList<Map> getCategoryList() {
        ArrayList arrayList = null;
        String url = MessageFormat.format(menuListUrl, cafeId);
        int i = 0;
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
            Log.i("Web", "" + categories.size() + " category block(s) found. on Web.getCategoryList");


            if (categories != null) { // 카테고리가 있으면
                arrayList = new ArrayList<>();

                for (Element category : categories) {
                    CategoryManager.CategoryBuilder categoryBuilder = new CategoryManager.CategoryBuilder();
                    if (category.selectFirst("h4") != null) { // 그룹

                        String name = category.selectFirst("h4").text();
                        categoryBuilder.setType(CategoryManager.TYPE_GROUP)
                                .setName(name);

                        //Log.v("Web", "카테고리: 그룹: " + name);

                    } else if (category.selectFirst("div") != null) { // 카테고리
                        Element a = category.selectFirst("a");
                        String name = a.text();
                        categoryBuilder.setName(name);

                        // 카테고리 타입
                        switch (name) {
                            case "전체글보기":
                                categoryBuilder.setType(CategoryManager.TYPE_MAINPAGE);
                                break;
                            case "인기글":
                                categoryBuilder.setType(CategoryManager.TYPE_BEST);
                                break;
                            case "▩ 표 지 왕 ▩":
                                categoryBuilder.setType(CategoryManager.TYPE_THUMBKING);
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
                                categoryBuilder.setType(CategoryManager.TYPE_BOARD);
                                break;
                        }

                        String menuId = a.attr("href");
                        if (menuId.contains("PopularArticleList.nhn")) {
                            categoryBuilder.setMenuId(CategoryManager.CATEGORY_POPULAR_ARTICLE);
                        } else if (menuId.contains("PopularMemberList.nhn")) {
                            categoryBuilder.setMenuId(CategoryManager.CATEGORY_POPULAR_MEMBER);
                        } else if (menuId.contains("WeeklyPopularTagList.nhn")) {
                            categoryBuilder.setMenuId(CategoryManager.CATEGORY_TAG);
                        } else {
                            try {
                                categoryBuilder.setMenuId(ParseUtils.getIntegerValue(menuId, "search.menuid"));
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }

                        //Log.v("Web", "카테고리: 게시판: " + name + "(id: " + menuId + ")");

                    } else if (category.selectFirst("span[class=blind]") != null) { // 구분선

                        categoryBuilder.setName("구분선");

                        // 카테고리 타입
                        categoryBuilder.setType(CategoryManager.TYPE_CONTOUR);
                        Log.v("Web", "카테고리: 구분선");
                    } else {
                        continue;
                    }

                    // 리스트 추가
                    arrayList.add(categoryBuilder.build());

                    // 디버그용 index
                    i++;
                }

            } else {
                throw new NullPointerException();
            }

        } catch (IOException e) {
            arrayList = null;
            Log.w("Web.err", "Connection error. (" + url + ")" + " on Web.getCategoryList");
            e.printStackTrace();

        } catch (NullPointerException e) {
            arrayList = null;
            Log.w("Web.err", "Error occurred in " + i + ". (" + url + ")" + " on Web.getCategoryList");
            e.printStackTrace();
        }
        if (arrayList != null)
            Log.i("Web", "" + arrayList.size() + " category(s) found. (" + url + ")" + " on Web.getCategoryList");
        return arrayList;
    }

    /**
     * 게시판의 글목록을 불러옵니다.
     *
     * @param context Context
     * @param mid     게시판
     * @param page    페이지
     * @param userId  사용자 id (프로필 페이지에서만 사용)
     * @see Web#getArticleElements(int, int, String)
     * @see ArticleList
     * @see com.wei756.ukkiukki.ProfileActivity
     */
    @Deprecated
    public ArrayList<Article> getArticleList(Context context, int mid, int page, @Nullable String userId) {
        ArrayList<Article> arrayList = null;
        Elements articles = null;
        try {
            articles = getArticleElements(mid, page, userId);

            arrayList = new ArrayList<>();
            DBHandler dbHandler = DBHandler.open(context, DBHandler.DB_ARTICLE); // for check already article

            if (!articles.isEmpty()) { // 게시글이 있으면
                for (Element article : articles) {

                    // 리스트 추가
                    Article article1 = new Article();
                    if (mid == CategoryManager.CATEGORY_POPULAR_ARTICLE) { // 인기글
                        // author
                        String author = article.selectFirst("div[class=user]").text();

                        article1.setAuthor(author)
                                .setTitle(article.selectFirst("strong[class=tit ellip]").text())
                                .setHref("" + ParseUtils.getIntegerValue(article.selectFirst("a").attr("href"), "articleid"))
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
                                .setHref("" + ParseUtils.getIntegerValue(article.selectFirst("a").attr("href"), "articleid"));

                    } else { // 전체글보기, 일반게시판
                        // author
                        String author = article.selectFirst("span[class=nick]").text();

                        article1.setAuthor(author)
                                .setNewArticle(article.selectFirst("span[class=icon_new_txt]") != null)
                                .setTitle(article.selectFirst("strong[class=tit]").text())
                                .setHref("" + ParseUtils.getIntegerValue(article.selectFirst("a").attr("href"), "articleid"))
                                .setTime(article.selectFirst("span[class=time]").text())
                                .setView(article.selectFirst("span[class=no]").text())
                                .setComment(article.selectFirst("em[class=num]").text());

                        // thumbnail
                        if (article.selectFirst("div[class=thumb]") != null) {
                            article1.setThumbnailUrl(article.selectFirst("div[class=thumb]").selectFirst("img").attr("src"));
                        }

                        // check already read
                        Cursor cursor = dbHandler.select();
                        Article oldArticle = DBHandler.getArticle(dbHandler.select(), article1.getHref());
                        if (oldArticle != null) {
                            article1.setReadArticle(oldArticle.isReadArticle())
                                    .setNewComment(!oldArticle.getComment().equals(article1.getComment()));
                        }
                        cursor.close();

                        // timestamp for likeit articles
                        if (mid == CategoryManager.CATEGORY_PROFILE_LIKEIT) { // 좋아요한 글
                            article1.setTimestamp(article.attr("data-timestamp"));
                        }
                    }

                    // add
                    arrayList.add(article1);
                    //Log.e("Web", "제목: " + article1.getTitle());
                }

            }
            dbHandler.close();
            Log.i("Web", "Article " + page + " page loaded. (mid=" + mid + "&page=" + page + ")" + " on Web.getArticleList");

        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (mid=" + mid + "&page=" + page + ")" + " on Web.getArticleList");
            e.printStackTrace();

            arrayList.add(0, new Article().setErrorcode(RETURNCODE_ERROR_CONNECTION));

        } catch (NullPointerException e) {
            Log.w("Web.err", "Error occurred. (mid=" + mid + "&page=" + page + ")" + " on Web.getArticleList");
            e.printStackTrace();

            arrayList.add(0, new Article().setErrorcode(RETURNCODE_FAILED));

        }

        return arrayList;
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
     * @see Web#getArticleList(Context, int, int, String)
     */
    @Deprecated
    public Elements getArticleElements(int mid, int page, @Nullable String userId) throws IOException, NullPointerException {
        Elements articles = null;

        // connect
        Document document;
        String url;
        if (mid == CategoryManager.CATEGORY_POPULAR_ARTICLE) // 인기글
            url = MessageFormat.format(popularArticleListUrl, cafeId);
        else if (mid == CategoryManager.CATEGORY_PROFILE_ARTICLE) // 프로필 작성 게시글
            url = MessageFormat.format(profileArticleListUrl, cafeId, userId, page, 30);
        else if (mid == CategoryManager.CATEGORY_PROFILE_COMMENT) // 프로필 작성 댓글
            url = MessageFormat.format(profileCommentListUrl, cafeId, userId, page, 30);
        else if (mid == CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE) // 프로필 댓글단 글
            url = MessageFormat.format(profileCommentArticleListUrl, cafeId, userId, page, 30);
        else if (mid == CategoryManager.CATEGORY_PROFILE_LIKEIT) // 프로필 좋아요한 글
            url = MessageFormat.format(profileLikeItArticleListUrl, cafeId, userId, "" + ((long) page * (long) 1000), 30);
        else
            url = MessageFormat.format(articleListUrl, cafeId, mid != 0 ? mid : "", page);
        Log.e("asdf", url);

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
                || mid == CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE
                || mid == CategoryManager.CATEGORY_PROFILE_LIKEIT) { // 프로필 작성글, 작성댓글, 댓글단 글, 좋아요한 글
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
        new Thread(() -> {
            String articleUrl = MessageFormat.format(articleReadUrl, cafeId, articleId);

            // get html
            String document;
            try {
                document = WebRequestBuilder.create()
                        .url(articleUrl)
                        .method(WebRequestBuilder.METHOD_GET)
                        .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                        .useCookie(true)
                        .buildWithoutElement();

                // parse JSON
                Map mapArticle;

                // String to JSON
                JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
                JSONObject jsonObj = (JSONObject) parser.parse(document);

                mapArticle = JsonUtil.getMapFromJsonObject(jsonObj);

                // check error
                String errorCode = String.valueOf(mapArticle.get("reason"));
                boolean noerror = true;
                if (!errorCode.equals("null")) { // mapArticle.get("reason")이 null이 아니라면
                    Log.w("Web.err", "게시글을 불러오는 중 에러가 발생했습니다.(errorcode:" + errorCode + ")");
                    noerror = false;

                    if (errorCode.equals("카페 멤버만 읽을 수 있는 게시글입니다.")) // 로그인 필요
                        throw new RequiresLoginException();
                    //if (errorCode.equals("게시글이 존재하지 않거나 삭제되었습니다.")) // 존재하지 않는 게시글
                }

                // start parse
                Article data = new Article();
                if (noerror) { // 존재하는 게시글

                    // 주요 컴포넌트 로드
                    Map article = (Map) mapArticle.get("article");
                    ArrayList attaches = (ArrayList) mapArticle.get("attaches");
                    Map comments = (Map) mapArticle.get("comments");
                    Map advert = (Map) mapArticle.get("advert");
                    Map authority = (Map) mapArticle.get("authority");

                    // mid
                    String mid = String.valueOf(((Map) article.get("menu")).get("id"));


                    String title = (String) article.get("subject"); // 글 제목 // 제목이 실제로 null인 경우 회피
                    String head = String.valueOf(article.get("head")); // 말머리
                    String time = "" + (long) article.get("writeDate");
                    String view = "" + (int) article.get("readCount");
                    String likeIt = "0";

                    // 게시글 작성자
                    Map mapAuthor = (Map) article.get("writer");
                    String author = String.valueOf(mapAuthor.get("nick")); // 작성자 닉네임
                    String authorId = String.valueOf(mapAuthor.get("id")); // 작성자 아이디
                    String authorProfile = String.valueOf(((Map) mapAuthor.get("image")).get("url")); // 작성자 프로필 사진

                    //Log.e("Web", "authorid: " + authorId + "\n" +
                    //        "authorProfile: " + authorProfile);

                    // 게시글 본문 html태그 복원
                    /*
                    int begin = rawDocument.html().indexOf("\"content\":\""),
                            end = rawDocument.html().indexOf("\",\"contentElements\"", begin);
                    String htmlBody = rawDocument.html().substring(begin + 11, end);
                    article.put("content", htmlBody);*/

                    // 데이터 입력
                    data.setTitle(title)
                            .setHead(head)
                            .setMid(mid)
                            .setAuthor(author)
                            .setAuthorProfile(authorProfile)
                            .setAuthorId(authorId)
                            .setTime(time)
                            .setView(view)
                            .setLikeIt(likeIt)
                            .setHref(articleId)
                            .setArticle(article)
                            .setAttaches(attaches)
                            .setComments(comments)
                            .setAdvert(advert)
                            .setAuthority(authority);

                    Log.i("Web", " article(s) found from page. (" + articleId + ")" + " on Web.getArticle");

                } else
                    data = null; // null 처리
                // callback
                articleViewerActivity.onLoadArticle(data);
            } catch (ParseException e) {
                Log.w("Web.err", "JSON Parse error. (" + articleUrl + ")" + " on Web.getArticle");
                e.printStackTrace();

                // callback
                articleViewerActivity.onLoadArticle(null);
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
        ).start();
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
     * 좋아요/좋아요 취소를 누릅니다.
     *
     * @param type      좋아요/좋아요 취소 구분
     * @param articleId 좋아요 할 게시글 id
     * @see ArticleViewerActivity
     */
    public int likeIt(final String type, final String articleId, String guestToken, long timestamp) {

        String url = MessageFormat.format(likeItUrl, type, cafeId, articleId, guestToken, "" + timestamp);
        Log.e("Web.deeeebug", url);

        // get html
        Document document = null;
        Map response;
        try {
            document = WebRequestBuilder.create()
                    .url(url)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .addHeader("sec-fetch-mode", "no-cors")
                    .addHeader("sec-fetch-site", "same-site")
                    .addHeader("referer", MessageFormat.format("https://m.cafe.naver.com/CommentView.nhn?search.clubid={0}&search.articleid={1}&page=", cafeId, articleId))
                    .build();

            // String to JSON
            String jsonString = document.text();

            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            JSONObject jsonObj = (JSONObject) parser.parse(jsonString);

            response = JsonUtil.getMapFromJsonObject(jsonObj);
            if (response.containsKey("message") && response.get("message").equals("좋아요가 되었습니다.")) // 좋아요 성공
                return RETURNCODE_SUCCESS;
            if (response.containsKey("message") && response.get("message").equals("좋아요를 취소했습니다.")) // 좋아요 취소 성공
                return RETURNCODE_SUCCESS;
            else if (response.containsKey("errorCode")) {
                switch ((int) response.get("errorCode")) {
                    case 4012: // 토큰 만료
                        return RETURNCODE_ERROR_LIKEIT_TOKEN_EXPIRED;
                    case 4013: // 요청 실패
                        return RETURNCODE_FAILED;
                    case 4091: // 이미 좋아요한 게시글
                        return RETURNCODE_ERROR_ALREADY_LIKED;
                    case 4092: // 좋아요하지 않은 게시글
                        return RETURNCODE_ERROR_NOT_LIKED;
                }
            }
            return RETURNCODE_FAILED; // 알 수 없는 오류

        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (" + url + ")" + " on Web.likeIt");
            e.printStackTrace();

            // callback
            return RETURNCODE_ERROR_CONNECTION;
        } catch (ParseException e) {
            Log.w("Web.err", "JSON Parse error. (" + url + ")" + " on Web.likeIt");
            e.printStackTrace();

            // callback
            return RETURNCODE_ERROR_CONNECTION;
        }
    }

    /**
     * 좋아요를 위한 토큰을 생성합니다.
     *
     * @param articleId 불러올 좋아요 리스트의 게시글 id
     * @see ArticleViewerActivity
     */
    public Map getGuestToken(final String articleId) {

        String url = MessageFormat.format(articleLikeItTokenUrl, cafeId, articleId, "");

        // get html
        Document document = null;
        Map likeItMap;
        try {
            document = WebRequestBuilder.create()
                    .url(url)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .addHeader("sec-fetch-mode", "no-cors")
                    .addHeader("sec-fetch-site", "same-site")
                    .addHeader("referer", MessageFormat.format("https://m.cafe.naver.com/CommentView.nhn?search.clubid={0}&search.articleid={1}&page=", cafeId, articleId))
                    .build();

            // String to JSON
            String jsonString = document.text();
            //jsonString = jsonString.substring(5, jsonString.length() - 1);

            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            JSONObject jsonObj = (JSONObject) parser.parse(jsonString);

            likeItMap = JsonUtil.getMapFromJsonObject(jsonObj);
            //Log.e("Web.debbbbbbbbbbug", "token");
            //printMap(likeItMap);

            // callback
            return likeItMap;
        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (" + url + ")" + " on Web.getGuestToken");
            e.printStackTrace();

            // callback
            return null;
        } catch (ParseException e) {
            Log.w("Web.err", "JSON Parse error. (" + url + ")" + " on Web.getGuestToken");
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
        //Log.e("Web.eeeeeeeeeeee", articleUrl);

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
                String author, time, imgProfile,
                        contentText = "", contentImage = "", sticker = "";
                boolean iconNew, iconArticleAuthor;
                if (comment.classNames().contains("u_cbox_comment")) { // 댓글인 경우에만
                    Comment comment1 = new Comment();

                    if (comment.selectFirst("span[class=u_cbox_delete_contents]") == null) { // 삭제된 댓글이 아닌 경우

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
                                .setIconArticleAuthor(iconArticleAuthor)
                                .setDeleted(false);

                        // 대댓글 관련
                        int indent = 0;
                        if (comment.classNames().contains("re")) // 대댓글이면
                            indent = 1;
                        comment1.setIndentLevel(indent);
                        Element targetName = comment.selectFirst("span[class=u_cbox_target_name]");// 대상 닉네임
                        if (targetName != null)
                            comment1.setParentAuthor(targetName.text());

                        //Log.e("Web", comment.className() + " " + indent);
                        // 본인댓글 여부
                        boolean mine = false;
                        mine = comment.child(0).className().contains("u_cbox_mine");
                        comment1.setMine(mine);
                    } else {
                        comment1.setDeleted(true);
                    }

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
     * 웹브라우저로 링크를 엽니다.
     *
     * @param context
     * @param url
     */
    public static void openWebPageWithBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        context.startActivity(intent);
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

