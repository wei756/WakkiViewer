package com.wei756.ukkiukki;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

/**
 * 지정된 색 테마로 액션바와 상태바의 색상을 변경하는 클래스
 */
public class ActionBarManager {

    private static ActionBarManager instance;

    /**
     * 액션바의 색 테마를 앱 기본 색으로 설정합니다
     *
     * @param act     적용할 Activity
     * @param toolbar 적용할 Toolbar
     */
    private void initActionBarBasic(Activity act, Toolbar toolbar) {
        act.getWindow().setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark, null));
        toolbar.setBackgroundResource(R.color.colorPrimary); // color
    }

    /**
     * 액션바의 색 테마를 기본 테마로 설정합니다
     *
     * @param act     적용할 Activity
     * @param toolbar 적용할 Toolbar
     */
    public void initActionBar(Activity act, Toolbar toolbar) {
        initActionBarBasic(act, toolbar);
        ToolbarColorizeHelper.colorizeToolbar(toolbar, act.getResources().getColor(R.color.colorTitle, null), act);
    }


    /**
     * Navigation drawer가 있는 Activity의 액션바의 색 테마를 기본 테마로 설정합니다
     *
     * @param act     적용할 Activity
     * @param toolbar 적용할 Toolbar
     * @param toggle  적용할 Toggle
     */
    public void initActionBar(Activity act, Toolbar toolbar, ActionBarDrawerToggle toggle) {
        initActionBar(act, toolbar);
        ToolbarColorizeHelper.colorizeToolbar(toolbar, act.getResources().getColor(R.color.colorTitle, null), act, toggle);
    }

    /**
     * 액션바의 색 테마를 해당 게시판 테마로 설정합니다
     *
     * @param act     적용할 Activity
     * @param toolbar 적용할 Toolbar
     * @param mid     게시판
     */
    public void setActionBar(Activity act, Toolbar toolbar, int mid) {
        try {
            CategoryManager category = CategoryManager.getInstance();
            String name = (String) category.getParam(mid, CategoryManager.NAME);
            int color = R.color.colorPrimary;
            int colorStatus = R.color.colorPrimaryDark;
            int colorStatusText = R.color.colorTitle;

            // toolbar title
            if (act instanceof MainActivity) {
                toolbar.setTitle(act.getResources().getString(R.string.app_name));
                toolbar.setBackgroundResource(R.color.colorTransparent); // color
                act.getWindow().setStatusBarColor(act.getResources().getColor(R.color.colorTransparent, null)); // colorStatus
            } else {
                toolbar.setTitle((String) category.getParam(mid, CategoryManager.NAME));
                toolbar.setBackgroundResource(color); // color
                act.getWindow().setStatusBarColor(act.getResources().getColor(colorStatus, null)); // colorStatus
            }

            ToolbarColorizeHelper.colorizeToolbar(toolbar, act.getResources().getColor(colorStatusText, null), act); // colorStatusText

            //setSystemBarTheme(act, (act.getResources().getColor(colorStatusText, null) == Color.rgb(250, 250, 250))); // colorStatusText
            setSystemBarTheme(act, true); // colorStatusText

            Log.i("ActionBarManager", "Open with board of " + mid + " on setActionBar");
        } catch (InvalidCategoryException e) {
            Log.e("ActionBarManager", "Wrong board error(" + mid + ") on setActionBar");
            e.printStackTrace();
            initActionBar(act, toolbar);
        }
    }


    /**
     * Navigation drawer가 있는 Activity의 액션바의 색 테마를 해당 게시판 테마로 설정합니다
     *
     * @param act     적용할 Activity
     * @param toolbar 적용할 Toolbar
     * @param mid     게시판
     * @param toggle  적용할 Toggle
     */
    public void setActionBar(Activity act, Toolbar toolbar, int mid, ActionBarDrawerToggle toggle) {
        int colorStatusText = R.color.colorTitle;

        setActionBar(act, toolbar, mid);
        ToolbarColorizeHelper.colorizeToolbar(toolbar, act.getResources().getColor(colorStatusText, null), act, toggle); // colorStatusText

        Log.i("ActionBarManager", "Open with board of " + mid + " on setActionBar");
    }

    /**
     * StatusBar의 item color를 설정합니다.
     *
     * @param pActivity 적용할 Activity
     * @param pIsDark   color
     */
    public static final void setSystemBarTheme(final Activity pActivity, final boolean pIsDark) {
        // Fetch the current flags.
        final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        pActivity.getWindow().getDecorView().setSystemUiVisibility(pIsDark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
    }

    /**
     * singleton
     */
    public static ActionBarManager getInstance() {
        if (instance == null)
            instance = new ActionBarManager();
        return instance;
    }
}
