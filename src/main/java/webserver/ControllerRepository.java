package webserver;

import java.util.HashMap;
import java.util.Map;

public class ControllerRepository {
    public static final Map<String, Controller> controller = new HashMap() {{
        put("/user/create", new CreateUserController());
        put("/user/login", new LoginController());
        put("/user/list", new ListUserController());
    }};
}
