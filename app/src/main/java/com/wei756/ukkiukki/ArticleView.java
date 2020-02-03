package com.wei756.ukkiukki;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.wei756.ukkiukki.Network.WebClientManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleView extends WebView {
    RelativeLayout layout;

    public ArticleView(Context context) {
        super(context);
        setupArticleView();
    }

    public ArticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupArticleView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupArticleView() {
        WebSettings webSettings = getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUserAgentString(WebClientManager.userAgentMobile);

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                String href = isWakkiPage(url);
                if (href != null) {  // 클릭한 링크가 우왁끼 게시글이면

                    ArticleViewerActivity.startArticleViewerActivity(getContext(), "", href, false);

                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);

            }

        });
    }

    public void setLayout(RelativeLayout layout) {
        this.layout = layout;
    }

    private String isWakkiPage(String url) {
        String[] pattern = {"https://cafe.naver.com/steamindiegame/(\\d+)",
                "https://m.cafe.naver.com/steamindiegame/(\\d+)",
                "https://m.cafe.naver.com/ca-fe/web/cafes/27842958/articles/(\\d+)"};
        Pattern p;
        for (String pat : pattern) {
            p = Pattern.compile(pat);
            Matcher m = p.matcher(url);
            while (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

}
