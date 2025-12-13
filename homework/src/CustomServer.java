import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

 
// server 
public class CustomServer { 
 
    private static HashSet<String> logins = new HashSet<>(); 
    private static Map<String, Integer> map = new HashMap<>();
    private static List<List<Integer>> matrix = new ArrayList<>() {{
    add(new ArrayList<>(Arrays.asList(0, -1, 1)));
    add(new ArrayList<>(Arrays.asList(1, 0, -1)));
    add(new ArrayList<>(Arrays.asList(-1, 1, 0)));
    }};
 
    public static void main(String[] args) throws IOException{  
        logins.add("ha");
        map.put("rock", 0);
        map.put("0", 0);
        map.put("paper", 1);
        map.put("1", 1);
        map.put("scissors", 2);
        map.put("2", 2);
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0); 
 
        server.createContext("/login", new LoginHandler()); 
        server.createContext("/image", new ImageHandler()); 
        server.createContext("/game", new GameHandler()); 
        server.createContext("/delete", new DeleteHandler()); 
         
        server.start(); 
        System.out.println("Started at http://localhost:8000/"); 
 
    } 
 
    static class LoginHandler implements HttpHandler { 
        @Override 
        public void handle(HttpExchange exchange) throws IOException { 
            if (!exchange.getRequestMethod().equals("GET")) {
                sendForbidden(exchange);
                return;
            }
            String response = "Your login is " + generateLogin();
            sendResponse(exchange, response);
        } 
    } 
 
    static class ImageHandler implements HttpHandler { 
        @Override 
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET") || !goodLogin(exchange)) {
                sendForbidden(exchange);
                return;
            }
            File file = new File("/Users/artemginsburg/Desktop/Books/java/homework/death_in_poor.jpg");
            if (!file.exists() || !file.isFile()) {
                System.out.println("NO file");
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "image/jpg");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } 
    } 

    static class GameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST") || !goodLogin(exchange)) {
                sendForbidden(exchange);
                return;
            }
            String requestBody;
            InputStream is = exchange.getRequestBody();
            requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim().toLowerCase();
            is.close();

            Integer choice = map.get(requestBody);
            if (choice == null) {
                sendResponse(exchange, "i cant recognize, sowwy");
                return;
            }
            int mine = (new Random()).nextInt(3);
            String response = "It is a " + switch (matrix.get(choice).get(mine)) {
                case 0 -> "draw";
                case 1 -> "YOUR VICTORY";
                case -1 -> "your defeat hahahaha";
                default -> "";
            };
            response += " because i choice " + switch(mine) {
                case 0 -> "rock";
                case 1 -> "paper";
                case 2 -> "scissors";
                default -> "";
            };
            sendResponse(exchange, response);
        }
    }
    
    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("DELETE") || !goodLogin(exchange)) {
                sendForbidden(exchange);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            
            if (query == null || query.isEmpty()) {
                sendResponse(exchange, "empty query(");
                return;
            }
            
            String path = null;
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && (keyValue[0].equals("path"))) {
                    path = keyValue[1];
                }
            }
            
            if (path == null) {
                sendResponse(exchange, "didnt found \"path\" in query");
                return;
            }
            try {
                Files.deleteIfExists(Paths.get("").toAbsolutePath().resolve(path));
            } catch (Exception e) {
                sendResponse(exchange,"error while deleting file");
            }

            sendResponse(exchange, "You deleted");

        }
    }
    private static void sendForbidden(HttpExchange exchange) throws IOException {
        String response = "This part is forbidden";
        exchange.sendResponseHeaders(403, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write("This part is forbidden".getBytes());
        os.close();
    }
    private static void sendResponse(HttpExchange exchange, String response) throws IOException { 
        exchange.sendResponseHeaders(200, response.length()); 
        OutputStream os = exchange.getResponseBody(); 
        os.write(response.getBytes()); 
        os.close(); 
    } 
    
    private static String generateLogin() {
        String login = UUID.randomUUID().toString();
        int was = logins.size();
        logins.add(login);
        if (logins.size() == was) {
            return generateLogin();
        }
        return login;
    }
    
    private static boolean goodLogin(HttpExchange exchange) {
        return logins.contains(exchange.getRequestHeaders().getFirst("login"));
    }
}
