package com.nathanpaternoster.models.users;

import com.nathanpaternoster.models.requests.ReimbursementRequest;

import java.time.LocalDate;
import java.util.ArrayList;

public class Employee extends User {
    private String email;
    private ArrayList<ReimbursementRequest> requests;

    public Employee() {
        super();
    }
    public Employee(int userID, String password, String firstName, String lastName, LocalDate dob, String email, ArrayList<ReimbursementRequest> requests) {
        super(userID, password, firstName, lastName, dob);
        this.email = email;
        this.requests = requests;
    }
    public Employee(String password, String firstName, String lastName, LocalDate dob, String email) {
        this(-1, password, firstName, lastName, dob, email, null);
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public ArrayList<ReimbursementRequest> getRequests() {
        return requests;
    }
    public void setRequests(ArrayList<ReimbursementRequest> requests) {
        this.requests = requests;
    }
}