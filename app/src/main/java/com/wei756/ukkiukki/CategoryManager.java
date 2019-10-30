package com.wei756.ukkiukki;

import java.util.HashMap;
import java.util.Map;

/**
 * category 별 속성값을 지정하는 싱글톤 매니저 클래스
 * 코드 구현에 있어 switch 문의 사용이 지나치게 많아져 이를 줄이고 코드 가독성을 늘리기 위해 선언함.
 */
public class CategoryManager {
    private static CategoryManager instance;
    private static Map<String, Map> category = new HashMap<>();

    /**
     * category 속성 값
     */
    public static final int NAME = 0;
    public static final int GROUP = 1;
    public static final int TYPE = 2;
    public static final int COLOR = 3;
    public static final int COLOR_DARK = 4;
    public static final int COLOR_STATUS = 5;
    public static final int COLOR_STATUS_TEXT = 6;
    public static final int HEADER_ICON = 100;
    public static final int HEADER_IMG = 101;
    public static final int HEADER_IMG_MARGIN = 102;
    public static final int CHANNEL_TWITCH = 200;
    public static final int CHANNEL_YOUTUBE = 201;
    public static final int STREAMER_ICON = 300;

    /**
     * category group 속성 값
     */
    public static final int GROUP_MAINPAGE = 0;
    public static final int GROUP_ANNOUNCEMENT = 1;
    public static final int GROUP_PIXEL = 2;
    public static final int GROUP_STREAMER = 3;
    public static final int GROUP_COMMUNITY = 4;
    public static final int GROUP_CREATIVE = 5;
    public static final int GROUP_POINT = 6;
    public static final int GROUP_COMMAWANG = 7;
    public static final int GROUP_QNA = 8;

    /**
     * category type 속성 값
     */
    public static final int TYPE_MAINPAGE = 0;
    public static final int TYPE_BOARD = 1;
    public static final int TYPE_PIXEL_MAIN = 2;
    public static final int TYPE_PIXEL = 3;
    public static final int TYPE_STREAMERS = 4;
    public static final int TYPE_BEST = 5;
    public static final int TYPE_CLIP = 6;
    public static final int TYPE_CREATIVE = 7;
    public static final int TYPE_POINT = 8;
    public static final int TYPE_COMMAWANG = 9;
    public static final int TYPE_QNA = 10;

    private static class CategoryBuilder {
        String name = null;
        int group = 0;
        int type = 0;

        int color = R.color.colorPrimary;
        int colorDark = R.color.colorPrimaryDark;
        int colorStatus = R.color.colorPrimaryDark;
        int colorStatusText = R.color.colorTitleDark;

        int headerIcon = R.drawable.article_header_icon_streamer;
        int headerImg = R.drawable.article_header_img_jinu;
        int headerImgMargin = 50;

        String channelTwitch = null;
        String channelYoutube = null;

        int streamerIcon = R.drawable.icon_streamer_jinu;

        public CategoryBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder setGroup(int group) {
            this.group = group;
            return this;
        }

        public CategoryBuilder setType(int type) {
            this.type = type;
            return this;
        }

        public CategoryBuilder setColor(int color, int colorDark, int colorStatus, int colorStatusText) {
            this.color = color;
            this.colorDark = colorDark;
            this.colorStatus = colorStatus;
            this.colorStatusText = colorStatusText;
            return this;
        }

        public CategoryBuilder setHeaderIcon(int headerIcon) {
            this.headerIcon = headerIcon;
            return this;
        }

        public CategoryBuilder setHeaderImg(int headerImg) {
            this.headerImg = headerImg;
            return this;
        }

        public CategoryBuilder setHeaderImgMargin(int headerImgMargin) {
            this.headerImgMargin = headerImgMargin;
            return this;
        }

        public CategoryBuilder setChannelTwitch(String channelTwitch) {
            this.channelTwitch = channelTwitch;
            return this;
        }

        public CategoryBuilder setChannelYoutube(String channelYoutube) {
            this.channelYoutube = channelYoutube;
            return this;
        }

        public CategoryBuilder setStreamerIcon(int streamerIcon) {
            this.streamerIcon = streamerIcon;
            return this;
        }

