package http;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {
    private final Map<String, Object> session = new HashMap<>();
    private final String id;

    public HttpSession(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setAttribute(String name, Object value) {
        session.put(name, value);
    }

    public Object getAttribute(String name) {
        return session.get(name);
    }

    public void removeAttribute(String name) {
        session.remove(name);
    }

}
