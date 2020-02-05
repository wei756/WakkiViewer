package com.wei756.ukkiukki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RefreshListner {

    // Section Top bar
    /**
     * NavDrawer, Overflow 버튼 등이 표시되는 툴바
     */
    private Toolbar toolbar;

    /**
     * 상단바 전체
     */
    private AppBarLayout appBarLayout;

    private SearchView mSearchView;

    /**
     * 하단 네비게이션 뷰
     */
    public NavigationView navigationView;

    /**
     * NavDrawer 토글 버튼
     */
    private ActionBarDrawerToggle toggle;

    /**
     * Mainpage 에 실제 표시중인 게시판 id
     */
    private int mid = CategoryManager.CATEGORY_MAINPAGE;

    /**
     * Mainpage 에 표시중이던 게시판 id(Tab 전환시 고정)
     */
    private int midTab = CategoryManager.CATEGORY_MAINPAGE;


    // Section Header
    /**
     * AppBar 내의 레이아웃 전체(이하 헤더)
     */
    private CollapsingToolbarLayout collapsingToolbarLayout;

    /**
     * 헤더 배경
     */
    private ImageView ivHeaderBackground;

    /**
     * 앱 로고
     */
    private ImageView ivLogo;

    /**
     * 멤버수, 카페정보 표시되는 레이아웃
     */
    private LinearLayout layoutHeader;

    /**
     * 헤더에 표시되는 인기글 리스트(1개 표시)
     */
    private ArticleList popularList;

    /**
     * popularList가 표시될 뷰
     */
    private RecyclerView popularView;

    /**
     * 헤더 인기글 더보기 버튼
     */
    private TextView popularMore;

    /**
     * 헤더 인기글 레이아웃
     */
    private ConstraintLayout popularBackground;
    //private ImageView popularBackground, popularIcon;
    //private LinearLayout popularInfo;

    private ScheduledExecutorService servicePopularShuffle;
    private Thread taskPopularShuffle;

    /**
     * 상단 탭 레이아웃
     */
    private TabLayout headerTabLayout;

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
        btnLogin.setOnClickListener(view -> {
            if (WebClientManager.getInstance().getLogined()) { // 프로필 페이지
                ProfileActivity.startProfileActivity(MainActivity.this, ProfileManager.getInstance().getId());
            } else { // 로그인 페이지
                // open login page
                Intent intent = new Intent(MainActivity.this, NaverLoginActivity.class);
                intent.putExtra("login_type", "LOGIN");
                startActivity(intent);
            }

            drawer.closeDrawer(GravityCompat.START);
        });
        TextView btnLogout = (TextView) navigationView.getHeaderView(0).findViewById(R.id.btn_nav_logout); // 로그아웃 버튼
        btnLogout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.app_name);
            builder.setMessage(R.string.real_logout);
            builder.setPositiveButton(R.string.dialog_yes,
                    (dialog, which) -> {
                        // open logout page
                        Intent intent = new Intent(MainActivity.this, NaverLoginActivity.class);
                        intent.putExtra("login_type", "LOGOUT");
                        startActivity(intent);
                    });
            builder.setNegativeButton(R.string.dialog_no,
                    (dialog, which) -> {
                    });
            builder.show();

            drawer.closeDrawer(GravityCompat.START);
        });

        // menu
        toolbar.inflateMenu(R.menu.main);


        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // appbar header popular list
        collapsingToolbarLayout = findViewById(R.id.layout_article_list_collapsing_appbar);

        ivHeaderBackground = findViewById(R.id.iv_article_list_header_background);

        ivLogo = findViewById(R.id.iv_article_list_header_icon);
        layoutHeader = findViewById(R.id.layout_article_list_header_info);

        popularBackground = findViewById(R.id.layout_header_popular);
        popularMore = findViewById(R.id.tv_popular_article_icon);

        popularView = (RecyclerView) findViewById(R.id.rv_popular_article_title);
        popularList = new ArticleList(this, popularView, null, ArticleListAdapter.THEME_HEADER_POPULAR);

        popularMore.setOnClickListener((view -> setCategory(CategoryManager.CATEGORY_POPULAR_ARTICLE, true, true)));

        // 인기글 자동전환
        taskPopularShuffle = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    popularList.shuffle(MainActivity.this);
                    Thread.sleep(20 * 1000);

                }
            } catch (InterruptedException e) {

            }
        });
        taskPopularShuffle.start();
        //servicePopularShuffle = Executors.newSingleThreadScheduledExecutor();

        //servicePopularShuffle.scheduleAtFixedRate(taskPopularShuffle, 10 * 1000, 3 * 1000, TimeUnit.MILLISECONDS);

        appBarLayout = findViewById(R.id.layout_article_list_appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout, i) -> {
            float percentage = 1 - ((float) Math.abs(i) / appBarLayout.getTotalScrollRange());
            popularMore.setAlpha(percentage);
            popularView.setAlpha(percentage);
        });

        // tab layout
        headerTabLayout = findViewById(R.id.tab_layout_category);
        headerTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                changeTab(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


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
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            setCategory(mid, false, false); // reset and update article list
            Web.getInstance().loadMyInfomation();
        });

        // 초기화면 로드
        setCategory(mid, true, true);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (taskPopularShuffle.isAlive())
            taskPopularShuffle.interrupt();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!taskPopularShuffle.isAlive()) {
            taskPopularShuffle = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        popularList.shuffle(MainActivity.this);
                        Thread.sleep(20 * 1000);

                    }
                } catch (InterruptedException e) {

                }
            });
            taskPopularShuffle.start();
        }
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

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
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
                setCategory(CategoryManager.CATEGORY_MAINPAGE, true, true);
            } else {
                setCategory(mid, true, true);
            }
        } catch (InvalidCategoryException e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * headerTabLayout에 선택된 탭에 따라 카테고리를 전환합니다.
     *
     * @param index
     */
    private void changeTab(int index) {
        int midPrev = this.mid;
        int mid = midPrev;
        if (midTab == CategoryManager.CATEGORY_ALLLIST) // 전체글보기
            switch (index) {
                case 0:
                    mid = CategoryManager.CATEGORY_ALLLIST;
                    break;
                case 1:
                    mid = CategoryManager.CATEGORY_POPULAR_ARTICLE;
                    break;
                case 2:
                    mid = CategoryManager.CATEGORY_NOTICE;
                    break;
            }
        else if (midTab > 0) // 일반 게시판
            switch (index) {
                case 0:
                    mid = midTab;
                    break;
                case 1:
                    mid = CategoryManager.CATEGORY_NOTICE;
                    break;
            }
        else if (midTab == CategoryManager.CATEGORY_POPULAR_ARTICLE) // 인기글
            switch (index) {
                case 0:
                    mid = CategoryManager.CATEGORY_POPULAR_ARTICLE; // 인기글
                    break;
                case 1:
                    //mid = CategoryManager.CATEGORY_NOTICE; // 댓글 TOP
                    break;
                case 2:
                    //mid = CategoryManager.CATEGORY_NOTICE; // 좋아요 TOP
                    break;
            }
        if (midPrev != mid)
            setCategory(mid, false, true);
    }

    /**
     * 게시판에 따른 headerTabLayout을 설정합니다.
     *
     * @param mid
     */
    private void changeTabLayout(int mid) {
        this.midTab = mid;

        // Set visibility
        if (midTab == CategoryManager.CATEGORY_MAINPAGE)  // 메인페이지
            headerTabLayout.setVisibility(View.GONE);
        else // 그 외에
            headerTabLayout.setVisibility(View.VISIBLE);

        // Tab Title
        headerTabLayout.removeAllTabs(); // reset tab
        if (midTab == CategoryManager.CATEGORY_ALLLIST) { // 전체글보기
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_all_article));
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_popular_article));
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_all_notice));

        } else if (midTab > 0) { // 일반 게시판
            try {
                headerTabLayout.addTab(
                        headerTabLayout.newTab().setText(
                                (String) CategoryManager.getInstance().getParam(midTab, CategoryManager.NAME)
                        ));
            } catch (InvalidCategoryException e) {
                e.printStackTrace();
            }
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_notice));

        } else if (midTab == CategoryManager.CATEGORY_POPULAR_ARTICLE) { // 인기글
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_popular_article));
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_popular_top_comment));
            headerTabLayout.addTab(
                    headerTabLayout.newTab().setText(R.string.tab_main_popular_top_likeit));

        }
    }

    /**
     * MainActivity에 표시될 게시판을 설정합니다.
     *
     * @param mid       게시판 id
     * @param changeTab 탭 전환 여부
     * @param refresh   게시판 테마 초기화 여부
     */
    public void setCategory(int mid, boolean changeTab, boolean refresh) {
        this.mid = mid;
        if (changeTab)
            changeTabLayout(mid);

        if (refresh) {
            // set actionbar theme
            actBarManager.setActionBar(MainActivity.this, toolbar, mid, toggle);
            Web.getInstance().loadMyInfomation();
            if (mid != CategoryManager.CATEGORY_POPULAR_ARTICLE &&
                    mid != CategoryManager.CATEGORY_LOGIN &&
                    mid != CategoryManager.CATEGORY_NOTICE) {
                setPopularList(true);

                popularList.loadArticleList(CategoryManager.CATEGORY_POPULAR_ARTICLE, 1, true);
            } else {
                setPopularList(false);
            }
        }


        if (mid == CategoryManager.CATEGORY_MAINPAGE) { // 메인페이지
            mainpageLayout.setVisibility(View.VISIBLE);
            articlepageLayout.setVisibility(View.GONE);

            freeList.loadArticleList(CategoryManager.CATEGORY_ALLLIST, 1, true);
            creativeList.loadArticleList(59, 1, true);

            runOnUiThread(() -> {
                //fab.hide();
            });
        } else if (mid == CategoryManager.CATEGORY_LOGIN) { // 로그인페이지
            Intent intent = new Intent(this, NaverLoginActivity.class);
            this.startActivity(intent);
            setCategory(CategoryManager.CATEGORY_MAINPAGE, true, true);
        } else { // 게시판 페이지
            mainpageLayout.setVisibility(View.GONE);
            articlepageLayout.setVisibility(View.VISIBLE);

            articleList.loadArticleList(mid, 1, true);

            runOnUiThread(() -> {
                //fab.show();
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
            popularBackground.setVisibility(View.VISIBLE);
            collapsingToolbarLayout.setExpandedTitleMarginBottom(dpToPx(this, 82));

            CollapsingToolbarLayout.LayoutParams ivParams = (CollapsingToolbarLayout.LayoutParams) ivLogo.getLayoutParams();
            CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) layoutHeader.getLayoutParams();
            CollapsingToolbarLayout.LayoutParams ivBackgroundParams = (CollapsingToolbarLayout.LayoutParams) ivHeaderBackground.getLayoutParams();


            ivParams.bottomMargin = dpToPx(this, 54);
            layoutParams.bottomMargin = dpToPx(this, 58);
            ivBackgroundParams.height = dpToPx(this, 180);

            ivLogo.setLayoutParams(ivParams);
            layoutHeader.setLayoutParams(layoutParams);
            ivHeaderBackground.setLayoutParams(ivBackgroundParams);
            //collapsingToolbarLayout.setTitleEnabled(true);
        } else {
            int heightPopularArticle = 40;

            popularBackground.setVisibility(View.GONE);
            collapsingToolbarLayout.setExpandedTitleMarginBottom(dpToPx(this, 82 - heightPopularArticle));

            CollapsingToolbarLayout.LayoutParams ivParams = (CollapsingToolbarLayout.LayoutParams) ivLogo.getLayoutParams();
            CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) layoutHeader.getLayoutParams();
            CollapsingToolbarLayout.LayoutParams ivBackgroundParams = (CollapsingToolbarLayout.LayoutParams) ivHeaderBackground.getLayoutParams();

            ivParams.bottomMargin = dpToPx(this, 54 - heightPopularArticle);
            layoutParams.bottomMargin = dpToPx(this, 58 - heightPopularArticle);
            ivBackgroundParams.height = dpToPx(this, 180 - heightPopularArticle);

            ivLogo.setLayoutParams(ivParams);
            layoutHeader.setLayoutParams(layoutParams);
            ivHeaderBackground.setLayoutParams(ivBackgroundParams);
            //collapsingToolbarLayout.setTitleEnabled(true);
        }
    }

    public int dpToPx(Context context, float dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    @Override
    public void onRefreshed(ArticleList articleList, final boolean refreshing) {
        runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
    }

}

interface RefreshListner {
    void onRefreshed(ArticleList articleList, boolean refreshing);
}