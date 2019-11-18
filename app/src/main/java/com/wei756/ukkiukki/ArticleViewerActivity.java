package com.wei756.ukkiukki;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wei756.ukkiukki.Network.Web;
import com.wei756.ukkiukki.Network.WebClientManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ArticleViewerActivity extends AppCompatActivity implements LoadArticleListner {
    private Web web = Web.getInstance();

    ActionBarManager actBarManager = ActionBarManager.getInstance();

    Toolbar toolbar;

    ConstraintLayout layoutArticle;

    TextView tvTitle;
    ImageView ivProfile;
    TextView tvAuthor;
    TextView tvTime, tvView, tvVote;

    WebView webBody;

    TextView tvVoteUp, tvVoteDown;
    TextView tvProfile;

    TextView tvCommentCount;

    CommentAdapter mAdapter;
    RecyclerView mRecyclerView;

    String articleTitle, articleHref, articleBoard;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_viewer);
        toolbar = findViewById(R.id.toolbar_article_viewer);
        toolbar.setTitle("");
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_overflow_24dp));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Floating button
        FloatingActionButton fab = findViewById(R.id.fab_article_viewer_comment);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // get intent
        Intent intent = new Intent(this.getIntent());
        articleTitle = intent.getStringExtra("article_title");
        articleHref = intent.getStringExtra("article_href");

        layoutArticle = findViewById(R.id.id_article_viewer_viewer);

        // read_header
        tvTitle = findViewById(R.id.tv_article_viewer_title);
        ivProfile = findViewById(R.id.iv_article_viewer_profile);
        tvAuthor = findViewById(R.id.tv_article_viewer_author);
        tvTime = findViewById(R.id.tv_article_viewer_time);
        tvView = findViewById(R.id.tv_article_viewer_view);
        tvVote = findViewById(R.id.tv_article_viewer_vote);

        // read_body
        webBody = findViewById(R.id.web_article_viewer_body);

        // read_footer
        /*
        tvVoteUp = findViewById(R.id.tv_article_viewer_vote_up);
        tvVoteDown = findViewById(R.id.tv_article_viewer_vote_down);
        loProfile = findViewById(R.id.layout_article_viewer_footer_profile);
        ivProfile = findViewById(R.id.iv_article_viewer_profile);
        tvProfile = findViewById(R.id.tv_article_viewer_profile);
        */

        tvTitle.setText(articleTitle);
        layoutArticle.setVisibility(View.GONE);

        // feedback
        tvCommentCount = findViewById(R.id.tv_article_viewer_comment_count);

        // RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.view_article_viewer_comment);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new CommentAdapter(new ArrayList<Item>(), ArticleViewerActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        try {
            web.getArticle(this, articleHref);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ArticleViewerActivity", "Article load error(" + articleHref + ") on getArticle");
        }
    }

    /**
     * Web.getArticle에서 게시글 로드가 끝났을 때 호출되는 콜백 리스너입니다.
     * 로드된 Article을 해당 액티비티에 출력합니다.
     *
     * @param article 출력할 Article
     * @see Web#getArticle(ArticleViewerActivity, String)
     * @see Article
     */
    @Override
    public void onLoadArticle(final Article article) {
        if (article != null) {
            // Load article
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // set actionbar theme
                    //int begin = articleHref.indexOf("mid="), end = articleHref.indexOf("&page=");
                    CategoryManager categoryManager = CategoryManager.getInstance();
                    try {
                        articleBoard = article.getMid();
                        actBarManager.setActionBar(ArticleViewerActivity.this, toolbar, Integer.parseInt(article.getMid()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("PersonalColor", "Wrong color error(" + articleBoard + ") on getIcon");
                    }

                    tvTitle.setText(article.getTitle());

                    String imgAuthorProfile = article.getAuthorProfile();
                    if (imgAuthorProfile.equals(""))
                        imgAuthorProfile = "https://ssl.pstatic.net/static/cafe/cafe_pc/default/cafe_profile_77.png"; // default image
                    Glide.with(getApplicationContext()).load(imgAuthorProfile).into(ivProfile); // load image
                    tvAuthor.setText(article.getAuthor());
                    tvTime.setText(article.getTime());
                    tvView.setText(article.getView());

                    // body 출력용 html 문서 생성
                    String cssTheme;
                    if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) // TODO: 다크모드
                        cssTheme = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/MyCafeStyle-dark.css\" type=\"text/css\">\n"; // dark theme
                    else
                        cssTheme = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/MyCafeStyle.css\" type=\"text/css\">\n"; // light theme
                    String articleBody = "<!DOCTYPE html>\n" +
                            "<html lang=\"ko\">\n" +
                            "<head>\n" +
                            cssTheme +
                            "</head><body>\n" +
                            article.getBody().outerHtml() +
                            "</body></html>";
                    webBody.getSettings().setJavaScriptEnabled(true);
                    webBody.getSettings().setUserAgentString(WebClientManager.userAgentMobile);
                    webBody.loadDataWithBaseURL("", articleBody, "text/html", "UTF-8", "");
                    //Log.i("ArticleViewerActivity", articleBody);

                    tvVote.setText(article.getLikeIt());
                    /*
                    // profile box
                    Element footer = article.getFooter();
                    if (footer != null) {
                        Element votes = footer.selectFirst("div[class=votes]");
                        Element fileList = footer.selectFirst("div[class=fileList]");
                        Element tns = footer.selectFirst("div[class=tns]");
                        Element profile = footer.selectFirst("div[class=sign]");

                        if (votes != null) { // 추천/비추천
                            //TODO: 추천/비추천 영역
                            Elements up = votes.select("b");

                            tvVoteUp.setText("  " + up.get(0).text());
                            tvVoteDown.setText("  " + up.get(1).text());
                        }
                        if (fileList != null) { // 첨부파일
                            //TODO: 첨부파일 영역
                        }
                        if (tns != null) { // 태그
                            //TODO: 태그 및 공유 영역
                        }
                        if (profile != null) { // 프로필 창
                            String url = profile.selectFirst("img[class=pf]").attr("src");
                            Glide.with(getApplicationContext()).load(url).into(ivProfile);
                            tvProfile.setText(profile.text());

                            loProfile.setVisibility(View.VISIBLE);
                        }
                    }
                    */


                    // commnet box
                    Element feedback = article.getFeedback();
                    tvCommentCount.setText(feedback.selectFirst("h3[class=tit]").selectFirst("em").text()); // comment table

                    Elements comments = feedback.select("li");

                    ArrayList<Comment> arrayList = new ArrayList<>();

                    for (Element comment : comments) {
                        String author = "", time = "", imgProfile = "",
                                contentText = "", contentImage = "", sticker = "";
                        boolean iconNew = false, iconArticleAuthor = false;
                        if (comment.classNames().contains("comment")) { // 댓글인 경우에만
                            Comment comment1 = new Comment();

                            imgProfile = comment.selectFirst("span[class=thumb]").selectFirst("img").attr("src");
                            author = comment.selectFirst("span[class=name ellip]").text();
                            time = comment.selectFirst("span[class=date]").text();

                            // 댓글 내용
                            Element elementContentText = comment.selectFirst("p[class=txt]");
                            /*
                            if (elementContentText.selectFirst("a[class=u_cbox_target_name]") != null) // 대댓글이면
                                elementContentText.removeClass("u_cbox_target_name"); // 닉네임 중복 방지
                            */
                            contentText = elementContentText.text();
                            // 댓글 이미지/스티커
                            Element elementSticker = comment.selectFirst("div[class=u_cbox_sticker_section]"),
                                    elementImage = comment.selectFirst("div[class=image_section]");
                            if (elementSticker != null) // 스티커
                                sticker = elementSticker.selectFirst("img").attr("src");
                            if (elementImage != null) // 이미지
                                contentImage = elementImage.selectFirst("img").attr("src");

                            iconNew = comment.selectFirst("span[class=u_cbox_ico_new]") != null;
                            iconArticleAuthor = comment.selectFirst("span[class=ico_author]") != null;

                            // 데이터 입력
                            comment1.setAuthor(author)
                                    .setTime(time)
                                    .setContent(contentText)
                                    .setContentImage(contentImage)
                                    .setSticker(sticker)
                                    .setImgProfile(imgProfile)
                                    .setIconNew(iconNew)
                                    .setIconArticleAuthor(iconArticleAuthor);

                            // 대댓글 관련
                            int indent = 0;
                            if (comment.classNames().contains("re")) // 대댓글이면
                                indent = 1;
                            comment1.setIndentLevel(indent);
                            Log.e("ArticleViewerActivity", comment.className() + " " + indent);
                            Element targetName = comment.selectFirst("a[class=u_cbox_target_name]");// 대상 닉네임
                            if (targetName != null)
                                comment1.setParentAuthor(targetName.text());

                            // 데이터 입력
                            arrayList.add(comment1);
                        }
                    }
                    Log.v("ArticleViewerActivity", "Comment count: " + arrayList.size());
                    mAdapter.setListWith(arrayList, ArticleViewerActivity.this);

                    // 로딩 완료
                    layoutArticle.setVisibility(View.VISIBLE);
                }
            });
        } else {
            //TODO: Article 로드 실패 시 오류창 레이아웃 표시
        }

    }

    private int parseNumber(String str) throws NumberFormatException {
        return Integer.parseInt(str.replaceAll("[^0-9]", ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.article_viewer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_copy_url: {
                // 클립보드에 게시글 URL 복사
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Article URL", articleHref);
                clipboard.setPrimaryClip(clip);

                Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_text_copy_url, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
            case R.id.action_print_article: {
                createWebPrintJob(webBody);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 출력중인 게시글을 프린트합니다.
     *
     * @param webView
     */
    private void createWebPrintJob(WebView webView) {


        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }
}

interface LoadArticleListner {
    void onLoadArticle(Article article);
}