package com.nathanpaternoster.models.users;

import java.time.LocalDate;

/**
 * Base class to represent an ERS user, login credentials are a userID and password
 */
public abstract class User {
    private int userID;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate dob;

    public User() {
    }
    public User(int userID, String password, String firstName, String lastName, LocalDate dob) {
        this.userID = userID;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }
    public User(int userID, String password) {
        this(userID, password, "", "", null);
    }
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public LocalDate getDob() {
        return dob;
    }
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
}
