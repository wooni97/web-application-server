package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
    private HttpMethod httpMethod;
    private final String path;
    private Map<String, String> parameters = new HashMap<>();


    public RequestLine(String requestLine) {
        log.debug("request line: {}", requestLine);

        String[] tokens = requestLine.split(" ");
        this.httpMethod = HttpMethod.valueOf(tokens[0]);

        if (this.httpMethod.isPost()) {
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

    public HttpMethod getMethod() {
        return this.httpMethod;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
