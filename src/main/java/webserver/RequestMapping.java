package webserver;

import controller.Controller;
import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginController;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, Controller> controller = new HashMap()
    {{
        put("/user/create", new CreateUserController());
        put("/user/login", new LoginController());
        put("/user/list", new ListUserController());
    }};

    public static Controller getController(String requestPath) {
        return controller.get(requestPath);
    }

}
