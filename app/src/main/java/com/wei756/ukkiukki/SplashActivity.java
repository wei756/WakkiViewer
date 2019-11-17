package com.wei756.ukkiukki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load category
        CategoryManager.getInstance().updateCategoryList();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}
