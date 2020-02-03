package com.wei756.ukkiukki;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wei756.ukkiukki.Network.Web;
import com.wei756.ukkiukki.Network.WebClientManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ArticleViewerActivity extends AppCompatActivity implements LoadArticleListner {
    private Web web = Web.getInstance();

    ActionBarManager actBarManager = ActionBarManager.getInstance();
    FragmentManager fragmentManager = getSupportFragmentManager();

    AppBarLayout appbar;
    Toolbar toolbar;

    ConstraintLayout layoutArticle;

    TextView tvTitle, tvHead;
    ImageView ivProfile;
    TextView tvAuthor;
    TextView tvTime, tvView;

    ArticleView webBody;


    TextView tvCommentCount;

    CommentAdapter mAdapter;
    RecyclerView mRecyclerView;

    String articleTitle, articleHref, articleBoard;

    ConstraintLayout bottomBarLayout;
    TextView btnLikeIt, btnComment, btnShare, btnReturnToList;

    ArticleViewerCommentListFragment fragmentCommentList;
    boolean isOpenedCommentPage = false;

    Map likeLiMap = null;
    String guestToken = null;
    long timestamp = 0;
    boolean isReacted = false;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_viewer);
        appbar = findViewById(R.id.appbar_article_viewer);
        toolbar = findViewById(R.id.toolbar_article_viewer);
        toolbar.setTitle("");
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_overflow_24dp));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Floating button
        FloatingActionButton fab = findViewById(R.id.fab_article_viewer_comment);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        // get intent
        Intent intent = new Intent(this.getIntent());
        articleTitle = intent.getStringExtra("article_title");
        articleHref = intent.getStringExtra("article_href");

        layoutArticle = findViewById(R.id.id_article_viewer_viewer);

        // read_header
        tvTitle = findViewById(R.id.tv_article_viewer_title);
        tvHead = findViewById(R.id.tv_article_viewer_head);
        ivProfile = findViewById(R.id.iv_article_viewer_profile);
        tvAuthor = findViewById(R.id.tv_article_viewer_author);
        tvTime = findViewById(R.id.tv_article_viewer_time);
        tvView = findViewById(R.id.tv_article_viewer_view);
        //tvVote = findViewById(R.id.tv_article_viewer_vote);

        // read_body
        webBody = findViewById(R.id.web_article_viewer_body);
        webBody.setVisibility(View.GONE);
        webBody.setLayout(findViewById(R.id.layout_article_viewer_body));

        tvTitle.setText(articleTitle);
        layoutArticle.setVisibility(View.GONE);

        // feedback
        tvCommentCount = findViewById(R.id.tv_article_viewer_comment_count);

        // RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.view_article_viewer_comment);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new CommentAdapter(new ArrayList<Item>(), ArticleViewerActivity.this, CommentAdapter.THEME_PREVIEW);
        mRecyclerView.setAdapter(mAdapter);

        // Bottom bar
        bottomBarLayout = (ConstraintLayout) findViewById(R.id.layout_article_viewer_bottom_bar);
        btnComment = (TextView) findViewById(R.id.btn_article_viewer_bottom_bar_comment);
        btnLikeIt = (TextView) findViewById(R.id.btn_article_viewer_bottom_bar_likeit);
        btnShare = (TextView) findViewById(R.id.btn_article_viewer_bottom_bar_share);
        btnReturnToList = (TextView) findViewById(R.id.btn_article_viewer_bottom_bar_returntolist);

        // Comment page
        fragmentCommentList = (ArticleViewerCommentListFragment) fragmentManager.findFragmentById(R.id.fragment_comment_list);
        fragmentCommentList.setParentActivity(ArticleViewerActivity.this);
        fragmentCommentList.setVisibility(View.GONE);
        hideCommentPage();

        // Load article
        try {
            web.getArticle(this, articleHref);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ArticleViewerActivity", "Article load error(" + articleHref + ") on getArticle");
        }
        if (intent.getBooleanExtra("commentpage", false))
            showCommentPage(false);

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
            runOnUiThread(() -> {
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

                tvTitle.setText(article.getTitle()); // 게시글 제목

                String head = article.getHead(); // 게시글 말머리
                if (head.equals("null")) {
                    tvHead.setVisibility(View.GONE);
                } else {
                    tvHead.setVisibility(View.VISIBLE);
                    tvHead.setText(head);
                }

                String imgAuthorProfile = article.getAuthorProfile(); // 글작성자 프로필 사진
                if (imgAuthorProfile.equals(""))
                    imgAuthorProfile = "https://ssl.pstatic.net/static/cafe/cafe_pc/default/cafe_profile_77.png"; // default image
                Glide.with(getApplicationContext()).load(imgAuthorProfile).into(ivProfile); // load image
                ivProfile.setClipToOutline(true);
                tvAuthor.setText(article.getAuthor()); // 글작성자 닉네임
                View.OnClickListener profileEvent = view -> ProfileActivity.startProfileActivity(ArticleViewerActivity.this, article.getAuthorId());
                ivProfile.setOnClickListener(profileEvent);
                tvAuthor.setOnClickListener(profileEvent);

                Long timestamp = Long.parseLong(article.getTime());
                tvTime.setText(new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss", Locale.KOREA).format(new Date(timestamp))); // 게시글 시간

                tvView.setText(article.getView()); // 게시글 조회수

                // body 출력용 html 문서 생성
                String cssTheme;
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) // 다크모드
                    cssTheme = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/app-dark.css\" type=\"text/css\">\n"; // dark theme
                else
                    cssTheme = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/app.css\" type=\"text/css\">\n"; // light theme

                // 본문 html 작성
                String body = (String) article.getArticle().get("content");
                ArrayList<Map> listElements = (ArrayList) article.getArticle().get("contentElements"); // CONTENT-ELEMENT 처리
                if (listElements != null)
                    for (int i = 0; i < listElements.size(); i++) {
                        Map mapElement = listElements.get(i);
                        String tag;
                        String stickerId, marketUrl, url;
                        int width, height;
                        String type = String.valueOf(mapElement.get("type"));
                        Map json = (Map) mapElement.get("json");
                        switch (type) {
                            case "STICKER": // 스티커
                                stickerId = (String) json.get("stickerId");
                                marketUrl = (String) json.get("marketUrl");
                                url = (String) json.get("url");
                                width = (int) json.get("width");
                                height = (int) json.get("height");

                                tag = "<a href=\"" + marketUrl + "\" target=\"_blank\">" +
                                        "<img src=\"" + url +
                                        "\" alt=\"" + stickerId +
                                        "\" width=\"" + width +
                                        "\" height=\"" + height +
                                        "\" nhn_extra_image=\"true\" data-type=\"sticker\"></a>";
                                break;
                            case "IMAGE": // 사진
                                Map mapImage = (Map) json.get("image");
                                url = "" + mapImage.get("url");
                                url += (url.contains("?") ? "&" : "?");
                                url += "type=" + mapImage.get("type");
                                //width = (int) json.get("width");
                                //height = (int) json.get("height");
                                Log.e("url", url);

                                tag = "<div style=\"margin:0 -15px;line-height:0;\"><img class=\"fx\" src=\"" + url +
                                        "\" alt=\"본문 이미지\"></div>";
                                break;
                            default:
                                tag = "[[[현재 버전에서 지원되지 않는 태그입니다.]]]"; // 에러 처리
                                break;
                        }

                        body = body.replaceFirst("\\[\\[\\[CONTENT-ELEMENT-" + i + "]]]", tag);

                    }
                String articleBody = "<!DOCTYPE html>\n" +
                        "<html lang=\"ko\">\n" +
                        "<head>\n" +
                        cssTheme +
                        "</head><body><div id=\"ct\" class style><div id=\"postContent\" class=\"post_cont font_zoom1\">" +
                        body +
                        /*
                        "<script type=\"text/javascript\">" +
                        "\n" +
                        "var g_sUserId = '" + (ProfileManager.getInstance().isLogined() ? ProfileManager.getInstance().getId() : "") + "';\n" +
                        "var g_sCafeId = '" + web.cafeId + "';\n" +
                        "var g_sCafeUrl = 'steamindiegame';\n" +
                        "\n" +
                        "var g_sCafeMainUrl = 'https://cafe.naver.com';\n" +
                        "var g_sCafeMobileWebUrl = 'https://m.cafe.naver.com';\n" +
                        "var g_sNoteSendUrl = 'https://m.note.naver.com/mobile/mobileSendNoteForm.nhn?reply=1&targetUserId=';\n" +
                        "var g_sUploadDomain = window.location.protocol === 'https:' ? 'https://up.cafe.naver.com' : 'http://up.cafe.naver.com';\n" +
                        "var g_sCafeFilesDomain = 'http://cafefiles.naver.net';\n" +
                        "var g_sCafeFilesHttpsDomain = 'https://cafefiles.pstatic.net';\n" +
                        "var g_sCafeThumbDomain = 'https://cafethumb.pstatic.net';\n" +
                        "var g_sMCafeThumbDomain = 'https://mcafethumb-phinf.pstatic.net';\n" +
                        "var g_sGfmarketDomain = 'http://m.gfmarket.naver.com';\n" +
                        "var g_sGfmarketThumbnailDomain = 'https://storep-phinf.pstatic.net';\n" +
                        "var g_sPhotoInfraUploadDomain = 'cafe.upphoto.naver.com';\n" +
                        "var g_sBlogUrl = 'https://m.blog.naver.com';\n" +
                        "var g_sDThumbUrl = 'https://dthumb-phinf.pstatic.net/?src=';\n" +
                        "var g_sLikeDomain = 'https://cafe.like.naver.com';\n" +
                        "var g_sNpayPurchaseDetailUrl = 'https://m.pay.naver.com/o/orderStatus/';\n" +
                        "var g_sNpaySaleDetaiUrl = 'https://m.pay.naver.com/o/saleStatus/';\n" +
                        "var g_sBAStatDomain = 'https://scv.band.us';\n" +
                        "var g_sCafeTalk = 'https://talk.cafe.naver.com';\n" +
                        "\n" +
                        "var g_sLoginUrl = 'https://nid.naver.com/nidlogin.login?svctype=262144&url=';\n" +
                        "var g_sLogoutUrl = 'https://nid.naver.com/nidlogin.logout?svctype=262144&url=';\n" +
                        "var g_sAutoComplateDomain = 'https://mac.search.naver.com/mobile/ac';\n" +
                        "var g_sNaverStatMobileUiUrl = 'https://cafe.stat.naver.com/m/cafe';\n" +
                        "\n" +
                        "var g_sEncodedRequestUrl = 'http%3A%2F%2Fm.cafe.naver.com%2FArticleRead.nhn%3Fclubid%3D" + web.cafeId + "%26articleid%3D" + articleHref + "';</script>" +
                        "<script type=\"text/javascript\" src=\"file:///android_asset/js/MyCafeCommonScript.js\" charset=\"UTF-8\"></script>" +
                        "<script type=\"text/javascript\" src=\"file:///android_asset/js/ugcvideoplayer.js\" charset=\"UTF-8\"></script>" +
                        */
                        "</div></div></body></html>";
                webBody.loadDataWithBaseURL(MessageFormat.format(web.articleReadUrl, web.cafeId, articleHref), articleBody, "text/html", "UTF-8", "");


                // commnet box
                article.setComment("" + article.getArticle().get("commentCount"));
                tvCommentCount.setText(article.getComment()); // 댓글수

                ArrayList<Map> listComment = (ArrayList) article.getComments().get("items");
                ArrayList<Comment> arrayList = new ArrayList<>();

                for (Map mapComment : listComment) {
                    String author, time, imgProfile,
                            contentText, contentImage = "", sticker = "";
                    boolean iconNew, iconArticleAuthor, mine;
                    Comment comment1 = new Comment();

                    Map mapProfile = (Map) mapComment.get("writer");
                    imgProfile = "" + ((Map) mapProfile.get("image")).get("url");
                    author = "" + mapProfile.get("nick");
                    time = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss", Locale.KOREA).format(new Date((Long) mapComment.get("updateDate")));

                    // 대댓글 관련
                    int indent = 0;
                    if ((boolean) mapComment.get("isRef")) // 대댓글이면
                        indent = 1;
                    comment1.setIndentLevel(indent);
                    Map mapReplyMember = (Map) mapComment.get("replyMember");// 대상 닉네임
                    if (mapReplyMember != null)
                        comment1.setParentAuthor((String) mapReplyMember.get("nick"));

                    // 댓글 내용
                    contentText = (comment1.getParentAuthor() != null ? comment1.getParentAuthor() : "") + " " + mapComment.get("content");

                    // 댓글 이미지/스티커
                    Map mapSticker = (Map) mapComment.get("sticker"),
                            mapImage = (Map) mapComment.get("image");
                    if (mapSticker != null) // 스티커
                        sticker = "" + mapSticker.get("url") + "?type=" + mapSticker.get("type");
                    if (mapImage != null) // 이미지
                        contentImage = "" + mapImage.get("url") + "?type=" + mapImage.get("type");

                    iconNew = (boolean) mapComment.get("isNew"); // 새 댓글인지 여부
                    iconArticleAuthor = (boolean) mapComment.get("isArticleWriter"); // 게시글 작성자의 댓글인지 여부
                    mine = (boolean) mapComment.get("isRemovable"); // 본인댓글 여부

                    // 데이터 입력
                    comment1.setAuthor(author)
                            .setTime(time)
                            .setContent(contentText)
                            .setContentImage(contentImage)
                            .setSticker(sticker)
                            .setImgProfile(imgProfile)
                            .setIconNew(iconNew)
                            .setIconArticleAuthor(iconArticleAuthor)
                            .setMine(mine);

                    // 데이터 입력
                    arrayList.add(comment1);
                }
                Log.v("ArticleViewerActivity", "Comment count: " + arrayList.size());
                mAdapter.setListWith(arrayList, ArticleViewerActivity.this);


                // Bottom bar
                btnComment.setText(article.getComment());

                btnComment.setOnClickListener(view -> showCommentPage(false));

                // 좋아요 버튼
                btnLikeIt.setOnClickListener(view -> {
                    if (isReacted)
                        unlikeIt();
                    else
                        likeIt();
                });

                // 좋아요 로딩
                updateLikeItStatus(true);


                // 로딩 완료
                layoutArticle.setVisibility(View.VISIBLE);

                // 댓글 리스트 로딩
                new Thread(() -> fragmentCommentList.initCommentPage(articleHref, null)).start();
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
                if (isOpenedCommentPage)
                    hideCommentPage();
                else
                    finish();
                return true;
            }
            case R.id.action_open_with_web: {
                Web.openWebPageWithBrowser(this, "https://cafe.naver.com/steamindiegame/" + articleHref);
                return true;
            }
            case R.id.action_copy_url: {
                // 클립보드에 게시글 URL 복사
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Article URL", "https://cafe.naver.com/steamindiegame/" + articleHref);
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

    @Override
    public void onBackPressed() {
        if (isOpenedCommentPage) {
            hideCommentPage();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * guestToken과 좋아요 여부를 불러옵니다.
     */
    protected void getGuestToken() {
        Map map = web.getGuestToken(articleHref);
        guestToken = null;
        try {
            // guestToken
            if (map.containsKey("guestToken"))
                guestToken = (String) map.get("guestToken");
            if (map.containsKey("timestamp"))
                timestamp = (long) map.get("timestamp");
            //Log.e("DEBUG", "guestToken: " + guestToken + "\n" +
            //        "timestamp: " + timestamp);

            // ifLikeIt
            ArrayList data = (ArrayList) map.get("contents");
            Map dataMap = (Map) data.get(0);
            data = (ArrayList) dataMap.get("reactions");
            dataMap = (Map) data.get(0);
            if (dataMap.containsKey("isReacted"))
                isReacted = (boolean) dataMap.get("isReacted");

            Log.e("DEBUG", "좋아요: " + isReacted);
        } catch (IndexOutOfBoundsException e) {
            Log.w("ArticleViewerAct.err", "좋아요가 없는 게시글입니다.");
            //e.printStackTrace();
            isReacted = false;
        } catch (NullPointerException e) {
            Log.w("ArticleViewerAct.err", "Error occur at ArticleViewerActivity.getGuestToken()");
            e.printStackTrace();
            isReacted = false;
        }
    }

    /**
     * 게시글에 좋아요 표시를 합니다.
     */
    void likeIt() {
        new Thread(() -> {
            if (guestToken == null)
                getGuestToken();
            else {
                final int returnCode = web.likeIt(Web.LIKE, articleHref, guestToken, timestamp);
                runOnUiThread(() -> {
                    switch (returnCode) {
                        case Web.RETURNCODE_SUCCESS:
                            //Toast.makeText(ArticleViewerActivity.this, "좋아요를 성공했습니다.", Toast.LENGTH_SHORT).show();
                            updateLikeItStatus(true);
                            break;
                        case Web.RETURNCODE_ERROR_LIKEIT_TOKEN_EXPIRED: // 토큰 만료
                            Toast.makeText(ArticleViewerActivity.this, "화면을 오랫동안 열어두어 클릭 가능 시간을 초과했습니다.\n이용을 위해서는 새로고침을 해주세요.", Toast.LENGTH_SHORT).show();
                            break;
                        case Web.RETURNCODE_FAILED: // 요청 실패
                            Toast.makeText(ArticleViewerActivity.this, "좋아요 요청이 실패했습니다.\n새로고침하여 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            break;
                        case Web.RETURNCODE_ERROR_ALREADY_LIKED: // 이미 좋아요한 게시글
                            Toast.makeText(ArticleViewerActivity.this, "이미 좋아요한 컨텐츠입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ArticleViewerActivity.this, "알 수 없는 에러가 발생하였습니다.\n새로고침하여 다시 시도해주세요.(에러코드: " + returnCode + ")", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).start();
    }

    /**
     * 게시글에 좋아요 표시를 해제합니다.
     */
    void unlikeIt() {
        new Thread(() -> {
            if (guestToken == null)
                getGuestToken();
            else {
                final int returnCode = web.likeIt(Web.UNLIKE, articleHref, guestToken, timestamp);
                runOnUiThread(() -> {
                    switch (returnCode) {
                        case Web.RETURNCODE_SUCCESS:
                            updateLikeItStatus(true);
                            break;
                        case Web.RETURNCODE_ERROR_LIKEIT_TOKEN_EXPIRED: // 토큰 만료
                            Toast.makeText(ArticleViewerActivity.this, "화면을 오랫동안 열어두어 클릭 가능 시간을 초과했습니다.\n이용을 위해서는 새로고침을 해주세요.", Toast.LENGTH_SHORT).show();
                            break;
                        case Web.RETURNCODE_FAILED: // 요청 실패
                            Toast.makeText(ArticleViewerActivity.this, "좋아요 요청이 실패했습니다.\n새로고침하여 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            break;
                        case Web.RETURNCODE_ERROR_NOT_LIKED: // 좋아요하지 않은 게시글
                            Toast.makeText(ArticleViewerActivity.this, "좋아요하지 않은 컨텐츠입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(ArticleViewerActivity.this, "알 수 없는 에러가 발생하였습니다.\n새로고침하여 다시 시도해주세요.(에러코드: " + returnCode + ")", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).start();
    }

    /**
     * 좋아요 상태를 업데이트합니다.
     */
    void updateLikeItStatus(final boolean resetToken) {
        new Thread(() -> {
            // 좋아요 리스트
            likeLiMap = web.getArticleLikeItList(articleHref);
            if (likeLiMap != null) {
                final String totalCount = "" + (int) likeLiMap.get("totalCount");

                runOnUiThread(() -> btnLikeIt.setText(totalCount));
            }
            // 좋아요 토큰
            if (resetToken)
                getGuestToken();

            // 좋아요 상태 반영
            runOnUiThread(() -> btnLikeIt.setSelected(isReacted));

        }).start();
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

    public void showCommentPage(boolean openCommentEdit) {
        isOpenedCommentPage = true;
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.commentlist_slide_up, R.anim.commentlist_slide_down)
                .show(fragmentCommentList)
                .commit();
        // Hide bottom bar
        bottomBarLayout.setVisibility(View.GONE);

        // Open comment EditText
        if (openCommentEdit)
            fragmentCommentList.etContent.post(() -> {
                fragmentCommentList.etContent.setFocusableInTouchMode(true);
                fragmentCommentList.etContent.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.showSoftInput(fragmentCommentList.etContent, 0);

            });

        // Change scrollflag
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();

        params.setScrollFlags(0);
        appBarLayoutParams.setBehavior(null);
        appbar.setLayoutParams(appBarLayoutParams);
    }

    public void hideCommentPage() {
        isOpenedCommentPage = false;
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.commentlist_slide_up, R.anim.commentlist_slide_down)
                .hide(fragmentCommentList)
                .commit();
        // Show bottom bar
        bottomBarLayout.setVisibility(View.VISIBLE);

        // Change scrollflag
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();

        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
        appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
        appbar.setLayoutParams(appBarLayoutParams);

    }

    public static void startArticleViewerActivity(Context context, String title, String href, boolean commentpage) {
        Intent intent = new Intent(context, ArticleViewerActivity.class);
        intent.putExtra("article_title", title);
        intent.putExtra("article_href", href);
        intent.putExtra("commentpage", commentpage);
        context.startActivity(intent);
    }
}

interface LoadArticleListner {
    void onLoadArticle(Article article);
}