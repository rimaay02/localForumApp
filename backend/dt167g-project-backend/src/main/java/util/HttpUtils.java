package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUtils {

    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerUtility.getLogger();

    public static Map<String, String> getPostParameters(HttpExchange exchange) throws IOException {
        logger.info("Extracting POST parameters");
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            jsonBuilder.append(line);
        }
        logger.info("POST parameters extracted successfully");
        return gson.fromJson(jsonBuilder.toString(), new TypeToken<Map<String, String>>() {}.getType());
    }

    public static <T> T getPostParameters(HttpExchange exchange, Class<T> clazz) throws IOException {
        logger.info("Extracting POST parameters into " + clazz.getSimpleName());
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            jsonBuilder.append(line);
        }
        logger.info("POST parameters extracted successfully into " + clazz.getSimpleName());
        return gson.fromJson(jsonBuilder.toString(), clazz);
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        try {
            logger.info("Sending response with status code: " + statusCode);
            addCORSHeaders(exchange);
            addSecurityHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            responseBody.close();
            logger.info("Response sent successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending response", e);
            throw e;
        }
    }

    public static void handleOptionsRequest(HttpExchange exchange) throws IOException {
        try {
            logger.info("Handling OPTIONS request");
            addCORSHeaders(exchange);
            addSecurityHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            logger.info("OPTIONS request handled successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling OPTIONS request", e);
            throw e;
        }
    }

    private static void addCORSHeaders(HttpExchange exchange) {
        logger.info("Adding CORS headers");
        String origin = "http://localhost:4200";;
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-CSRF-Token");
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().add("Access-Control-Expose-Headers", "Content-Type, X-CSRF-Token");
        logger.info("CORS headers added successfully");
    }

    private static void addSecurityHeaders(HttpExchange exchange) {
        logger.info("Adding security headers: Content-Security-Policy & X-XSS-Protection");
        exchange.getResponseHeaders().add("Content-Security-Policy", "default-src 'self'; script-src 'self';");
        exchange.getResponseHeaders().add("X-XSS-Protection", "1; mode=block");
        logger.info("Security headers added successfully");
    }

    private static String getLocalIpAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Error getting local IP address", e);
            return "localhost";
        }
    }
}
