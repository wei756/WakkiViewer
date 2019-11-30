package com.wei756.ukkiukki;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.graphics.Typeface.BOLD;

public class CommentAdapter extends RecyclerViewCustomAdapter {

    public final static int THEME_PREVIEW = 0;
    public final static int THEME_COMMENTPAGE = 1;

    public static String ORDERBY_UPLOAD = "asc";
    public static String ORDERBY_NEW = "desc";

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ConstraintLayout layout;
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
            this.layout = (ConstraintLayout) view.findViewById(R.id.layout_article_comment);

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

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        protected ImageView profile;
        protected TextView content;

        public FooterViewHolder(View view) {
            super(view);
            this.profile = (ImageView) view.findViewById(R.id.iv_article_comment_profile);
            this.content = (TextView) view.findViewById(R.id.tv_article_comment_content);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        protected TextView btnLikeIt;
        protected ConstraintLayout likeItMore;
        protected ImageView profile1, profile2, profile3;

        protected TextView btnOrderByUpload,btnOrderByNew;
        protected Switch switchFollowComment;

        public HeaderViewHolder(View view) {
            super(view);
            this.btnLikeIt = (TextView) view.findViewById(R.id.btn_commentpage_likeit);
            this.likeItMore = (ConstraintLayout) view.findViewById(R.id.layout_commentpage_likeit_more);
            this.profile1 = (ImageView) view.findViewById(R.id.iv_commentpage_likeit_member1);
            this.profile2 = (ImageView) view.findViewById(R.id.iv_commentpage_likeit_member2);
            this.profile3 = (ImageView) view.findViewById(R.id.iv_commentpage_likeit_member3);

            this.btnOrderByUpload = (TextView) view.findViewById(R.id.btn_commentpage_orderby_upload);
            this.btnOrderByNew = (TextView) view.findViewById(R.id.btn_commentpage_orderby_new);
            this.switchFollowComment = (Switch) view.findViewById(R.id.switch_commentpage_follow_comment);
        }
    }

    public CommentAdapter(ArrayList<Item> list, Context context, int theme) {
        this.mList = list;
        this.context = context;

        hasHeader = false;
        hasFooter = true;
        headerHasItem = false;

        THEME_NUMBER = 2;
        SUBTHEME_NUMBER = 0;

        scrollable = false;
        maxItemCount = 0;

        setTheme(theme);
        setItemLayout();
    }

    public void setTheme(int theme) {
        this.TYPE_THEME = theme;

        switch (theme) {
            case THEME_PREVIEW:
                maxItemCount = 0;
                headerHasItem = false;
                scrollable = false;
                hasHeader = false;
                hasFooter = true;
                break;

            case THEME_COMMENTPAGE:
                maxItemCount = 0;
                headerHasItem = false;
                scrollable = true;
                hasHeader = true;
                hasFooter = false;
                break;
        }
    }

    /**
     * layout 정의
     */
    protected void setItemLayout() {
        layoutHeader = new int[THEME_NUMBER];
        layoutItem = new int[THEME_NUMBER];
        layoutFooter = new int[THEME_NUMBER];

        layoutHeader[THEME_PREVIEW] = R.layout.article_list; // dummy
        layoutHeader[THEME_COMMENTPAGE] = R.layout.comment_list_header;

        layoutItem[THEME_PREVIEW] = R.layout.comment_list;
        layoutItem[THEME_COMMENTPAGE] = R.layout.comment_list_commentpage_theme;

        layoutFooter[THEME_PREVIEW] = R.layout.comment_list_footer;
        layoutFooter[THEME_COMMENTPAGE] = R.layout.comment_list_footer; // dummy
    }

    /**
     * header layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutHeader[TYPE_THEME + TYPE_SUBTHEME], viewGroup, false);
        HeaderViewHolder viewHolder = new HeaderViewHolder(view);

        return viewHolder;
    }

    /**
     * item layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutItem[TYPE_THEME], viewGroup, false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);

        return viewHolder;
    }

    /**
     * footer layout 정의
     */
    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutFooter[TYPE_THEME], viewGroup, false);
        FooterViewHolder viewHolder = new FooterViewHolder(view);

        return viewHolder;
    }

    /**
     * header content 정의
     */
    @Override
    protected void bindHeaderViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
        HeaderViewHolder itemViewHolder = (HeaderViewHolder) viewholder;
        if (TYPE_THEME == THEME_COMMENTPAGE) {

        }
    }

    /**
     * footer content 정의
     */
    @Override
    protected void bindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewholder) {
        FooterViewHolder itemViewHolder = (FooterViewHolder) viewholder;
        if (TYPE_THEME == THEME_PREVIEW) {
            // profile image
            String imgProfile = ProfileManager.getInstance().getProfile();
            if (imgProfile == null || imgProfile.equals(""))
                imgProfile = "https://ssl.pstatic.net/static/cafe/cafe_pc/default/cafe_profile_77.png"; // default image
            Glide.with(context).load(imgProfile).into(itemViewHolder.profile); // load image
            // rounded corners
            itemViewHolder.profile.setBackground(new ShapeDrawable(new OvalShape()));
            itemViewHolder.profile.setClipToOutline(true);

            if (mList.size() > 0) // 댓글이 있을 경우
                itemViewHolder.content.setText(R.string.comment_list_new_comment);
            else // 댓글이 없을 경우
                itemViewHolder.content.setText(R.string.comment_list_first_comment);
        }
    }

    /**
     * item content 정의
     */
    @Override
    protected void bindItemViewHolder(@NonNull RecyclerView.ViewHolder viewholder, final int pos) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewholder;
        Comment comment = (Comment) mList.get(pos);

        // 댓글 배경
        if (comment.isMine()) // 본인댓글 여부
            itemViewHolder.layout.setBackgroundResource(R.color.colorCommentListMineBackground);
        else
            itemViewHolder.layout.setBackgroundResource(R.color.colorBackground);

        // profile image
        String imgProfile = comment.getImgProfile();
        if (imgProfile.equals(""))
            imgProfile = "https://ssl.pstatic.net/static/cafe/cafe_pc/default/cafe_profile_77.png"; // default image
        Glide.with(context.getApplicationContext()).load(imgProfile).into(itemViewHolder.profile); // load image
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
            int maxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, context.getApplicationContext().getResources().getDisplayMetrics());
            itemViewHolder.contentImage.setMaxHeight(maxSize);
            itemViewHolder.contentImage.setMaxWidth(maxSize);
            Glide.with(context.getApplicationContext()).load(image).into(itemViewHolder.contentImage); // load image
            itemViewHolder.contentImage.setVisibility(View.VISIBLE);
        } else if (!sticker.equals("")) {
            int maxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, context.getApplicationContext().getResources().getDisplayMetrics());
            itemViewHolder.contentImage.setMaxHeight(maxSize);
            itemViewHolder.contentImage.setMaxWidth(maxSize);
            Glide.with(context.getApplicationContext()).load(sticker).into(itemViewHolder.contentImage); // load image
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
