package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wei756.ukkiukki.Preference.DBHandler;

import java.util.ArrayList;

public class ArticleListAdapter extends RecyclerViewCustomAdapter {

    public final static int THEME_BOARD = 0;
    public final static int THEME_MAINPAGE = 3;
    public final static int THEME_ARTICLE_VIEWER = 6;
    public final static int THEME_HEADER_POPULAR = 9;
    public final static int THEME_PROFILE = 12;

    public final static int SUBTHEME_ARTICLE = 0;
    public final static int SUBTHEME_POPULAR = 1;
    public final static int SUBTHEME_ALBUM = 2;
    public final static int SUBTHEME_PROFILE_COMMENT = 3;

    /**
     * 표시할 게시판 코드
     */
    private int mid;

    private RecyclerView mRecyclerView;

    // header title, img, icon
    private String category = "";
    private int icon = R.drawable.article_header_default;
    private int img = R.drawable.article_header_default;
    private int imgMargin = 50;

    // link menu
    private boolean icons = false;
    private String channelTwitch = "";
    private String channelYoutube = "";
    private final String URLTwitch = "https://www.twitch.tv/";
    private final String URLYoutube = "https://www.youtube.com/channel/";

    /**
     * ItemView 정의
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ConstraintLayout layout;
        protected ImageView newIcon;
        protected TextView title;
        protected TextView levelIcon;
        protected TextView author;
        protected TextView time;
        protected TextView view;
        protected ImageView newIconComment;
        protected ConstraintLayout btnComment;
        protected LinearLayout layoutFooter;
        protected TextView numComment;
        protected TextView numLikeIt;
        protected TextView commentCount;
        protected ImageView thumbnail;
        protected ConstraintLayout article;

        // 프로필 작성댓글
        protected TextView content;
        //protected TextView time;
        protected TextView articleTitle;
        //protected TextView numComment;


        public ItemViewHolder(View view) {
            super(view);
            this.layout = (ConstraintLayout) view.findViewById(R.id.layout_article);
            this.article = (ConstraintLayout) view.findViewById(R.id.btn_layout_article);
            this.newIcon = (ImageView) view.findViewById(R.id.iv_icon_new);
            this.title = (TextView) view.findViewById(R.id.tv_title);
            this.levelIcon = (TextView) view.findViewById(R.id.icon_author_color);
            this.author = (TextView) view.findViewById(R.id.tv_author);
            this.time = (TextView) view.findViewById(R.id.tv_time);
            this.view = (TextView) view.findViewById(R.id.tv_view);
            this.newIconComment = (ImageView) view.findViewById(R.id.iv_icon_new_comment);
            this.btnComment = (ConstraintLayout) view.findViewById(R.id.btn_comment);
            this.layoutFooter = (LinearLayout) view.findViewById(R.id.layout_footer);
            this.numComment = (TextView) view.findViewById(R.id.tv_comment);
            this.numLikeIt = (TextView) view.findViewById(R.id.tv_likeit);
            this.commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            this.thumbnail = (ImageView) view.findViewById(R.id.iv_thumbnail);

            this.content = (TextView) view.findViewById(R.id.tv_content);
            this.articleTitle = (TextView) view.findViewById(R.id.tv_article_title);
        }
    }

    /**
     * HeaderView 정의
     */
    public class HeaderViewHolder extends ItemViewHolder {
        protected TextView category;
        protected ImageView icon;

        protected TextView more;

        protected LinearLayout icons;


        public HeaderViewHolder(View view) {
            super(view);
            this.layout = (ConstraintLayout) view.findViewById(R.id.layout_article);
            this.category = (TextView) view.findViewById(R.id.tv_article_list_header_title);
            this.more = (TextView) view.findViewById(R.id.tv_article_list_mainpage_more);
        }
    }

    /**
     * FooterView 정의
     */
    public class FooterViewHolder extends RecyclerView.ViewHolder {
        protected ConstraintLayout layout;
        protected ConstraintLayout article;

        protected TextView category;
        protected ImageView icon;

