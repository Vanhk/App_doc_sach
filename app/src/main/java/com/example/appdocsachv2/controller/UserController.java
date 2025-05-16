package com.example.appdocsachv2.controller;

import com.example.appdocsachv2.model.User;
import com.example.appdocsachv2.model.UserDAO;

public class UserController {
    private UserDAO userDAO;

    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

//    public long registerUser(String username, String password, String email) {
//        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || email == null || email.trim().isEmpty()) {
//            return -1; // Trả về -1 nếu dữ liệu không hợp lệ
//        }
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(password);
//        user.setEmail(email);
//        return userDAO.insertUser(user);
//    }
//
//    public int loginUser(String username, String password) {
//        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
//            return -1; // Trả về -1 nếu dữ liệu không hợp lệ
//        }
//        return userDAO.loginUser(username, password);
//    }
//
//    public boolean updateUser(User user) {
//        if (user == null || user.getUserId() < 0 || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
//            return false; // Trả về false nếu dữ liệu không hợp lệ
//        }
//        return userDAO.updateUser(user);
//    }
//
//    public boolean deleteUser(int userId) {
//        if (userId < 0) {
//            return false; // Trả về false nếu userId không hợp lệ
//        }
//        userDAO.deleteUser(userId);
//        return true;
//    }
//
//    public User getUserById(int userId) {
//        if (userId < 0) {
//            return null; // Trả về null nếu userId không hợp lệ
//        }
//        return userDAO.getUserById(userId);
//    }
}