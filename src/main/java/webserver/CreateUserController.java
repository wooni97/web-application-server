package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CreateUserController extends AbstractController{

    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    void doPost(HttpRequest request, HttpResponse response) throws IOException {
        User user = new User(request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));
        log.debug("created user info : {}", user);

        DataBase.addUser(user);

        response.sendRedirect("/index.html");
    }

    @Override
    void doGet(HttpRequest request, HttpResponse response) {

    }

}
