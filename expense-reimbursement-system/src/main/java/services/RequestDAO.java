package services;

import models.ReimbursementRequest;

import java.util.List;

public interface RequestDAO {

    // insert/update
    int addRequest(ReimbursementRequest r);
    boolean resolveRequest(int requestID, String resolution);

    // get
    ReimbursementRequest getRequest(int requestID);
    List<ReimbursementRequest> getPendingRequests(int userID);
    List<ReimbursementRequest> getResolvedRequests(int userID);
    List<ReimbursementRequest> getAllPendingRequests();
    List<ReimbursementRequest> getAllResolvedRequests();

}
