package handler;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.HttpUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            // For preflight requests, handle the OPTIONS method
            handleOptionsRequest(exchange);
            return;
        }

        // Set CORS headers
        String origin = "http://localhost:4200";
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Create a response object with the IP address
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("ip", getLocalIpAddress());
        String jsonResponse = new Gson().toJson(responseMap);

        // Set the status code and response body
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(jsonResponse.getBytes());
        os.close();
    }

    private static String getLocalIpAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
    private static void handleOptionsRequest(HttpExchange exchange) throws IOException {
        String origin = "http://localhost:4200";
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "86400"); // 24 hours
        exchange.sendResponseHeaders(200, -1);
    }
}
