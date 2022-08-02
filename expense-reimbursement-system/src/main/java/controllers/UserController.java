package controllers;

import models.users.User;
import services.UserDAO;

import java.time.LocalDate;

/**
 * Base class to handle user authentication and input validation
 */
public class UserController {
    private static final int MIN_PASSWORD_LENGTH = 7;
    protected UserDAO dao;

    public UserController(UserDAO dao) {
        this.dao = dao;
    }

    /**
     * Authenticate the given user credentials
     * @param userID The user's "employeeID" serving as their login username
     * @param password The user's password
     * @return the user if login credentials are valid, null otherwise
     */
    public User login(int userID, String password) {
        return dao.authenticateUser(userID, password);
    }

    /**
     * Check a user input representing a first and last name
     * @param firstName The entered first name
     * @param lastName The entered last name
     * @throws RuntimeException if:
     *      - either name is not given
     *      - either name contains non-alphanumeric characters
     *      - either name has leading/trailing spaces or consecutive spaces
     */
    protected void validateName(String firstName, String lastName) throws RuntimeException {
        if (firstName == null || firstName.isEmpty())
            throw new RuntimeException("No first name entered");
        if (lastName == null || lastName.isEmpty())
            throw new RuntimeException("No last name entered");
        if (!firstName.matches("^[a-zA-Z\\d]+( [a-zA-Z\\d]+)*$") || !lastName.matches("^[a-zA-Z\\d]+( [a-zA-Z\\d]+)*$"))
            throw new RuntimeException("Invalid name");
    }

    /**
     * Check a user input representing a password
     * @param password The entered password
     * @throws RuntimeException if:
     *      - the password is not at least MIN_PASSWORD_LENGTH characters long
     *      - the password does not include at least 1 letter
     *      - the password does not include at least 1 number
     */
    protected void validatePassword(String password) throws RuntimeException {
        if (password == null || password.matches("^(.{0," + (MIN_PASSWORD_LENGTH-1) + "}|\\D*|[^a-zA-Z]*)$"))
            throw new RuntimeException("Password must contain at least " + MIN_PASSWORD_LENGTH + " characters and include at least 1 letter and 1 number");
    }

    /**
     * Check a user input representing an email
     * @param email The entered email
     * @throws RuntimeException if the email address is already registered in the database
     */
    protected void validateEmail(String email) throws RuntimeException {
        if (!dao.emailIsAvailable(email))
            throw new RuntimeException("Email address is unavailable");
    }

    /**
     * Check a user input representing a date of birth
     * @param dob The entered date of birth (null values are allowed)
     * @throws RuntimeException if the date of birth is in the future
     */
    protected void validateDOB(LocalDate dob) throws RuntimeException {
        if (dob != null && dob.isAfter(LocalDate.now()))
            throw new RuntimeException("Invalid date of birth entered");
    }
}
