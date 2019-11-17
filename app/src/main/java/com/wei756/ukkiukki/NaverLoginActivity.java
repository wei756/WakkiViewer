package com.wei756.ukkiukki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class NaverLoginActivity extends AppCompatActivity {
    private NaverLoginView loginView;
    private ConstraintLayout layoutLoadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naver_login);

        loginView = (NaverLoginView) findViewById(R.id.web_naver_login);
        layoutLoadingPanel = (ConstraintLayout) findViewById(R.id.layout_naver_login_loadingPanel);

        // get intent
        Intent intent = new Intent(this.getIntent());
        String type = intent.getStringExtra("login_type");
        if (type.equals("LOGIN")) {
            loginView.loadLoginPage(NaverLoginActivity.this);
        } else if (type.equals("LOGOUT")) {
            new Thread() {
                public void run() {
                    if (Web.getInstance().logoutLoginSession() == 0) {
                        showToast("로그아웃되었습니다.");
                    } else {
                        showToast("로그아웃하는 중에 오류가 발생하였습니다.");
                    }
                    finish();
                }
            }.start();
        }
    }

    public void setLoading() {
        loginView.setVisibility(View.GONE);
        layoutLoadingPanel.setVisibility(View.VISIBLE);
    }

    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void showToast(final int resid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), resid, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
