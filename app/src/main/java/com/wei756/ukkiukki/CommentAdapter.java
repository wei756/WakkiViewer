package com.wei756.ukkiukki;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerViewCustomAdapter {

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView levelIcon;
        protected TextView author;
        protected TextView time;
        protected TextView content;
        protected LinearLayout reply;
        protected ImageView replyIcon;

        public ItemViewHolder(View view) {
            super(view);
            this.levelIcon = (TextView) view.findViewById(R.id.icon_article_comment_author_color);
            this.author = (TextView) view.findViewById(R.id.tv_article_comment_author);
            this.time = (TextView) view.findViewById(R.id.tv_article_comment_time);
            this.content = (TextView) view.findViewById(R.id.tv_article_comment_content);
            this.reply = (LinearLayout) view.findViewById(R.id.layout_article_comment_reply);
            this.replyIcon = (ImageView) view.findViewById(R.id.img_article_comment_subcomment);
        }
    }

    public CommentAdapter(ArrayList<Item> list, ArticleViewerActivity act) {
        this.mList = list;
        this.act = act;

        hasHeader = false;
        hasFooter = false;
        headerHasItem = false;

        THEME_NUMBER = 0;
        SUBTHEME_NUMBER = 0;

        scrollable = false;
        maxItemCount = 0;
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
                .inflate(R.layout.comment_list, viewGroup, false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);

        return viewHolder;
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
     * footer content 정의
     */
    @Override
    protected void bindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
    }

    /**
     * item content 정의
     */
    @Override
    protected void bindItemViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int pos) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewholder;
        Comment comment = (Comment) mList.get(pos);

        String levelIcon = comment.getLevelIcon();
        if (!levelIcon.equals("")) {
            itemViewHolder.levelIcon.setText(levelIcon);
            itemViewHolder.levelIcon.setVisibility(View.VISIBLE);
            try {
                itemViewHolder.levelIcon.setBackground(act.getResources().getDrawable(LevelIcon.getInstance().getIcon(levelIcon)));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PersonalColor", "Wrong color error(" + levelIcon + ") on getIcon");
            }
        } else {
            itemViewHolder.levelIcon.setVisibility(View.GONE);
        }
        itemViewHolder.author.setText(comment.getAuthor());
        itemViewHolder.time.setText(comment.getTime());


        // 댓글 본문
        String parentAuthor = comment.getParentAuthor();
        if (parentAuthor == null)
            itemViewHolder.content.setText(comment.getContent());
        else {
            SpannableStringBuilder text = new SpannableStringBuilder(parentAuthor + " " + comment.getContent());
            int color;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = act.getResources().getColor(R.color.colorArticleCommentParentAuthor, null);
            else
                color = act.getResources().getColor(R.color.colorArticleCommentParentAuthor);

            text.setSpan(new ForegroundColorSpan(color), 0, parentAuthor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            itemViewHolder.content.setText(text);
        }

        itemViewHolder.replyIcon.setVisibility(comment.getIndentLevel() == 0 ? View.GONE : View.VISIBLE);
        itemViewHolder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: 대댓글창 열기
                //Uri uri = Uri.parse(mList.get(position).getHref());

                //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                //Intent intent = new Intent(act, ArticleViewerActivity.class);
                //intent.putExtra("article_title", mList.get(position).getTitle());
                //intent.putExtra("article_href", mList.get(position).getHref());
                //act.startActivity(intent);
            }
        });
    }

}
