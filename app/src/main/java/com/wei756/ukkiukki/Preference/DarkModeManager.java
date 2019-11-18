package com.wei756.ukkiukki.Preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;


public class DarkModeManager extends PreferencesManager {
    private static DarkModeManager instance = null;

    public static final String NIGHT_MODE = "NIGHT_MODE";

    private boolean isNightModeEnabled = false;


    private DarkModeManager() {
        super();
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);
    }

    /**
     * 다크 모드가 켜져있는지 여부를 반환합니다.
     */
    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    /**
     * 다크 모드를 전환합니다.
     */
    public void toggleIsNightModeEnabled() {
        setIsNightModeEnabled(!isNightModeEnabled);
    }

    /**
     * 다크 모드가 켜져있는지 여부를 설정합니다.
     *
     * @param isNightModeEnabled 다크 모드가 켜져있는지 여부
     */
    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled);
        editor.apply();
    }

    /**
     * 다크모드를 적용합니다.
     */
    public void setDefaultNightMode() {
        if (isNightModeEnabled)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Log.v("DarkModeManager", "Changed default dark mode.");
    }

    /**
     * singleton
     */
    public static DarkModeManager getInstance(Context context1) {
        context = context1;
        if (instance == null) {
            instance = new DarkModeManager();
        }
        return instance;
    }
}