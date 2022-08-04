package com.nathanpaternoster.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nathanpaternoster.controllers.users.EmployeeController;
import com.nathanpaternoster.controllers.requests.ReimbursementRequestController;
import com.nathanpaternoster.models.requests.ReimbursementRequest;
import com.nathanpaternoster.models.users.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.nathanpaternoster.services.RequestDAO;
import com.nathanpaternoster.services.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Servlet to handle actions an authenticated employee can perform
 */
@WebServlet("/employees/*")
public class EmployeeServlet extends HttpServlet {
    private static final Logger log = LogManager.getLogger(EmployeeServlet.class);
    private ObjectMapper om;
    private EmployeeController employeeController;
    private ReimbursementRequestController requestController;

    // classes for JSON serialization/deserialization
    static class ReimbursementInput {
        public String amount;
        public String category;
        public String description;
    }
    static class RequestArray {
        public List<ReimbursementRequest> requests;
        public RequestArray(List<ReimbursementRequest> requests) {
            this.requests = requests;
        }
    }

    @Override
    public void init() throws ServletException {
        om = new ObjectMapper();

        // register the object mapper to display Java 8 dates
        om.registerModule(new JavaTimeModule());
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        om.setDateFormat(df);

        // get the DAOs and initialize the controllers
        UserDAO userDao = (UserDAO) getServletContext().getAttribute("userDAO");
        RequestDAO requestDAO = (RequestDAO) getServletContext().getAttribute("requestDAO");
        employeeController = new EmployeeController(userDao);
        requestController = new ReimbursementRequestController(requestDAO, userDao);
        log.debug("EmployeeServlet initialized");
    }

