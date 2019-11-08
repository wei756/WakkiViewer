package com.wei756.ukkiukki;

import java.util.HashMap;
import java.util.Map;

public class LevelIcon {
    private static Map<String, Integer> icon = new HashMap<>();

    private static LevelIcon instance;

    private LevelIcon() {
        icon.put("1", R.drawable.icon_green);
        icon.put("2", R.drawable.icon_green);
        icon.put("3", R.drawable.icon_green);
        icon.put("4", R.drawable.icon_green);
        icon.put("5", R.drawable.icon_green);
    }

    public int getIcon(String iconName) throws Exception {
        return icon.get(iconName);
        //Log.e("PersonalColor", "Wrong color error(" + iconName + ") on getIcon");
    }

    //singleton
    public static LevelIcon getInstance() {
        if (instance == null)
            instance = new LevelIcon();
        return instance;
    }
}
