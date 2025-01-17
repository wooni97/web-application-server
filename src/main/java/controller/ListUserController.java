package controller;

import db.DataBase;
import http.HttpSession;
import model.User;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.util.Collection;

public class ListUserController extends AbstractController {
    @Override
    public void doGet(HttpRequest request, HttpResponse response) throws IOException {
        if (!isLogin(request)) {
            response.sendRedirect("/user/login.html");
            return;
        }

        Collection<User> users = DataBase.findAll();
        String responseBody = generateHtmlUserList(users);
        response.forwardBody(responseBody);
    }

    private boolean isLogin(HttpRequest request) {
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");

        return user != null;
    }

    private String generateHtmlUserList(Collection<User> users) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table>");
        for(User user : users) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>").append(user.getUserId()).append("</td>");
            stringBuilder.append("</tr>");
        }
        stringBuilder.append("</table>");

        return stringBuilder.toString();
    }
}
