package controllers;

import models.requests.ReimbursementRequest;
import services.RequestDAO;

import java.time.LocalDateTime;
import java.util.List;

public class ReimbursementRequestController {
    private final RequestDAO dao;

    public ReimbursementRequestController(RequestDAO dao) {
        this.dao = dao;
    }

    public int submitNewRequest(int submitterID, long amount, String category, String description) {
        ReimbursementRequest newRequest = new ReimbursementRequest(-1, submitterID, -1, amount, category, description, LocalDateTime.now(), "Pending");
        return dao.addRequest(newRequest);
    }

    public boolean resolveRequest(int requestID, String newStatus) {
        return dao.resolveRequest(requestID, newStatus);
    }

    public ReimbursementRequest viewRequest(int requestID) {
        return dao.getRequest(requestID);
    }

    public List<ReimbursementRequest> viewRequests(int userID) {
        return dao.getRequests(userID);
    }

    public List<ReimbursementRequest> viewPendingRequests(int userID) {
        return dao.getPendingRequests(userID);
    }

    public List<ReimbursementRequest> viewResolvedRequests(int userID) {
        return dao.getResolvedRequests(userID);
    }

    public List<ReimbursementRequest> viewAllRequests() {
        return dao.getAllRequests();
    }

    public List<ReimbursementRequest> viewAllPendingRequests() {
        return dao.getAllPendingRequests();
    }

    public List<ReimbursementRequest> viewAllResolvedRequests() {
        return dao.getAllResolvedRequests();
    }
}
