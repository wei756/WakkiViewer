package com.wei756.ukkiukki;

import android.app.Activity;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wei756.ukkiukki.Network.Web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StickerList {
    private Web web = Web.getInstance();

    private Activity act;

    private RecyclerView stickerView, stickerPackView;
    private StickerPackListAdapter stickerPackAdapter;
    private StickerListAdapter stickerAdapter;

    private boolean scollable;

    private ArrayList<Sticker> listStickerPack;
    private Map<String, ArrayList<Sticker>> mapStickerList; // for cache
    private int packCodeInd = 0;
    private int selectedSticker = -1;
    private String stickerCode = null;

    public final static String domain = "https://storep-phinf.pstatic.net",
            mobileTabOnName = "original_m_tab_on.png?type=m96_74",
            mobileTabOffName = "original_m_tab_off.png?type=m96_74_bw",
            mobilePreviewName = "original_m_preview.png?type=p100_100";

    public StickerList(Activity act, RecyclerView stickerPackView, RecyclerView stickerView) {
        this.act = act;

        // RecyclerView
        this.stickerPackView = stickerPackView;
        this.stickerView = stickerView;
        this.stickerPackView.setHasFixedSize(true);
        this.stickerView.setHasFixedSize(true);

        // RecyclerViewAdapter
        stickerPackAdapter = new StickerPackListAdapter(this, act, StickerPackListAdapter.THEME_DEFAULT, stickerPackView);
        stickerAdapter = new StickerListAdapter(this, act, StickerListAdapter.THEME_DEFAULT, stickerView);
        this.stickerPackView.setAdapter(stickerPackAdapter);
        this.stickerView.setAdapter(stickerAdapter);
        ((GridLayoutManager) this.stickerView.getLayoutManager()).setSpanCount(5);
        //this.stickerView.setLayoutManager(new GridLayoutManager(act, 5));

        // Sticker list
        listStickerPack = new ArrayList<>();
        mapStickerList = new HashMap<>();

        loadStickerPackList();
    }

    public void selectStickerPack(int ind) {
        this.packCodeInd = ind;
        for (int i = 0; i < listStickerPack.size(); i++) {
            listStickerPack.get(i).setSelected(i == ind);
        }
        stickerPackAdapter.update(act);
        loadStickerList(ind);
    }

    public void setStickerCode(Sticker sticker) {
        setStickerCode(sticker.getStickerCode() + "-" + sticker.getImageWidth() + "-" + sticker.getImageHeight());
    }

    public void setStickerCode(String stickerCode) {
        this.stickerCode = stickerCode;
    }

    public String getStickerCode() {
        return stickerCode;
    }

    public void loadStickerPackList() {
        new Thread(() -> {
            listStickerPack = web.getStickerPackList(act); // get stickerpack list

            // callback
            if (listStickerPack.size() > 0 && listStickerPack.get(0).getErrorcode() == Web.RETURNCODE_SUCCESS) { // 로딩 성공이고 표시할 내용이 있을 경우

                stickerPackAdapter.setListWith(listStickerPack, act);
                selectStickerPack(0);
                Log.v("StickerList", "스티커팩 " + listStickerPack.size() + "개 로딩");

            } else if (listStickerPack.size() == 0) { // 글이 없을 때
                stickerPackAdapter.clearList(act);

                // 스티커팩이 없을 때
                Log.i("StickerList", "스티커팩이 없습니다. on loadStickerPackList");

            } else { // 에러
                stickerPackAdapter.clearList(act);

                // 인터넷 연결이 안 될 때
                Log.w("StickerList.err", "인터넷에 연결되지 않았습니다. on loadStickerPackList");

            }
        }).start();
    }

    /**
     * 스티커 팩의 스티커들을 불러와서 맵에 저장합니다.
     *
     * @param packCodeInd
     */
    private void loadStickerList(int packCodeInd) {
        Log.e("asdf", "ind" + packCodeInd);
        loadStickerList(listStickerPack.get(packCodeInd).getPackCode());
    }

    /**
     * 스티커 팩의 스티커들을 불러와서 맵에 저장합니다.
     *
     * @param packCode
     */
    private void loadStickerList(String packCode) {
        new Thread(() -> {
            ArrayList<Sticker> listSticker = mapStickerList.get(packCode);
            Log.e("asdf", "" + (listSticker == null));
            if (listSticker == null) { // mapStickerList.get(packCode) == null
                listSticker = web.getStickerList(act, packCode);
                mapStickerList.put(packCode, listSticker);
            }
            Log.e("asdf", packCode);
            Log.e("asdf", mapStickerList.get(packCode).toString());


            // callback
            if (listSticker.size() > 0 && listSticker.get(0).getErrorcode() == Web.RETURNCODE_SUCCESS) { // 로딩 성공이고 표시할 내용이 있을 경우

                stickerAdapter.setListWith((ArrayList) listSticker.clone(), act);
                Log.v("StickerList", "스티커 " + listSticker.size() + "개 로딩");

            } else if (listStickerPack.size() == 0) { // 글이 없을 때
                stickerAdapter.clearList(act);

                // 스티커팩이 없을 때
                Log.i("StickerList", "스티커팩이 없습니다. on loadStickerList");

            } else { // 에러
                stickerAdapter.clearList(act);

                // 인터넷 연결이 안 될 때
                Log.w("StickerList.err", "인터넷에 연결되지 않았습니다. on loadStickerList");

            }
        }).start();
    }

}