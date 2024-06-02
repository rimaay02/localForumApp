package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.UserController;
import util.HttpUtils;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler implements HttpHandler {

    private final UserController userController;

    public RegisterHandler(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            HttpUtils.handleOptionsRequest(exchange);
        } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Map<String, String> params = HttpUtils.getPostParameters(exchange);
            String firstName = params.get("firstName");
            String lastName = params.get("lastName");
            String username = params.get("username");
            String password = params.get("password");

            if (username != null && password != null) {
                boolean registered = userController.registerUser(firstName, lastName, username, password);
                String responseMessage = registered ? "{\"message\":\"Registration success!\"}" : "{\"message\":\"Username already exists\"}";
                HttpUtils.sendResponse(exchange, registered ? 200 : 409, responseMessage);
            } else {
                HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad request\"}");
            }
        } else {
            HttpUtils.sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
        }
    }
}
