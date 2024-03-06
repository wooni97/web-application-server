package webserver;

import http.HttpMethod;
import http.HttpRequest;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class HttpRequestTest {

    private final String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = Files.newInputStream(
                new File(testDirectory + "Http_GET.txt").toPath());
        HttpRequest request = new HttpRequest(in);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getParameter("userId"));

    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = Files.newInputStream(
                new File(testDirectory + "Http_POST.txt").toPath());
        HttpRequest request = new HttpRequest(in);

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getParameter("userId"));
    }

    @Test
    public void request_GET_html() throws IOException {
        InputStream in = Files.newInputStream(
                new File(testDirectory + "Http_GET_html.txt").toPath());
        HttpRequest request = new HttpRequest(in);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertNull(request.getParameter("userId"));
    }

    @Test
    public void 로그인된_쿠키에서_로그인_상태를_확인하면_true_반환() throws IOException {
        InputStream in = Files.newInputStream(
                new File(testDirectory + "Http_logined_true.txt").toPath());
        HttpRequest request = new HttpRequest(in);

        assertTrue(request.isLogin());
    }

    @Test
    public void 로그인되지_않은_쿠키에서_로그인_상태를_확인하면_false_반환() throws IOException {
        InputStream in = Files.newInputStream(
                new File(testDirectory + "Http_logined_false.txt").toPath());
        HttpRequest request = new HttpRequest(in);

        assertFalse(request.isLogin());
    }

    @Test
    public void 로그인_상태가_존재하지_않으면_false_반환() throws IOException {
        InputStream in = Files.newInputStream(
                new File(testDirectory + "Http_GET_html.txt").toPath());
        HttpRequest request = new HttpRequest(in);

        assertFalse(request.isLogin());
    }
}
