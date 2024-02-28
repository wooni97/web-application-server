package webserver;

import db.DataBase;
import model.User;

import java.io.IOException;
import java.util.Collection;

public class ListUserController extends AbstractController{

    @Override
    void doPost(HttpRequest request, HttpResponse response) throws IOException {}

    @Override
    void doGet(HttpRequest request, HttpResponse response) throws IOException {
        if (!isLogin(request.getParameter("Cookie"))) {
            response.sendRedirect("/user/login.html");
            return;
        }

        Collection<User> users = DataBase.findAll();
        String responseBody = generateHtmlUserList(users);
        response.forwardBody(responseBody);
    }

    private boolean isLogin(String cookieValue) {
        return Boolean.parseBoolean(cookieValue);
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
