package com.nathanpaternoster.services;

import com.nathanpaternoster.models.users.Employee;
import com.nathanpaternoster.models.users.User;

import java.util.List;

/**
 * Data access object for the users ("users") table
 */
public interface UserDAO {

    /**
     * Insert a new user into the database
     * @param newUser The user to insert
     * @return The generated userID or -1 if user wasn't inserted
     */
    int addNewUser(User newUser);

    /**
     * Update the information of an existing user
     * @param updatedUser The user to update
     * @return True if the specified user was successfully found and updated, false otherwise
     */
    boolean updateUser(User updatedUser);

    /**
     * Query the database with login credentials
     * @param userID The user's id
     * @param password The user's password
     * @return A new user object if found, null otherwise
     */
    User authenticateUser(int userID, String password);

    /**
     * Search the database for a user with the given id
     * @param userID The user's id
     * @return A new user object if found, null otherwise
     */
    User getUser(int userID);

    /**
     * Query the database for all employees
     * @return A list of all employees
     */
    List<Employee> getAllEmployees();

    /**
     * Check if an email address is already registered to an existing user
     * @param email Email address to check
     * @return True if the email address is not currently assigned to an existing user, otherwise false
     */
    boolean emailIsAvailable(String email);
}