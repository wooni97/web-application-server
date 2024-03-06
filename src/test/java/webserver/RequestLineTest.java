package webserver;

import http.RequestLine;
import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class RequestLineTest {

    @Test
    public void create_GET_method() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_POST_method() {
        RequestLine line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals("POST", line.getMethod());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        RequestLine line = new RequestLine("GET /user/create?userId=javajigi&password=password&name=JaeSung HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/user/create", line.getPath());
        Map<String, String> params = line.getParameters();
        assertEquals(3, params.size());
    }

}