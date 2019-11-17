package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * category 별 속성값을 지정하는 싱글톤 매니저 클래스
 * 코드 구현에 있어 switch 문의 사용이 지나치게 많아져 이를 줄이고 코드 가독성을 늘리기 위해 선언함.
 */
public class CategoryManager {
    private Web web = Web.getInstance();

    private static CategoryManager instance;
    private static ArrayList<Map> category = new ArrayList<>();

    private boolean loaded = false;

    /**
     * category 속성 값
     */
    public static final int NAME = 0;
    public static final int TYPE = 1;
    public static final int MENU_ID = 2;
    public static final int NEW_ARTICLE = 3;

    /**
     * category group 속성 값
     */
    //public static final int GROUP_MAINPAGE = 0;

    /**
     * category type 속성 값
     */
    public static final int TYPE_MAINPAGE = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_BOARD = 2;
    public static final int TYPE_ALBUM = 3;
    public static final int TYPE_THUMBKING = 4;
    public static final int TYPE_REPORT = 5;
    public static final int TYPE_BEST = 6;
    public static final int TYPE_CONTOUR = 7;

    /**
     * 고정 category id
     */
    public static final int CATEGORY_LOGIN = -99; // 메인페이지
    public static final int CATEGORY_MAINPAGE = -4; // 메인페이지
    public static final int CATEGORY_ALLLIST = 0; // 전체글보기
    public static final int CATEGORY_POPULAR_ARTICLE = -1; // 인기글
    public static final int CATEGORY_POPULAR_MEMBER = -2; // 인기멤버
    public static final int CATEGORY_TAG = -3; // 카페태그


    public static class CategoryBuilder {
        String name = null;
        int type = 0;
        int menuId = -1;
        boolean newArticle = false;

        public CategoryBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder setType(int type) {
            this.type = type;
            return this;
        }

        public CategoryBuilder setMenuId(int menuId) {
            this.menuId = menuId;
            return this;
        }

        public CategoryBuilder setNewArticle(boolean newArticle) {
            this.newArticle = newArticle;
            return this;
        }

        public Map<Integer, Object> build() {
            Map<Integer, Object> category = new HashMap<>();

            category.put(NAME, name);
            category.put(TYPE, type);
            category.put(MENU_ID, menuId);
            category.put(NEW_ARTICLE, newArticle);

            return category;
        }
    }

    private CategoryManager() {

    }

    public void updateCategoryList() {
        new Thread() {
            public void run() {
                // 카테고리 로드
                category.clear();
                ArrayList<Map> categoryList = web.loadCategoryList();
                category.add(new CategoryBuilder()
                        .setName("메인페이지")
                        .setType(TYPE_MAINPAGE)
                        .setMenuId(CATEGORY_MAINPAGE)
                        .build());
                category.add(new CategoryBuilder()
                        .setName("전체글보기")
                        .setType(TYPE_BOARD)
                        .setMenuId(CATEGORY_ALLLIST)
                        .build());
                for (Map cat : categoryList)
                    category.add(cat);

            }
        }.start();
    }

    public void updateCategoryMenu(MainActivity act, final Menu menu) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                menu.clear();
                for (Map cat : category)
                    menu.add((String) cat.get(CategoryManager.NAME));
            }
        });
    }

    public Object getParam(int mid, int type) throws InvalidCategoryException {
        for (Map cat : category) {
            if ((int) cat.get(MENU_ID) == mid)
                return cat.get(type);
        }
        throw new InvalidCategoryException("" + mid);
    }

    public int findIdByName(String name) throws InvalidCategoryException {
        for (Map cat : category) {
            if (cat.get(NAME).equals(name))
                return (int) cat.get(MENU_ID);
        }
        throw new InvalidCategoryException(name);
    }

    public static CategoryManager getInstance() {
        if (instance == null)
            instance = new CategoryManager();
        return instance;
    }
}

class InvalidCategoryException extends Exception {
    InvalidCategoryException(String category) {
        super(category);
    }
}
