package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public abstract class AbstractController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getMethod().isPost()) {
            doPost(request, response);
            return;
        }

        doGet(request, response);
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {}
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException{}
}
