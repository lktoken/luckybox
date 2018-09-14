package com.lktoken.luckybox.phonecheck;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class InternRobin {

    private static final String   BAIDU_SEARCH_URL = "http://www.baidu.com/s?wd=";
    private static final MarkDesc BAIDU_NO_MARK    = new MarkDesc("baidu");
    private static final MarkDesc SO360_NO_MARK    = new MarkDesc("so360");
    private static final String   SO360_SEARCH_URL = "https://www.so.com/s?q=";

    private OkHttpClient          client;
    private String                proxyAddr;
    private Integer               proxyPort;
    private String                proxyUser;
    private String                proxyPassword;

    public void init() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // 有效配置代理IP和地址
        if (proxyAddr != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddr, proxyPort));
            builder.proxy(proxy);

            // 需要认证的代理
            if (proxyUser != null) {
                builder.proxyAuthenticator(new ProxyAuthenticator(proxyUser, proxyPassword));
            }
        }

        builder.addInterceptor(new NormalWebBrowserHeaderInterceptor());
        client = builder.build();
    }

    /**
     * 检查百度标记情况
     * 
     * @param phone
     * @return
     */
    public MarkDesc baiduCheck(String phone) {
        Request request = new Request.Builder().url(BAIDU_SEARCH_URL + phone).build();
        try {
            Call call = client.newCall(request);
            Response ret = call.execute();
            if (!ret.isSuccessful()) {
                ret = call.clone().execute();
            }
            if (ret.isSuccessful()) {
                String body = ret.body().string();
                return parseBaidu(body);
            }
        } catch (IOException e) {
            // TODO: warning log
        }
        return BAIDU_NO_MARK;
    }

    /**
     * 解析百度返回的页面
     * 
     * @param body
     * @return
     */
    public MarkDesc parseBaidu(String body) {
        if (null == body || body.length() == 0) {
            return BAIDU_NO_MARK;
        }
        Document doc = Jsoup.parse(body);
        Elements fraudphoneLabel = doc.getElementsByClass("op_fraudphone_label");
        if (fraudphoneLabel.isEmpty()) {
            return BAIDU_NO_MARK;
        }
        MarkDesc mark = new MarkDesc("baidu");
        mark.setMark(trim(fraudphoneLabel.get(0).text()));

        Elements fraudphoneWord = doc.getElementsByClass("op_fraudphone_word");
        if (fraudphoneWord.isEmpty()) {
            return mark;
        }

        String descText = trim(fraudphoneWord.get(0).text());
        if (descText == null) {
            return mark;
        }
        String tagCountStr = substrBetween(descText, "被", "个");
        if (tagCountStr != null) {
            mark.setCount(toInt(tagCountStr));
        }
        mark.setMarkAs(substrBetween(descText, "标记为", ","));
        mark.setOriginDesc((descText));

        Elements markSource = fraudphoneWord.get(0).getElementsByTag("a");
        if (!markSource.isEmpty()) {
            mark.setSource(markSource.get(0).text());
        }
        return mark;
    }

    /**
     * 检查360标记情况
     *
     * @param phone
     * @return
     */
    public MarkDesc so360Check(String phone) {
        Request request = new Request.Builder().url(SO360_SEARCH_URL + phone).build();
        try {
            Call call = client.newCall(request);
            Response ret = call.execute();
            if (!ret.isSuccessful()) {
                ret = call.clone().execute();
            }
            if (ret.isSuccessful()) {
                String body = ret.body().string();
                return parseSo360(body);
            }
        } catch (IOException e) {
            // TODO: warning log
        }
        return SO360_NO_MARK;
    }

    /**
     * 解析360返回的页面
     *
     * @param body
     * @return
     */
    public MarkDesc parseSo360(String body) {
        if (null == body || body.length() == 0) {
            return SO360_NO_MARK;
        }
        Document doc = Jsoup.parse(body);
        Elements fraudphoneLabel = doc.getElementsByClass("mohe-ph-mark");
        if (fraudphoneLabel.isEmpty()) {
            return SO360_NO_MARK;
        }
        MarkDesc mark = new MarkDesc("so360");
        mark.setMark(trim(fraudphoneLabel.get(0).text()));

        Element fraudphoneWord = fraudphoneLabel.get(0).nextElementSibling();
        if (fraudphoneWord == null) {
            return mark;
        }

        String descText = trim(fraudphoneWord.text());
        if (descText == null) {
            return mark;
        }
        String tagCountStr = substrBetween(descText, "被", "位");
        if (tagCountStr != null) {
            mark.setCount(toInt(tagCountStr));
        }
        mark.setMarkAs(substrBetween(descText, "疑似为", "电话"));
        mark.setOriginDesc(descText);

        Elements markSource = fraudphoneWord.getElementsByTag("a");
        if (!markSource.isEmpty()) {
            mark.setSource(markSource.get(0).text());
        }
        return mark;
    }

    private static String trim(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }
        return s;
    }

    /**
     * 转为整数，失败返回0
     * 
     * @param num
     * @return
     */
    private static int toInt(String num) {
        try {
            return Integer.valueOf(num);
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    /**
     * 截取两个字符串间的字符串
     * 
     * @param text
     * @param begin
     * @param end
     * @return
     */
    private static String substrBetween(String text, String begin, String end) {
        if (text == null || begin == null) {
            return null;
        }
        int beginPos = text.indexOf(begin);
        if (beginPos == -1) {
            return null;
        }

        if (end == null) {
            return text.substring(beginPos + begin.length());
        }

        int endPos = text.indexOf(end);
        if (endPos == -1) {
            return null;
        }
        if (beginPos + begin.length() >= endPos) {
            return null;
        }
        return text.substring(beginPos + begin.length(), endPos);
    }

    static class NormalWebBrowserHeaderInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader("Connection", "keep-alive");
            requestBuilder.addHeader("Pragma", "no-cache");
            requestBuilder.addHeader("Cache-Control", "no-cache");
            requestBuilder.addHeader("Upgrade-Insecure-Requests", "1");
            requestBuilder.addHeader("User-Agent",
                                     "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ;  QIHU 360EE)");
            requestBuilder.addHeader("Accept",
                                     "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            requestBuilder.addHeader("DNT", "1");
            requestBuilder.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            return chain.proceed(requestBuilder.build());
        }
    }

    static class ProxyAuthenticator implements Authenticator {

        private String username;
        private String password;

        ProxyAuthenticator(String username, String password){
            this.username = username;
            this.password = password;
        }

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder().header("Proxy-Authorization", credential).build();
        }
    }

}
