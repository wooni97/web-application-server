package http;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private RequestLine requestLine;
    private final Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line = bufferedReader.readLine();

        // 무한 루프 방지
        if (line == null) { return; }

        requestLine = new RequestLine(line);

        line = bufferedReader.readLine();
        while(!line.isEmpty()){
            splitHeader(line);

            line = bufferedReader.readLine();
        }

        if (requestLine.getMethod().isPost()) {
            String requestBody = IOUtils.readData(bufferedReader,
                    Integer.parseInt(headers.get("Content-Length")));

            this.parameters = HttpRequestUtils.parseQueryString(requestBody);
        } else if (requestLine.getMethod().isGet()) {
           this.parameters = requestLine.getParameters();
        }
    }

    private void splitHeader(String requestHeader) {
        log.debug("request header : {}", requestHeader);

        String[] parts = requestHeader.split(":");
        String headerName = parts[0].trim();
        String headerValue = parts[1].trim();


        headers.put(headerName, headerValue);
    }

    public boolean isLogin() {
        String cookieValue = headers.get("Cookie");

        if (cookieValue == null) {
            return false;
        }

        return Boolean.parseBoolean(HttpRequestUtils
                .parseCookies(cookieValue)
                .get("logined"));
    }
    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String headerName) {
        return this.headers.get(headerName);
    }

    public String getParameter(String parameterName) {
        return this.parameters.get(parameterName);
    }

}
