package com.wei756.ukkiukki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * category 별 속성값을 지정하는 싱글톤 매니저 클래스
 * 코드 구현에 있어 switch 문의 사용이 지나치게 많아져 이를 줄이고 코드 가독성을 늘리기 위해 선언함.
 */
public class CategoryManager {
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
        new Thread() {
            public void run() {
                // 카테고리 로드
                ArrayList<Map> categoryList = Web.loadCategoryList();
                for (Map cat : categoryList)
                    category.add(cat);
            }
        }.start();
    }

    public Object getParam(int mid, int type) throws InvalidCategoryException {
        if (!(mid < category.size()))
            throw new InvalidCategoryException("" + mid);

        return category.get(mid).get(type);
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
