package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ForumController;

import java.io.IOException;

public class CreateRoomHandler implements HttpHandler {
    private final ForumController forumController;

    public CreateRoomHandler(ForumController forumController) {
        this.forumController = forumController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        forumController.createRoom(exchange);
    }
}





