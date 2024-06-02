package model;

public class Vote {
    private int id;
    private int userId;
    private int answerId;

    public Vote() {
        // Default constructor
    }

    public Vote(int userId, int answerId) {
        this.userId = userId;
        this.answerId = answerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

}
