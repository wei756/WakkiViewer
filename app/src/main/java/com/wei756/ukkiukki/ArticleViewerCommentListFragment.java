package com.wei756.ukkiukki;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wei756.ukkiukki.Network.Web;

import java.util.ArrayList;
import java.util.Map;


public class ArticleViewerCommentListFragment extends Fragment {

    public final static int GALLERY_REQUEST_CODE = 0;
    public final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private Context mContext;

    ArticleViewerActivity act;
    SwipeRefreshLayout mSwipeRefreshLayout;

    RecyclerView mRecyclerView;
    CommentAdapter mAdapter;
    EndlessRecyclerViewScrollListener scrollListener;
    RelativeLayout mProgressBar;

    Web web = Web.getInstance();

    EditText etContent;
    TextView btnSubmitComment;
    ImageView btnEmoticon, btnPhoto;

    StickerList stickerList;
    RecyclerView stickerView, stickerpackView;
    StickerPackListAdapter stickerpackAdapter;

    private String articleId;
    private int page = 1;

    Map likeItMap;

    Map<String, String> commentPhotoMap = null;

    public static String LAST_PAGE = "마지막 페이지";

    private boolean loadedLastPage = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_viewer_comment_list, container, false);

        // RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_comment_list);
        mAdapter = new CommentAdapter(new ArrayList<Item>(), getContext(), CommentAdapter.THEME_COMMENTPAGE);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // ProgressBar
        mProgressBar = (RelativeLayout) view.findViewById(R.id.commentList_loadingPanel);

        // Endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadCommentList(page, false);
            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);
        mAdapter.setScrollListener(scrollListener); // for endless scroll

        // Swipe refresh
        mSwipeRefreshLayout = view.findViewById(R.id.layout_commentpage_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(() -> initCommentPage(articleId, likeItMap));

        // Bottom bar
        etContent = (EditText) view.findViewById(R.id.et_commentpage_content);
        btnSubmitComment = (TextView) view.findViewById(R.id.btn_commentpage_submit);
        btnSubmitComment.setOnClickListener(view1 -> postComment());
        btnEmoticon = (ImageView) view.findViewById(R.id.btn_commentpage_emoticon);
        btnPhoto = (ImageView) view.findViewById(R.id.btn_commentpage_photo);
        btnPhoto.setOnClickListener(view12 -> {
            // 권한 체크
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(act,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(act,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                }
            } else { // 권한 있을 시
                postCommentPhoto();
            }


        });

        // Sticker
        stickerView = (RecyclerView) view.findViewById(R.id.rv_sticker_list);
        stickerpackView = (RecyclerView) view.findViewById(R.id.rv_stickerpack_list);

        stickerList = new StickerList(getActivity(), stickerpackView, stickerView);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 부여됨
                } else {
                    // 권한 부여 거부
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE: // 사진 선택 창
                    //data.getData returns the content URI for the selected Image
                    final Uri selectedImage = data.getData();
                    Log.e("ArticleViewerCommentLis", "Raw uri: " + selectedImage.toString());
                    new Thread(() -> {
                        Map<String, String> map = web.postCommentPhoto(act, selectedImage);
                        String returncode = map.get("returncode");
                        if (returncode.equals("" + Web.RETURNCODE_SUCCESS)) { // success
                            commentPhotoMap = map;

                        } else {
                            //TODO: 에러코드 처리
                        }
                    }).start();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 댓글을 업로드합니다.
     */
    private void postComment() {
        new Thread(() -> {
            final int returncode = web.postComment(articleId, "", etContent.getText().toString(), stickerList.getStickerCode(), commentPhotoMap);
            act.runOnUiThread(() -> {
                if (returncode == Web.RETURNCODE_SUCCESS) {
                    Toast.makeText(act, "댓글을 작성하였습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("dddddddddd", etContent.getText().toString());

                    etContent.setText(""); // 댓글내용 삭제
                    stickerList.setStickerCode(null); // 댓글스티커 삭제
                    commentPhotoMap = null; // 댓글사진 삭제
                } else if (returncode == Web.RETURNCODE_ERROR_COMMENT_SPAM) {
                    Toast.makeText(act, "스팸으로 감지된 댓글입니다.", Toast.LENGTH_SHORT).show();
                } else if (returncode == Web.RETURNCODE_ERROR_COMMENT_BLANK) {
                    Toast.makeText(act, "내용이 없는 댓글 작성 시도입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * 댓글에 사진을 첨부하기 위해 사진 선택 창을 불러옵니다.
     *
     * @see ArticleViewerCommentListFragment#onActivityResult(int, int, Intent)
     */
    private void postCommentPhoto() {
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    public void initCommentPage(final String articleId, final Map likeItMap) {
        this.articleId = articleId;
        this.loadedLastPage = false;
        commentPhotoMap = null;

        loadCommentList(1, true);
        this.likeItMap = likeItMap;
        if (likeItMap != null) {

        }
    }

    public void loadCommentList(final int page, final boolean reset) {
        setRefreshing(true);
        this.page = page;
        new Thread(() -> {
            if (reset)
                setVisibility(View.GONE);

            ArrayList<Comment> comments = null;
            if (!loadedLastPage) {
                comments = web.getArticleCommentList(articleId, page, CommentAdapter.ORDERBY_UPLOAD); // 마지막 페이지가 아닐 때만 로드

                if (comments != null) { // 댓글목록 로딩 성공시
                    if (comments.get(comments.size() - 1).getImgProfile() == LAST_PAGE) { // 로드된 게 마지막 페이지일 경우
                        comments.remove(comments.size() - 1);
                        loadedLastPage = true;
                    }

                    // 리스트 추가
                    if (reset)
                        mAdapter.setListWith(comments, act);
                    else
                        mAdapter.addListWith(comments, act);

                    setVisibility(View.VISIBLE);

                } else {
                    mAdapter.clearList(act);
                }
            }
            setRefreshing(false);
        }).start();
    }

    public void setParentActivity(ArticleViewerActivity act) {
        this.act = act;
    }

    public void setVisibility(final int visibility) {
        act.runOnUiThread(() -> {
            mRecyclerView.setVisibility(visibility);
            if (mProgressBar != null) {
                if (visibility == View.GONE)
                    mProgressBar.setVisibility(View.VISIBLE);
                else
                    mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    public void setRefreshing(final boolean refreshing) {
        act.runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
    }
}
