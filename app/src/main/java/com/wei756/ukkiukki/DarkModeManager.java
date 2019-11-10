package com.wei756.ukkiukki;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class DarkModeManager {
    public static final String NIGHT_MODE = "NIGHT_MODE";
    private boolean isNightModeEnabled = false;

    private static DarkModeManager singleton = null;
    private static Context context;

    public static DarkModeManager getInstance(Context context1) {
        context = context1;
        if(singleton == null)
        {
            singleton = new DarkModeManager();
        }
        return singleton;
    }

    private void darkManager() {
        singleton = this;
        SharedPreferences mPrefs = context.getSharedPreferences("pref", MODE_PRIVATE);
        //SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;

        SharedPreferences mPrefs = context.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled);
        editor.commit();
    }
}