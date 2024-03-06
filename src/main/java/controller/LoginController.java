package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class LoginController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) throws IOException {
        User loginUser = DataBase.findUserById(request.getParameter("userId"));

        if (loginUser != null && loginUser.getPassword().equals(request.getParameter("password"))) {
            log.debug("login success");

            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/index.html");
            return;
        }

        log.debug("login failed");
        response.forward("/user/login_failed.html");
    }
}
