package com.wei756.ukkiukki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RefreshListner {

    private Toolbar toolbar;

    private SearchView mSearchView;

    private String mid = "mainpage";

    // article page
    private View articlepageLayout;
    private ArticleList articleList;
    private RecyclerView articleView;
    private RelativeLayout articleViewProgressBar;

    // main page
    private View mainpageLayout;
    private ArticleList announcementList, freeList, creativeList;
    private RecyclerView announcementView, freeView, creativeView;
    private RelativeLayout announcementViewProgressBar, freeViewProgressBar, creativeViewProgressBar;
    private RelativeLayout announcementLayout, freeLayout, creativeLayout;

    // streamer page
    private View streamerpageLayout;
    private StreamerList streamerList;
    private RecyclerView streamerView;

    // floating action bar
    private FloatingActionButton fab;

    private ActionBarManager actBarManager = ActionBarManager.getInstance();

    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle("");
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_overflow_24dp));
        setSupportActionBar(toolbar);

        ImageView imgLogo = (ImageView) findViewById(R.id.img_appbar_main_logo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory("mainpage", true);
            }
        });

        // Floating button
        fab = findViewById(R.id.fab_write_article);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // search bar
        //toolbar.inflateMenu(R.menu.main);

        // set actionbar theme
        actBarManager.setActionBar(MainActivity.this, toolbar, "mainpage");

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Pages
        // article page
        articlepageLayout = findViewById(R.id.layout_articlepage);

        articleView = (RecyclerView) findViewById(R.id.articleList);
        articleViewProgressBar = (RelativeLayout) findViewById(R.id.articleList_loadingPanel);
        articleList = new ArticleList(this, articleView, articleViewProgressBar, ArticleListAdapter.THEME_BOARD);

        // main page
        mainpageLayout = findViewById(R.id.layout_mainpage);

        announcementView = (RecyclerView) findViewById(R.id.view_article_list_announcement);
        freeView = (RecyclerView) findViewById(R.id.view_article_list_free);
        creativeView = (RecyclerView) findViewById(R.id.view_article_list_creative);

        announcementViewProgressBar = (RelativeLayout) findViewById(R.id.view_article_list_announcement_loadingPanel);
        freeViewProgressBar = (RelativeLayout) findViewById(R.id.view_article_list_free_loadingPanel);
        creativeViewProgressBar = (RelativeLayout) findViewById(R.id.view_article_list_creative_loadingPanel);

        announcementLayout = (RelativeLayout) findViewById(R.id.layout_article_list_announcement);
        freeLayout = (RelativeLayout) findViewById(R.id.layout_article_list_free);
        creativeLayout = (RelativeLayout) findViewById(R.id.layout_article_list_creative);

        announcementList = new ArticleList(this, announcementView, announcementViewProgressBar, ArticleListAdapter.THEME_MAINPAGE);
        freeList = new ArticleList(this, freeView, freeViewProgressBar, ArticleListAdapter.THEME_MAINPAGE);
        //creativeList = new ArticleList(this, creativeView, creativeViewProgressBar, ArticleListAdapter.THEME_MAINPAGE);

        // streamer page
        streamerpageLayout = findViewById(R.id.layout_streamerpage);
        streamerView = (RecyclerView) findViewById(R.id.view_streamer_list_streamerList);
        streamerList = (StreamerList) new StreamerList(this, streamerView);


        // Swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_article_list_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setCategory(mid, false); // reset and update article list
                if (mid.equals("streamer")) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // 초기화면 로드
        setCategory(mid, true);

        // test naver cafe parsing
        CategoryManager categoryManager = CategoryManager.getInstance();
        Web.loadArticleList(null, 0, 1, true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) { // Drawer 열려있을 때
            drawer.closeDrawer(GravityCompat.START);
            doubleBackToExitPressedOnce = false;
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        } else {
            // 두 번 누러 종료
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.toast_text_press_back_again_to_exit, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // SearchView
        mSearchView = (SearchView) toolbar.getMenu().findItem(R.id.menu_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    /*
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_update) {
                // 다음 페이지 불러오기
                //page++;
                //Web.loadArticleList(MainActivity.this, mAdapter, mArrayList, mid, page, false); // update article list
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            setCategory("mainpage", true);
        } else if (id == R.id.nav_notice) {
            setCategory("announcement", true);
        } else if (id == R.id.nav_pixel_main) {
            setCategory("pixel_main", true);
        } else if (id == R.id.nav_pixel) {
            setCategory("pixel", true);
        } else if (id == R.id.nav_notice) {
            setCategory("announcement", true);
        } else if (id == R.id.nav_streamer) {
            setCategory("streamer", true);
        } else if (id == R.id.nav_jinu) {
            setCategory("jinu", true);
        } else if (id == R.id.nav_temtem) {
            setCategory("temtem", true);
        } else if (id == R.id.nav_dduddi) {
            setCategory("dduddi", true);
        } else if (id == R.id.nav_nanayang) {
            setCategory("nanayang", true);
        } else if (id == R.id.nav_collet) {
            setCategory("collet11", true);
        } else if (id == R.id.nav_gambler) {
            setCategory("gambler", true);
        } else if (id == R.id.nav_silph) {
            setCategory("silph", true);
        } else if (id == R.id.nav_free) {
            setCategory("free", true);
        } else if (id == R.id.nav_humor) {
            setCategory("humor", true);
        } else if (id == R.id.nav_humor_best) {
            setCategory("humor_best", true);
        } else if (id == R.id.nav_clip) {
            setCategory("clip", true);
        } else if (id == R.id.nav_creative) {
            setCategory("creative", true);
        } else if (id == R.id.nav_point) {
            setCategory("point", true);
        } else if (id == R.id.nav_commawang) {
            setCategory("commawang", true);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setCategory(String mid, boolean refresh) {
        this.mid = mid;

        if (mid.equals("mainpage")) { // 메인페이지
            mainpageLayout.setVisibility(View.VISIBLE);
            streamerpageLayout.setVisibility(View.GONE);
            articlepageLayout.setVisibility(View.GONE);

            //announcementList.loadArticleListFirst("announcement", refresh);
            //freeList.loadArticleListFirst("free", refresh);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.hide();
                }
            });
        } else if (mid.equals("streamer")) { //스트리머 페이지
            mainpageLayout.setVisibility(View.GONE);
            streamerpageLayout.setVisibility(View.VISIBLE);
            articlepageLayout.setVisibility(View.GONE);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.hide();
                }
            });
        } else {
            mainpageLayout.setVisibility(View.GONE);
            streamerpageLayout.setVisibility(View.GONE);
            articlepageLayout.setVisibility(View.VISIBLE);

            //articleList.loadArticleListFirst(mid, refresh);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.show();
                }
            });
        }
    }

    @Override
    public void onRefreshed(ArticleList articleList, final boolean refreshing) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(refreshing);
            }
        });
    }

}

interface RefreshListner {
    void onRefreshed(ArticleList articleList, boolean refreshing);
}