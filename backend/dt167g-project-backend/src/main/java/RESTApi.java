import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import config.DatabaseConfig;
import controller.ForumController;
import controller.UserController;
import handler.*;
import repository.ForumRepository;
import repository.UserRepository;
import service.ForumService;
import service.UserService;
import util.HttpUtils;
import util.LoggerUtility;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RESTApi {
    private static final Logger logger = LoggerUtility.getLogger();

    public static void main(String[] args) {
        try {
            // Initialize the database
            DatabaseConfig.initializeDatabase();
            logger.info("Database initialized.");

            System.out.println("ip address is: " + getLocalIpAddress());

            UserRepository userRepository = new UserRepository();
            UserService userService = new UserService(userRepository);
            UserController userController = new UserController(userService);

            ForumRepository forumRepository = new ForumRepository();
            ForumService forumService = new ForumService(forumRepository);
            ForumController forumController = new ForumController(forumService);

            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            logger.info("HTTP Server created at port 8000");

            server.createContext("/test", new TestHandler());


            server.createContext("/register", exchange -> {
                logger.info("Received request at /register");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new RegisterHandler(userController).handle(exchange);
                }
            });
            server.createContext("/login", exchange -> {
                logger.info("Received request at /login");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new LoginHandler(userController).handle(exchange);
                }
            });
            server.createContext("/logout", exchange -> {
                logger.info("Received request at /logout");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new LogoutHandler().handle(exchange);
                }
            });
            server.createContext("/session", exchange -> {
                logger.info("Received request at /session");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new SessionCheckHandler().handle(exchange);
                }
            });
            server.createContext("/createTopic", exchange -> {
                logger.info("Received request at /createTopic");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new CreateRoomHandler(forumController).handle(exchange);
                }
            });
            server.createContext("/getRooms", exchange -> {
                logger.info("Received request at /getRooms");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new GetRoomsHandler(forumController).handle(exchange);
                }
            });
            server.createContext("/deleteAnswer", exchange -> {
                String origin = "http://localhost:4200";
                List<String> allowedOrigins = Arrays.asList(origin);

                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    handleOptionsRequest(exchange, allowedOrigins);
                } else {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                    exchange.getResponseHeaders().add("Access-Control-Max-Age", "86400"); // 24 hours
                    exchange.sendResponseHeaders(200, -1);

                    new DeleteAnswerHandler(forumController).handle(exchange);
                }
            });

            server.createContext("/postAnswer", exchange -> {
                logger.info("Received request at /postAnswer");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new PostAnswerHandler(forumController).handle(exchange);
                }
            });

            server.createContext("/insertVote", exchange -> {
                logger.info("Received request at /insertVote");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    new InsertVoteHandler(forumController).handle(exchange);
                }
            });
            server.createContext("/deleteVote", exchange -> {
                logger.info("Received request at /deleteVote");
                String origin = "http://localhost:4200";
                List<String> allowedOrigins = Arrays.asList(origin);

                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    handleOptionsRequest(exchange, allowedOrigins);
                } else {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                    exchange.getResponseHeaders().add("Access-Control-Max-Age", "86400"); // 24 hours
                    exchange.sendResponseHeaders(200, -1);

                    new DeleteVoteHandler(forumController).handle(exchange);
                }

            });

            server.createContext("/room", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    forumController.getRoomById(exchange);
                } else if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    HttpUtils.sendResponse(exchange, 405, "{\"message\":\"Method Not Allowed\"}");
                }
            });
            server.createContext("/answersByRoomId", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    forumController.getAnswersByRoomId(exchange);
                } else if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    HttpUtils.sendResponse(exchange, 405, "{\"message\":\"Method Not Allowed\"}");
                }
            });

            server.createContext("/votesByAnswerId", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    forumController.getVotesByAnswerId(exchange);
                } else if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    HttpUtils.sendResponse(exchange, 405, "{\"message\":\"Method Not Allowed\"}");
                }
            });

            server.createContext("/search", exchange -> {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    forumController.searchRooms(exchange);
                } else if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    HttpUtils.handleOptionsRequest(exchange);
                } else {
                    HttpUtils.sendResponse(exchange, 405, "{\"message\":\"Method Not Allowed\"}");
                }
            });

            server.setExecutor(null);
            server.start();
            logger.info("Server started on port 8000");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start the server", e);
        }

    }
    private static void handleOptionsRequest(HttpExchange exchange, List<String> allowedOrigins) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String origin = headers.getFirst("Origin");
        if (allowedOrigins.contains(origin)) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().add("Access-Control-Max-Age", "86400"); // 24 hours
            exchange.sendResponseHeaders(200, -1);
        } else {
            exchange.sendResponseHeaders(403, -1);
        }
    }

    private static String getLocalIpAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Error getting local IP address", e);
            return "localhost";
        }
    }
}
