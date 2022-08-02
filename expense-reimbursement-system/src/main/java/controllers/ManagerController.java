package controllers;

import models.users.Employee;
import models.users.User;
import services.UserDAO;

import java.time.LocalDate;
import java.util.List;

/**
 * Class to handle actions a manager can perform when logged in
 */
public class ManagerController extends UserController {

    public ManagerController(UserDAO dao) {
        super(dao);
    }

    /**
     * Add a new employee
     * @param password The new employee's password
     * @param firstName The new employee's first name
     * @param lastName The new employee's last name
     * @param dob The new employee's date of birth
     * @param email The new employee's email
     * @return a generated employee id if successfully added, or -1 if not
     * @throws RuntimeException if any provided input values are invalid
     */
    public int addNewEmployee(String password, String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        // validate input
        validateName(firstName, lastName);
        validateDOB(dob);
        validateEmail(email);
        validatePassword(password);

        // add the new employee
        Employee newEmployee = new Employee(-1, password, firstName, lastName, dob, email, null);
        int generatedID = dao.addNewUser(newEmployee);
        if (generatedID < 0) generatedID = -1;
        return generatedID;
    }

    /**
     * Get an employee's profile info
     * @param userID The employee's user id
     * @return the employee if found, null otherwise
     */
    public Employee viewEmployee(int userID) {
        User u = dao.getUser(userID);
        return (u instanceof Employee) ? (Employee) u : null;
    }

    /**
     * Get the profile info for every employee
     * @return a list of employees
     */
    public List<Employee> viewAllEmployees() {
        return dao.getAllEmployees();
    }
}