        protected TextView more;

        protected LinearLayout icons;


        public FooterViewHolder(View view) {
            super(view);
            this.layout = (ConstraintLayout) view.findViewById(R.id.layout_article);
            this.article = (ConstraintLayout) view.findViewById(R.id.btn_layout_article);
            this.category = (TextView) view.findViewById(R.id.tv_article_list_header_title);
            this.more = (TextView) view.findViewById(R.id.tv_article_list_mainpage_more);
        }
    }

    /**
     * ArticleListAdapter 정의
     *
     * @param list ArticleListAdapter 가 표시할 Item 리스트
     * @param act  recyclerView 가 표시되는 액티비티
     */
    public ArticleListAdapter(ArrayList<Item> list, Activity act, int theme, RecyclerView mRecyclerView) {
        this.mList = list;
        this.context = act;
        this.mRecyclerView = mRecyclerView;

        hasHeader = true;
        hasFooter = false;

        THEME_NUMBER = 5;
        SUBTHEME_NUMBER = 4;

        setTheme(theme);
        setItemLayout();
    }

    public void setTheme(int theme) {
        this.TYPE_THEME = theme;

        switch (theme) {
            case THEME_BOARD:
                maxItemCount = 0;
                headerHasItem = false;
                scrollable = true;
                hasHeader = false;
                hasFooter = false;
                break;

            case THEME_MAINPAGE:
                maxItemCount = 5;
                headerHasItem = false;
                scrollable = false;
                hasHeader = true;
                hasFooter = false;
                break;

            case THEME_ARTICLE_VIEWER:
                maxItemCount = 20;
                headerHasItem = false;
                scrollable = false;
                hasHeader = true;
                hasFooter = false;
                break;

            case THEME_HEADER_POPULAR:
                maxItemCount = 5;
                headerHasItem = false;
                scrollable = false;
                hasHeader = true;
                hasFooter = true;
                break;

            case THEME_PROFILE:
                maxItemCount = 0;
                headerHasItem = false;
                scrollable = true;
                hasHeader = false;
                hasFooter = false;
                break;
        }
    }

    public void setSubTheme(int subtheme) {
        this.TYPE_SUBTHEME = subtheme;
    }

    /**
     * 게시판 헤더에 들어가는 값을 설정합니다.
     *
     * @param mid 게시판 코드
     */
    public void setMidTheme(int mid) {
        CategoryManager category = CategoryManager.getInstance();
        try {
            if (!CategoryManager.isLoadedCategory()) // 로딩되어있는지 먼저 체크
                category.updateCategoryList();

            int type = (int) category.getParam(mid, CategoryManager.TYPE);

            if (type == CategoryManager.TYPE_BEST) { // 인기글
                setSubTheme(SUBTHEME_POPULAR);
            } else {
                setSubTheme(SUBTHEME_ARTICLE);
            }

            if (mid == CategoryManager.CATEGORY_PROFILE_COMMENT)
                setSubTheme(SUBTHEME_PROFILE_COMMENT);

            if ((Integer) category.getParam(mid, CategoryManager.TYPE) == CategoryManager.TYPE_MAINPAGE) { // 메인 페이지

            } else {
                icons = false;
                this.category = (String) category.getParam(mid, CategoryManager.NAME);

                this.mid = mid;
                Log.v("ArticleListAdapter", "게시판 헤더 \"" + this.category + "(" + mid + ")\" (으)로 설정됨. on ArticleListAdapter.setMidTheme");
            }
        } catch (InvalidCategoryException e) {
            Log.e("에러", "존재하지 않는 게시판 코드입니다(" + e.getMessage() + ") on ArticleListAdapter.setMidTheme");
            e.printStackTrace();
        }
    }

