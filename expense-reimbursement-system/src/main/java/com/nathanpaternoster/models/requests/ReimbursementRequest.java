package com.nathanpaternoster.models.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * Class to represent a reimbursement request
 */
public class ReimbursementRequest {
    private int requestID;
    private int submitterID;
    private int resolverID;
    private long amount;
    private String category;
    private String description;
    private LocalDateTime timeSubmitted;
    private LocalDateTime timeResolved;
    private String status;

    public ReimbursementRequest() {
    }
    public ReimbursementRequest(int requestID, int submitterID, int resolverID, long amount, String category, String description, LocalDateTime timeSubmitted, LocalDateTime timeResolved, String status) {
        this.requestID = requestID;
        this.submitterID = submitterID;
        this.resolverID = resolverID;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.timeSubmitted = timeSubmitted;
        this.timeResolved = timeResolved;
        this.status = status;
    }

    public int getRequestID() {
        return requestID;
    }
    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }
    public int getSubmitterID() {
        return submitterID;
    }
    public void setSubmitterID(int submitterID) {
        this.submitterID = submitterID;
    }
    public int getResolverID() {
        return resolverID;
    }
    public void setResolverID(int resolverID) {
        this.resolverID = resolverID;
    }
    @JsonIgnore
    public long getAmount() {
        return amount;
    }
    public void setAmount(long amount) {
        this.amount = amount;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getTimeSubmitted() {
        return timeSubmitted;
    }
    public void setTimeSubmitted(LocalDateTime timeSubmitted) {
        this.timeSubmitted = timeSubmitted;
    }
    public LocalDateTime getTimeResolved() {
        return timeResolved;
    }
    public void setTimeResolved(LocalDateTime timeResolved) {
        this.timeResolved = timeResolved;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * When displaying the request amount, convert to an amount in dollars
     * @return A string representing the request amount
     */
    @JsonGetter("amount")
    public String getStringAmount() {
        String s = String.valueOf(amount);
        StringBuilder displayAmount = new StringBuilder(s);
        if (s.length() == 1) displayAmount.insert(0, "00");
        else if (s.length() == 2) displayAmount.insert(0, "0");
        displayAmount.insert(0, "$").insert(displayAmount.length()-2, '.');
        s = displayAmount.toString();
        return s;
    }
}
