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
            String request = bufferedReader.readLine();

            log.debug("request line: {}", request);

            // 무한 루프 방지
            if (request == null) { return; }

            String[] tokens = request.split(" ");
            String url = tokens[1];

            int contentLength = 0;
            boolean isLogined = false;

            while(!request.isEmpty()){
                request = bufferedReader.readLine();
                log.debug("request header : {}", request);

                if (request.contains("Content-Length")) {
                    contentLength = getContentLength(request);
                }

                if (request.contains("Cookie")) {
                    isLogined = getLoginStatus(request);
                }
            }

            if(url.startsWith(userCreatePath)) {
                User user = createUserWithPostMethod(bufferedReader, contentLength);
                log.debug("created user info : {}", user);

                DataBase.addUser(user);

                String redirectUrl = "/index.html";
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, redirectUrl);
                return;
            }

            if(url.equals(userLoginPath)) {
                String requestData = IOUtils.readData(bufferedReader, contentLength);
                Map<String, String> loginData = HttpRequestUtils.parseQueryString(requestData);

                User loginUser = DataBase.findUserById(loginData.get("userId"));

                if (loginUser != null && loginUser.getPassword().equals(loginData.get("password"))) {
                    log.debug("login success");

                    DataOutputStream dos = new DataOutputStream(out);
                    String redirectUrl = "/index.html";
                    response302LoginSuccessHeader(dos, redirectUrl);
                    return;
                }

                log.debug("login failed");
                String redirectUrl = "/user/login_failed.html";
                responseAsResource(out, redirectUrl);
            }

            if (url.equals(userListPath)) {
                if (!isLogined) {return;}

                Collection<User> users = DataBase.findAll();

                String responseBody = generateHtmlUserList(users);

                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = responseBody.getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

            if (url.endsWith("css")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                responseCSSHeader(dos, body.length);
                responseBody(dos, body);
            }

            responseAsResource(out, url);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private boolean getLoginStatus(String request) {
        String[] tokens = request.split(" ");
        return Boolean.parseBoolean(
                HttpRequestUtils
                        .parseCookies(tokens[1])
                        .get("logined"));
    }

    private int getContentLength(String line) {
        String[] tokens = line.split(" ");
        return Integer.parseInt(tokens[1]);
    }

    private void responseAsResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response302LoginSuccessHeader(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
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
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseCSSHeader(DataOutputStream dos, int lengthOfBodyContent) {
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

    private User createUserWithPostMethod(BufferedReader bufferedReader, int contentLength) throws IOException {
        String requestBody = IOUtils.readData(bufferedReader, contentLength);
        Map<String, String> userCreateData = HttpRequestUtils.parseQueryString(requestBody);

        String userId = userCreateData.get("userId");
        String password = userCreateData.get("password");
        String name = userCreateData.get("name");
        String email = userCreateData.get("email");

        return new User(userId, password, name, email);
    }

}
