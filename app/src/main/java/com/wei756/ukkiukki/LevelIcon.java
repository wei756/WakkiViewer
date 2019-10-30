package com.wei756.ukkiukki;

import java.util.HashMap;
import java.util.Map;

public class LevelIcon {
    private static Map<String, Integer> icon = new HashMap<>();

    private static LevelIcon instance;

    private LevelIcon() {
        icon.put("1", R.drawable.icon_green);
        icon.put("2", R.drawable.icon_green);
        //TODO: LevelIcon 3,4,5레벨 확인 후 수정해야 됨
        icon.put("3", R.drawable.icon_green);
        icon.put("4", R.drawable.icon_green);
        icon.put("5", R.drawable.icon_green);

        icon.put("S", R.drawable.icon_shiftpsh);
        icon.put("C", R.drawable.icon_cryental);

        //TODO: LevelIcon 나머지 스트리머 확인 후 추가해야 됨
        icon.put("J", R.drawable.icon_jinu);
        icon.put("N", R.drawable.icon_nanayang);
        /*
        color.put("temtem", R.color.colorTemtem);
        color.put("temtem_dark", R.color.colorTemtemStatus);
        color.put("dduddi", R.color.colorDduddi);
        color.put("dduddi_dark", R.color.colorDduddiStatus);
        color.put("gambler", R.color.colorGambler);
        color.put("gambler_dark", R.color.colorGamblerStatus);
        color.put("collet11", R.color.colorCollet);
        color.put("collet11_dark", R.color.colorColletStatus);
        color.put("silph", R.color.colorSilph);
        color.put("silph_dark", R.color.colorSilphStatus);
        */
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
