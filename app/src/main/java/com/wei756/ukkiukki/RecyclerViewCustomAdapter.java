package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract class RecyclerViewCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public void setTYPE_THEME(int TYPE_THEME) {
        this.TYPE_THEME = TYPE_THEME;
    }

    /**
     * 메인페이지에 표시되는 글목록과 게시판 글목록 구분용
     */
    protected int TYPE_THEME;
    protected int THEME_NUMBER;
    /**
     * 게시글 보기 형식 구분용 (게시글형, 앨범형 등)
     */
    protected int TYPE_SUBTHEME;
    protected int SUBTHEME_NUMBER;


    public void setTheme(int theme, int subtheme) {
        this.TYPE_THEME = theme;
        this.TYPE_SUBTHEME = subtheme;
    }

    /**
     * Item 의 최대 표시 갯수(0= 제한 없음)
     */
    protected int maxItemCount = 0;

    /**
     * ArticleList item 종류 정의
     */
    protected final int TYPE_HEADER = 0;
    protected final int TYPE_ITEM = 1;
    protected final int TYPE_FOOTER = 2;

    /**
     * RecyclerView 가 배치되는 액티비티의 정보
     */
    protected Context context;
    protected ArrayList<Item> mList;
    protected EndlessRecyclerViewScrollListener mListener;

    /**
     * EndlessScrolling 활성화 여부
     */
    protected boolean scrollable;

    /**
     * Header 사용 여부
     */
    protected boolean hasHeader;

    /**
     * Footer 사용 여부
     */
    protected boolean hasFooter;

    /**
     * Header 가 첫번째 Item 포함 여부
     */
    protected boolean headerHasItem;

    protected int[] layoutHeader;
    protected int[] layoutItem;
    protected int[] layoutFooter;

    /**
     * EndlessRecyclerViewScrollListener의 resetState 메서드를 호출하기 위해 사용됩니다.
     *
     * @param mListener
     */
    public void setScrollListener(EndlessRecyclerViewScrollListener mListener) {
        this.mListener = mListener;
    }

    /**
     * EndlessScrolling 활성화 여부를 반환합니다.
     */
    public boolean getScollable() {
        return scrollable;
    }


    /**
     * 전달받은 ArrayList를 뷰홀더에 추가하고 뷰홀더를 업데이트합니다.
     *
     * @param mList 추가할 ArrayList
     */
    public void setListWith(final ArrayList mList, Activity act) {

        // 리사이클러뷰 버그 때문에 리스트 초기화 후 리스트 업데이트용 변수입니다.
        final RecyclerViewCustomAdapter adapter = this;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.mList.clear();
                notifyDataSetChanged();
                adapter.mList = mList;
                notifyDataSetChanged();
                if (scrollable)
                    mListener.resetState();
                Log.v("RecyclerViewAdapter", mList.size() + "개 Items 추가됨, " + getItemCount() + " on RecyclerViewCustomAdapter.setListWith");
            }
        });
    }

    /**
     * 전달받은 ArrayList의 Item을 기존 뷰홀더 리스트에 추가하고 추가된 부분만 업데이트합니다.
     *
     * @param mList 추가할 ArrayList
     */
    public void addListWith(final ArrayList mList, Activity act) {
        final int originalSize = this.mList.size();
        this.mList.addAll(mList);
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyItemRangeInserted(originalSize, mList.size());
            }
        });
        Log.v("RecyclerViewAdapter", mList.size() + "개 추가됨, " + originalSize + " -> " + this.mList.size() + ", " + getItemCount() + ". on RecyclerViewCustomAdapter.addListWith");
    }

    /**
     * 뷰홀더 리스트를 초기화합니다.
     */
    public void clearList(Activity act) {

        // 리사이클러뷰 버그 때문에 리스트 초기화 후 리스트 업데이트용 변수입니다.
        final RecyclerViewCustomAdapter adapter = this;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.mList.clear();
                notifyDataSetChanged();
                if (scrollable)
                    mListener.resetState();
            }
        });
    }

    protected ArrayList getArrayList() {
        return mList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;

        if (viewType == TYPE_HEADER) {
            viewHolder = createHeaderViewHolder(viewGroup);
        } else if (viewType == TYPE_FOOTER) {
            viewHolder = createFooterViewHolder(viewGroup);
        } else {
            viewHolder = createItemViewHolder(viewGroup);
        }

        return viewHolder;
    }

    abstract protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup viewGroup);

    abstract protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup viewGroup);

    abstract protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup viewGroup);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int position) {
        //Log.v("ArticleListAdapter", (position) + "번 on ArticleListAdapter.onBindViewHolder");
        int viewType = getItemViewType(position);
        if (viewType == TYPE_HEADER) {
            bindHeaderViewHolder(viewholder);
        } else if (viewType == TYPE_FOOTER) {
            bindFooterViewHolder(viewholder);
        } else {
            bindItemViewHolder(viewholder, (hasHeader && !headerHasItem) ? position - 1 : position);
        }
    }

    abstract protected void bindHeaderViewHolder(@NonNull RecyclerView.ViewHolder viewholder);

    abstract protected void bindItemViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int listPos);

    abstract protected void bindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewholder);

    @Override
    public int getItemCount() {
        int size = (null != mList ? mList.size() : 0);
        size = (maxItemCount == 0 ? size : Math.min(size, maxItemCount));

        if (hasHeader)
            size += (headerHasItem ? 0 : 1);

        size += (hasFooter ? 1 : 0);

        return size;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeader && (position == 0))
            return TYPE_HEADER;
        else if (hasFooter && (position == getItemCount() - 1))
            return TYPE_FOOTER;
        else
            return TYPE_ITEM;
    }

}
