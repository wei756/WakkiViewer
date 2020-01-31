package com.wei756.ukkiukki;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity
        implements RefreshListner {

    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    TabLayout tabLayout;

    SwipeRefreshLayout mSwipeRefreshLayout;

    // article page
    private ArticleList profileList;
    private RecyclerView profileView;
    private RelativeLayout profileViewProgressBar;

    private TextView tvUserId, tvGrade, tvVisit;
    private ImageView ivProfile;
    private Button btnProfileSetting, btnGradeInfo;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        collapsingToolbarLayout = findViewById(R.id.layout_profile_collapsing_appbar);

        toolbar = findViewById(R.id.toolbar_profile);
        toolbar.setTitle("");
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

        // get intent
        Intent intent = new Intent(this.getIntent());
        userId = intent.getStringExtra("userId");

        // Content
        profileView = (RecyclerView) findViewById(R.id.rv_profile_list);
        profileViewProgressBar = (RelativeLayout) findViewById(R.id.loadingPanel_profile_list);
        profileList = new ArticleList(this, profileView, profileViewProgressBar, ArticleListAdapter.THEME_PROFILE);

        // Profile info
        tvUserId = (TextView) findViewById(R.id.tv_profile_id);
        tvGrade = (TextView) findViewById(R.id.tv_profile_grade);
        tvVisit = (TextView) findViewById(R.id.tv_profile_visit);
        ivProfile = (ImageView) findViewById(R.id.iv_profile_profile);
        btnProfileSetting = (Button) findViewById(R.id.btn_profile_profile_options);
        btnGradeInfo = (Button) findViewById(R.id.btn_profile_membar_grade_info);

        // Load member info
        loadMemberInfo();

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
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            changeView(tabLayout.getSelectedTabPosition()); // reset and update article list
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
        int type = 0;
        switch (index) {
            case 0:
                type = CategoryManager.CATEGORY_PROFILE_ARTICLE;
                break;
            case 1:
                type = CategoryManager.CATEGORY_PROFILE_COMMENT;
                break;
            case 2:
                type = CategoryManager.CATEGORY_PROFILE_COMMENT_ARTICLE;
                break;
            case 3:
                type = CategoryManager.CATEGORY_PROFILE_LIKEIT;
                break;
            case 4:
                type = CategoryManager.CATEGORY_PROFILE_WARDING;
                break;
        }
        profileList.loadArticleList(type, 1, userId, true);
    }

    private void loadMemberInfo() {
        new Thread(() -> {
            Map mapInfo = Web.getInstance().getProfile(userId);

            // callback
            int status = (int)mapInfo.get("status");
            if (status == Web.RETURNCODE_SUCCESS) { // 로딩 성공
                String myUserId = ProfileManager.getInstance().getId();

                String userId = (String)mapInfo.get("id");
                String nickname = (String)mapInfo.get("nickname");
                String profile = (String)mapInfo.get("profile");
                String date = (String)mapInfo.get("date");
                String grade = (String)mapInfo.get("grade");
                int article = (int)mapInfo.get("article");
                int comment = (int)mapInfo.get("comment");
                int visit = (int)mapInfo.get("visit");

                // update view
                runOnUiThread(() -> {
                    collapsingToolbarLayout.setTitle(nickname);
                    tvUserId.setText(userId);
                    tvGrade.setText(grade);
                    tvVisit.setText("" + visit);
                    Glide.with(getApplicationContext()).load(profile).into(ivProfile); // profile image

                    if (userId.equals(myUserId)) {
                        btnProfileSetting.setVisibility(View.VISIBLE);
                        btnGradeInfo.setVisibility(View.VISIBLE);
                    } else {
                        btnProfileSetting.setVisibility(View.GONE);
                        btnGradeInfo.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    public static void startProfileActivity(Context context, String userId) {
        // open profile page
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("userId", userId);
        context.startActivity(intent);
    }

    @Override
    public void onRefreshed(ArticleList articleList, final boolean refreshing) {
        runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
    }

}
