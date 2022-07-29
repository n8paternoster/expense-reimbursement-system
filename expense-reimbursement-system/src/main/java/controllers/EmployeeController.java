package controllers;

import models.Employee;
import services.UserDAO;

import java.time.LocalDate;

public class EmployeeController extends UserController {

    public EmployeeController(UserDAO dao) {
        super(dao);
    }

    public Employee viewProfile() {
        if (activeUser == null || !(activeUser instanceof Employee)) return null;
        return (Employee)activeUser;
    }

    public boolean updateProfile(String password, String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        if (activeUser == null || !(activeUser instanceof Employee))
            throw new RuntimeException("Invalid user");
        if (!dao.emailIsAvailable(email))
            throw new RuntimeException("New email address is unavailable");
        Employee updated = new Employee(activeUser.getUserID(), password, firstName, lastName, dob, email, ((Employee) activeUser).getRequests());
        return dao.updateUser(updated);
    }
}
