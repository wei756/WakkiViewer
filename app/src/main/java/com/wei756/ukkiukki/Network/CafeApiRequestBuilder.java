package com.wei756.ukkiukki.Network;

import android.content.Context;
import android.util.Log;

import com.wei756.ukkiukki.Article;
import com.wei756.ukkiukki.CategoryManager;
import com.wei756.ukkiukki.Item;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class CafeApiRequestBuilder {

    public final static String cafeId = Web.cafeId;

    public final static String cafeApiHostUrl = "https://apis.naver.com/cafe-web/",
            cafeApiArticleListUrl = "cafe2/ArticleList.json?search.clubid={0}&search.menuid={1}&search.pageLastArticleId={2}&search.queryType=lastArticleId",
            cafeApiPopularArticleListUrl = "cafe2/WeeklyPopularArticleList.json?cafeId={0}",
            cafeApiNoticeListUrl = "cafe2/NoticeList.json?cafeId={0}&menuId={1}",
            profileArticleListUrl = "https://m.cafe.naver.com/CafeMemberArticleList.nhn?search.clubid={0}&search.writerid={1}&search.page={2}&search.perPage={3}",
            profileCommentListUrl = "https://m.cafe.naver.com/CafeMemberCommentList.nhn?search.clubid={0}&search.writerid={1}&search.page={2}&search.perPage={3}",
            profileCommentArticleListUrl = "https://m.cafe.naver.com/CafeMemberReplyList.nhn?search.clubid={0}&search.query={1}&search.page={2}&search.perPage={3}",
            profileLikeItArticleListUrl = "https://m.cafe.naver.com/CafeMemberLikeItList.nhn?search.cafeId={0}&search.memberId={1}&search.likeItTimestamp={2}&search.count={3}";

    public final static int REQUEST_ARTICLELIST_BOARD = 1,
            REQUEST_ARTICLELIST_POPULAR = CategoryManager.CATEGORY_POPULAR_ARTICLE,
            REQUEST_ARTICLELIST_NOTICE = CategoryManager.CATEGORY_NOTICE,
            REQUEST_PROFILE_ARTICLELIST = CategoryManager.CATEGORY_PROFILE_ARTICLE,
            REQUEST_PROFILE_COMMENTLIST = CategoryManager.CATEGORY_PROFILE_COMMENT,
            REQUEST_PROFILE_COMMENTARTICLELIST = CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE,
            REQUEST_PROFILE_LIEKITLIST = CategoryManager.CATEGORY_PROFILE_LIKEIT;

    Context context;
    int requestType;
    int mid, page;
    String userId, lastArticleId;
    long lastTimestamp;

    public CafeApiRequestBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public CafeApiRequestBuilder setRequestType(int requestType) {
        this.requestType = requestType;
        return this;
    }

    public CafeApiRequestBuilder setMid(int mid) {
        this.mid = mid;
        return this;
    }

    public CafeApiRequestBuilder setPage(int page) {
        this.page = page;
        return this;
    }

    public CafeApiRequestBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public CafeApiRequestBuilder setLastArticleId(String lastArticleId) {
        this.lastArticleId = lastArticleId;
        return this;
    }

    public CafeApiRequestBuilder setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
        return this;
    }

    public ArrayList<Item> build() {
        ParseUtils parseUtils = ParseUtils.getInstance();
        ArrayList<Item> arrayList = null;
        try {
            Elements articles = null;

            // Get document
            String url = null, response;
            switch (requestType) {
                case REQUEST_ARTICLELIST_BOARD:
                    url = cafeApiHostUrl + MessageFormat.format(cafeApiArticleListUrl, cafeId, mid != 0 ? mid : "", lastArticleId);
                    break;
                case REQUEST_ARTICLELIST_POPULAR:
                    url = cafeApiHostUrl + MessageFormat.format(cafeApiPopularArticleListUrl, cafeId);
                    break;
                case REQUEST_ARTICLELIST_NOTICE:
                    url = cafeApiHostUrl + MessageFormat.format(cafeApiNoticeListUrl, cafeId, "");
                    break;
                case REQUEST_PROFILE_ARTICLELIST:
                    url = MessageFormat.format(profileArticleListUrl, cafeId, userId, page, 30);
                    break;
                case REQUEST_PROFILE_COMMENTLIST:
                    url = MessageFormat.format(profileCommentListUrl, cafeId, userId, page, 30);
                    break;
                case REQUEST_PROFILE_COMMENTARTICLELIST:
                    url = MessageFormat.format(profileCommentArticleListUrl, cafeId, userId, page, 30);
                    break;
                case REQUEST_PROFILE_LIEKITLIST:
                    url = MessageFormat.format(profileLikeItArticleListUrl, cafeId, userId, "" + lastTimestamp, 30);
                    break;
            }

            response = WebRequestBuilder.create()
                    .url(url)
                    .method(WebRequestBuilder.METHOD_GET)
                    .userAgent(WebRequestBuilder.USER_AGENT_MOBILE)
                    .useCookie(true)
                    .buildWithoutElement();

            switch (requestType) {
                case REQUEST_ARTICLELIST_BOARD:
                case REQUEST_ARTICLELIST_POPULAR:
                case REQUEST_ARTICLELIST_NOTICE:
                    arrayList = parseUtils.parseArticleListJson(context, response); // throws NullPointerException
                    break;
                case REQUEST_PROFILE_ARTICLELIST:
                case REQUEST_PROFILE_COMMENTLIST:
                case REQUEST_PROFILE_COMMENTARTICLELIST:
                case REQUEST_PROFILE_LIEKITLIST:
                    arrayList = parseUtils.parseArticleListAjax(context, response); // throws NullPointerException
                    break;
            }

            Log.i("Web", "Article " + page + " page loaded. (mid=" + mid + "&page=" + page + ")" + " on Web.CafeApiRequestBuilder.build");

        } catch (IOException e) {
            Log.w("Web.err", "Connection error. (mid=" + mid + "&page=" + page + ")" + " on Web.CafeApiRequestBuilder.build");
            e.printStackTrace();

            arrayList.add(0, new Article().setErrorcode(Web.RETURNCODE_ERROR_CONNECTION));

        } catch (NullPointerException e) {
            Log.w("Web.err", "Error occurred. (mid=" + mid + "&page=" + page + ")" + " on Web.CafeApiRequestBuilder.build");
            e.printStackTrace();

            arrayList.add(0, new Article().setErrorcode(Web.RETURNCODE_FAILED));

        }

        return arrayList;
    }


}