package service;

import model.Room;
import model.Answer;
import model.Vote;
import repository.ForumRepository;
import java.sql.SQLException;
import java.util.List;

public class ForumService {
    private final ForumRepository forumRepository;

    public ForumService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    public void createRoom(Room room) {
        forumRepository.createRoom(room);
    }

    public List<Room> getRooms() {
        return forumRepository.getRooms();
    }
    public Room getRoomById(int roomId) throws SQLException {
        return forumRepository.getRoomById(roomId);
    }

    public Answer postAnswer(Answer answer) {
        return forumRepository.postAnswer(answer);
    }

    public int deleteAnswer(int answerId) {
        return forumRepository.deleteAnswer(answerId);
    }
    public void insertVote(int userId, int answerId) throws SQLException {
        forumRepository.insertVote(userId, answerId);
    }

    public void deleteVote(int userId, int answerId) {
        try {
            forumRepository.deleteVote(userId, answerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Answer> getAnswersByRoomId(int roomId) {
        return forumRepository.getAnswersByRoomId(roomId);
    }

    public List<Vote> getVotesByAnswerId(int answerId) {
        return forumRepository.getVotesByAnswerId(answerId);
    }

    public List<Room> searchRooms(String query, String type) {
        try {
            return forumRepository.searchRooms(query, type);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
