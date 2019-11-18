package com.wei756.ukkiukki;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.wei756.ukkiukki.Network.Web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ArticleList implements LoadedListner {
    private Web web = Web.getInstance();

    private final Activity act;

    private ArticleListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RelativeLayout mProgressBar;
    private EndlessRecyclerViewScrollListener scrollListener;

    private boolean scollable;

    private int page = 1;
    private int mid = 0;
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
        setRefreshing(true);
        this.mid = mid;
        this.page = page;
        web.loadArticleList(this, mid, page, refresh); // update article list
    }

    private void setRefreshing(boolean refreshing) {
        if (act instanceof MainActivity)
            ((MainActivity) act).onRefreshed(this, refreshing);
    }

    @Override
    public void onLoadedArticleList(int mid, ArrayList arrayList, boolean reset) {
        setVisibility(View.VISIBLE); // 로딩 완료 후 표시
        setMidTheme(mid, reset);
        if (theme == ArticleListAdapter.THEME_HEADER_POPULAR)
            Collections.shuffle(arrayList);
        if (reset == true)
            mAdapter.setListWith(arrayList, act);
        else
            mAdapter.addListWith(arrayList, act);
        Log.v("ArticleList", "" + page + " 페이지 로딩");

        setRefreshing(false);
    }

    @Override
    public void onLoadedArticleList(int mid, Exception e, boolean reset) {
        setMidTheme(mid, reset);

        if (reset)
            mAdapter.clearList(act);
        else
            setVisibility(View.VISIBLE); // 로딩 완료 후 표시

        if (e instanceof NullPointerException) {
            if (reset) {
                //TODO: 게시판에 글이 없을 때
                Log.i("ArticleList", "게시판에 글이 없습니다. on onLoadedArticleList");
            } else {
                //TODO: 마지막 페이지일 때
                Log.i("ArticleList", "마지막 페이지입니다. on onLoadedArticleList");
            }
        }

        if (e instanceof IOException) {
            //TODO: 인터넷 연결이 안 될 때
            Log.w("ArticleList.err", "인터넷에 연결되지 않았습니다. on onLoadedArticleList");
        }
        setRefreshing(false);
    }

    private void setVisibility(final int visibility) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.setVisibility(visibility);
                if (mProgressBar != null) {
                    if (visibility == View.GONE)
                        mProgressBar.setVisibility(View.VISIBLE);
                    else
                        mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setMidTheme(final int mid, boolean refresh) {
        mAdapter.setMidTheme(mid);
        if (refresh) {
            // 레이아웃 전환
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                                loadArticleList(mid, page, false);
                            }
                        };
                        mRecyclerView.addOnScrollListener(scrollListener);
                        mAdapter.setScrollListener(scrollListener); // for endless scroll
                    }
                }
            });
        }
    }
}

interface LoadedListner {
    void onLoadedArticleList(int mid, ArrayList arrayList, boolean reset);

    void onLoadedArticleList(int mid, Exception e, boolean reset);
}
