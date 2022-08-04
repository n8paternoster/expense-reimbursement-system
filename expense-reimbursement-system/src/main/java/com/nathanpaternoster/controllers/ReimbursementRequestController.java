package com.nathanpaternoster.controllers;

import com.nathanpaternoster.services.EmailService;
import com.nathanpaternoster.models.requests.ReimbursementRequest;
import com.nathanpaternoster.models.users.Employee;
import com.nathanpaternoster.models.users.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.nathanpaternoster.services.RequestDAO;
import com.nathanpaternoster.services.UserDAO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class to handle actions on reimbursement requests
 */
public class ReimbursementRequestController {
    private static final Logger log = LogManager.getLogger(ReimbursementRequestController.class);
    private final RequestDAO requestDAO;
    private final UserDAO userDAO;

    public ReimbursementRequestController(RequestDAO requestDAO, UserDAO userDAO) {
        this.requestDAO = requestDAO;
        this.userDAO = userDAO;
        log.debug("ReimbursementRequestController created");
    }

    /**
     * Check a user input representing money and convert to a long representing the value in cents
     * @param dollarAmount The entered amount of money
     * @return the amount of money in cents
     * @throws RuntimeException if:
     *  - The string cannot be converted to a BigDecimal
     *  - The value is negative
     *  - The value contains more than 2 decimal digits
     *  - The value is too large to fit in a long
     */
    public long validateMoney(String dollarAmount) throws RuntimeException {
        if (dollarAmount == null || dollarAmount.isEmpty())
            throw new RuntimeException("No amount entered");
        try {
            BigDecimal bd = new BigDecimal(dollarAmount);
            if (bd.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
            bd = bd.scaleByPowerOfTen(2);    // convert to cents
            return bd.longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new RuntimeException("Invalid amount");
        }
    }

    /**
     * Add a new reimbursement request
     * @param submitterID The user id belonging to the user submitting the request
     * @param amount The new request's amount
     * @param category The new request's category
     * @param description The new request's description
     * @return a generated request id if successfully added, or -1 if not
     * @throws RuntimeException if any provided input values are invalid
     */
    public int submitNewRequest(int submitterID, String amount, String category, String description) throws RuntimeException {
        // validate user input
        long amountInCents = validateMoney(amount);

        // add the new request
        ReimbursementRequest newRequest = new ReimbursementRequest(-1, submitterID, -1, amountInCents, category, description, LocalDateTime.now(), null, "Pending");
        int generatedID = requestDAO.addRequest(newRequest);
        if (generatedID < 0) generatedID = -1;
        return generatedID;
    }

    /**
     * Approve or deny a pending reimbursement request
     * @param resolverID The user id of the manager resolving the request
     * @param requestID The request id of the request to update
     * @param approved True if the request is approved, false if the request is denied
     * @return True if the request was successfully updated, false otherwise
     */
    public boolean resolveRequest(int resolverID, int requestID, boolean approved) {
        String newStatus = approved ? "Approved" : "Denied";
        boolean success = requestDAO.resolveRequest(resolverID, requestID, newStatus);
        if (success) {
            try {
                EmailService emailService = new EmailService();
                ReimbursementRequest r = requestDAO.getRequest(requestID);
                User u = userDAO.getUser(r.getSubmitterID());
                String to = ((Employee) u).getEmail();
                String subject = "Reimbursement Request Resolution";
                String body = u.getFirstName() + ",\n\nYour reimbursement request for " + r.getCategory() +
                        " in the amount of " + r.getStringAmount() + " has been " + r.getStatus() +
                        ".\n\nThis is an automated message. For any questions or concerns please contact your direct manager." +
                        "\n\nERS Department of Billing";
                emailService.sendEmail(to, subject, body);
            } catch (RuntimeException e) {
                log.info("Email notification for a resolved request failed");
            }
        }
        return success;
    }

    /**
     * Get a reimbursement request by id
     * @param requestID The request id
     * @return the reimbursement request if found, null otherwise
     */
    public ReimbursementRequest viewRequest(int requestID) {
        return requestDAO.getRequest(requestID);
    }

    /**
     * Get a list of all reimbursement requests submitted by one user
     * @param userID The user id of the user submitting the requests
     * @return a list of reimbursement requests
     */
    public List<ReimbursementRequest> viewRequests(int userID) {
        return requestDAO.getRequests(userID);
    }

    /**
     * Get a list of all pending reimbursement requests submitted by one user
     * @param userID The user id of the user submitting the requests
     * @return a list of reimbursement requests
     */
    public List<ReimbursementRequest> viewPendingRequests(int userID) {
        return requestDAO.getPendingRequests(userID);
    }

    /**
     * Get a list of all resolved (approved or denied) reimbursement requests submitted by one user
     * @param userID The user id of the user submitting the requests
     * @return a list of reimbursement requests
     */
    public List<ReimbursementRequest> viewResolvedRequests(int userID) {
        return requestDAO.getResolvedRequests(userID);
    }

    /**
     * Get a list of all reimbursement requests
     * @return a list of reimbursement requests
     */
    public List<ReimbursementRequest> viewAllRequests() {
        return requestDAO.getAllRequests();
    }

    /**
     * Get a list of all pending reimbursement requests
     * @return a list of reimbursement requests
     */
    public List<ReimbursementRequest> viewAllPendingRequests() {
        return requestDAO.getAllPendingRequests();
    }

    /**
     * Get a list of all resolved (approved or denied) reimbursement requests
     * @return a list of reimbursement requests
     */
    public List<ReimbursementRequest> viewAllResolvedRequests() {
        return requestDAO.getAllResolvedRequests();
    }
}