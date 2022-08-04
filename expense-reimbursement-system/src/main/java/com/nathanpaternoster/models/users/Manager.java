package com.nathanpaternoster.models.users;

import java.time.LocalDate;

/**
 * Class to represent an ERS manager
 */
public class Manager extends User {
    public Manager() {
        super();
    }
    public Manager(int userID, String password, String firstName, String lastName, LocalDate dob) {
        super(userID, password, firstName, lastName, dob);
    }
}