    /**
     * layout 정의
     */
    protected void setItemLayout() {
        layoutHeader = new int[THEME_NUMBER * SUBTHEME_NUMBER];
        layoutItem = new int[THEME_NUMBER * SUBTHEME_NUMBER];
        layoutFooter = new int[THEME_NUMBER * SUBTHEME_NUMBER];

        layoutHeader[THEME_BOARD + SUBTHEME_ARTICLE] = R.layout.article_list; //TODO: deprecated
        layoutHeader[THEME_BOARD + SUBTHEME_POPULAR] = R.layout.article_list; //TODO: deprecated
        layoutHeader[THEME_BOARD + SUBTHEME_ALBUM] = R.layout.article_list; //TODO: deprecated
        layoutHeader[THEME_MAINPAGE + SUBTHEME_ARTICLE] = R.layout.article_list_mainpage_header;
        layoutHeader[THEME_MAINPAGE + SUBTHEME_POPULAR] = R.layout.article_list_mainpage_header;
        layoutHeader[THEME_MAINPAGE + SUBTHEME_ALBUM] = R.layout.article_list_mainpage_header; //TODO: clip album 분리 필요
        layoutHeader[THEME_ARTICLE_VIEWER + SUBTHEME_ARTICLE] = R.layout.article_list; //TODO: deprecated
        layoutHeader[THEME_ARTICLE_VIEWER + SUBTHEME_POPULAR] = R.layout.article_list; //TODO: deprecated
        layoutHeader[THEME_ARTICLE_VIEWER + SUBTHEME_ALBUM] = R.layout.article_list; //TODO: deprecated
        layoutHeader[THEME_HEADER_POPULAR + SUBTHEME_ARTICLE] = R.layout.article_list_popular_theme_header;
        layoutHeader[THEME_HEADER_POPULAR + SUBTHEME_POPULAR] = R.layout.article_list_popular_theme_header;
        layoutHeader[THEME_HEADER_POPULAR + SUBTHEME_ALBUM] = R.layout.article_list_popular_theme_header;
        layoutHeader[THEME_PROFILE + SUBTHEME_ARTICLE] = R.layout.article_list; //TODO: 프로필 레이아웃 변경 필요
        layoutHeader[THEME_PROFILE + SUBTHEME_POPULAR] = R.layout.article_list_popular_theme; //TODO: 프로필 레이아웃 변경 필요
        layoutHeader[THEME_PROFILE + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme; //TODO: 프로필 레이아웃 변경 필요

        layoutItem[THEME_BOARD + SUBTHEME_ARTICLE] = R.layout.article_list;
        layoutItem[THEME_BOARD + SUBTHEME_POPULAR] = R.layout.article_list_popular_theme;
        layoutItem[THEME_BOARD + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme;
        layoutItem[THEME_MAINPAGE + SUBTHEME_ARTICLE] = R.layout.article_list_mainpage;
        layoutItem[THEME_MAINPAGE + SUBTHEME_POPULAR] = R.layout.article_list_mainpage;
        layoutItem[THEME_MAINPAGE + SUBTHEME_ALBUM] = R.layout.article_list_mainpage; //TODO: clip album 분리 필요
        layoutItem[THEME_ARTICLE_VIEWER + SUBTHEME_ARTICLE] = R.layout.article_list;
        layoutItem[THEME_ARTICLE_VIEWER + SUBTHEME_POPULAR] = R.layout.article_list_clip_theme;
        layoutItem[THEME_ARTICLE_VIEWER + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme;
        layoutItem[THEME_HEADER_POPULAR + SUBTHEME_ARTICLE] = R.layout.article_list_popular_theme;
        layoutItem[THEME_HEADER_POPULAR + SUBTHEME_POPULAR] = R.layout.article_list_popular_theme;
        layoutItem[THEME_HEADER_POPULAR + SUBTHEME_ALBUM] = R.layout.article_list_popular_theme;
        layoutItem[THEME_PROFILE + SUBTHEME_ARTICLE] = R.layout.article_list; //TODO: 프로필 레이아웃 변경 필요
        layoutItem[THEME_PROFILE + SUBTHEME_POPULAR] = R.layout.article_list_popular_theme; //TODO: 프로필 레이아웃 변경 필요
        layoutItem[THEME_PROFILE + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme; //TODO: 프로필 레이아웃 변경 필요
        layoutItem[THEME_PROFILE + SUBTHEME_PROFILE_COMMENT] = R.layout.article_list_comment_theme;

        layoutFooter[THEME_BOARD + SUBTHEME_ARTICLE] = R.layout.article_list; // dummy
        layoutFooter[THEME_BOARD + SUBTHEME_POPULAR] = R.layout.article_list; // dummy
        layoutFooter[THEME_BOARD + SUBTHEME_ALBUM] = R.layout.article_list; // dummy
        layoutFooter[THEME_MAINPAGE + SUBTHEME_ARTICLE] = R.layout.article_list_mainpage_header; // dummy
        layoutFooter[THEME_MAINPAGE + SUBTHEME_POPULAR] = R.layout.article_list_mainpage_header; // dummy
        layoutFooter[THEME_MAINPAGE + SUBTHEME_ALBUM] = R.layout.article_list_mainpage_header; // dummy
        layoutFooter[THEME_ARTICLE_VIEWER + SUBTHEME_ARTICLE] = R.layout.article_list; // dummy
        layoutFooter[THEME_ARTICLE_VIEWER + SUBTHEME_POPULAR] = R.layout.article_list; // dummy
        layoutFooter[THEME_ARTICLE_VIEWER + SUBTHEME_ALBUM] = R.layout.article_list; // dummy
        layoutFooter[THEME_HEADER_POPULAR + SUBTHEME_ARTICLE] = R.layout.article_list_popular_theme_footer;
        layoutFooter[THEME_HEADER_POPULAR + SUBTHEME_POPULAR] = R.layout.article_list_popular_theme_footer;
        layoutFooter[THEME_HEADER_POPULAR + SUBTHEME_ALBUM] = R.layout.article_list_popular_theme_footer;
        layoutFooter[THEME_PROFILE + SUBTHEME_ARTICLE] = R.layout.article_list; // dummy
        layoutFooter[THEME_PROFILE + SUBTHEME_POPULAR] = R.layout.article_list; // dummy
        layoutFooter[THEME_PROFILE + SUBTHEME_ALBUM] = R.layout.article_list; // dummy
        layoutFooter[THEME_PROFILE + SUBTHEME_PROFILE_COMMENT] = R.layout.article_list; // dummy
    }

    /**
     * header layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutHeader[TYPE_THEME + TYPE_SUBTHEME], viewGroup, false);

        return new HeaderViewHolder(view);
    }

    /**
     * item layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutItem[TYPE_THEME + TYPE_SUBTHEME], viewGroup, false);

        return new HeaderViewHolder(view);
    }

    /**
     * footer layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutFooter[TYPE_THEME + TYPE_SUBTHEME], viewGroup, false);

        return new FooterViewHolder(view);
    }

    /**
     * header content 정의
     */
    @Override
    protected void bindHeaderViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewholder;
        // size
        if (TYPE_THEME == THEME_HEADER_POPULAR) {
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
            headerViewHolder.layout.setPadding(padding, padding, padding, padding);
            headerViewHolder.layout.setBackgroundResource(R.color.colorTransparent);
            ViewGroup.LayoutParams params = headerViewHolder.layout.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, context.getResources().getDisplayMetrics());
            headerViewHolder.layout.setLayoutParams(params);

            params = headerViewHolder.article.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            headerViewHolder.article.setLayoutParams(params);
        }

        // header
        if (TYPE_THEME != THEME_HEADER_POPULAR)
            headerViewHolder.category.setText(category);
        if (TYPE_THEME == THEME_MAINPAGE) {
            headerViewHolder.more.setOnClickListener(view -> ((MainActivity) context).setCategory(mid, true));
        }
        if (TYPE_THEME == THEME_HEADER_POPULAR) {
            headerViewHolder.article.setOnClickListener(view -> ((MainActivity) context).setCategory(CategoryManager.CATEGORY_POPULAR_ARTICLE, true));
        }
    }

    /**
     * item content 정의
     */
    @Override
    protected void bindItemViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int pos) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewholder;
        final Article article = (Article) mList.get(pos);

        // size
        if (TYPE_THEME == THEME_HEADER_POPULAR) {
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
            itemViewHolder.layout.setPadding(padding, padding, padding, padding);
            itemViewHolder.layout.setBackgroundResource(R.color.colorTransparent);
            ViewGroup.LayoutParams params = itemViewHolder.layout.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, context.getResources().getDisplayMetrics());
            itemViewHolder.layout.setLayoutParams(params);

            params = itemViewHolder.article.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            itemViewHolder.article.setLayoutParams(params);
        }

