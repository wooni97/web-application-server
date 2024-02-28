package webserver;

import java.io.IOException;

public abstract class AbstractController implements Controller{
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getMethod().equals("POST")) {
            doPost(request, response);
            return;
        }

        if (request.getMethod().equals("GET")) {
            doGet(request, response);
        }
    }

    abstract void doPost(HttpRequest request, HttpResponse response) throws IOException;

    abstract void doGet(HttpRequest request, HttpResponse response) throws IOException;
}