        public Map<Integer, Object> build() {
            Map<Integer, Object> category = new HashMap<>();

            category.put(NAME, name);
            category.put(GROUP, group);
            category.put(TYPE, type);
            category.put(COLOR, color);
            category.put(COLOR_DARK, colorDark);
            category.put(COLOR_STATUS, colorStatus);
            category.put(COLOR_STATUS_TEXT, colorStatusText);
            category.put(HEADER_ICON, headerIcon);
            category.put(HEADER_IMG, headerImg);
            category.put(HEADER_IMG_MARGIN, headerImgMargin);
            category.put(CHANNEL_TWITCH, channelTwitch);
            category.put(CHANNEL_YOUTUBE, channelYoutube);
            category.put(STREAMER_ICON, streamerIcon);

            return category;
        }
    }

    private CategoryManager() {
        category.put("mainpage",
                new CategoryBuilder()
                        .setName("메인페이지").setGroup(GROUP_MAINPAGE).setType(TYPE_MAINPAGE)
                        .setColor(R.color.colorMainPage, R.color.colorMainPageDark, R.color.colorMainPageStatus, R.color.colorTitleLight)
                        .build());

        category.put("announcement",
                new CategoryBuilder()
                        .setName("공지사항").setGroup(GROUP_ANNOUNCEMENT).setType(TYPE_BOARD)
                        .setColor(R.color.colorAnnouncement, R.color.colorAnnouncementDark, R.color.colorAnnouncementStatus, R.color.colorTitleDark)
                        .setHeaderIcon(R.drawable.article_header_icon_announcement).setHeaderImg(R.drawable.article_header_img_announcement)
                        .build());

        category.put("pixel_main",
                new CategoryBuilder()
                        .setName("픽셀 메인").setGroup(GROUP_PIXEL).setType(TYPE_PIXEL_MAIN)
                        .setHeaderIcon(R.drawable.article_header_icon_pixel)
                        .build());
        category.put("pixel",
                new CategoryBuilder()
                        .setName("픽셀 모아보기").setGroup(GROUP_PIXEL).setType(TYPE_PIXEL)
                        .setHeaderIcon(R.drawable.article_header_img_pixel_main)
                        .build());

        category.put("streamer",
                new CategoryBuilder()
                        .setName("스트리머").setGroup(GROUP_STREAMER).setType(TYPE_STREAMERS)
                        .build());

        category.put("jinu",
                new CategoryBuilder()
                        .setName("지누").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorJinu, R.color.colorJinuDark, R.color.colorJinuStatus, R.color.colorTitleDark)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_jinu).setHeaderImgMargin(64)
                        .setChannelTwitch("jinu6734").setChannelYoutube("UCJGww2K__Q3y-MtDi0XWH2w")
                        .setStreamerIcon(R.drawable.icon_streamer_jinu)
                        .build());
        category.put("temtem",
                new CategoryBuilder()
                        .setName("탬탬버린").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorTemtem, R.color.colorTemtemDark, R.color.colorTemtemStatus, R.color.colorTitleLight)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_temtem).setHeaderImgMargin(45)
                        .setChannelTwitch("2chamcham2").setChannelYoutube("UCCA8UWUW80iHqK9ymdjRwPg")
                        .setStreamerIcon(R.drawable.icon_streamer_temtem)
                        .build());
        category.put("dduddi",
                new CategoryBuilder()
                        .setName("김뚜띠").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorDduddi, R.color.colorDduddiDark, R.color.colorDduddiStatus, R.color.colorTitleLight)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_dduddi).setHeaderImgMargin(45)
                        .setChannelTwitch("kimdduddi").setChannelYoutube("UCTZPZo3xuW5k6RkhlVaJ0jQ")
                        .setStreamerIcon(R.drawable.icon_streamer_dduddi)
                        .build());
        category.put("nanayang",
                new CategoryBuilder()
                        .setName("나나양").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorNanayang, R.color.colorNanayangDark, R.color.colorNanayangStatus, R.color.colorTitleLight)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_nanayang).setHeaderImgMargin(45)
                        .setChannelTwitch("nanayango3o").setChannelYoutube("UC2z7Vj0qpcjIsAEHwOE1-NA")
                        .setStreamerIcon(R.drawable.icon_streamer_nanayang)
                        .build());
        category.put("collet11",
                new CategoryBuilder()
                        .setName("코렛트").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorCollet, R.color.colorColletDark, R.color.colorColletStatus, R.color.colorTitleDark)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_collet11).setHeaderImgMargin(76)
                        .setChannelTwitch("collet11").setChannelYoutube("UCvmHDocKuL6MVx1qRgqLV9g")
                        .setStreamerIcon(R.drawable.icon_streamer_collet11)
                        .build());
        category.put("gambler",
                new CategoryBuilder()
                        .setName("감블러").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorGambler, R.color.colorGamblerDark, R.color.colorGamblerStatus, R.color.colorTitleLight)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_gambler).setHeaderImgMargin(30)
                        .setChannelTwitch("9ambler").setChannelYoutube("UCgmQJAJuiddthVUfJmWxltA")
                        .setStreamerIcon(R.drawable.icon_streamer_gambler)
                        .build());
        category.put("silph",
                new CategoryBuilder()
                        .setName("실프").setGroup(GROUP_STREAMER).setType(TYPE_BOARD)
                        .setColor(R.color.colorSilph, R.color.colorSilphDark, R.color.colorSilphStatus, R.color.colorTitleLight)
                        .setHeaderIcon(R.drawable.article_header_icon_streamer).setHeaderImg(R.drawable.article_header_img_silph).setHeaderImgMargin(48)
                        .setChannelTwitch("silphtv").setChannelYoutube("UCWOPxkftlmCstC5drb9VM_A")
                        .setStreamerIcon(R.drawable.icon_streamer_silph)
                        .build());

        category.put("best",
                new CategoryBuilder()
                        .setName("커뮤니티").setGroup(GROUP_COMMUNITY).setType(TYPE_BEST)
                        .setHeaderIcon(R.drawable.article_header_icon_community) // TODO:커뮤니티 게시판 헤더 이미지 추가해야 함
                        .build());
        category.put("free",
                new CategoryBuilder()
                        .setName("자유게시판").setGroup(GROUP_COMMUNITY).setType(TYPE_BOARD)
                        .setHeaderIcon(R.drawable.article_header_icon_community).setHeaderImg(R.drawable.article_header_img_free).setHeaderImgMargin(59)
                        .build());
        category.put("humor",
                new CategoryBuilder()
                        .setName("유머게시판").setGroup(GROUP_COMMUNITY).setType(TYPE_BOARD)
                        .setHeaderIcon(R.drawable.article_header_icon_community).setHeaderImg(R.drawable.article_header_img_humor).setHeaderImgMargin(36)
                        .build());
        category.put("humor_best",
                new CategoryBuilder()
                        .setName("추천 유머").setGroup(GROUP_COMMUNITY).setType(TYPE_BOARD)
                        .setHeaderIcon(R.drawable.article_header_icon_community).setHeaderImg(R.drawable.article_header_img_humor).setHeaderImgMargin(36)
                        .build());
        category.put("clip",
                new CategoryBuilder()
                        .setName("클립").setGroup(GROUP_COMMUNITY).setType(TYPE_CLIP)
                        .setHeaderIcon(R.drawable.article_header_icon_community).setHeaderImg(R.drawable.article_header_img_clip).setHeaderImgMargin(89)
                        .build());

        category.put("creative",
                new CategoryBuilder()
                        .setName("창작마당").setGroup(GROUP_CREATIVE).setType(TYPE_CREATIVE)
                        .setHeaderIcon(R.drawable.article_header_icon_creative).setHeaderImg(R.drawable.article_header_img_creative).setHeaderImgMargin(5)
                        .build());

        category.put("point",
                new CategoryBuilder()
                        .setName("포인트").setGroup(GROUP_POINT).setType(TYPE_POINT)
                        .setHeaderIcon(R.drawable.article_header_title_point).setHeaderImg(R.drawable.article_header_img_point).setHeaderImgMargin(0)
                        .build());

        category.put("commawang",
                new CategoryBuilder()
                        .setName("컴마왕").setGroup(GROUP_COMMAWANG).setType(TYPE_COMMAWANG)
                        .setHeaderIcon(R.drawable.article_header_icon_commawang).setHeaderImg(R.drawable.article_header_img_commawang).setHeaderImgMargin(58)
                        .build());

        category.put("qna",
                new CategoryBuilder()
                        .setName("고객센터").setGroup(GROUP_QNA).setType(TYPE_QNA)
                        .build());
    }

    public Object getParam(String mid, int type) throws InvalidCategoryException {
        if (!category.containsKey(mid)) {
            throw new InvalidCategoryException(mid);
        }

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
