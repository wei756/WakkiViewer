package com.wei756.ukkiukki.Network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wei756.ukkiukki.NaverLoginActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("SetJavaScriptEnabled")
public class NaverLoginView extends WebView {

    private Web web = Web.getInstance();
    private WebClient webClient;
    private WebClientManager webClientManager = WebClientManager.getInstance();
    private NaverLoginActivity naverLoginActivity;

    public NaverLoginView(Context context) {
        super(context);

        initWebView(context);
    }

    public NaverLoginView(Context context, AttributeSet attr) {
        super(context, attr);

        initWebView(context);
    }

    private void initWebView(Context context) {
        getSettings().setDomStorageEnabled(true);
        getSettings().setJavaScriptEnabled(true);

        webClient = new WebClient(context);
        setWebViewClient(webClient);
        Log.v("NaverLoginView", "NaverLoginView loaded.");
    }

    public void loadLoginPage(NaverLoginActivity naverLoginActivity) {
        this.naverLoginActivity = naverLoginActivity;
        loadUrl("https://nid.naver.com/nidlogin.login?url=https%3A%2F%2Fm.cafe.naver.com%2FArticleAllList.nhn%3Fcluburl%3Dsteamindiegame");
    }

    class WebClient extends WebViewClient
    {
        private Context m_context;

        public WebClient(Context context) {
            super();

            this.m_context = context;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.v("NaverLoginView", "Loading URL started: " + url);

            // get cookies
            String[] cookies = CookieManager.getInstance().getCookie(url).split(" ");
            Log.d("WebClientManager", "All the cookies in a string:");

            // generate cookies map
            Map<String, String> mapCookies = new HashMap<>();
            for (String cookie : cookies) {
                String key = "", value = "";

                // cookie 값 추출
                Pattern patKey = Pattern.compile("([a-zA-Z0-9_]+)="),
                        patValue = Pattern.compile("=(.*);?");
                Matcher m = patKey.matcher(cookie);
                if (m.find())
                    key = m.group(1);
                m = patValue.matcher(cookie);
                if (m.find())
                    value = m.group(1).replaceAll(";", "");

                Log.e("NaverLoginView", "\"" + key + "\": " + "\"" + value + "\"");
                mapCookies.put(key, value);
            }

            if (url.equals("https://m.cafe.naver.com/ArticleAllList.nhn?cluburl=steamindiegame")) { // 로그인 성공
                Log.i("NaverLoginView", "Login success");
                // 로그인 세션 저장
                web.applyLoginSession(mapCookies);
                webClientManager.setLogined(true);
                Web.getInstance().loadMyInfomation();

                // 페이지 복귀
                naverLoginActivity.finish();
            }
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.equals("https://m.cafe.naver.com/ArticleAllList.nhn?cluburl=steamindiegame")) { // 로그인 성공
                // 로딩 레이아웃 표시
                naverLoginActivity.setLoading();
            }
        }

    }
}

