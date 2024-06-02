package model;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;

    // Constructor without ID (for creating new users)
    public User(String firstName, String lastName, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }

    public User(){};

    // Constructor with ID (for existing users)
    public User(int id, String firstName, String lastName, String username, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }

    // Getter and setter for ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getters and setters for other fields
    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }


}
