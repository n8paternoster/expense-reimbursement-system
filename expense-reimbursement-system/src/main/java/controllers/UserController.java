package controllers;

import models.User;
import services.UserDAO;

import java.time.LocalDate;

public abstract class UserController {
    protected UserDAO dao;
    protected User activeUser;

    public UserController(UserDAO dao) {
        this.dao = dao;
    }

    public boolean login(int userID, String password) {
        User u = dao.authenticateUser(userID, password);
        if (u == null) return false;
        else {
            activeUser = u;
            return true;
        }
    }

    public void logout() {
        activeUser = null;
    }
}
