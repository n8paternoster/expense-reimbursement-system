package com.nathanpaternoster.services;

import com.nathanpaternoster.models.requests.ReimbursementRequest;

import java.util.List;

/**
 * Data access object for the reimbursement requests ("requests") table
 */
public interface RequestDAO {

    /**
     * Add a new reimbursement request to the database
     * @param r The reimbursement request to insert
     * @return The generated requestID or -1 if the request wasn't inserted
     */
    int addRequest(ReimbursementRequest r);

    /**
     * Update a request's status
     * @param resolverID The user id of the user resolving the request
     * @param requestID The reimbursement request's id
     * @param resolution The updated status
     * @return True if the request was successfully updated, false otherwise
     */
    boolean resolveRequest(int resolverID, int requestID, String resolution);

    /**
     * Search the database for a reimbursement request with the given id
     * @param requestID The request's id
     * @return A new reimbursement request object if found, null otherwise
     */
    ReimbursementRequest getRequest(int requestID);

    /**
     * Get all requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of reimbursement requests
     */
    List<ReimbursementRequest> getRequests(int userID);

    /**
     * Get all pending requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of pending reimbursement requests
     */
    List<ReimbursementRequest> getPendingRequests(int userID);

    /**
     * Get all resolved requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of resolved reimbursement requests
     */
    List<ReimbursementRequest> getResolvedRequests(int userID);

    /**
     * Get all requests
     * @return A list of reimbursement requests
     */
    List<ReimbursementRequest> getAllRequests();

    /**
     * Get all pending requests
     * @return A list of pending reimbursement requests
     */
    List<ReimbursementRequest> getAllPendingRequests();

    /**
     * Get all resolved requests
     * @return A list of resolved reimbursement requests
     */
    List<ReimbursementRequest> getAllResolvedRequests();
}