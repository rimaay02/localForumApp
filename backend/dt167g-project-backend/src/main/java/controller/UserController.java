package controller;

import model.User;
import service.UserService;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public boolean registerUser(String firstName, String lastName, String username, String password) {
        return userService.registerUser(firstName, lastName, username, password);
    }

    public User authenticateUser(String username, String password) {
        return userService.authenticateUser(username, password);
    }
}