        // title
        if (!(TYPE_THEME == THEME_PROFILE
                && TYPE_SUBTHEME == SUBTHEME_PROFILE_COMMENT))
            itemViewHolder.title.setText(article.getTitle());
        if (TYPE_THEME == THEME_BOARD || TYPE_THEME == THEME_PROFILE) {
            if (TYPE_SUBTHEME == SUBTHEME_ARTICLE) {
                if (article.isReadArticle())
                    itemViewHolder.title.setTextColor(context.getResources().getColor(R.color.colorTextSecondary, null));
                else
                    itemViewHolder.title.setTextColor(context.getResources().getColor(R.color.colorTextPrimary, null));
            } else if (TYPE_SUBTHEME == SUBTHEME_POPULAR) {
                if (article.isReadArticle())
                    itemViewHolder.title.setTextColor(context.getResources().getColor(R.color.colorPopularListTextSecondary, null));
                else
                    itemViewHolder.title.setTextColor(context.getResources().getColor(R.color.colorPopularListTextPrimary, null));
            }
        }
        if (TYPE_THEME != THEME_MAINPAGE && TYPE_SUBTHEME != SUBTHEME_PROFILE_COMMENT)
            itemViewHolder.author.setText(article.getAuthor());

        //TODO: 회원등급 표시기능 추가 가능성 있음
        // levelIcon
        /*
        if (TYPE_SUBTHEME == SUBTHEME_ARTICLE) {
            String levelIcon = article.getLevelIcon();
            if (!levelIcon.equals("")) {
                itemViewHolder.levelIcon.setText(levelIcon);
                itemViewHolder.levelIcon.setVisibility(View.VISIBLE);
                try {
                    itemViewHolder.levelIcon.setBackgroundResource(LevelIcon.getInstance().getIcon(levelIcon));
                } catch (Exception e) {
                    Log.e("PersonalColor", "Wrong color error(" + levelIcon + ") on ArticleListAdapter.getIcon");
                    e.printStackTrace();
                }
            } else {
                //itemViewHolder.levelIcon.setVisibility(View.GONE);
            }
        }
        if (TYPE_THEME != THEME_MAINPAGE)
            itemViewHolder.levelIcon.setVisibility(View.GONE);
        */

