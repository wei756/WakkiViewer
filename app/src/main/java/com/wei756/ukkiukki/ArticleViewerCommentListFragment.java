package com.wei756.ukkiukki;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.wei756.ukkiukki.Network.Web;

import java.util.ArrayList;
import java.util.Map;


public class ArticleViewerCommentListFragment extends Fragment {
    ArticleViewerActivity act;
    SwipeRefreshLayout mSwipeRefreshLayout;

    RecyclerView mRecyclerView;
    CommentAdapter mAdapter;
    EndlessRecyclerViewScrollListener scrollListener;
    RelativeLayout mProgressBar;

    Web web = Web.getInstance();

    private String articleId;
    private int page = 1;

    Map likeItMap;

    public static String LAST_PAGE = "마지막 페이지";

    private boolean loadedLastPage = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_viewer_comment_list, container, false);

        // RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_comment_list);
        mAdapter = new CommentAdapter(new ArrayList<Item>(), getContext(), CommentAdapter.THEME_COMMENTPAGE);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // ProgressBar
        mProgressBar = (RelativeLayout) view.findViewById(R.id.commentList_loadingPanel);

        // Endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadCommentList(page, false);
            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);
        mAdapter.setScrollListener(scrollListener); // for endless scroll

        // Swipe refresh
        mSwipeRefreshLayout = view.findViewById(R.id.layout_commentpage_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initCommentPage(articleId, likeItMap);
            }
        });

        return view;
    }

    public void initCommentPage(final String articleId, final Map likeItMap) {
        this.articleId = articleId;
        this.loadedLastPage = false;

        loadCommentList(1, true);
        this.likeItMap = likeItMap;
        if (likeItMap != null) {

        }
    }

    public void loadCommentList(final int page, final boolean reset) {
        setRefreshing(true);
        this.page = page;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (reset)
                    setVisibility(View.GONE);

                ArrayList<Comment> comments = null;
                if (!loadedLastPage) {
                    comments = web.getArticleCommentList(articleId, page, CommentAdapter.ORDERBY_UPLOAD); // 마지막 페이지가 아닐 때만 로드

                    if (comments != null) { // 댓글목록 로딩 성공시
                        if (comments.get(comments.size() - 1).getImgProfile() == LAST_PAGE) { // 로드된 게 마지막 페이지일 경우
                            comments.remove(comments.size() - 1);
                            loadedLastPage = true;
                        }

                        // 리스트 추가
                        if (reset)
                            mAdapter.setListWith(comments, act);
                        else
                            mAdapter.addListWith(comments, act);

                        setVisibility(View.VISIBLE);

                    } else {
                        mAdapter.clearList(act);
                    }
                }
                setRefreshing(false);
            }
        }).start();
    }

    public void setParentActivity(ArticleViewerActivity act) {
        this.act = act;
    }

    public void setVisibility(final int visibility) {
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

    public void setRefreshing(final boolean refreshing) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(refreshing);
            }
        });
    }
}
