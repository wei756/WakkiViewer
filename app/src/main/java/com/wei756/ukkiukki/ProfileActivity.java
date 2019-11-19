package com.wei756.ukkiukki;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.wei756.ukkiukki.Network.Web;
import com.wei756.ukkiukki.Preference.DarkModeManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

public class ProfileActivity extends AppCompatActivity
        implements RefreshListner {

    Toolbar toolbar;
    TabLayout tabLayout;

    SwipeRefreshLayout mSwipeRefreshLayout;

    // article page
    private ArticleList profileList;
    private RecyclerView profileView;
    private RelativeLayout profileViewProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundResource(R.color.colorTransparent);

        //// Set theme
        int color = R.color.colorBackground;
        int colorStatus = R.color.colorBackground;
        int colorStatusText = R.color.colorTextPrimary;

        // toolbar title
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundResource(color); // color
        getWindow().setStatusBarColor(getResources().getColor(colorStatus, null)); // colorStatus

        ToolbarColorizeHelper.colorizeToolbar(toolbar, getResources().getColor(colorStatusText, null), ProfileActivity.this); // colorStatusText

        ActionBarManager.setSystemBarTheme(ProfileActivity.this, DarkModeManager.getInstance(ProfileActivity.this).isNightModeEnabled()); // colorStatusText
        ////


        // Content
        profileView = (RecyclerView) findViewById(R.id.rv_profile_list);
        profileViewProgressBar = (RelativeLayout) findViewById(R.id.loadingPanel_profile_list);
        profileList = new ArticleList(this, profileView, profileViewProgressBar, ArticleListAdapter.THEME_PROFILE);

        // Tab layout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_profile);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                changeView(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_profile_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                changeView(tabLayout.getSelectedTabPosition()); // reset and update article list
            }
        });

        changeView(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeView(int index) {
        switch (index) {
            case 0:
                profileList.loadArticleList(CategoryManager.CATEGORY_PROFILE_ARTICLE, 1, true);
                break;
            case 1:
                profileList.loadArticleList(CategoryManager.CATEGORY_PROFILE_COMMENT, 1, true);
                break;
            case 2:
                profileList.loadArticleList(CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE, 1, true);
                break;
            case 3:
                profileList.loadArticleList(CategoryManager.CATEGORY_PROFILE_LIKEIT, 1, true);
                break;
            case 4:
                profileList.loadArticleList(CategoryManager.CATEGORY_PROFILE_WARDING, 1, true);
                break;
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
