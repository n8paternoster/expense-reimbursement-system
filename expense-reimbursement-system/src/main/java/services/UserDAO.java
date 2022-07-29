package services;

import models.Employee;
import models.User;

import java.util.List;

public interface UserDAO {

    // insert/update
    int addNewUser(User newUser);
    boolean updateUser(User updatedUser);

    // get
    User authenticateUser(int userID, String password);
    User getUser(int userID);
    List<Employee> getAllEmployees();
    boolean emailIsAvailable(String email);
}
