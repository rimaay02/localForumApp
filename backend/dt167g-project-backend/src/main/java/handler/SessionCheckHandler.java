package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.HttpUtils;
import util.LoggerUtility;
import util.SessionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionCheckHandler implements HttpHandler {

    private static final Logger logger = LoggerUtility.getLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Received request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                boolean isLoggedIn = SessionHandler.isLoggedIn(exchange);
                String username = isLoggedIn ? SessionHandler.getSessionUser(exchange) : null;
                int userId = isLoggedIn ? SessionHandler.getSessionUserId(exchange) : -1; // Fetch user ID

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("isLoggedIn", isLoggedIn);
                responseMap.put("username", username);
                responseMap.put("id", userId);

                String jsonResponse = new Gson().toJson(responseMap);
                HttpUtils.sendResponse(exchange, 200, jsonResponse);
                logger.info("Session check response: " + responseMap);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during session check", e);
                HttpUtils.sendResponse(exchange, 500, "Internal server error");
            }
        } else {
            HttpUtils.sendResponse(exchange, 405, "Method not allowed");
            logger.warning("Method not allowed: " + exchange.getRequestMethod());
        }
    }

}
