package services;

import models.Employee;
import models.User;

import java.util.List;

public interface UserDAO {
    User authenticateUser(int userID, String password);
    boolean addNewUser(User newUser);
    User getUser(int userID);
    boolean updateUser(User updatedUser);
    List<Employee> getAllEmployees();
    boolean emailIsAvailable(String email);
}
