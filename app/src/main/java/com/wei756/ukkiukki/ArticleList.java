package com.wei756.ukkiukki;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.wei756.ukkiukki.Network.CafeApiRequestBuilder;
import com.wei756.ukkiukki.Network.Web;

import java.util.ArrayList;
import java.util.Collections;

public class ArticleList {
    private Web web = Web.getInstance();

    private final Activity act;

    private ArticleListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RelativeLayout mProgressBar;
    private EndlessRecyclerViewScrollListener scrollListener;

    private boolean scollable;

    private int page = 1;
    private int mid = 0;
    private String userId; // ProfileActivity only
    private int theme;

    public ArticleList(Activity act, RecyclerView mRecyclerView, RelativeLayout mProgressBar, int theme) {
        this.act = act;
        this.theme = theme;

        // ProgressBar
        this.mProgressBar = mProgressBar;

        // RecyclerView
        this.mRecyclerView = mRecyclerView;
        mRecyclerView.setHasFixedSize(true);

        // RecyclerViewAdapter
        mAdapter = new ArticleListAdapter(new ArrayList<Item>(), act, theme, this.mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        setMidTheme(mid, true);

        setVisibility(View.GONE);
    }

    public void loadArticleList(int mid, int page, boolean refresh) {
        loadArticleList(mid, page, null, refresh);
    }

    public void loadArticleList(int mid, int page, @Nullable String userId, boolean refresh) {
        setRefreshing(true);
        this.mid = mid;
        this.page = page;
        this.userId = userId;
        new Thread(() -> {
            //ArrayList<Article> articleList = web.getArticleList(act, mid, page, userId); // update article list
            CafeApiRequestBuilder requestBuilder = new CafeApiRequestBuilder();

            requestBuilder.setContext(act) // context
                    .setMid(mid); // 메뉴 id

            Article lastArticle;
            String lastArticleId = null, lastArticleTimestamp = null;
            if (page != 1 && mAdapter.getArrayList().size() > 0) {
                lastArticle = ((Article) mAdapter.getArrayList().get(mAdapter.getArrayList().size() - 1));
                lastArticleId = lastArticle.getHref();
                lastArticleTimestamp = lastArticle.getTimestamp();
                if (lastArticleId != null)
                    requestBuilder.setLastArticleId(lastArticleId); // lastArticleId
                if (lastArticleTimestamp != null)
                    requestBuilder.setLastTimestamp(Long.parseLong(lastArticleTimestamp)); // lastArticle Timestamp
            }

            if (mid < 0) // 일반 게시판이 아니면
                requestBuilder.setRequestType(mid); // requestType
            else // 일반 게시판
                requestBuilder.setRequestType(CafeApiRequestBuilder.REQUEST_ARTICLELIST_BOARD);

            requestBuilder.setUserId(userId); // userId

            ArrayList<Item> articleList = requestBuilder.build(); // get article list

            // callback
            setMidTheme(mid, refresh);
            if (articleList.size() > 0 && ((Article) articleList.get(0)).getErrorcode() == Web.RETURNCODE_SUCCESS) { // 로딩 성공이고 표시할 내용이 있을 경우
                setVisibility(View.VISIBLE); // 로딩 완료 후 표시

                if (theme == ArticleListAdapter.THEME_HEADER_POPULAR) // 앱바헤더 인기글 리스트 셔플
                    Collections.shuffle(articleList);

                if (refresh)
                    mAdapter.setListWith(articleList, act);
                else
                    mAdapter.addListWith(articleList, act);
                Log.v("ArticleList", "" + page + " 페이지 로딩");


            } else if (articleList.size() == 0) { // 글이 없을 때

                if (refresh)
                    mAdapter.clearList(act);
                else
                    setVisibility(View.VISIBLE); // 로딩 완료 후 표시

                if (refresh) {
                    //TODO: 게시판에 글이 없을 때
                    Log.i("ArticleList", "게시판에 글이 없습니다. on loadArticleList");
                } else {
                    Log.i("ArticleList", "마지막 페이지입니다. on loadArticleList");
                }

            } else { // 에러
                if (refresh)
                    mAdapter.clearList(act);
                else
                    setVisibility(View.VISIBLE); // 로딩 완료 후 표시

                //TODO: 인터넷 연결이 안 될 때
                Log.w("ArticleList.err", "인터넷에 연결되지 않았습니다. on loadArticleList");
            }
            setRefreshing(false);
        }).start();
    }

    private void setRefreshing(boolean refreshing) {
        if (act instanceof RefreshListner)
            ((RefreshListner) act).onRefreshed(this, refreshing);
    }

    private void setVisibility(final int visibility) {
        act.runOnUiThread(() -> {
            mRecyclerView.setVisibility(visibility);
            if (mProgressBar != null) {
                if (visibility == View.GONE)
                    mProgressBar.setVisibility(View.VISIBLE);
                else
                    mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setMidTheme(final int mid, boolean refresh) {
        mAdapter.setMidTheme(mid);
        if (refresh) {
            // 레이아웃 전환
            act.runOnUiThread(() -> {
                mRecyclerView.removeAllViewsInLayout();
                mRecyclerView.setAdapter(mAdapter);
                RecyclerView.LayoutManager mLayoutManager;
                if (mAdapter.TYPE_THEME == ArticleListAdapter.THEME_BOARD) {
                    if (mAdapter.TYPE_SUBTHEME == ArticleListAdapter.SUBTHEME_POPULAR) {
                        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                    } else {
                        mLayoutManager = new LinearLayoutManager(act);
                        ((LinearLayoutManager) mLayoutManager).setOrientation(RecyclerView.VERTICAL);
                    }
                } else if (mAdapter.TYPE_THEME == ArticleListAdapter.THEME_HEADER_POPULAR) {
                    mLayoutManager = new LinearLayoutManager(act);
                    ((LinearLayoutManager) mLayoutManager).setOrientation(RecyclerView.HORIZONTAL);
                } else {
                    mLayoutManager = new LinearLayoutManager(act);
                    ((LinearLayoutManager) mLayoutManager).setOrientation(RecyclerView.VERTICAL);
                }
                mRecyclerView.setLayoutManager(mLayoutManager);

                // Endless scrolling
                scollable = mAdapter.getScollable();
                if (scollable && mAdapter.TYPE_SUBTHEME != ArticleListAdapter.SUBTHEME_POPULAR) {
                    scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
                        @Override
                        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                            if (mid == CategoryManager.CATEGORY_PROFILE_LIKEIT) { // 좋아요한 글
                                loadArticleList(mid, (int) (Long.parseLong(((Article) mAdapter.getArrayList().get(mAdapter.getArrayList().size() - 1)).getTimestamp()) / 1000), userId, false);
                            } else
                                loadArticleList(mid, page, userId, false);
                        }
                    };
                    mRecyclerView.addOnScrollListener(scrollListener);
                    mAdapter.setScrollListener(scrollListener); // for endless scroll
                }
            });
        }
    }
}