package services;

import models.User;

public interface UserDAO {
    User authenticate(int id, String password);

}
