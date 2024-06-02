package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.UserController;
import model.User; // Import the User model
import util.HttpUtils;
import util.LoggerUtility;
import util.SessionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LoginHandler implements HttpHandler {

    private final UserController userController;
    private static final Logger logger = LoggerUtility.getLogger();

    public LoginHandler(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Map<String, String> params = HttpUtils.getPostParameters(exchange);
            String username = params.get("username");
            String password = params.get("password");

            if (username != null && password != null) {
                User user = userController.authenticateUser(username, password); // Authenticate user

                if (user != null) { // If authentication successful
                    SessionHandler.createSession(exchange, username); // Pass user ID to createSession
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("isLoggedIn", true);
                    responseMap.put("username", username);
                    responseMap.put("userId", user.getId());
                    responseMap.put("sessionTimeout", 60000 * 30); // 30 minute
                    responseMap.put("message", "Login successful");
                    String jsonResponse = new Gson().toJson(responseMap);
                    HttpUtils.sendResponse(exchange, 200, jsonResponse);
                    logger.info("Login attempt for user: " + username + " - Successful");
                } else {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("isLoggedIn", false);
                    responseMap.put("username", username);
                    responseMap.put("message", "Invalid credentials");
                    String jsonResponse = new Gson().toJson(responseMap);
                    HttpUtils.sendResponse(exchange, 401, jsonResponse);
                    logger.info("Login attempt for user: " + username + " - Failed (Invalid credentials)");
                }
            } else {
                HttpUtils.sendResponse(exchange, 400, "Bad request");
                logger.warning("Login attempt with missing username or password.");
            }
        } else {
            HttpUtils.sendResponse(exchange, 405, "Method not allowed");
            logger.warning("Invalid request method for login: " + exchange.getRequestMethod());
        }
    }
}
