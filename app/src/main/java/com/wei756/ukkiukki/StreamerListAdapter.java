package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StreamerListAdapter extends RecyclerView.Adapter<StreamerListAdapter.CustomViewHolder> {

    private final Activity act;
    // 스트리머 목록
    private ArrayList<Streamer> mList;
    // 스트리머 뱅온 목록
    private ArrayList<Boolean> mLiveList = new ArrayList<>();

    private final String URLTwitch = "https://www.twitch.tv/";
    private final String URLYoutube = "https://www.youtube.com/channel/";

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ConstraintLayout layout;
        protected ImageView icon;
        protected TextView iconLive;
        protected TextView name;
        protected ImageView twitch;
        protected ImageView youtube;


        public CustomViewHolder(View view) {
            super(view);
            this.layout = (ConstraintLayout) view.findViewById(R.id.layout_streamer);
            this.icon = (ImageView) view.findViewById(R.id.img_streamer);
            this.iconLive = (TextView) view.findViewById(R.id.tv_live);
            this.name = (TextView) view.findViewById(R.id.tv_streamer);
            this.twitch = (ImageView) view.findViewById(R.id.btn_twitch);
            this.youtube = (ImageView) view.findViewById(R.id.btn_youtube);
        }
    }

    /**
     * 전달받은 ArrayList를 뷰홀더에 추가하고 뷰홀더를 업데이트합니다.
     *
     * @param mList 추가할 ArrayList
     */
    public void setListWith(final ArrayList mList, Activity act) {
        //final int size = this.mList.size();

        // 리사이클러뷰 버그 때문에 리스트 초기화 후 리스트 업데이트용 변수입니다.
        final StreamerListAdapter adapter = this;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.mList.clear();
                notifyDataSetChanged();
                adapter.mList = mList;
                notifyDataSetChanged();

                for (int i = 0; i < mList.size(); i++) {
                    adapter.mLiveList.add(false);
                }
            }
        });
    }

    protected ArrayList getArrayList() {
        return mList;
    }

    protected void setLive(final int index, final boolean live) {

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLiveList.set(index, live);
                notifyDataSetChanged();
            }
        });
    }


    public StreamerListAdapter(ArrayList<Streamer> list, Activity act) {
        this.mList = list;
        this.act = act;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.streamer_list, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, final int position) {
        viewholder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) act).setCategory(mList.get(position).getMid(), true);
            }
        });

        viewholder.icon.setImageResource(mList.get(position).getIcon());
        if (mLiveList.get(position))
            viewholder.iconLive.setVisibility(View.VISIBLE);
        else
            viewholder.iconLive.setVisibility(View.INVISIBLE);

        viewholder.name.setText(mList.get(position).getName());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            viewholder.name.setTextColor(act.getResources().getColor(mList.get(position).getNameColor()));
        else
            viewholder.name.setTextColor(act.getResources().getColor(mList.get(position).getNameColor(), null));

        viewholder.twitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(URLTwitch + mList.get(position).getTwitch());

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                act.startActivity(intent);
            }
        });

        viewholder.youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(URLYoutube + mList.get(position).getYoutube());

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                act.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
