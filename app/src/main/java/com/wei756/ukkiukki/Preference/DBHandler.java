package com.wei756.ukkiukki.Preference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wei756.ukkiukki.Article;

public class DBHandler {

    public static final String DB_ARTICLE = "article";

    SQLiteOpenHelper mHelper = null;
    SQLiteDatabase mDB = null;

    public DBHandler(Context context, String name) {
        mHelper = new DBHelper(context, name, null, 1);
    }

    public static DBHandler open(Context context, String name) {
        return new DBHandler(context, name);
    }

    /**
     * Database를 열고 Cursor로 반환합니다.
     *
     * @return
     */
    public Cursor select() {
        mDB = mHelper.getReadableDatabase();
        Cursor c = mDB.query("list", null, null, null, null, null, null);
        return c;
    }

    //_id integer primary key autoincrement, articleid text, read tinyint, comment text
    public static Article getArticle(Cursor cursor, String href) {
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            if (cursor.getString(1).equals(href)) { // href 기준으로 커서 탐색
                Article article = new Article();
                article.setHref(href);
                article.setComment(cursor.getString(3));
                article.setReadArticle(cursor.getInt(2) == 1);

                return article;
            }
        }
        return null;
    }

    /**
     * 게시글 정보를 DB에 저장합니다.
     *
     * @param article 게시글 정보
     */
    public void insert(Article article) {
        deleteByArticleId(article.getHref());

        mDB = mHelper.getWritableDatabase();

        // input data
        ContentValues value = new ContentValues();

        String href = article.getHref();
        String comment = article.getComment();
        boolean read = article.isReadArticle();

        if (href != null)
            value.put("articleid", href);
        if (comment != null)
            value.put("comment", comment);

        value.put("read", read ? 1 : 0);

        mDB.insert("list", null, value);

    }

    /**
     * 데이터를 DB에서 제거합니다.
     *
     * @param articleId 게시글 id
     */
    public void deleteByArticleId(String articleId) {
        mDB = mHelper.getWritableDatabase();
        mDB.delete("list", "articleid=?", new String[]{articleId});
    }

    public void close() {
        mHelper.close();
    }
}