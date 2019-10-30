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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    ActionBarManager actBarManager = ActionBarManager.getInstance();

    Toolbar toolbar;

    ConstraintLayout layoutArticle;

    TextView tvTitle;
    TextView tvLevelIcon;
    TextView tvAuthor;
    TextView tvTime, tvView, tvVote;

    WebView webBody;

    TextView tvVoteUp, tvVoteDown;
    ConstraintLayout loProfile;
    ImageView ivProfile;
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

        // set actionbar theme
        int begin = articleHref.indexOf("mid="), end = articleHref.indexOf("&page=");
        articleBoard = articleHref.substring(begin + 4, end);
        actBarManager.setActionBar(this, toolbar, articleBoard);

        layoutArticle = findViewById(R.id.id_article_viewer_viewer);

        // read_header
        tvTitle = findViewById(R.id.tv_article_viewer_title);
        tvLevelIcon = findViewById(R.id.icon_article_viewer_author_color);
        tvAuthor = findViewById(R.id.tv_article_viewer_author);
        tvTime = findViewById(R.id.tv_article_viewer_time);
        tvView = findViewById(R.id.tv_article_viewer_view);
        tvVote = findViewById(R.id.tv_article_viewer_vote);

        // read_body
        webBody = findViewById(R.id.web_article_viewer_body);

        // read_footer
        tvVoteUp = findViewById(R.id.tv_article_viewer_vote_up);
        tvVoteDown = findViewById(R.id.tv_article_viewer_vote_down);
        loProfile = findViewById(R.id.layout_article_viewer_footer_profile);
        ivProfile = findViewById(R.id.iv_article_viewer_profile);
        tvProfile = findViewById(R.id.tv_article_viewer_profile);

        tvTitle.setText(articleTitle);
        layoutArticle.setVisibility(View.GONE);
        loProfile.setVisibility(View.GONE);

        // feedback
        tvCommentCount = findViewById(R.id.tv_article_viewer_comment_count);

        // RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.view_article_viewer_comment);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new CommentAdapter(new ArrayList<Item>(), ArticleViewerActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        try {
            Web.getArticle(this, articleHref);
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
                    tvTitle.setText(article.getTitle());

                    String levelIcon = article.getLevelIcon();
                    if (!levelIcon.equals("")) {
                        tvLevelIcon.setText(levelIcon);
                        tvLevelIcon.setVisibility(View.VISIBLE);
                        try {
                            tvLevelIcon.setBackground(getResources().getDrawable(LevelIcon.getInstance().getIcon(levelIcon)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("PersonalColor", "Wrong color error(" + levelIcon + ") on getIcon");
                        }
                    } else {
                        tvLevelIcon.setVisibility(View.GONE);
                    }
                    tvAuthor.setText(article.getAuthor());
                    tvTime.setText(article.getTime());
                    tvView.setText(article.getView());

                    // body 출력용 html 문서 생성
                    String articleBody = "<!DOCTYPE html>\n" +
                            "<html lang=\"ko\">\n" +
                            "<head>\n" +
                            "<link rel=\"stylesheet\" href=\"file:///android_asset/css/board.default.css\" />\n" +
                            "<link rel=\"stylesheet\" href=\"file:///android_asset/css/commons.css\" />\n" +
                            "<link rel=\"stylesheet\" href=\"file:///android_asset/css/nanumsquareround.min.css\" />\n" +
                            "</head><body>\n" +
                            article.getBody().outerHtml() +
                            "</body></html>";
                    webBody.getSettings().setJavaScriptEnabled(true);
                    webBody.loadDataWithBaseURL("", articleBody, "text/html", "UTF-8", "");
                    //Log.i("ArticleViewerActivity", articleBody);

                    tvVote.setText(article.getVote());
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

                    // commnet box
                    Element feedback = article.getFeedback();
                    tvCommentCount.setText(feedback.selectFirst("em[class=theme_color_dark]").text());
                    try {
                        tvCommentCount.setTextColor(getResources().getColor((Integer) CategoryManager.getInstance().getParam(articleBoard, CategoryManager.COLOR_DARK)));
                    } catch (InvalidCategoryException e) {
                        Log.e("에러", "존재하지 않는 게시판 코드입니다(" + e.getMessage() + ")");
                        e.printStackTrace();
                    }

                    try {
                        Elements comments = feedback.selectFirst("ul[class=fbList]").select("li");

                        ArrayList<Comment> arrayList = new ArrayList<>();
                        String author, commentLevelIcon;
                        for (Element comment : comments) {
                            // 레벨 아이콘과 닉네임 분리
                            Element elementAuthor = comment.selectFirst("h3[class=author]");
                            author = elementAuthor.html();
                            commentLevelIcon = "";
                            if (author.contains("<a")) {
                                if (author.contains("</span>")) { // 레벨 아이콘이 있을 경우
                                    commentLevelIcon = elementAuthor.selectFirst("a").selectFirst("span").text();
                                    author = author.substring(author.indexOf("</span>") + 7, author.indexOf("</a>"));
                                } else {                          // 레벨 아이콘이 없을 경우
                                    author = author.substring(author.indexOf("\">") + 2, author.indexOf("</a>"));
                                }
                            } else {
                                if (author.contains("</span>")) {
                                    commentLevelIcon = elementAuthor.selectFirst("span").text();
                                    author = author.substring(author.indexOf("</span>") + 7);
                                }
                            }
                            // 데이터 입력
                            Comment comment1 = new Comment()
                                    .setId(parseNumber(comment.attr("id")))
                                    .setLevelIcon(commentLevelIcon)
                                    .setAuthor(author)
                                    .setTime(comment.selectFirst("p[class=time]").text())
                                    .setContent(comment.select("div").get(1).text())
                                    .setHref(comment.selectFirst("p[class=action]").selectFirst("a").attr("href"));

                            // 대댓글 관련
                            int indent;
                            try {
                                indent = parseNumber(comment.className());
                            } catch (NumberFormatException e) {
                                // 대댓글 아닐 경우
                                indent = 0;
                            }
                            comment1.setIndentLevel(indent);

                            if (indent > 1) { // 대댓글일 때
                                Comment parent;
                                for (int i = arrayList.size() - 1; i >= 0; i--) {
                                    parent = arrayList.get(i);
                                    if (indent > parent.getIndentLevel()) { // 부모 댓글 확인
                                        comment1.setParentAuthor(parent.getAuthor());
                                        break;
                                    }

                                }
                            }

                            arrayList.add(comment1);
                        }
                        mAdapter.setListWith(arrayList, ArticleViewerActivity.this);
                    } catch (NullPointerException e) {
                        //TODO: 댓글 없을 경우
                        e.printStackTrace();
                    }

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