package controllers;

import models.Employee;
import models.Manager;
import services.UserDAO;

import java.time.LocalDate;
import java.util.List;

public class ManagerController extends UserController {

    public ManagerController(UserDAO dao) {
        super(dao);
    }

    public int addNewEmployee(String password, String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        if (activeUser == null || !(activeUser instanceof Manager))
            throw new RuntimeException("Forbidden");
        if (!dao.emailIsAvailable(email))
            throw new RuntimeException("Email address is unavailable");
        Employee newEmployee = new Employee(-1, password, firstName, lastName, dob, email, null);
        int generatedID = dao.addNewUser(newEmployee);
        if (generatedID < 0)
            throw new RuntimeException("Error inserting new employee into the database");
        return generatedID;
    }

    public List<Employee> viewAllEmployees() throws RuntimeException {
        if (activeUser == null || !(activeUser instanceof Manager))
            throw new RuntimeException("Forbidden");
        return dao.getAllEmployees();
    }

}
