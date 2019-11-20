package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

public class ProfileManager {
    private static ProfileManager instance;

    /**
     * login 여부
     * <p>
     * WebClientManager.logined 와는 다르게 프로필 정보가 로드되어 있는지 여부까지 포함함.
     */
    private boolean logined = false;

    private String id, nickname, profile;
    private String date, grade;
    private int visit, article, comment;

    private NavigationView navigationView;
    private View header;
    private TextView btnLogout;
    private TextView tvNickname;
    private TextView tvGrade;
    private ImageView ivProfile;
    private LinearLayout llStats;
    private TextView tvArticle, tvComment, tvVisit;
    private View vFooterLine;

    private ProfileManager() {
    }

    public void setNavigationView(NavigationView navigationView) {
        this.navigationView = navigationView;
        header = navigationView.getHeaderView(0);
        btnLogout = (TextView) header.findViewById(R.id.btn_nav_logout);
        tvNickname = (TextView) header.findViewById(R.id.tv_nav_nickname);
        tvGrade = (TextView) header.findViewById(R.id.tv_nav_grade);
        ivProfile = (ImageView) header.findViewById(R.id.iv_nav_profile);

        llStats = (LinearLayout) header.findViewById(R.id.layout_nav_stats);
        tvArticle = (TextView) header.findViewById(R.id.tv_nav_article);

        tvComment = (TextView) header.findViewById(R.id.tv_nav_comment);
        tvVisit = (TextView) header.findViewById(R.id.tv_nav_visit);
        vFooterLine = (View) header.findViewById(R.id.view_nav_footer_line);
    }

    public void updateNavigationDrawerProfile(final Context context) {
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (logined) {
                    btnLogout.setVisibility(View.VISIBLE);
                    tvNickname.setText(nickname);
                    tvGrade.setText(grade);
                    tvGrade.setVisibility(View.VISIBLE);

                    llStats.setVisibility(View.VISIBLE);
                    vFooterLine.setVisibility(View.VISIBLE);
                    tvArticle.setText("" + article);
                    tvComment.setText("" + comment);
                    tvVisit.setText("" + visit);

                    String url = profile;
                    Glide.with(context).load(url).into(ivProfile);
                    // rounded corners
                    ivProfile.setBackground(new ShapeDrawable(new OvalShape()));
                    ivProfile.setClipToOutline(true);

                } else {
                    btnLogout.setVisibility(View.GONE);
                    tvNickname.setText(R.string.nav_header_title);
                    tvGrade.setVisibility(View.GONE);

                    llStats.setVisibility(View.GONE);
                    vFooterLine.setVisibility(View.GONE);
                    ivProfile.setImageResource(R.mipmap.ic_launcher_round);

                }
                Log.v("ProfileManager", "프로필 업데이트(Logined=" + logined + ")");
            }
        });
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getVisit() {
        return visit;
    }

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public boolean isLogined() {
        return logined;
    }

    public void setLogined(boolean logined) {
        this.logined = logined;
    }

    /**
     * Singleton
     */
    public static ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }
}
