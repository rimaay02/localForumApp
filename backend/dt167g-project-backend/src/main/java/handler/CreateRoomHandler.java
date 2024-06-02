package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ForumController;
import util.HttpUtils;
import util.SessionHandler;

import java.io.IOException;

public class CreateRoomHandler implements HttpHandler {
    private final ForumController forumController;

    public CreateRoomHandler(ForumController forumController) {
        this.forumController = forumController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            // Verify CSRF token
            if (SessionHandler.verifyCsrfToken(exchange)) {
                // CSRF token is valid, proceed with creating the room
                forumController.createRoom(exchange);

            } else {
                // CSRF token is invalid, send 403 Forbidden response
                HttpUtils.sendResponse(exchange, 403, "Invalid CSRF token");
            }
        } else {
            // Handle other HTTP methods
            HttpUtils.handleOptionsRequest(exchange);
        }
    }
}





