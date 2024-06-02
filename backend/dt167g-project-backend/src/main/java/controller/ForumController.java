package controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import model.Room;
import model.Answer;
import model.Vote;
import service.ForumService;
import util.HttpUtils;
import util.LoggerUtility;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ForumController {
    private final ForumService forumService;
    private static final Logger logger = LoggerUtility.getLogger();

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    public void createRoom(HttpExchange exchange) throws IOException {
        try {
            Room room = HttpUtils.getPostParameters(exchange, Room.class);
            System.out.println("inside controller " + room.getCreatorId());
            forumService.createRoom(room);
            HttpUtils.sendResponse(exchange, 200, "{\"message\":\"Room created successfully\"}");
        } catch (Exception e) {
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Failed to create room\"}");
        }
    }

    public void getRooms(HttpExchange exchange) throws IOException {
        logger.info("Handling getRooms request");
        try {
            List<Room> rooms = forumService.getRooms();
            String jsonResponse = new Gson().toJson(rooms);
            HttpUtils.sendResponse(exchange, 200, jsonResponse);
            logger.info("Rooms retrieved successfully");
        } catch (Exception e) {
            logger.severe("Failed to retrieve rooms: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Failed to retrieve rooms\"}");
        }
    }

    public void postAnswer(HttpExchange exchange) throws IOException {
        logger.info("Handling postAnswer request");
        try {
            Answer answer = HttpUtils.getPostParameters(exchange, Answer.class);
            Answer createdAnswer = forumService.postAnswer(answer);
            System.out.println(answer);
            HttpUtils.sendResponse(exchange, 200, new Gson().toJson(createdAnswer));
            logger.info("Answer posted successfully");
        } catch (Exception e) {
            logger.severe("Failed to post answer: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Failed to post answer\"}");
        }
    }

    public void deleteAnswer(HttpExchange exchange) throws IOException {
        logger.info("Handling deleteAnswer request");
        try {
            String requestPath = exchange.getRequestURI().getPath();
            String[] pathComponents = requestPath.split("/");
            int answerId = Integer.parseInt(pathComponents[pathComponents.length - 1]);
            int deletedAnswer = forumService.deleteAnswer(answerId);
            HttpUtils.sendResponse(exchange, 200, new Gson().toJson(deletedAnswer));
            logger.info("Answer deleted successfully");

        } catch (NumberFormatException e) {
            logger.warning("Invalid answer ID format: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Invalid answer ID format\"}");
        } catch (Exception e) {
            logger.severe("Failed to delete answer: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Failed to delete answer\"}");
        }
    }


    public void insertVote(HttpExchange exchange) throws IOException {
        logger.info("Handling updateVote request");
        try {
            Map<String, String> params = HttpUtils.getPostParameters(exchange);
            int userId = Integer.parseInt(params.get("userId"));
            int answerId = Integer.parseInt(params.get("answerId"));
            forumService.insertVote(userId, answerId);
            HttpUtils.sendResponse(exchange, 200, "{\"message\":\"Vote updated successfully\"}");
            logger.info("Vote updated successfully for answerId: " + answerId);
        } catch (Exception e) {
            logger.severe("Failed to update vote: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Failed to update vote\"}");
        }
    }
    public void deleteVote(HttpExchange exchange) throws IOException {
        logger.info("Handling deleteVote request");
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            int userId = Integer.parseInt(segments[2]);
            int answerId = Integer.parseInt(segments[3]);

            forumService.deleteVote(userId, answerId);
            String jsonResponse = "{\"message\":\"Vote deleted successfully\"}";
            HttpUtils.sendResponse(exchange, 200, jsonResponse);
            logger.info("Vote deleted successfully for answerId: " + answerId);
        } catch (Exception e) {
            logger.severe("Failed to delete vote: " + e.getMessage());
            String errorResponse = "{\"message\":\"Failed to delete vote\"}";
            HttpUtils.sendResponse(exchange, 500, errorResponse);
        }
    }


    public void getRoomById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length < 3) {
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Missing room ID\"}");
            return;
        }

        try {
            int roomId = Integer.parseInt(pathParts[2]);
            Room room = forumService.getRoomById(roomId);

            if (room != null) {
                String jsonResponse = new Gson().toJson(room);
                HttpUtils.sendResponse(exchange, 200, jsonResponse);
            } else {
                HttpUtils.sendResponse(exchange, 404, "{\"message\":\"Room not found\"}");
            }
        } catch (NumberFormatException e) {
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Invalid room ID format\"}");
        } catch (Exception e) {
            logger.severe("Failed to retrieve room: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Internal Server Error\"}");
        }
    }


    public void getAnswersByRoomId(HttpExchange exchange) throws IOException {
        logger.info("Handling getAnswersByRoomId request");
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length < 2) {
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Missing room ID\"}");
            return;
        }

        try {
            int roomId = Integer.parseInt(pathParts[2]);
            List<Answer> answers = forumService.getAnswersByRoomId(roomId);

            if (!answers.isEmpty()) {
                String jsonResponse = new Gson().toJson(answers);
                HttpUtils.sendResponse(exchange, 200, jsonResponse);
                logger.info("Answers retrieved successfully for room ID: " + roomId);
            } else {
                HttpUtils.sendResponse(exchange, 404, "{\"message\":\"No answers found for the given room ID\"}");
                logger.info("No answers found for room ID: " + roomId);
            }
        } catch (NumberFormatException e) {
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Invalid room ID format\"}");
        } catch (Exception e) {
            logger.severe("Failed to retrieve answers: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Internal Server Error\"}");
        }
    }

    public void getVotesByAnswerId(HttpExchange exchange) throws IOException {
        logger.info("Handling getVotesByAnswerId request");

        // Extract answer ID from the request URI
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length < 2) {
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Missing answer ID\"}");
            return;
        }

        try {
            int answerId = Integer.parseInt(parts[parts.length - 1]);
            List<Vote> votes = forumService.getVotesByAnswerId(answerId);
            System.out.println(votes.size());
            StringBuilder votesJson = new StringBuilder("[");
            for (int i = 0; i < votes.size(); i++) {
                Vote vote = votes.get(i);
                String voteJson = "{\"id\":" + vote.getId() + ", \"userId\":" + vote.getUserId() + ", \"answerId\":" + vote.getAnswerId() + "}";
                votesJson.append(voteJson);
                if (i < votes.size() - 1) {
                    votesJson.append(",");
                }
            }
            votesJson.append("]");

            String jsonResponse = "{\"votes\":" + votesJson.toString() + "}";
            HttpUtils.sendResponse(exchange, 200, jsonResponse);
            logger.info("Votes retrieved successfully for answer ID: " + answerId);
        } catch (NumberFormatException e) {
            HttpUtils.sendResponse(exchange, 400, "{\"message\":\"Bad Request: Invalid answer ID format\"}");
        } catch (Exception e) {
            logger.severe("Failed to retrieve votes: " + e.getMessage());
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Internal Server Error\"}");
        }
    }
    public void searchRooms(HttpExchange exchange) throws IOException {
        try {
           String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            String query = segments[2];
            String type = segments[3];

            List<Room> searchResults = forumService.searchRooms(query, type);
            String jsonResponse = new Gson().toJson(searchResults);
            HttpUtils.sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            HttpUtils.sendResponse(exchange, 500, "{\"message\":\"Failed to search rooms\"}");
        }
    }

}
