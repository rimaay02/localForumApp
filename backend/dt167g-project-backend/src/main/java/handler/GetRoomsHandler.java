package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ForumController;

import java.io.IOException;

public class GetRoomsHandler implements HttpHandler {
    private final ForumController forumController;

    public GetRoomsHandler(ForumController forumController) {
        this.forumController = forumController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        forumController.getRooms(exchange);
    }
}

