package controllers;

import models.users.Employee;
import models.users.User;
import services.UserDAO;

import java.time.LocalDate;

public class EmployeeController extends UserController {

    public EmployeeController(UserDAO dao) {
        super(dao);
    }

    public Employee getProfile(int userID) {
        User u = dao.getUser(userID);
        return (u instanceof Employee) ? (Employee) u : null;
    }

    public boolean updateProfile(int userID, String password, String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        User u = dao.getUser(userID);
        if (!(u instanceof Employee)) return false;
        String currentEmail = ((Employee) u).getEmail();
        if (!email.equalsIgnoreCase(currentEmail) && !dao.emailIsAvailable(email))
            throw new RuntimeException("Email address is unavailable");
        Employee updated = new Employee(userID, password, firstName, lastName, dob, email, null);
        return dao.updateUser(updated);
    }
}
