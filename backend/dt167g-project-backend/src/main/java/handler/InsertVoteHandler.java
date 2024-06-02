package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ForumController;

import java.io.IOException;

public class InsertVoteHandler implements HttpHandler {
    private final ForumController forumController;

    public InsertVoteHandler(ForumController forumController) {
        this.forumController = forumController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        forumController.insertVote(exchange);
    }
}
