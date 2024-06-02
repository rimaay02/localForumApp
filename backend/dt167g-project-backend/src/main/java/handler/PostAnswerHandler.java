package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ForumController;

import java.io.IOException;

public class PostAnswerHandler implements HttpHandler {
    private final ForumController forumController;

    public PostAnswerHandler(ForumController forumController) {
        this.forumController = forumController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            forumController.postAnswer(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}
