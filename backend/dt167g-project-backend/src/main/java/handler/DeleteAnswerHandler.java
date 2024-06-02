package handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ForumController;

import java.io.IOException;

public class DeleteAnswerHandler implements HttpHandler {
    private final ForumController forumController;

    public DeleteAnswerHandler(ForumController forumController) {
        this.forumController = forumController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            forumController.deleteAnswer(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}

