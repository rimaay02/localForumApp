package util;

import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SessionHandler {

    private static final Logger logger = LoggerUtility.getLogger();
    private static final Map<String, Map<String, Object>> sessionStore = new ConcurrentHashMap<>();

    public static void createSession(HttpExchange exchange, String username) {
        String sessionId = UUID.randomUUID().toString();
        String csrfToken = UUID.randomUUID().toString();
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("csrfToken", csrfToken);
        sessionStore.put(sessionId, userData);

        // Set Secure and SameSite attributes
        String cookieValue = "sessionId=" + sessionId + "; HttpOnly; Path=/; Secure; SameSite=None";
        exchange.getResponseHeaders().add("Set-Cookie", cookieValue);

        logger.info("Session created for user: " + username + " with sessionId: " + sessionId);
    }

    public static void invalidateSession(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        if (sessionId != null) {
            sessionStore.remove(sessionId);
            logger.info("Session invalidated for sessionId: " + sessionId);
        } else {
            logger.warning("Attempted to invalidate session, but no sessionId found in request.");
        }
    }

    public static boolean isLoggedIn(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        boolean loggedIn = sessionId != null && sessionStore.containsKey(sessionId);
        logger.info("Session check: " + (loggedIn ? "User is logged in with sessionId: " + sessionId : "User is not logged in."));
        return loggedIn;
    }

    public static String getSessionUser(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        Map<String, Object> userData = sessionId != null ? sessionStore.get(sessionId) : null;
        String username = userData != null ? (String) userData.get("username") : null;
        logger.info("Retrieved session user: " + username + " for sessionId: " + sessionId);
        return username;
    }

    public static int getSessionUserId(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        Map<String, Object> userData = sessionId != null ? sessionStore.get(sessionId) : null;
        Integer userId = userData != null ? (Integer) userData.get("userId") : null;
        logger.info("Retrieved session user ID: " + userId + " for sessionId: " + sessionId);
        return userId != null ? userId : -1;
    }

    private static String getSessionId(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=");
                if (parts.length == 2 && parts[0].equals("sessionId")) {
                    return parts[1];
                }
            }
        }
        logger.warning("No sessionId found in cookies.");
        return null;
    }
}
