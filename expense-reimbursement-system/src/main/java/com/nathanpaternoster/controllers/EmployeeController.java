package com.nathanpaternoster.controllers;

import com.nathanpaternoster.models.users.Employee;
import com.nathanpaternoster.models.users.User;
import com.nathanpaternoster.services.UserDAO;

import java.time.LocalDate;

/**
 * Class to handle actions an employee can perform when logged in
 */
public class EmployeeController extends UserController {

    public EmployeeController(UserDAO dao) {
        super(dao);
    }

    /**
     * @param userID The user id of the employee to look up
     * @return the employee if found, null otherwise
     */
    public Employee getProfile(int userID) {
        User u = dao.getUser(userID);
        return (u instanceof Employee) ? (Employee) u : null;
    }

    /**
     * Update the employee's information
     * @param userID The user id of the employee
     * @param password An updated password
     * @param firstName An updated first name
     * @param lastName An updated last name
     * @param dob An updated date of birth
     * @param email An updated email
     * @return true if the information was successfully changed, false otherwise
     * @throws RuntimeException if any provided input values are invalid
     */
    public boolean updateProfile(int userID, String password, String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        User u = dao.getUser(userID);
        if (!(u instanceof Employee)) return false;
        String currentEmail = ((Employee) u).getEmail();

        // validate input
        validateName(firstName, lastName);
        validateDOB(dob);
        if (!email.equalsIgnoreCase(currentEmail))
            validateEmail(email);
        validatePassword(password);

        // update the user info
        Employee updated = new Employee(userID, password, firstName, lastName, dob, email, null);
        return dao.updateUser(updated);
    }
}
