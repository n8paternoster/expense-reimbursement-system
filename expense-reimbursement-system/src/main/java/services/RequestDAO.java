package services;

import models.ReimbursementRequest;

import java.util.List;

public interface RequestDAO {
    boolean addRequest(ReimbursementRequest r);
    ReimbursementRequest getRequest(int requestID);
    List<ReimbursementRequest> getPendingRequests(int userID);
    List<ReimbursementRequest> getResolvedRequests(int userID);
    List<ReimbursementRequest> getAllPendingRequests();
    List<ReimbursementRequest> getAllResolvedRequests();
    boolean resolveRequest(int requestID, String resolution);
}