        if ((TYPE_THEME == THEME_BOARD || TYPE_THEME == THEME_ARTICLE_VIEWER || TYPE_THEME == THEME_PROFILE)
                && TYPE_SUBTHEME == SUBTHEME_ARTICLE) {
            if (article.isNewArticle() && !article.isReadArticle())
                itemViewHolder.newIcon.setVisibility(View.VISIBLE);
            else
                itemViewHolder.newIcon.setVisibility(View.GONE);
            itemViewHolder.time.setText(article.getTime());
            itemViewHolder.view.setText(article.getView());
            if (article.isNewComment())
                itemViewHolder.newIconComment.setVisibility(View.VISIBLE);
            else
                itemViewHolder.newIconComment.setVisibility(View.GONE);
            itemViewHolder.numComment.setText(article.getComment());
        } else if (TYPE_THEME == THEME_BOARD
                && TYPE_SUBTHEME == SUBTHEME_POPULAR) {
            itemViewHolder.layoutFooter.setVisibility(View.VISIBLE);
            itemViewHolder.numComment.setText(article.getComment());
            itemViewHolder.numLikeIt.setText(article.getLikeIt());
        } else if (TYPE_THEME == THEME_HEADER_POPULAR) {
            itemViewHolder.layoutFooter.setVisibility(View.GONE);
        } else if (TYPE_THEME == THEME_MAINPAGE) {
            itemViewHolder.time.setText(article.getTime());
        }

