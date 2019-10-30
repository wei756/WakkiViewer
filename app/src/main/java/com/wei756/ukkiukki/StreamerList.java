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

public class StreamerList {
    private final Activity act;

    private StreamerListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private ArrayList<Streamer> mList = new ArrayList<>();

    private final String[] mids = {"jinu", "temtem", "dduddi", "nanayang", "collet11", "gambler", "silph"};

    public StreamerList(Activity act, RecyclerView mRecyclerView) {
        this.act = act;

        // RecyclerView
        this.mRecyclerView = mRecyclerView;
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(act);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new StreamerListAdapter(new ArrayList<Streamer>(), act);
        mRecyclerView.setAdapter(mAdapter);

        CategoryManager category = CategoryManager.getInstance();
        try {
            for (String mid : mids) {
                mList.add(new Streamer(mid
                        , (String) category.getParam(mid, CategoryManager.NAME)
                        , (Integer) category.getParam(mid, CategoryManager.COLOR_DARK)
                        , (Integer) category.getParam(mid, CategoryManager.STREAMER_ICON)
                        , (String) category.getParam(mid, CategoryManager.CHANNEL_TWITCH)
                        , (String) category.getParam(mid, CategoryManager.CHANNEL_YOUTUBE)));
            }
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }

        mAdapter.setListWith(mList, act);

        //TODO:왁왁라이브
        //loadStreamersLive();
    }


    public void loadStreamersLive() {
        new Thread() {
            public void run() {
                int i = 0;
                for (String mid : mids)
                    mAdapter.setLive(i++, Web.getTwitchLive(mid));
            }

        }.start();
    }
/*
    @Override
    public void onLoadedArticleList(String mid, ArrayList arrayList, boolean reset) {
        setVisibility(View.VISIBLE); // 로딩 완료 후 표시
        mAdapter.setHeader(mid);

        if (reset == true)
            mAdapter.setListWith(arrayList, act);
        else
            mAdapter.addListWith(arrayList, act);
        Log.v("ArticleList", "" + page + " 페이지 로딩");

        setRefreshing(false);
    }

    @Override
    public void onLoadedArticleList(String mid, Exception e, boolean reset) {
        mAdapter.setHeader(mid);

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
    */
}

