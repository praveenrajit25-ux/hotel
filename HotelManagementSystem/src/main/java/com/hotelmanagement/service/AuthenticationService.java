package com.hotelmanagement.service;

import com.hotelmanagement.dao.UserDAO;
import com.hotelmanagement.exception.AuthenticationException;
import com.hotelmanagement.model.User;
import com.hotelmanagement.util.PasswordUtil;
import com.hotelmanagement.util.ValidationUtil;

public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User authenticate(String username, String plainPassword) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(plainPassword)) {
            throw new AuthenticationException("Username and password are required.");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!"Active".equalsIgnoreCase(user.getStatus())) {
            throw new AuthenticationException("User account is " + user.getStatus().toLowerCase() + ". Please contact the administrator.");
        }

        boolean match = PasswordUtil.checkPassword(plainPassword, user.getPasswordHash());
        if (!match) {
            throw new AuthenticationException("Invalid username or password.");
        }

        // Update last login timestamp asynchronously / silently
        userDAO.updateLastLogin(user.getUserId());
        return user;
    }
}
