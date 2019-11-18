package com.wei756.ukkiukki.Network;

import android.os.Build;
import android.util.Log;

import org.jsoup.Connection;

import java.util.HashMap;
import java.util.Map;

public class WebClientManager {
    private static WebClientManager instance = null;

    private boolean logined = false;
    private final Map<String, String> cookie = new HashMap<>(), header = new HashMap<>();
    public final static String userAgentMobile = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.92 Mobile Safari/537.36";
    public final static String userAgentPC = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36";


    /**
     * http request로 전송할 cookie를 설정합니다.
     *
     * @return Connection
     */
    public Connection putHeader(Connection connection) {
        if (!cookie.containsKey("JSESSIONID")) // 최초 접속인지 확인
            resetCookies();

        return connection.cookies(cookie).headers(getHeader());
    }

    /**
     * http response로부터 전달받은 cookie를 저장합니다.
     */
   public void getCookies(Connection.Response response) {
        setCookiesMap(response.cookies());
        Log.i("Web", "Get cookies.");
        //printMap(cookie); // for debug
    }

    /**
     * Map을 cookie에 저장합니다.
     */
    void setCookiesMap(Map map) {
        cookie.putAll(map);
    }

    /**
     * cookie Map을 반환합니다.
     */
    Map getCookiesMap() {
        return cookie;
    }

    /**
     * cookie를 초기화합니다.
     */
    private void resetCookies() {
        cookie.clear();
    }

    /**
     * http request에 필요한 header와 cookie를 반환합니다.
     *
     * @return header
     */
    private Map getHeader() {
        if (!header.containsKey("user-agent")) {
            header.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            header.put("accept-encoding", "gzip, deflate, br");
            header.put("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            header.put("sec-fetch-mode", "navigate");
            header.put("sec-fetch-site", "none");
            header.put("sec-fetch-user", "?1");
            header.put("upgrade-insecure-requests", "1");
        }
        return header;
    }

    public static WebClientManager getInstance() {
        if (instance == null) {
            instance = new WebClientManager();
        }
        return instance;
    }

    public void setLogined(boolean logined) {
        this.logined = logined;
    }

    public boolean getLogined() {
        return logined;
    }
}
