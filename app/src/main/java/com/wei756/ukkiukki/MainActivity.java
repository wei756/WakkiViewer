package com.wei756.ukkiukki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.wei756.ukkiukki.Network.Web;
import com.wei756.ukkiukki.Network.WebClientManager;
import com.wei756.ukkiukki.Preference.DarkModeManager;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RefreshListner {

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private SearchView mSearchView;

    public NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private int mid = CategoryManager.CATEGORY_MAINPAGE;

    // header popular list
    private CollapsingToolbarLayout collapsingToolbarLayout;
    //private ArticleList popularList;
    //private RecyclerView popularView;
    private TextView popularMore, popularArticle;
    //private ImageView popularBackground, popularIcon;
    //private LinearLayout popularInfo;

    // article page
    private View articlepageLayout;
    private ArticleList articleList;
    private RecyclerView articleView;
    private RelativeLayout articleViewProgressBar;

    // main page
    private View mainpageLayout;
    private ArticleList freeList, creativeList;
    private RecyclerView freeView, creativeView;
    private RelativeLayout freeViewProgressBar, creativeViewProgressBar;
    private RelativeLayout freeLayout, creativeLayout;


    // floating action bar
    //private FloatingActionButton fab;

    private ActionBarManager actBarManager = ActionBarManager.getInstance();

    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dark mode
        DarkModeManager.getInstance(this).setDefaultNightMode();

        // Toolbar
        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle(R.string.app_name);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_overflow_24dp));
        setSupportActionBar(toolbar);

        // Floating button
        /*
        fab = findViewById(R.id.fab_write_article);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        // Bottom navigation view
        BottomNavigationView mBottomNavigationView = findViewById(R.id.view_main_bottom_navview);
        mBottomNavigationView.getMenu().setGroupCheckable(0, false, true);

        // Navigation drawer
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        final ProfileManager profileManager = ProfileManager.getInstance();
        profileManager.setNavigationView(navigationView);
        CategoryManager.getInstance().updateCategoryMenu(MainActivity.this, navigationView.getMenu());
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                profileManager.updateNavigationDrawerProfile(MainActivity.this);
                CategoryManager.getInstance().updateCategoryMenu(MainActivity.this, navigationView.getMenu());
            }
        };

        ConstraintLayout btnLogin = (ConstraintLayout) navigationView.getHeaderView(0).findViewById(R.id.layout_nav_profile); // 로그인 버튼
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WebClientManager.getInstance().getLogined()) { // 프로필 페이지
                    // open profile page
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("id", ProfileManager.getInstance().getId());
                    startActivity(intent);
                } else { // 로그인 페이지
                    // open login page
                    Intent intent = new Intent(MainActivity.this, NaverLoginActivity.class);
                    intent.putExtra("login_type", "LOGIN");
                    startActivity(intent);
                }

                drawer.closeDrawer(GravityCompat.START);
            }
        });
        TextView btnLogout = (TextView) navigationView.getHeaderView(0).findViewById(R.id.btn_nav_logout); // 로그아웃 버튼
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.real_logout);
                builder.setPositiveButton(R.string.dialog_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // open logout page
                                Intent intent = new Intent(MainActivity.this, NaverLoginActivity.class);
                                intent.putExtra("login_type", "LOGOUT");
                                startActivity(intent);
                            }
                        });
                builder.setNegativeButton(R.string.dialog_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();

                drawer.closeDrawer(GravityCompat.START);
            }
        });

        // menu
        toolbar.inflateMenu(R.menu.main);


        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // appbar header popular list
        collapsingToolbarLayout = findViewById(R.id.layout_article_list_collapsing_appbar);

        /*
        popularView = findViewById(R.id.view_appbar_popular);
        popularBackground = findViewById(R.id.iv_article_list_header_background);
        popularIcon = findViewById(R.id.iv_article_list_header_icon);
        popularInfo = findViewById(R.id.layout_article_list_header_info);
        */
        popularMore = findViewById(R.id.tv_popular_article_icon);
        popularArticle = findViewById(R.id.tv_popular_article_title);

        popularMore.setOnClickListener((view -> setCategory(CategoryManager.CATEGORY_POPULAR_ARTICLE, true)));

        appBarLayout = findViewById(R.id.layout_article_list_appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout, i) -> {
            float percentage = 1 - ((float)Math.abs(i)/appBarLayout.getTotalScrollRange());
            popularMore.setAlpha(percentage);
            popularArticle.setAlpha(percentage);
        });

        //TODO: 대표 인기글 설정
        //popularList = new ArticleList(this, popularView, null, ArticleListAdapter.THEME_HEADER_POPULAR);

        // Pages
        // article page
        articlepageLayout = findViewById(R.id.layout_articlepage);

        articleView = (RecyclerView) findViewById(R.id.articleList);
        articleViewProgressBar = (RelativeLayout) findViewById(R.id.articleList_loadingPanel);
        articleList = new ArticleList(this, articleView, articleViewProgressBar, ArticleListAdapter.THEME_BOARD);

        // main page
        mainpageLayout = findViewById(R.id.layout_mainpage);

        freeView = (RecyclerView) findViewById(R.id.view_article_list_free);
        creativeView = (RecyclerView) findViewById(R.id.view_article_list_creative);

        freeViewProgressBar = (RelativeLayout) findViewById(R.id.view_article_list_free_loadingPanel);
        creativeViewProgressBar = (RelativeLayout) findViewById(R.id.view_article_list_creative_loadingPanel);

        freeLayout = (RelativeLayout) findViewById(R.id.layout_article_list_free);
        creativeLayout = (RelativeLayout) findViewById(R.id.layout_article_list_creative);

        freeList = new ArticleList(this, freeView, freeViewProgressBar, ArticleListAdapter.THEME_MAINPAGE);
        creativeList = new ArticleList(this, creativeView, creativeViewProgressBar, ArticleListAdapter.THEME_MAINPAGE);

        // Swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_article_list_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setCategory(mid, false); // reset and update article list
                Web.getInstance().loadMyInfomation();
            }
        });

        // 초기화면 로드
        setCategory(mid, true);

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
            // 두 번 눌러 종료
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
        /*
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
        */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_darkmode) {
            // 다크모드 전환
            DarkModeManager darkModeManager = DarkModeManager.getInstance(this);
            darkModeManager.toggleIsNightModeEnabled();
            darkModeManager.setDefaultNightMode();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        String id = (String) item.getTitle();

        int mid;
        try {
            mid = CategoryManager.getInstance().findIdByName(id);

            if (mid == CategoryManager.CATEGORY_MAINPAGE) {
                setCategory(CategoryManager.CATEGORY_MAINPAGE, true);
            } else {
                setCategory(mid, true);
            }
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setCategory(int mid, boolean refresh) {
        this.mid = mid;
        if (refresh) {
            // set actionbar theme
            actBarManager.setActionBar(MainActivity.this, toolbar, mid, toggle);
            Web.getInstance().loadMyInfomation();
            if (mid != CategoryManager.CATEGORY_POPULAR_ARTICLE
                    && mid != CategoryManager.CATEGORY_LOGIN) {
                setPopularList(true);

                //popularList.loadArticleList(CategoryManager.CATEGORY_POPULAR_ARTICLE, 1, true);
            } else {
                setPopularList(false);
            }
        }


        if (mid == CategoryManager.CATEGORY_MAINPAGE) { // 메인페이지
            mainpageLayout.setVisibility(View.VISIBLE);
            articlepageLayout.setVisibility(View.GONE);

            freeList.loadArticleList(CategoryManager.CATEGORY_ALLLIST, 1, true);
            creativeList.loadArticleList(59, 1, true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //fab.hide();
                }
            });
        } else if (mid == CategoryManager.CATEGORY_LOGIN) { // 로그인페이지
            Intent intent = new Intent(this, NaverLoginActivity.class);
            this.startActivity(intent);
            setCategory(CategoryManager.CATEGORY_MAINPAGE, true);
        } else { // 게시판 페이지
            mainpageLayout.setVisibility(View.GONE);
            articlepageLayout.setVisibility(View.VISIBLE);

            articleList.loadArticleList(mid, 1, true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //fab.show();
                }
            });
        }
    }

    /**
     * CollapsingAppbarLayout 에 인기글 리스트 표시여부를 설정합니다.
     *
     * @param open 인기글 리스트 표시여부
     */
    public void setPopularList(boolean open) {
        if (open) {
            popularMore.setVisibility(View.VISIBLE);
            popularArticle.setVisibility(View.VISIBLE);
            collapsingToolbarLayout.setTitleEnabled(true);
        } else {
            popularMore.setVisibility(View.GONE);
            popularArticle.setVisibility(View.GONE);
            collapsingToolbarLayout.setTitleEnabled(false);
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