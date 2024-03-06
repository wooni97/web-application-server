package http;

import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpCookie {
    private Map<String, String> cookies = new HashMap<>();

    public HttpCookie(String cookieValue) {
        this.cookies = HttpRequestUtils.parseCookies(cookieValue);
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }
}
