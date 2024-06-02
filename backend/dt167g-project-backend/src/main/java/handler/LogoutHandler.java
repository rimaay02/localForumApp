package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.HttpUtils;
import util.LoggerUtility;
import util.SessionHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogoutHandler implements HttpHandler {

    private static final Logger logger = LoggerUtility.getLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Received request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                // Invalidate the session
                SessionHandler.invalidateSession(exchange);
                HttpUtils.sendResponse(exchange, 200, "Log out successful");
                logger.info("User logged out successfully.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during logout", e);
                HttpUtils.sendResponse(exchange, 500, "Internal server error");
            }
        } else {
            HttpUtils.sendResponse(exchange, 405, "Method not allowed");
            logger.warning("Method not allowed: " + exchange.getRequestMethod());
        }
    }
}