        // thumbnail
        if (TYPE_THEME == THEME_BOARD
                || TYPE_THEME == THEME_ARTICLE_VIEWER
                || TYPE_THEME == THEME_PROFILE) {
            String thumbnailUrl = article.getThumbnailUrl();
            if (thumbnailUrl != null) {
                Glide.with(context.getApplicationContext()).load(thumbnailUrl).into(itemViewHolder.thumbnail);

                if (TYPE_SUBTHEME == SUBTHEME_ARTICLE) { // 인기글 리스트 적용 제외
                    // rounded corners
                    GradientDrawable drawable =
                            (GradientDrawable) context.getApplicationContext().getDrawable(R.drawable.article_list_bg_thumbnail);
                    itemViewHolder.thumbnail.setBackground(drawable);
                    itemViewHolder.thumbnail.setClipToOutline(true);
                }

                itemViewHolder.thumbnail.setVisibility(View.VISIBLE);
            } else if (TYPE_SUBTHEME != SUBTHEME_PROFILE_COMMENT) {
                itemViewHolder.thumbnail.setVisibility(View.GONE);
            }
        }

        // 프로필 작성댓글 리스트
        if (TYPE_THEME == THEME_PROFILE
                && TYPE_SUBTHEME == SUBTHEME_PROFILE_COMMENT) {
            itemViewHolder.content.setText(article.getContent());
            itemViewHolder.time.setText(article.getTime());
            itemViewHolder.articleTitle.setText(article.getArticleTitle());
            itemViewHolder.numComment.setText(article.getComment());
        }

        // click event
        View.OnClickListener clickArticle = view -> {
            article.setReadArticle(true);
            article.setNewComment(false);
            setReadArticle(article);
            ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
            ArticleViewerActivity.startArticleViewerActivity(context, article.getTitle(), article.getHref(), false);
        };
        itemViewHolder.article.setOnClickListener(clickArticle);

        if ((TYPE_THEME == THEME_BOARD && TYPE_SUBTHEME != SUBTHEME_POPULAR) ||
                (TYPE_THEME == THEME_PROFILE &&
                        TYPE_SUBTHEME == SUBTHEME_ARTICLE)) {
            View.OnClickListener clickComment = view -> {
                article.setReadArticle(true);
                setReadArticle(article);
                ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                ArticleViewerActivity.startArticleViewerActivity(context, article.getTitle(), article.getHref(), true);
            };
            itemViewHolder.btnComment.setOnClickListener(clickComment);
        }
        //Log.v("ArticleListAdapter", (pos + 1) + "번째 게시글 표시됨 \"" + article.getTitle() + "\"(" + article.getHref() + ") on ArticleListAdapter.onBindViewHolder");


    }

    /**
     * footer content 정의
     */
    @Override
    protected void bindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) viewholder;

        // size
        if (TYPE_THEME == THEME_HEADER_POPULAR) {
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
            footerViewHolder.layout.setPadding(padding, padding, padding, padding);
            footerViewHolder.layout.setBackgroundResource(R.color.colorTransparent);
            ViewGroup.LayoutParams params = footerViewHolder.layout.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, context.getResources().getDisplayMetrics());
            footerViewHolder.layout.setLayoutParams(params);

            params = footerViewHolder.article.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            footerViewHolder.article.setLayoutParams(params);
        }

        if (TYPE_THEME == THEME_HEADER_POPULAR) {
            footerViewHolder.article.setOnClickListener(view -> ((MainActivity) context).setCategory(CategoryManager.CATEGORY_POPULAR_ARTICLE, true));
        }
    }

    private void setReadArticle(Article article) {
        DBHandler dbHandler = DBHandler.open(context, DBHandler.DB_ARTICLE); // for set already article
        dbHandler.insert(article);
    }
}
