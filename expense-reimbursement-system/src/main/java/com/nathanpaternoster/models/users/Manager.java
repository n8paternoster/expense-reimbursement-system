package com.nathanpaternoster.models.users;

import java.time.LocalDate;

public class Manager extends User {
    public Manager() {
        super();
    }
    public Manager(int userID, String password, String firstName, String lastName, LocalDate dob) {
        super(userID, password, firstName, lastName, dob);
    }
}
