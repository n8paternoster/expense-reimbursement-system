package services;

import models.requests.ReimbursementRequest;

import java.util.List;

public interface RequestDAO {

    // insert/update
    int addRequest(ReimbursementRequest r);
    boolean resolveRequest(int requestID, String resolution);

    // get
    ReimbursementRequest getRequest(int requestID);
    List<ReimbursementRequest> getRequests(int userID);
    List<ReimbursementRequest> getPendingRequests(int userID);
    List<ReimbursementRequest> getResolvedRequests(int userID);
    List<ReimbursementRequest> getAllRequests();
    List<ReimbursementRequest> getAllPendingRequests();
    List<ReimbursementRequest> getAllResolvedRequests();

}
