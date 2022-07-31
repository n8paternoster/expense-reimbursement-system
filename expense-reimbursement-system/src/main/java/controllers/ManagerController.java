package controllers;

import models.users.Employee;
import models.users.User;
import services.UserDAO;

import java.time.LocalDate;
import java.util.List;

public class ManagerController extends UserController {

    public ManagerController(UserDAO dao) {
        super(dao);
    }

    public int addNewEmployee(String password, String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        if (!dao.emailIsAvailable(email))
            throw new RuntimeException("Email address is unavailable");
        Employee newEmployee = new Employee(-1, password, firstName, lastName, dob, email, null);
        int generatedID = dao.addNewUser(newEmployee);
        if (generatedID < 0)
            throw new RuntimeException("Error inserting new employee into the database");
        return generatedID;
    }

    public Employee viewEmployee(int userID) {
        User u = dao.getUser(userID);
        return (u instanceof Employee) ? (Employee) u : null;
    }

    public List<Employee> viewAllEmployees() {
        return dao.getAllEmployees();
    }

}
