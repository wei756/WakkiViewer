package com.wei756.ukkiukki.Network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.EntityBuilder;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.InputStreamBody;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

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
    @Deprecated
    public Connection putHeader(Connection connection) {
        if (!cookie.containsKey("JSESSIONID")) // 최초 접속인지 확인
            resetCookies();

        return connection.cookies(cookie).headers(getHeader());
    }

    /**
     * http request로 전송할 cookie를 설정합니다.
     */
    public void putHeader(HttpRequestBase connection) {
        if (!cookie.containsKey("JSESSIONID")) // 최초 접속인지 확인
            resetCookies();

        // put Cookies
        String cookieString = "";
        for (Map.Entry header : cookie.entrySet()) {

            String key = "" + header.getKey();
            String value = "" + header.getValue();

            Log.d("Web", key + ": " + value);
            cookieString += key + "=" + value + ";";
        }
        connection.addHeader("Cookie", cookieString);

        // put Headers
        for (Map.Entry header : getHeader().entrySet()) {

            String key = "" + header.getKey();
            String value = "" + header.getValue();

            Log.d("Web", key + ": " + value);
            connection.addHeader(key, value);
        }
    }

    /**
     * http response로부터 전달받은 cookie를 저장합니다.
     */
    @Deprecated
    public void getCookies(Connection.Response response) {
        setCookiesMap(response.cookies());
        Log.i("Web", "Get cookies.");
        //printMap(cookie); // for debug
    }

    /**
     * http response로부터 전달받은 cookie를 저장합니다.
     */
    public void getCookies(Header[] cookies) {
        Map<String, String> cookiesMap = new HashMap<>();

        for (Header cookieHeader : cookies) {
            String cookieStr = cookieHeader.getValue();
            int point = cookieStr.indexOf("=");
            String name = cookieStr.substring(0, point);
            String value = cookieStr.substring(point + 1, cookieStr.indexOf(";"));

            cookiesMap.put(name, value);
        }
        setCookiesMap(cookiesMap);
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
    private Map<String, String> getHeader() {
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

class WebRequestBuilder {
    public static final int METHOD_POST = 100;
    public static final int METHOD_GET = 101;

    public static final int USER_AGENT_PC = 300;
    public static final int USER_AGENT_MOBILE = 301;

    private HttpRequestBase request = null;
    private WebClientManager webClientManager = WebClientManager.getInstance();

    private String url = null;
    private int method;
    private String userAgent;
    private boolean useCookie;
    private String formData = null;
    private Uri uri = null;
    private Context context;

    private WebRequestBuilder() {
    }

    public static WebRequestBuilder create() {
        return new WebRequestBuilder();
    }

    public WebRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public WebRequestBuilder method(int method) {
        this.method = method;
        return this;
    }

    public WebRequestBuilder userAgent(int userAgent) {
        if (userAgent == USER_AGENT_PC)
            this.userAgent = WebClientManager.userAgentPC;
        else if (userAgent == USER_AGENT_MOBILE)
            this.userAgent = WebClientManager.userAgentMobile;
        return this;
    }

    public WebRequestBuilder useCookie(boolean useCookie) {
        this.useCookie = useCookie;
        return this;
    }

    public WebRequestBuilder dataFormUrlencoded(String formData) {
        this.formData = formData;
        return this;
    }

    public WebRequestBuilder dataMultipartUri(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        return this;
    }

    public Document build() throws IOException {
        // create httpClient
        HttpClient client = HttpClients.createDefault();

        // create heepRequest
        if (method == METHOD_POST)
            request = new HttpPost(url);
        else if (method == METHOD_GET)
            request = new HttpGet(url);

        // set User-Agent
        if (userAgent != null)
            request.addHeader("User-Agent", userAgent);

        // set Cookies
        if (useCookie)
            webClientManager.putHeader(request);

        // set Form-Data
        if (method == METHOD_POST && formData != null) {
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setContentType(ContentType.APPLICATION_FORM_URLENCODED);
            entityBuilder.setText(formData);
            ((HttpPost) request).setEntity(entityBuilder.build());
            request.addHeader("Upgrade-Insecure-Requests", "1");
            request.addHeader("Sec-Fetch-Site", "same-origin");
            request.addHeader("Sec-Fetch-Mode", "navigate");
        }

        // set Multipart
        FileInputStream fileInputStream = null;
        if (method == METHOD_POST && uri != null) {
            // image
            String imgPath = getImagePathToUri(context, uri); // uri로부터 실제 경로 추출
            String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1); // 파일 이름 추출
            String imgType = imgName.substring(imgName.lastIndexOf(".") + 1); // 파일 종류 추출
            Log.e("Web.debug", "imgPath: " + imgPath +
                    "\nimgName: " + imgName +
                    "\nimgType: " + imgType +
                    "\non Web.httpRequest");
            File file = new File(imgPath);
            fileInputStream = new FileInputStream(file);

            MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.addPart("image", new InputStreamBody(fileInputStream, file.getName()));

            ((HttpPost) request).setEntity(entity.build());

            // headers
            request.addHeader("Connection", "keep-alive");
            request.addHeader("Accept", "*/*");
            request.addHeader("Sec-Fetch-Site", "same-site");
            request.addHeader("Sec-Fetch-Mode", "cors");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");
            request.addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        }


        // execute httpRequest
        HttpResponse response = client.execute(request);
        String html = EntityUtils.toString(response.getEntity());

        // close Multipart fileStream
        if (method == METHOD_POST && uri != null) {
            fileInputStream.close();
        }

        // get Cookies from Response
        if (useCookie) {
            webClientManager.getCookies(response.getHeaders("Set-Cookie"));
            Log.v("Web", "JSESSIONID is " + webClientManager.getCookiesMap().get("JSESSIONID") + " now.");
        }

        // extract Response
        Document document = Jsoup.parse(html, url);
        //Log.e("Web", document.html()); // for debug: view loaded page with text
        return document;
    }

    /**
     * Uri로부터 절대 경로를 반환합니다.
     *
     * @param context context
     * @param data    uri
     * @return 절대 경로
     */
    public String getImagePathToUri(Context context, Uri data) {
        // 사용자가 선택한 이미지의 정보를 받아옴
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        // 이미지의 경로 값
        String imgPath = cursor.getString(column_index);

        return imgPath;
    }
}