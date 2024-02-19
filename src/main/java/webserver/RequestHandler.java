package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import util.StringUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String userCreatePath = "/user/create";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String inputLine = bufferedReader.readLine();
            log.debug("request : {}", inputLine);
            if (inputLine == null) { return;}

            String[] tokens = StringUtils.parseValues(inputLine);
            String httpMethod = tokens[0];
            String url = tokens[1];


            User user;
            if(url.startsWith(userCreatePath) && httpMethod.equals("GET")) {
                user = createUserWithGetMethod(url);
                log.debug("user : {}", user);

                String redirectUrl = "/index.html";
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + redirectUrl).toPath());
                response302Header(dos, redirectUrl);
                responseBody(dos, body);
                return;
            }

            int contentLength = 0;

            while(!inputLine.isEmpty()){
                inputLine = bufferedReader.readLine();
                log.debug("header : {}", inputLine);

                if(inputLine.contains("Content-Length")) {
                    String[] lineTokens = inputLine.split(" ");
                    contentLength = Integer.parseInt(lineTokens[1]);
                }
            }

            if(url.startsWith(userCreatePath) && httpMethod.equals("POST")) {
                user = createUserWithPostMethod(bufferedReader, contentLength);

                String redirectUrl = "/index.html";
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + redirectUrl).toPath());
                response302Header(dos, redirectUrl);
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
