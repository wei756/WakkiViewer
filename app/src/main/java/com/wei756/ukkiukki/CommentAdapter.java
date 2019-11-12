package com.wei756.ukkiukki;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.graphics.Typeface.BOLD;

public class CommentAdapter extends RecyclerViewCustomAdapter {

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView profile;
        protected TextView author;
        protected TextView time;
        protected TextView content;
        protected ImageView contentImage;
        protected LinearLayout reply;
        protected ImageView replyMargin;
        protected TextView iconNew;
        protected TextView iconArticleAuthor;
        protected View footerLine;

        public ItemViewHolder(View view) {
            super(view);
            this.profile = (ImageView) view.findViewById(R.id.iv_article_comment_profile);
            this.author = (TextView) view.findViewById(R.id.tv_article_comment_author);
            this.time = (TextView) view.findViewById(R.id.tv_article_comment_time);
            this.content = (TextView) view.findViewById(R.id.tv_article_comment_content);
            this.contentImage = (ImageView) view.findViewById(R.id.iv_article_comment_content);
            this.reply = (LinearLayout) view.findViewById(R.id.layout_article_comment_reply);
            this.replyMargin = (ImageView) view.findViewById(R.id.view_article_comment_margin_subcomment);
            this.iconNew = (TextView) view.findViewById(R.id.icon_article_comment_new);
            this.iconArticleAuthor = (TextView) view.findViewById(R.id.icon_article_comment_article_author);
            this.footerLine = (View) view.findViewById(R.id.view_article_comment_footer_line);
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

        // profile image
        String imgProfile = comment.getImgProfile();
        if (imgProfile.equals(""))
            imgProfile = "https://ssl.pstatic.net/static/cafe/cafe_pc/default/cafe_profile_77.png"; // default image
        Glide.with(act.getApplicationContext()).load(imgProfile).into(itemViewHolder.profile); // load image
        // rounded corners
        //itemViewHolder.profile.setBackground(new ShapeDrawable(new OvalShape()));
        //itemViewHolder.profile.setClipToOutline(true);

        itemViewHolder.author.setText(comment.getAuthor());
        itemViewHolder.time.setText(comment.getTime());

        // 댓글 본문
        String parentAuthor = comment.getParentAuthor(),
                content = comment.getContent();
        if (!content.equals("")) {// 텍스트가 있을 때
            if (parentAuthor == null)
                itemViewHolder.content.setText(content);
            else {
                SpannableStringBuilder text = new SpannableStringBuilder(content);
                //int color = act.getResources().getColor(R.color.colorTextPrimary, null);

                text.setSpan(new StyleSpan(BOLD), 0, parentAuthor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                itemViewHolder.content.setText(text);
            }
            itemViewHolder.content.setVisibility(View.VISIBLE);
        } else
            itemViewHolder.content.setVisibility(View.GONE);

        // 댓글 이미지, 스티커
        String image = comment.getContentImage(),
                sticker = comment.getSticker();
        if (!image.equals("")) {
            int maxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, act.getApplicationContext().getResources().getDisplayMetrics());
            itemViewHolder.contentImage.setMaxHeight(maxSize);
            itemViewHolder.contentImage.setMaxWidth(maxSize);
            Glide.with(act.getApplicationContext()).load(image).into(itemViewHolder.contentImage); // load image
            itemViewHolder.contentImage.setVisibility(View.VISIBLE);
        } else if (!sticker.equals("")) {
            int maxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, act.getApplicationContext().getResources().getDisplayMetrics());
            itemViewHolder.contentImage.setMaxHeight(maxSize);
            itemViewHolder.contentImage.setMaxWidth(maxSize);
            Glide.with(act.getApplicationContext()).load(sticker).into(itemViewHolder.contentImage); // load image
            itemViewHolder.contentImage.setVisibility(View.VISIBLE);
        } else
            itemViewHolder.contentImage.setVisibility(View.GONE);

        itemViewHolder.replyMargin.setVisibility(comment.getIndentLevel() == 0 ? View.GONE : View.INVISIBLE);
        itemViewHolder.iconNew.setVisibility(comment.isIconNew() ? View.VISIBLE : View.GONE);
        itemViewHolder.iconArticleAuthor.setVisibility(comment.isIconArticleAuthor() ? View.VISIBLE : View.GONE);

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

        // 구분선
        if (pos < mList.size() - 1 &&
                ((Comment) mList.get(pos + 1)).getIndentLevel() > 0)
            itemViewHolder.footerLine.setVisibility(View.INVISIBLE);
        else
            itemViewHolder.footerLine.setVisibility(View.VISIBLE);
    }

}
