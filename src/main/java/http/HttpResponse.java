package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String STATUS_CODE_200 = "200 OK";
    private static final String STATUS_CODE_302 = "302 Found";
    private final DataOutputStream out;
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        this.out = new DataOutputStream(out);
    }

    public void forward(String url) {
        try {
            byte[] body =
                    Files.readAllBytes(new File("./webapp" + url).toPath());

            if (url.endsWith("css")) {
                headers.put("Content-Type", "text/css");
            } else if (url.endsWith("js")) {
                headers.put("Content-Type", "application/javascript");
            } else {
                headers.put("Content-Type", "text/html;charset=utf-8");
            }

            headers.put("Content-Length", String.valueOf(body.length));

            writeStatusLine(STATUS_CODE_200);
            writeResponseHeaders();
            writeResponseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardBody(String responseBody) throws IOException {
        byte[] body = responseBody.getBytes();
        headers.put("Content-Type", "text/html;charset=utf-8");
        headers.put("Content-Length", String.valueOf(body.length));

        writeStatusLine(STATUS_CODE_200);
        writeResponseHeaders();
        writeResponseBody(body);
    }

    public void sendRedirect(String url) throws IOException {
        headers.put("Location", url);

        writeStatusLine(STATUS_CODE_302);
        writeResponseHeaders();
    }

    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }

    private void writeStatusLine(String statusCode) throws IOException {
        out.writeBytes(HTTP_VERSION + " " + statusCode + " \r\n");
    }

    private void writeResponseHeaders() throws IOException {
        headers.forEach((key, value) -> {
            try {
                out.writeBytes(key + ":" + " " + value + " \r\n");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        out.writeBytes("\r\n");
    }

    private void writeResponseBody(byte[] body) {
        try {
            out.write(body, 0, body.length);
            out.writeBytes("\r\n");
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
