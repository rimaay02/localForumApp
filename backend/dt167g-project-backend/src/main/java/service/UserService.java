package service;

import model.User;
import repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userRepository.createUserTable();
    }

    public boolean registerUser(String firstName, String lastName, String username, String password) {
        if (userRepository.findUserByUsername(username) != null) {
            return false; // Username already exists
        }
        User user = new User(firstName, lastName, username, hashPassword(password));
        return userRepository.saveUser(user);
    }

    public User authenticateUser(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        if (user != null && checkPassword(password, user.getPassword())) {
            return user; // Return the user if authentication is successful
        } else {
            return null; // Return null if authentication fails
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
