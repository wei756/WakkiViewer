package com.wei756.ukkiukki;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class ArticleList implements LoadedListner {
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
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(act);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new ArticleListAdapter(new ArrayList<Item>(), act, theme, this.mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        // Endless scrolling
        this.scollable = mAdapter.getScollable();
        if (scollable) {
            scrollListener = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    loadArticleList(mid, true, false);
                }
            };
            mRecyclerView.addOnScrollListener(scrollListener);
            mAdapter.setScrollListener(scrollListener); // for endless scroll

            // 구분선
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    mLinearLayoutManager.getOrientation());
            mRecyclerView.addItemDecoration(dividerItemDecoration);
        }

        setVisibility(View.GONE);
    }

    public void loadArticleList(int mid, boolean moreLoad, boolean refresh) {
        setRefreshing(true);
        this.mid = mid;
        this.page = page;
        Web.loadArticleList(this, mid, page, refresh); // update article list
    }

    private void setRefreshing(boolean refreshing) {
        if (act instanceof MainActivity)
            ((MainActivity) act).onRefreshed(this, refreshing);
    }

    @Override
    public void onLoadedArticleList(int mid, ArrayList arrayList, boolean reset) {
        setVisibility(View.VISIBLE); // 로딩 완료 후 표시
        //mAdapter.setTheme(mid);

        if (reset == true)
            mAdapter.setListWith(arrayList, act);
        else
            mAdapter.addListWith(arrayList, act);
        Log.v("ArticleList", "" + page + " 페이지 로딩");

        setRefreshing(false);
    }

    @Override
    public void onLoadedArticleList(int mid, Exception e, boolean reset) {
        //mAdapter.setTheme(mid);

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
                if (visibility == View.GONE)
                    mProgressBar.setVisibility(View.VISIBLE);
                else
                    mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}

interface LoadedListner {
    void onLoadedArticleList(int mid, ArrayList arrayList, boolean reset);

    void onLoadedArticleList(int mid, Exception e, boolean reset);
}
