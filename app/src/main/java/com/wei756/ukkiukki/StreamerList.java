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

        //TODO:왁왁라이브
        //loadStreamersLive();
    }


    public void loadStreamersLive() {
        new Thread() {
            public void run() {
                int i = 0;
                for (String mid : mids)
                    mAdapter.setLive(i++, Web.getTwitchLive());
            }

        }.start();
    }
}

