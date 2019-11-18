package com.wei756.ukkiukki.Preference;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesManager {
    protected static Context context;

    protected SharedPreferences mPrefs;
    protected SharedPreferences.Editor editor = null;

    protected PreferencesManager() {
        mPrefs = context.getSharedPreferences("pref", MODE_PRIVATE);
    }

    protected String getString(String var1, @Nullable String var2) {
        return mPrefs.getString(var1, var2);
    }

    protected Set<String> getStringSet(String var1, @Nullable Set<String> var2) {
        return mPrefs.getStringSet(var1, var2);
    }

    protected int getInt(String var1, int var2) {
        return mPrefs.getInt(var1, var2);
    }

    protected long getLong(String var1, long var2) {
        return mPrefs.getLong(var1, var2);
    }

    protected float getFloat(String var1, float var2) {
        return mPrefs.getFloat(var1, var2);
    }

    protected boolean getBoolean(String var1, boolean var2) {
        return mPrefs.getBoolean(var1, var2);
    }

    protected SharedPreferences.Editor putString(String var1, @Nullable String var2) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.putString(var1, var2);
    }

    protected SharedPreferences.Editor putStringSet(String var1, @Nullable Set<String> var2) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.putStringSet(var1, var2);
    }

    protected SharedPreferences.Editor putInt(String var1, int var2) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.putInt(var1, var2);
    }

    protected SharedPreferences.Editor putLong(String var1, long var2) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.putLong(var1, var2);
    }

    protected SharedPreferences.Editor putFloat(String var1, float var2) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.putFloat(var1, var2);
    }

    protected SharedPreferences.Editor putBoolean(String var1, boolean var2) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.putBoolean(var1, var2);
    }

    protected SharedPreferences.Editor remove(String var1) {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.remove(var1);
    }

    protected SharedPreferences.Editor clear() {
        if (editor == null)
            editor = mPrefs.edit();
        return editor.clear();
    }

    protected void apply() {
        editor.apply();
        editor = null;
    }
}
