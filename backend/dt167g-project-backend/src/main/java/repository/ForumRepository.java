package repository;

import config.DatabaseConfig;
import model.Answer;
import model.Room;
import model.User;
import model.Vote;
import util.LoggerUtility;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ForumRepository {
    private static final Logger logger = LoggerUtility.getLogger();

    public void createRoom(Room room) {
        System.out.println(room.getCreatorId());
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println(room.getCreatorId());
            String sql = "INSERT INTO rooms (title, message, creator_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, room.getTitle());
            stmt.setString(2, room.getMessage());
            stmt.setInt(3, room.getCreatorId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT r.id, r.title FROM rooms r";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("id"));
                room.setTitle(rs.getString("title"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }
    public Room getRoomById(int roomId) throws SQLException {
        Room room = null;
        String sql = "SELECT r.id, r.title, r.message, r.creator_id, r.created_at, u.username " +
                "FROM rooms r " +
                "JOIN users u ON r.creator_id = u.id " +
                "WHERE r.id = ?;";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (room == null) {
                        room = new Room();
                        room.setId(rs.getInt("id"));
                        room.setTitle(rs.getString("title"));
                        room.setMessage(rs.getString("message"));
                        room.setCreatorId(rs.getInt("creator_id"));
                        room.setCreatorName(rs.getString("username")); // Set creator's name
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtility.getLogger().severe("SQL Error in getRoomById: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggerUtility.getLogger().severe("Unexpected Error in getRoomById: " + e.getMessage());
            throw new SQLException("Unexpected Error in getRoomById", e);
        }
        return room;
    }

    public List<Answer> getAnswersByRoomId(int roomId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT a.id, a.message, a.user_id, a.room_id, u.username " +
                "FROM answers a " +
                "JOIN users u ON a.user_id = u.id " +
                "WHERE a.room_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Answer answer = new Answer();
                    answer.setId(rs.getInt("id"));
                    answer.setMessage(rs.getString("message"));
                    answer.setUserId(rs.getInt("user_id"));
                    answer.setRoomId(rs.getInt("room_id"));
                    answer.setUsername(rs.getString("username")); // Assign username
                    answers.add(answer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answers;
    }


    public int deleteAnswer(int answerId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String deleteAnswerSql = "DELETE FROM answers WHERE id = ?";
            try (PreparedStatement deleteAnswerStmt = conn.prepareStatement(deleteAnswerSql)) {
                deleteAnswerStmt.setInt(1, answerId);
                deleteAnswerStmt.executeUpdate();
            }

            String updateAnswersCountSql = "UPDATE rooms SET answers_count = answers_count - 1 WHERE id = " +
                    "(SELECT room_id FROM answers WHERE id = ?)";
            try (PreparedStatement updateAnswersCountStmt = conn.prepareStatement(updateAnswersCountSql)) {
                updateAnswersCountStmt.setInt(1, answerId);
                updateAnswersCountStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answerId;
    }


    public Answer postAnswer(Answer answer) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("in repos " + answer);
            String sql = "INSERT INTO answers (message, user_id, room_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, answer.getMessage());
            stmt.setInt(2, answer.getUserId());
            stmt.setInt(3, answer.getRoomId());
            stmt.executeUpdate();
            System.out.println(answer);

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int answerId = generatedKeys.getInt(1);
                answer.setId(answerId);
            }
            return answer;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertVote(int userId, int answerId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Check if the user has already voted
            String checkVoteSql = "SELECT * FROM votes WHERE user_id = ? AND answer_id = ?";
            try (PreparedStatement checkVoteStmt = conn.prepareStatement(checkVoteSql)) {
                checkVoteStmt.setInt(1, userId);
                checkVoteStmt.setInt(2, answerId);
                try (ResultSet rs = checkVoteStmt.executeQuery()) {
                    if (rs.next()) {
                        // User has already voted, you can handle this case as needed
                        throw new SQLException("User has already voted on this answer.");
                    } else {
                        // Insert new vote
                        String insertVoteSql = "INSERT INTO votes (user_id, answer_id) VALUES (?, ?)";
                        try (PreparedStatement insertVoteStmt = conn.prepareStatement(insertVoteSql)) {
                            insertVoteStmt.setInt(1, userId);
                            insertVoteStmt.setInt(2, answerId);
                            insertVoteStmt.executeUpdate();
                        }
                    }
                }
            }

            logger.info("Vote inserted for answerId: " + answerId + " by userId: " + userId);
        } catch (SQLException e) {
            logger.severe("Error inserting vote: " + e.getMessage());
            throw e;
        }
    }
    public void deleteVote(int userId, int answerId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Check if the user has voted for the answer
            String checkVoteSql = "SELECT * FROM votes WHERE user_id = ? AND answer_id = ?";
            try (PreparedStatement checkVoteStmt = conn.prepareStatement(checkVoteSql)) {
                checkVoteStmt.setInt(1, userId);
                checkVoteStmt.setInt(2, answerId);
                try (ResultSet rs = checkVoteStmt.executeQuery()) {
                    if (rs.next()) {
                        // User has voted, delete the vote
                        String deleteVoteSql = "DELETE FROM votes WHERE user_id = ? AND answer_id = ?";
                        try (PreparedStatement deleteVoteStmt = conn.prepareStatement(deleteVoteSql)) {
                            deleteVoteStmt.setInt(1, userId);
                            deleteVoteStmt.setInt(2, answerId);
                            deleteVoteStmt.executeUpdate();
                        }
                    } else {
                        // User has not voted, you can handle this case as needed
                        throw new SQLException("User has not voted on this answer.");
                    }
                }
            }

            logger.info("Vote deleted for answerId: " + answerId + " by userId: " + userId);
        } catch (SQLException e) {
            logger.severe("Error deleting vote: " + e.getMessage());
            throw e;
        }
    }


    public List<Vote> getVotesByAnswerId(int answerId) {
        List<Vote> votes = new ArrayList<>();
        String sql = "SELECT id, user_id, answer_id FROM votes WHERE answer_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, answerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vote vote = new Vote();
                    vote.setId(rs.getInt("id"));
                    vote.setUserId(rs.getInt("user_id"));
                    vote.setAnswerId(rs.getInt("answer_id"));
                    votes.add(vote);
                }
            }
        } catch (SQLException e) {
            LoggerUtility.getLogger().severe("SQL Error in getVotesByAnswerId: " + e.getMessage());
        }
        return votes;
    }
    public List<Room> searchRooms(String keyword, String searchType) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "";
        if(searchType.equals("user")){
            sql = "SELECT DISTINCT rooms.id AS room_id, rooms.title, rooms.message, users.username " +
                    "FROM rooms " +
                    "JOIN users ON rooms.creator_id = users.id " +
                    "LEFT JOIN answers ON rooms.id = answers.room_id " +
                    "LEFT JOIN users AS answer_users ON answers.user_id = answer_users.id " +
                    "WHERE users.username LIKE ? OR answer_users.username LIKE ?";
        } else {
            sql = "SELECT 'room' AS type, r.id AS room_id, r.title, r.message, u.username " +
                    "FROM rooms r " +
                    "LEFT JOIN answers a ON r.id = a.room_id " +
                    "LEFT JOIN users u ON a.user_id = u.id " +
                    "WHERE r.title LIKE ? OR r.message LIKE ? " +
                    "OR a.message LIKE ? " +
                    "OR u.username LIKE ? " +
                    "GROUP BY r.id";
        }

        System.out.println(sql);


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            if (searchType.equals("user")) {
                stmt.setString(1, likeKeyword);
                stmt.setString(2, likeKeyword);
            } else {
                stmt.setString(1, likeKeyword);
                stmt.setString(2, likeKeyword);
                stmt.setString(3, likeKeyword);
                stmt.setString(4, likeKeyword);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    int roomId = rs.getInt("room_id");
                    room.setId(roomId);
                    String title = rs.getString("title");
                    room.setTitle(title);
                    String message = rs.getString("message");
                    room.setMessage(message);
                    String username = rs.getString("username");
                    room.setCreatorName(username);
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }

}
