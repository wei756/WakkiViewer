package com.wei756.ukkiukki;

import android.util.Log;
import android.view.Menu;
import android.view.SubMenu;

import com.wei756.ukkiukki.Network.Web;

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

    private long lastLoadedTime = 0;

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
    public static final int TYPE_PROFILE = 8;

    /**
     * 고정 category id
     */
    public static final int CATEGORY_LOGIN = -99; // 메인페이지
    public static final int CATEGORY_MAINPAGE = -4; // 메인페이지
    public static final int CATEGORY_ALLLIST = 0; // 전체글보기
    public static final int CATEGORY_POPULAR_ARTICLE = -1; // 인기글
    public static final int CATEGORY_POPULAR_MEMBER = -2; // 인기멤버
    public static final int CATEGORY_TAG = -3; // 카페태그
    public static final int CATEGORY_BOARD = -4; // 일반 게시판
    public static final int CATEGORY_NOTICE = -5; // 전체 공지

    public static final int CATEGORY_PROFILE_ARTICLE = -10; // 프로필 작성글
    public static final int CATEGORY_PROFILE_COMMENT = -11; // 프로필 작성댓글
    public static final int CATEGORY_PROFILE_COMMENT_ARTICLE = -12; // 프로필 댓글단 글
    public static final int CATEGORY_PROFILE_LIKEIT = -13; // 프로필 좋아요한 글
    public static final int CATEGORY_PROFILE_WARDING = -14; // 프로필 와딩한 글


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

    public void updateCategoryListAsync() {
        new Thread() {
            public void run() {
                updateCategoryList();
            }
        }.start();
    }

    public void updateCategoryList() {
        long currentTime = System.currentTimeMillis();
        Log.v("CategoryManager", "시간 차이: " + (currentTime - lastLoadedTime) + "ms");
        if (!isLoadedCategory() && currentTime - lastLoadedTime > 2 * 1000) {
            lastLoadedTime = currentTime;
            Log.v("CategoryManager", "Loading.");
            ArrayList<Map> categoryList = web.getCategoryList(); // 카테고리 로드
            category.clear();
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
            if (categoryList != null)
                for (Map cat : categoryList)
                    category.add(cat);
            else { // 카테고리 로딩 에러났을 때
            }
            category.add(new CategoryBuilder()
                    .setName("작성글")
                    .setType(TYPE_PROFILE)
                    .setMenuId(CATEGORY_PROFILE_ARTICLE)
                    .build());
            category.add(new CategoryBuilder()
                    .setName("작성댓글")
                    .setType(TYPE_PROFILE)
                    .setMenuId(CATEGORY_PROFILE_COMMENT)
                    .build());
            category.add(new CategoryBuilder()
                    .setName("댓글단 글")
                    .setType(TYPE_PROFILE)
                    .setMenuId(CATEGORY_PROFILE_COMMENT_ARTICLE)
                    .build());
            category.add(new CategoryBuilder()
                    .setName("좋아요한 글")
                    .setType(TYPE_PROFILE)
                    .setMenuId(CATEGORY_PROFILE_LIKEIT)
                    .build());
            category.add(new CategoryBuilder()
                    .setName("와딩한 글")
                    .setType(TYPE_PROFILE)
                    .setMenuId(CATEGORY_PROFILE_WARDING)
                    .build());
        }
    }


    public void updateCategoryMenu(MainActivity act, final Menu menu) {
        act.runOnUiThread(() -> {
            menu.clear();
            boolean grouped = false;
            SubMenu subMenu = null;
            for (Map cat : category) {
                int type = (int) cat.get(CategoryManager.TYPE);
                String name = (String) cat.get(CategoryManager.NAME);

                if (type == CategoryManager.TYPE_GROUP) {
                    if (!grouped)
                        grouped = true;
                    subMenu = menu.addSubMenu(name);
                } else if (type == CategoryManager.TYPE_CONTOUR) {
                    subMenu = menu.addSubMenu("");
                } else if (type != CategoryManager.TYPE_PROFILE) {
                    if (grouped)
                        subMenu.add(name);
                    else
                        menu.add(name);
                }
            }
        });
    }

    /**
     * 카테고리가 정상적으로 로딩되어있는지 여부를 반환합니다
     */
    public static boolean isLoadedCategory() {
        return (category.size() > 10);
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

    /**
     * singleton
     */
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
