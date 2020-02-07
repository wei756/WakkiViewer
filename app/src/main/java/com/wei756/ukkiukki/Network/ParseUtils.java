package com.wei756.ukkiukki.Network;


import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.wei756.ukkiukki.Article;
import com.wei756.ukkiukki.Item;
import com.wei756.ukkiukki.JsonUtil;
import com.wei756.ukkiukki.Preference.DBHandler;
import com.wei756.ukkiukki.Sticker;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtils {
    private static ParseUtils instance = null;

    private ParseUtils() {
    }
    //// 여기서부터 파싱 메서드

    /**
     * articleList, popularArticleList, mainNoticeList 에 사용
     *
     * @param context
     * @param JSONString
     * @return
     * @throws NullPointerException
     */
    public ArrayList<Item> parseArticleListJson(Context context, String JSONString) throws NullPointerException {
        ArrayList<Item> arrayList = new ArrayList<>();
        DBHandler dbHandler = DBHandler.open(context, DBHandler.DB_ARTICLE); // for check already article
        Map map = convertJsonToMap(JSONString);

        Map mapTmp;
        if (map != null) {
            mapTmp = (Map) map.get("message"); // json -> message

            String status = (String) mapTmp.get("status"); // json -> message -> status

            Map mapError = (Map) mapTmp.get("error"), // json -> message -> error
                    mapResult = (Map) mapTmp.get("result"); // json -> message -> result

            if ("200".equals(status)) { // 접근성공
                ArrayList listArticleList = (ArrayList) mapResult.get("articleList"); // REQUEST_ARTICLELIST_BOARD
                if (listArticleList == null)
                    listArticleList = (ArrayList) mapResult.get("popularArticleList"); // REQUEST_ARTICLELIST_POPULAR
                if (listArticleList == null)
                    listArticleList = (ArrayList) mapResult.get("mainNoticeList"); // REQUEST_ARTICLELIST_NOTICE

                ArrayList<Map> listArticle = listArticleList; // json -> message -> result -> articleList or popularArticleList or mainNoticeList

                for (Map mapArticle : listArticle) {
                    Article article = new Article(); // 게시글 객체 생성

                    article.setHref("" + mapArticle.get("articleId")) // 게시글 링크
                            .setTitle("" + mapArticle.get("subject")) // 게시글 제목
                            .setMid("" + mapArticle.get("menuId")) // 게시판 id
                            .setAuthorId("" + mapArticle.get("writerId")) // 게시글 작성자 id
                            .setNewArticle((boolean) mapArticle.get("newArticle")) // 새 게시글
                            .setView("" + mapArticle.get("readCount")) // 게시글 조회수
                            .setComment("" + mapArticle.get("commentCount")) // 게시글 댓글수
                            .setTimestamp("" + mapArticle.get("writeDateTimestamp")) // 게시글 타임스탬프
                            .setTime((String) mapArticle.get("aheadOfWriteDate")); // 게시글 작성시각

                    String nickname = (String) mapArticle.get("writerNickname");
                    article.setAuthor(nickname != null ? nickname : "" + mapArticle.get("nickname")); // 게시글 작성자 닉네임

                    String likeItCount = "" + mapArticle.get("likeItCount");
                    article.setLikeIt(likeItCount != null ? likeItCount : "" + mapArticle.get("upCount")); // 게시글 좋아요수

                    if (article.getTime() == null) { // formatted time이 제공되지 않았을 경우
                        Date dateNow = new Date(System.currentTimeMillis()), dateArticle = new Date(Long.parseLong(article.getTimestamp()));
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
                        String time;
                        if (format.format(dateNow)
                                .equals(format.format(dateArticle)))
                            time = new SimpleDateFormat("HH:mm", Locale.KOREA).format(dateArticle); // 같은 날
                        else
                            time = new SimpleDateFormat("yy.MM.dd.", Locale.KOREA).format(dateArticle); // 이전

                        article.setTime(time); // 시간
                    }

                    article.setHead((String) mapArticle.get("headName")); // 말머리
                    article.setThumbnailUrl((String) mapArticle.get("representImage")); // 썸네일

                    // 이미 읽은 글인지 여부, 새 댓글 여부
                    checkArticleIsNew(dbHandler, article);

                    arrayList.add(article);
                }
            } else {
                // 접근실패
            }
        }
        dbHandler.close();

        return arrayList;
    }

    /**
     * 스티커팩에 사용
     *
     * @param context
     * @param JSONString
     * @return
     * @throws NullPointerException
     */
    public ArrayList<Sticker> parseStickerPackListJson(Context context, String JSONString) throws NullPointerException {
        ArrayList<Sticker> arrayList = new ArrayList<>();
        Map map = convertJsonToMap(JSONString);

        if (map != null) {
            ArrayList<Map> listStickerPackList = (ArrayList) map.get("list");

            for (Map mapStickerPack : listStickerPackList) {
                Sticker stickerPack = new Sticker(); // 게시글 객체 생성

                stickerPack.setPackCode("" + mapStickerPack.get("packCode")) // 팩 코드
                        .setStickerCount((int) mapStickerPack.get("stickerCount")); // 스티커 갯수

                arrayList.add(stickerPack);
            }
        }

        return arrayList;
    }

    /**
     * 스티커에 사용
     *
     * @param context
     * @param JSONString
     * @return
     * @throws NullPointerException
     */
    public ArrayList<Sticker> parseStickerListJson(Context context, String JSONString) throws NullPointerException {
        ArrayList<Sticker> arrayList = new ArrayList<>();
        Map map = convertJsonToMap(JSONString);

        if (map != null) {
            ArrayList<Map> listStickerList = (ArrayList) map.get("list");

            for (Map mapSticker : listStickerList) {
                Sticker sticker = new Sticker(); // 게시글 객체 생성

                sticker.setStickerCode("" + mapSticker.get("stickerCode")) // 스티커 코드
                        .setOriginalUrl(mapSticker.get("originalUrl") + "?" + mapSticker.get("type")) // 스티커 이미지 url
                        .setImageWidth((int)mapSticker.get("imageWidth")) // 스티커 가로 길이
                        .setImageHeight((int)mapSticker.get("imageHeight")); // 스티커 세로 길이

                arrayList.add(sticker);
            }
        }

        return arrayList;
    }

    /**
     * 프로필 페이지에 사용
     *
     * @param context
     * @param htmlString
     * @return
     * @throws NullPointerException
     */
    public ArrayList<Item> parseArticleListAjax(Context context, String htmlString) throws NullPointerException {
        ArrayList<Item> arrayList = new ArrayList<>();
        DBHandler dbHandler = DBHandler.open(context, DBHandler.DB_ARTICLE); // for check already article

        Document document = Jsoup.parse(htmlString); // String to Document

        Elements elementsArticles = document.select("li");

        for (Element elementArticle : elementsArticles) {

            // 리스트 추가
            Article article = new Article();

            Element elementTime = elementArticle.selectFirst("span[class=info_item]"); // CATEGORY_PROFILE_COMMENT 인지 확인용
            if (elementTime != null) { // 프로필 작성댓글
                // content
                String time = elementTime.ownText();
                String articleTitle = elementArticle.selectFirst("span[class=desc]").ownText();
                String comment = "";
                if (!articleTitle.equals("원글이 삭제된 게시글입니다."))
                    comment = elementArticle.selectFirst("span[class=num]").text();

                // content
                Element contentElement = elementArticle.selectFirst("strong[class=txt]");
                String content;
                if (contentElement == null)
                    content = "";
                else {
                    if (Build.VERSION.SDK_INT < 24)
                        content = Html.fromHtml(contentElement.html()).toString();
                    else
                        content = Html.fromHtml(contentElement.html(), Html.FROM_HTML_MODE_COMPACT).toString();
                }

                article.setContent(content)
                        .setTime(time)
                        .setArticleTitle(articleTitle)
                        .setComment(comment)
                        .setHref("" + getIntegerValue(elementArticle.selectFirst("a").attr("href"), "articleid"));

            } else { // 전체글보기, 일반게시판
                // author
                String author = elementArticle.selectFirst("span[class=nick]").text();

                article.setAuthor(author)
                        .setNewArticle(elementArticle.selectFirst("span[class=icon_new_txt]") != null)
                        .setTitle(elementArticle.selectFirst("strong[class=tit]").text())
                        .setHref("" + getIntegerValue(elementArticle.selectFirst("a").attr("href"), "articleid"))
                        .setTime(elementArticle.selectFirst("span[class=time]").text())
                        .setView(elementArticle.selectFirst("span[class=no]").text())
                        .setComment(elementArticle.selectFirst("em[class=num]").text());

                // thumbnail
                if (elementArticle.selectFirst("div[class=thumb]") != null) {
                    article.setThumbnailUrl(elementArticle.selectFirst("div[class=thumb]").selectFirst("img").attr("src"));
                }

                // 이미 읽은 글인지 여부, 새 댓글 여부
                checkArticleIsNew(dbHandler, article);

                article.setTimestamp(elementArticle.attr("data-timestamp")); // 좋아요한 글 only
            }

            arrayList.add(article);
            //Log.e("Web", "제목: " + article1.getTitle());
        }
        dbHandler.close();

        return arrayList;

    }

    //// 여기서부터 tool적인 메서드

    /**
     * String 형태의 JSON 데이터를 Map으로 변환합니다.
     *
     * @param JSONString
     * @return
     */
    public static Map convertJsonToMap(String JSONString) {
        try {
            // String to JSON
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            JSONObject jsonObj = (JSONObject) parser.parse(JSONString);

            return JsonUtil.getMapFromJsonObject(jsonObj);

        } catch (ParseException e) {
            Log.w("ParseUtils.err", "JSON Parse error on ParseUtils.convertJsonToMap.");
            e.printStackTrace();

            return null;
        }
    }

    /**
     * article 이 이미 읽은 글인지 여부, 새 댓글 여부를 확인하고 article에 값을 저장합니다.
     *
     * @param dbHandler
     * @param article
     */
    public void checkArticleIsNew(DBHandler dbHandler, Article article) {
        Cursor cursor = dbHandler.select();
        Article oldArticle = DBHandler.getArticle(dbHandler.select(), article.getHref());
        if (oldArticle != null) {
            article.setReadArticle(oldArticle.isReadArticle())
                    .setNewComment(!oldArticle.getComment().equals(article.getComment()));
        }
        cursor.close();
    }


    /**
     * encodedUrl 에 포함된 paramter값을 추출합니다
     */
    public static Integer getIntegerValue(String str, String valName) throws NullPointerException {
        Pattern p = Pattern.compile(valName + "=([0-9]*)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    /**
     * encodedUrl 끝에 포함된 paramter값을 추출합니다
     */
    public static String getStringValue(String str, String valName) throws NullPointerException {
        Pattern p = Pattern.compile(valName + "=([\\w-]*)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * singleton
     *
     * @return instance
     */
    public static ParseUtils getInstance() {
        if (instance == null) {
            instance = new ParseUtils();
        }
        return instance;
    }
}
