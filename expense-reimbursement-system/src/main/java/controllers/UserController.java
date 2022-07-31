package controllers;

import models.users.User;
import services.UserDAO;

public class UserController {
    protected UserDAO dao;

    public UserController(UserDAO dao) {
        this.dao = dao;
    }

    public User login(int userID, String password) {
        return dao.authenticateUser(userID, password);
    }

    public void logout() { }
}
