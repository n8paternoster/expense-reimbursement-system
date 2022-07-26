package models;

import java.time.LocalDateTime;

public class ReimbursementRequest {
    private int requestID;
    private int submitterID;
    private int resolverID;
    private long amount;
    private String category;
    private String description;
    private LocalDateTime timeSubmitted;
    private String status;

    public ReimbursementRequest() {
    }
    public ReimbursementRequest(int requestID, int submitterID, int resolverID, long amount, String category, String description, LocalDateTime timeSubmitted, String status) {
        this.requestID = requestID;
        this.submitterID = submitterID;
        this.resolverID = resolverID;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.timeSubmitted = timeSubmitted;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
