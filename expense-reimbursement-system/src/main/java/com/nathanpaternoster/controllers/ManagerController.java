package com.nathanpaternoster.controllers;

import com.nathanpaternoster.services.EmailService;
import com.nathanpaternoster.models.users.Employee;
import com.nathanpaternoster.models.users.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.nathanpaternoster.services.UserDAO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Class to handle actions a manager can perform when logged in
 */
public class ManagerController extends UserController {
    private static final Logger log = LogManager.getLogger(ManagerController.class);

    public ManagerController(UserDAO dao) {
        super(dao);
        log.debug("ManagerController created");
    }

    /**
     * Generates a random password with at least 1 letter, 1 number, and 1 special character
     * @param length The length of the generated password
     * @return a generated password
     */
    private static String generatePassword(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "1234567890";
        String special = "!@#$";
        String all = letters + special + numbers;
        Random random = new Random();
        List<Character> chars = new ArrayList<>(length);

        // at least 1 letter, 1 number, and 1 special character
        chars.add(letters.charAt(random.nextInt(letters.length())));
        chars.add(numbers.charAt(random.nextInt(numbers.length())));
        chars.add(special.charAt(random.nextInt(special.length())));
        for (int i = 0; i < length-3; ++i)
            chars.add(all.charAt(random.nextInt(all.length())));
        Collections.shuffle(chars);

        log.debug("Generated a random password");
        return chars.stream().map(Object::toString).collect(Collectors.joining());
    }

    /**
     * Add a new employee, generate an employee id and temporary password and send an email instructing a password change
     * @param firstName The new employee's first name
     * @param lastName The new employee's last name
     * @param dob The new employee's date of birth
     * @param email The new employee's email
     * @return a generated employee id if successfully added, or -1 if not
     * @throws RuntimeException if any provided input values are invalid
     */
    public int addNewEmployee(String firstName, String lastName, LocalDate dob, String email) throws RuntimeException {
        // validate input
        validateName(firstName, lastName);
        validateDOB(dob);
        validateEmail(email);

        // add the new employee
        String generatedPassword = generatePassword(MIN_PASSWORD_LENGTH);
        Employee newEmployee = new Employee(-1, generatedPassword, firstName, lastName, dob, email, null);
        int generatedID = dao.addNewUser(newEmployee);
        if (generatedID >= 0) {
            try {
                EmailService emailService = new EmailService();
                String subject = "Welcome to ERS";
                String body = "Hello " + firstName + " " + lastName + "," +
                        "\n\nYour new ERS account has been created with an employee id of " + generatedID +
                        " and a temporary password of " + generatedPassword + ". At your earliest convenience please use these" +
                        " credentials to log in to your account and create a new password." +
                        "\n\nThis is an automated message. For any questions or concerns please contact your direct manager." +
                        "\n\nERS IT Department";
                emailService.sendEmail(email, subject, body);
            } catch (RuntimeException e) {
                log.info("Email notification for a new user's temporary password failed");
            }
        } else generatedID = -1;
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
