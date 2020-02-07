package com.wei756.ukkiukki;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class StickerPackListAdapter extends RecyclerViewCustomAdapter {

    public final static int THEME_DEFAULT = 0;

    public final static int SUBTHEME_DEFAULT = 0;


    private RecyclerView mRecyclerView;

    private StickerList stickerList;

    /**
     * ItemView 정의
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ConstraintLayout layout;
        protected ImageView icon;

        public ItemViewHolder(View view) {
            super(view);
            this.layout = (ConstraintLayout) view.findViewById(R.id.btn_layout_stickerpack);
            this.icon = (ImageView) view.findViewById(R.id.iv_icon);
        }
    }

    /**
     * ArticleListAdapter 정의
     *
     * @param act recyclerView 가 표시되는 액티비티
     */
    public StickerPackListAdapter(StickerList stickerList, Activity act, int theme, RecyclerView mRecyclerView) {
        this.mList = new ArrayList<>();
        this.stickerList = stickerList;
        this.context = act;
        this.mRecyclerView = mRecyclerView;

        hasHeader = false;
        hasFooter = false;

        THEME_NUMBER = 1;
        SUBTHEME_NUMBER = 1;

        setTheme(theme);
        setItemLayout();
    }

    public void setTheme(int theme) {
        this.TYPE_THEME = theme;

        switch (theme) {
            case THEME_DEFAULT:
                maxItemCount = 0;
                headerHasItem = false;
                scrollable = false;
                hasHeader = false;
                hasFooter = false;
                break;
        }
    }

    public void setSubTheme(int subtheme) {
        this.TYPE_SUBTHEME = subtheme;
    }

    /**
     * layout 정의
     */
    protected void setItemLayout() {
        layoutHeader = new int[THEME_NUMBER * SUBTHEME_NUMBER];
        layoutItem = new int[THEME_NUMBER * SUBTHEME_NUMBER];
        layoutFooter = new int[THEME_NUMBER * SUBTHEME_NUMBER];

        layoutHeader[THEME_DEFAULT + SUBTHEME_DEFAULT] = R.layout.stickerpack_list; // dummy

        layoutItem[THEME_DEFAULT + SUBTHEME_DEFAULT] = R.layout.stickerpack_list;

        layoutFooter[THEME_DEFAULT + SUBTHEME_DEFAULT] = R.layout.stickerpack_list; // dummy
    }

    /**
     * header layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup viewGroup) {

        return null;
    }

    /**
     * item layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutItem[TYPE_THEME + TYPE_SUBTHEME], viewGroup, false);

        return new ItemViewHolder(view);
    }

    /**
     * footer layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup viewGroup) {

        return null;
    }

    /**
     * header content 정의
     */
    @Override
    protected void bindHeaderViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
    }

    /**
     * item content 정의
     */
    @Override
    protected void bindItemViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int pos) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewholder;

        final Sticker stickerPack = (Sticker) mList.get(pos);
        String packCode = stickerPack.getPackCode();
        if (packCode != null) {
            packCode = StickerList.domain + "/" + packCode + "/";
            if (stickerPack.isSelected())
                packCode += StickerList.mobileTabOnName;
            else
                packCode += StickerList.mobileTabOffName;
            Glide.with(context.getApplicationContext()).load(packCode).into(itemViewHolder.icon);
        }


        // click event
        View.OnClickListener clickArticle = view -> stickerList.selectStickerPack(pos);
        itemViewHolder.layout.setOnClickListener(clickArticle);


    }

    /**
     * footer content 정의
     */
    @Override
    protected void bindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
    }
}
