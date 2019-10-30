package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ArticleListAdapter extends RecyclerViewCustomAdapter {

    public final static int THEME_BOARD = 0;
    public final static int THEME_MAINPAGE = 3;
    public final static int THEME_ARTICLE_VIEWER = 6;

    public final static int SUBTHEME_ARTICLE = 0;
    public final static int SUBTHEME_CLIP = 1;
    public final static int SUBTHEME_ALBUM = 2;

    /**
     * 표시할 게시판 코드
     */
    private String mid;

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
        protected TextView title;
        protected TextView levelIcon;
        protected TextView author;
        protected TextView time;
        protected TextView view;
        protected Button comment;
        protected TextView commentCount;
        protected ImageView thumbnail;
        protected ConstraintLayout article;


        public ItemViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.tv_title);
            this.levelIcon = (TextView) view.findViewById(R.id.icon_author_color);
            this.author = (TextView) view.findViewById(R.id.tv_author);
            this.time = (TextView) view.findViewById(R.id.tv_time);
            this.view = (TextView) view.findViewById(R.id.tv_view);
            this.comment = (Button) view.findViewById(R.id.btn_comment);
            this.commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            this.thumbnail = (ImageView) view.findViewById(R.id.iv_thumbnail);
            this.article = (ConstraintLayout) view.findViewById(R.id.btn_layout_article);
        }
    }

    /**
     * HeaderView 정의
     */
    public class HeaderViewHolder extends ItemViewHolder {
        protected TextView category;
        protected ImageView img;
        protected ImageView icon;

        protected TextView more;

        protected LinearLayout icons;
        protected ImageView iconTwitch;
        protected ImageView iconYoutube;

        protected ConstraintLayout itemFirst;


        public HeaderViewHolder(View view) {
            super(view);
            this.category = (TextView) view.findViewById(R.id.tv_article_list_header_title);
            this.img = (ImageView) view.findViewById(R.id.img_article_list_header_img);
            this.icon = (ImageView) view.findViewById(R.id.img_article_list_header_icon);
            this.more = (TextView) view.findViewById(R.id.tv_article_list_mainpage_more);
            this.icons = (LinearLayout) view.findViewById(R.id.layout_article_list_header_icons);
            this.iconTwitch = (ImageView) view.findViewById(R.id.btn_article_list_header_twitch);
            this.iconYoutube = (ImageView) view.findViewById(R.id.btn_article_list_header_youtube);
            this.itemFirst = (ConstraintLayout) view.findViewById(R.id.item_article_list_first);
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
        this.act = act;
        this.mRecyclerView = mRecyclerView;

        hasHeader = true;
        hasFooter = false;

        THEME_NUMBER = 3;
        SUBTHEME_NUMBER = 3;

        setTheme(theme);
        setItemLayout();
    }

    public void setTheme(int theme) {
        this.TYPE_THEME = theme;

        switch (theme) {
            case THEME_BOARD:
                maxItemCount = 0;
                headerHasItem = true;
                scrollable = true;
                break;

            case THEME_MAINPAGE:
                maxItemCount = 5;
                headerHasItem = false;
                scrollable = false;
                break;

            case THEME_ARTICLE_VIEWER:
                maxItemCount = 20;
                headerHasItem = true;
                scrollable = false;
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
    public void setTheme(String mid) {
        CategoryManager category = CategoryManager.getInstance();
        try {
            int type = (int) category.getParam(mid, CategoryManager.TYPE);

            if (type == CategoryManager.TYPE_CLIP
                    || type == CategoryManager.TYPE_CREATIVE
                    || type == CategoryManager.TYPE_PIXEL) {
                setSubTheme(SUBTHEME_CLIP);
            } else {
                setSubTheme(SUBTHEME_ARTICLE);
            }
            // 레이아웃 전환 시 필요
            if (this.mid != mid) {
                final ArticleListAdapter mAdapter = this;
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.removeAllViewsInLayout();
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
            }

            if ((Integer) category.getParam(mid, CategoryManager.TYPE) == CategoryManager.TYPE_MAINPAGE) { // 메인 페이지

            } else {
                icons = false;
                this.category = (String) category.getParam(mid, CategoryManager.NAME);
                this.icon = (Integer) category.getParam(mid, CategoryManager.HEADER_ICON);
                this.img = (Integer) category.getParam(mid, CategoryManager.HEADER_IMG);
                this.imgMargin = (Integer) category.getParam(mid, CategoryManager.HEADER_IMG_MARGIN);
                if ((Integer) category.getParam(mid, CategoryManager.GROUP) == CategoryManager.GROUP_STREAMER) { // 스트리머 게인 게시판
                    icons = true;
                    this.channelTwitch = (String) category.getParam(mid, CategoryManager.CHANNEL_TWITCH);
                    this.channelYoutube = (String) category.getParam(mid, CategoryManager.CHANNEL_YOUTUBE);
                }

                this.mid = mid;
                Log.v("ArticleListAdapter", "게시판 헤더 \"" + this.category + "(" + mid + ")\" (으)로 설정됨. on ArticleListAdapter.setHeader");
            }
        } catch (InvalidCategoryException e) {
            Log.e("에러", "존재하지 않는 게시판 코드입니다(" + e.getMessage() + ") on ArticleListAdapter.setHeader");
            e.printStackTrace();
        }
    }

    /**
     * layout 정의
     */
    protected void setItemLayout() {
        layoutHeader = new int[3 * SUBTHEME_NUMBER];
        layoutItem = new int[3 * SUBTHEME_NUMBER];

        layoutHeader[THEME_BOARD + SUBTHEME_ARTICLE] = R.layout.article_list_header;
        layoutHeader[THEME_BOARD + SUBTHEME_CLIP] = R.layout.article_list_clip_theme_header;
        layoutHeader[THEME_BOARD + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme_header;
        layoutHeader[THEME_MAINPAGE + SUBTHEME_ARTICLE] = R.layout.article_list_mainpage_header;
        layoutHeader[THEME_MAINPAGE + SUBTHEME_CLIP] = R.layout.article_list_mainpage_header;
        layoutHeader[THEME_MAINPAGE + SUBTHEME_ALBUM] = R.layout.article_list_mainpage_header; //TODO: clip album 분리 필요
        layoutHeader[THEME_ARTICLE_VIEWER + SUBTHEME_ARTICLE] = R.layout.article_list_header;
        layoutHeader[THEME_ARTICLE_VIEWER + SUBTHEME_CLIP] = R.layout.article_list_clip_theme_header;
        layoutHeader[THEME_ARTICLE_VIEWER + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme_header;

        layoutItem[THEME_BOARD + SUBTHEME_ARTICLE] = R.layout.article_list;
        layoutItem[THEME_BOARD + SUBTHEME_CLIP] = R.layout.article_list_clip_theme;
        layoutItem[THEME_BOARD + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme;
        layoutItem[THEME_MAINPAGE + SUBTHEME_ARTICLE] = R.layout.article_list_mainpage;
        layoutItem[THEME_MAINPAGE + SUBTHEME_CLIP] = R.layout.article_list_mainpage;
        layoutItem[THEME_MAINPAGE + SUBTHEME_ALBUM] = R.layout.article_list_mainpage; //TODO: clip album 분리 필요
        layoutItem[THEME_ARTICLE_VIEWER + SUBTHEME_ARTICLE] = R.layout.article_list;
        layoutItem[THEME_ARTICLE_VIEWER + SUBTHEME_CLIP] = R.layout.article_list_clip_theme;
        layoutItem[THEME_ARTICLE_VIEWER + SUBTHEME_ALBUM] = R.layout.article_list_clip_theme;
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
        return null;
    }

    /**
     * header content 정의
     */
    @Override
    protected void bindHeaderViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewholder;

        // header
        headerViewHolder.category.setText(category);
        headerViewHolder.icon.setImageResource(icon);
        if (TYPE_THEME == THEME_BOARD
                || TYPE_THEME == THEME_ARTICLE_VIEWER) {
            headerViewHolder.img.setImageResource(img);
            ImageView viewImg = headerViewHolder.img;
            // set marginTop of img
            if (viewImg.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) viewImg.getLayoutParams();
                final float scale = act.getResources().getDisplayMetrics().density;
                int pixels = (int) (imgMargin * scale + 0.5f);

                p.topMargin = pixels;
            }
            if (icons) { // icon menu
                headerViewHolder.icons.setVisibility(View.VISIBLE);

                headerViewHolder.iconTwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(URLTwitch + channelTwitch);

                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        act.startActivity(intent);
                    }
                });

                headerViewHolder.iconYoutube.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(URLYoutube + channelYoutube);

                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        act.startActivity(intent);
                    }
                });
            } else {
                headerViewHolder.icons.setVisibility(View.GONE);
            }

            if (headerHasItem) {
                if (mList.size() == 0) { // ArticleList에 Article 없을 경우 firstItem 숨김
                    headerViewHolder.itemFirst.setVisibility(View.GONE);
                } else {
                    headerViewHolder.itemFirst.setVisibility(View.VISIBLE);
                    bindItemViewHolder(viewholder, 0);
                }
            }
        } else { // TYPE_THEME == THEME_MAINPAGE
            headerViewHolder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) act).setCategory(mid, true);
                }
            });
        }
    }

    /**
     * item content 정의
     */
    @Override
    protected void bindItemViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int pos) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewholder;
        final Article article = (Article) mList.get(pos);

        itemViewHolder.title.setText(article.getTitle());
        itemViewHolder.author.setText(article.getAuthor());

        // levelIcon
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
                itemViewHolder.levelIcon.setVisibility(View.GONE);
            }
        }


        if ((TYPE_THEME == THEME_BOARD
                || TYPE_THEME == THEME_ARTICLE_VIEWER)
                && TYPE_SUBTHEME == SUBTHEME_ARTICLE) {
            itemViewHolder.time.setText(article.getTime());

            itemViewHolder.view.setText(article.getView());
            itemViewHolder.comment.setText(article.getComment());
            itemViewHolder.thumbnail.setVisibility(View.GONE);
        } else if (TYPE_THEME == THEME_MAINPAGE) {
            itemViewHolder.time.setText(article.getTime().substring(5).replace('.', '-'));
        }

        // thumbnail
        if (TYPE_SUBTHEME == SUBTHEME_CLIP
                || TYPE_SUBTHEME == SUBTHEME_ALBUM) {
            itemViewHolder.commentCount.setText(article.getComment());

            String thumbnailUrl = article.getThumbnailUrl();
            if (thumbnailUrl != null)
                Glide.with(act.getApplicationContext()).load(thumbnailUrl).into(itemViewHolder.thumbnail);
        }

        // click event
        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(article.getHref());

                //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Intent intent = new Intent(act, ArticleViewerActivity.class);
                intent.putExtra("article_title", article.getTitle());
                intent.putExtra("article_href", article.getHref());
                act.startActivity(intent);
            }
        };
        itemViewHolder.article.setOnClickListener(click);
        Log.v("ArticleListAdapter", (pos + 1) + "번째 게시글 표시됨 \"" + article.getTitle() + "\"(" + article.getHref() + ") on ArticleListAdapter.onBindViewHolder");


    }

    /**
     * footer content 정의
     */
    @Override
    protected void bindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {

    }
}