    /**
     * Validate that the employee specified by userID in the url parameters is currently logged in (by comparing with the HTTPSession attribute)
     * @return the userID of the employee, or -1 if the employee is not logged in or the url is malformed
     */
    private int validate(HttpServletRequest req, HttpServletResponse resp) {
        String[] params = req.getPathInfo().split("/");
        try {
            int userID = Integer.parseInt(params[1]);
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null || (int) session.getAttribute("user") != userID)
                return -1;
            return userID;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     *  GET requests are used on endpoints:
     *      /employees/(userID)                         |   display welcome and options
     *      /employees/(userID)/profile                 |   display profile
     *      /employees/(userID)/requests                |   display request options
     *      /employees/(userID)/requests/(requestID)    |   display specified request information
     *      /employees/(userID)/requests/all            |   display all requests
     *      /employees/(userID)/requests/pending        |   display all pending requests
     *      /employees/(userID)/requests/resolved       |   display all resolved requests
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("EmployeeServlet doGet called with: " + req.getPathInfo());

        String[] params = req.getPathInfo().split("/");

        // validate the user
        int userID = validate(req, resp);
        if (userID < 0) {
            log.info("Unauthorized request made to EmployeeServlet doGet with " + req.getPathInfo());
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the get request
        try {
            if (params.length == 2) {
                // employees/(userID)

                resp.setContentType("text/plain");
                resp.getWriter().println("Successfully logged in!");
                resp.getWriter().println("Enter /profile, /update, /requests, or /logout");
                resp.setStatus(200);
            } else if (params.length == 3 && params[2].equalsIgnoreCase("profile")) {
                // employees/(userID)/profile

                Employee e = employeeController.getProfile(userID);
                if (e == null) {
                    log.warn("Failed to retrieve profile information for an authorized employee");
                    resp.getWriter().println("Employee profile not found");
                    resp.setStatus(404);    // employee profile not found
                } else {
                    resp.setContentType("application/json");
                    resp.getWriter().write(om.writeValueAsString(e));
                    resp.setStatus(200);    // employee profile found
                }
            } else if (params[2].equalsIgnoreCase("requests")) {
                if (params.length == 3) {
                    // employees/(userID)/requests

                    resp.setContentType("text/plain");
                    resp.getWriter().println("Viewing requests");
                    resp.getWriter().println("Enter /all, /pending, /resolved, or /(requestID)");
                    resp.setStatus(200);
                } else if (params.length == 4) {
                    if (params[3].equalsIgnoreCase("pending") || params[3].equalsIgnoreCase("resolved") || params[3].equalsIgnoreCase("all")) {
                        // employees/(userID)/requests/all or pending or resolved

                        List<ReimbursementRequest> requests = null;
                        if (params[3].equalsIgnoreCase("pending")) {
                            requests = requestController.viewPendingRequests(userID);
                        } else if (params[3].equalsIgnoreCase("resolved")) {
                            requests = requestController.viewResolvedRequests(userID);
                        } else {
                            requests = requestController.viewRequests(userID);
                        }
                        if (requests == null) {
                            log.warn("Failed to retrieve reimbursement requests for an authorized employee");
                            resp.getWriter().println("No reimbursement requests found");
                            resp.setStatus(404);    // reimbursement requests not found
                        } else {
                            log.debug("Retrieved reimbursement requests for an employee");
                            resp.setContentType("application/json");
                            RequestArray arr = new RequestArray(requests);
                            resp.getWriter().write(om.writeValueAsString(arr));
                            resp.setStatus(200);    // reimbursement requests found
                        }
                    } else {
                        try {
                            // employees/(userID)/requests/(requestID)

                            int requestID = Integer.parseInt(params[3]);
                            ReimbursementRequest r = requestController.viewRequest(requestID);
                            if (r == null) {
                                log.debug("The reimbursement request with id '" + requestID + "' was not found");
                                resp.getWriter().println("Reimbursement request not found");
                                resp.setStatus(404);    // reimbursement request not found
                            } else if (r.getSubmitterID() != userID) {
                                log.info("Authorized user requested a reimbursement request belonging to another user");
                                resp.getWriter().println("Reimbursement request does not belong to this user");
                                resp.setStatus(403);    // reimbursement request does not belong to this user
                            } else {
                                log.debug("Retrieved a reimbursement request for an employee");
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(r));
                                resp.setStatus(200);    // reimbursement request found
                            }
                        } catch (NumberFormatException e) {
                            log.info("An invalid request was made for a reimbursement request with specified id '" + params[3] + "'");
                            resp.getWriter().println("Invalid request ID entered");
                            resp.setStatus(400);
                        }
                    }
                } else {
                    log.info("A GET request was made with too many parameters");
                    resp.setStatus(404);
                }
            } else {
                log.info("An invalid GET request was made");
                resp.setStatus(404);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            log.info("An invalid GET request was made");
            resp.setStatus(404);
        }
    }


    /**
     *  POST requests are used on endpoints:
     *      /employees/(userID)/requests/new            |   submit a new request
     *      /employees/(userID)/requests/logout         |   logout
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("EmployeeServlet doPost called with: " + req.getPathInfo());

        String[] params = req.getPathInfo().split("/");

        // validate the user
        int userID = validate(req, resp);
        if (userID < 0) {
            log.info("Unauthorized request made to EmployeeServlet doPost with " + req.getPathInfo());
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the post request
        try {
            if (params.length == 3 && params[2].equalsIgnoreCase("logout")) {
                // employees/(userID)/logout

                log.debug("Employee logged out, redirecting to /login");
                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            } else if (params.length == 4 && params[2].equalsIgnoreCase("requests") && params[3].equalsIgnoreCase("new")) {
                // employees/(userID)/requests/new

                // Get reimbursement request input
                ReimbursementInput input;
                try {
                    input = om.readValue(req.getInputStream(), ReimbursementInput.class);
                } catch (IOException e) {
                    resp.getWriter().write("Invalid reimbursement request parameters");
                    resp.setStatus(400);
                    return;
                }

                try {
                    int requestID = requestController.submitNewRequest(userID, input.amount, input.category, input.description);
                    if (requestID < 0) {
                        log.warn("Failed to add a valid new request");
                        resp.getWriter().println("Failed to add the request");
                        resp.setStatus(500);    // failed to add new request
                    } else {
                        log.info("Successfully added a new request");
                        ReimbursementRequest request = requestController.viewRequest(requestID);
                        resp.setContentType("application/json");
                        resp.getWriter().write(om.writeValueAsString(request));
                        resp.setStatus(201);    // new request added successfully
                    }
                } catch (RuntimeException e) {
                    log.info("An invalid reimbursement request was not added");
                    resp.setContentType("plain/text");
                    resp.getWriter().println(e.getMessage());
                    resp.setStatus(409);    // input was invalid
                }
            } else {
                log.info("An invalid POST request was made");
                resp.setStatus(404);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            log.info("An invalid POST request was made");
            resp.setStatus(404);    // invalid request
        }
    }

    /**
     *  PUT requests are used on endpoints:
     *      /employees/(userID)/update                  | update profile
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("EmployeeServlet doPut called with: " + req.getPathInfo());

        String[] params = req.getPathInfo().split("/");

        // validate the user
        int userID = validate(req, resp);
        if (userID < 0) {
            log.info("Unauthorized request made to EmployeeServlet doPut with " + req.getPathInfo());
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the put request
        try {
            if (params.length == 3 && params[2].equalsIgnoreCase("update")) {
                // employees/(userID)/update

                // Get new profile info
                Employee updated = om.readValue(req.getInputStream(), Employee.class);
                try {
                    boolean success = employeeController.updateProfile(userID, updated.getPassword(), updated.getFirstName(), updated.getLastName(), updated.getDob(), updated.getEmail());
                    if (success) {
                        log.debug("Employee profile successfully updated");
                        Employee e = employeeController.getProfile(userID);
                        resp.setContentType("application/json");
                        resp.getWriter().write(om.writeValueAsString(e));
                        resp.setStatus(200);    // profile was successfully updated
                    } else {
                        log.warn("Failed to update employee profile");
                        resp.setStatus(400);    // profile was not updated
                    }
                } catch (RuntimeException e) {
                    log.info("Invalid values provided when updating employee profile");
                    resp.setContentType("plain/text");
                    resp.getWriter().println(e.getMessage());
                    resp.setStatus(409);        // input was invalid
                }
            } else {
                log.info("An invalid PUT request was made");
                resp.setStatus(404);
            }
        } catch (IndexOutOfBoundsException | IOException e) {
            log.info("An invalid PUT request was made");
            resp.setStatus(404);    // invalid request
        }
    }
}