package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import util.StringUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String userCreatePath = "/user/create";
    private static final String userLoginPath = "/user/login";
    private static final String userListPath = "/user/list";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String requestLine = bufferedReader.readLine();

            log.debug("request : {}", requestLine);

            log.debug("request : {}", inputLine);

            String[] tokens = StringUtils.parseValues(requestLine);
            String httpMethod = tokens[0];
            String url = tokens[1];


            User user;
            if(url.startsWith(userCreatePath) && httpMethod.equals("GET")) {
                user = createUserWithGetMethod(url);
                log.debug("user : {}", user);
                DataBase.addUser(user);

                String redirectUrl = "/index.html";
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + redirectUrl).toPath());
                response302Header(dos, redirectUrl);
                responseBody(dos, body);
                return;
            }

            int contentLength = 0;
            boolean isLogined = false;

            while(!requestLine.isEmpty()){
                requestLine = bufferedReader.readLine();
                log.debug("header : {}", requestLine);

                if (requestLine.contains("Content-Length")) {
                    String[] lineTokens = requestLine.split(" ");
                    contentLength = Integer.parseInt(lineTokens[1]);
                }

                if (requestLine.contains("Cookie")) {
                    String[] lineTokens = requestLine.split(" ");
                    Map<String, String> logined = HttpRequestUtils.parseCookies(lineTokens[1]);
                    log.debug("logined : {}" , logined);
                    isLogined = Boolean.parseBoolean(logined.get("logined"));
                }
            }

            if(url.startsWith(userCreatePath) && httpMethod.equals("POST")) {
                user = createUserWithPostMethod(bufferedReader, contentLength);
                log.debug("user : {}", user);

                DataBase.addUser(user);

                String redirectUrl = "/index.html";
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + redirectUrl).toPath());
                response302Header(dos, redirectUrl);
                responseBody(dos, body);
                return;
            }

            if(url.equals(userLoginPath)) {
                String requestData = IOUtils.readData(bufferedReader, contentLength);
                Map<String, String> loginData = HttpRequestUtils.parseQueryString(requestData);

                User loginUser = DataBase.findUserById(loginData.get("userId"));

                DataOutputStream dos = new DataOutputStream(out);

                if (loginUser != null && loginUser.getPassword().equals(loginData.get("password"))) {

                    log.debug("login success");
                    String redirectUrl = "/index.html";
                    byte[] body = Files.readAllBytes(new File("./webapp" + redirectUrl).toPath());
                    responseLoginSuccessHeader(dos, "true");
                    responseBody(dos, body);
                    return;
                }

                log.debug("login failed");
                String redirectUrl = "/user/login_failed.html";
                byte[] body = Files.readAllBytes(new File("./webapp" + redirectUrl).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }

            if (url.equals(userListPath)) {
                if (!isLogined) {return;}

                Collection<User> users = DataBase.findAll();

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<ul>");
                users.forEach(u -> {
                    stringBuilder.append("<li>");
                    stringBuilder.append(u.getUserId());
                    stringBuilder.append("</li>");
                });

                stringBuilder.append("</ul>");
                String responseBody = stringBuilder.toString();
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = responseBody.getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

            if (url.endsWith("css")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                responseCSS(dos, body.length);
                responseBody(dos, body);
                return;
            }

//            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = "Hello World".getBytes();
//            response200Header(dos, body.length);
//            responseBody(dos, body);

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLoginSuccessHeader(DataOutputStream dos, String isLoginSuccess) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Typ: text/html \r\n");
            dos.writeBytes("Set-Cookie: logined=" + isLoginSuccess);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private User createUserWithGetMethod(String url) {
        int index = url.indexOf("?");
        String requestPath = url.substring(0, index);
        String params = url.substring(index + 1);

        Map<String, String> userData;
        userData = HttpRequestUtils.parseQueryString(params);

        String userId = userData.get("userId");
        String password = userData.get("password");
        String name = userData.get("name");
        String email = userData.get("email");

        return new User(userId, password, name, email);
    }

    private User createUserWithPostMethod(BufferedReader bufferedReader,int contentLength) throws IOException {
        String requestData = IOUtils.readData(bufferedReader, contentLength);
        Map<String, String> userData = HttpRequestUtils.parseQueryString(requestData);

        String userId = userData.get("userId");
        String password = userData.get("password");
        String name = userData.get("name");
        String email = userData.get("email");

        return new User(userId, password, name, email);
    }

}
