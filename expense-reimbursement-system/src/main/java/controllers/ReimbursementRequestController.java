package controllers;

import models.ReimbursementRequest;
import services.RequestDAO;

import java.time.LocalDateTime;
import java.util.List;

public class ReimbursementRequestController {
    private RequestDAO dao;

    public ReimbursementRequestController(RequestDAO dao) {
        this.dao = dao;
    }

    public int submitNewRequest(int submitterID, long amount, String category, String description) throws RuntimeException {
        ReimbursementRequest newRequest = new ReimbursementRequest(-1, submitterID, -1, amount, category, description, LocalDateTime.now(), "Pending");
        int generatedID = dao.addRequest(newRequest);
        if (generatedID < 0)
            throw new RuntimeException("Error inserting new request into the database");
        return generatedID;
    }

    public boolean resolveRequest(int requestID, String newStatus) {
        return dao.resolveRequest(requestID, newStatus);
    }

    public ReimbursementRequest viewRequest(int requestID) {
        return dao.getRequest(requestID);
    }

    public List<ReimbursementRequest> viewPendingRequests(int userID) {
        return dao.getPendingRequests(userID);
    }

    public List<ReimbursementRequest> viewResolvedRequests(int userID) {
        return dao.getResolvedRequests(userID);
    }

    public List<ReimbursementRequest> viewAllPendingRequests() {
        return dao.getAllPendingRequests();
    }

    public List<ReimbursementRequest> viewAllResolvedRequests() {
        return dao.getAllResolvedRequests();
    }
}
