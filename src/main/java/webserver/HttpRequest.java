package webserver;

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
    private final InputStream in;
    private String method;
    private String path;
    private final Map<String, String> header = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {
        this.in = in;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String requestLine = bufferedReader.readLine();

        log.debug("request line: {}", requestLine);

        // 무한 루프 방지
        if (requestLine == null) { return; }

        splitRequestLine(requestLine);

        String requestHeader = bufferedReader.readLine();
        while(!requestHeader.isEmpty()){
            log.debug("request header : {}", requestHeader);

            splitHeader(requestHeader);

            requestHeader = bufferedReader.readLine();
        }

        if (this.method.equals("POST")) {
            String requestBody = IOUtils.readData(bufferedReader,
                    Integer.parseInt(header.get("Content-Length")));

            parameters = HttpRequestUtils.parseQueryString(requestBody);
        }
    }

    private void splitRequestLine(String requestLine) throws IOException {
        String[] tokens = requestLine.split(" ");
        this.method = tokens[0];

        if (this.method.equals("POST")) {
            this.path = tokens[1];
            return;
        }

        String[] parts = tokens[1].split("\\?");

        if (parts.length == 1) {
            this.path = parts[0];
            return;
        }
        this.path = parts[0];
        this.parameters = HttpRequestUtils.parseQueryString(parts[1]);
    }

    private void splitHeader(String requestHeader) {
        String[] parts = requestHeader.split(":");
        String headerName = parts[0].trim();
        String headerValue = parts[1].trim();


        header.put(headerName, headerValue);
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getHeader(String headerName) {
        return this.header.get(headerName);
    }

    public String getParameter(String parameterName) {
        return this.parameters.get(parameterName);
    }

}
