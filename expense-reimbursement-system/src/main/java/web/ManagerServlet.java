package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controllers.ManagerController;
import controllers.ReimbursementRequestController;
import models.requests.ReimbursementRequest;
import models.users.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import services.RequestDAO;
import services.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/managers/*")
public class ManagerServlet extends HttpServlet {
    private static final Logger log = LogManager.getLogger(ManagerServlet.class.getName());
    private ObjectMapper om;
    private ManagerController managerController;
    private ReimbursementRequestController requestController;

    @Override
    public void init() {
        om = new ObjectMapper();

        // register the object mapper to display Java 8 dates
        om.registerModule(new JavaTimeModule());
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        om.setDateFormat(df);

        // get the DAOs and initialize the controllers
        UserDAO userDao = (UserDAO) getServletContext().getAttribute("userDAO");
        RequestDAO requestDAO = (RequestDAO) getServletContext().getAttribute("requestDAO");
        managerController = new ManagerController(userDao);
        requestController = new ReimbursementRequestController(requestDAO);
        log.debug("ManagerServlet initialized");
    }

    /**
     * Validate that the manager specified by userID in the url parameters is currently logged in (by comparing with the HTTPSession attribute)
     * @return the userID of the manager, or -1 if the manager is not logged in or the url is malformed
     */
    private int validate(HttpServletRequest req, HttpServletResponse resp) {
        String path = req.getPathInfo();
        String[] params = path.split("/");
        int userID = -1;
        try {
            userID = Integer.parseInt(params[1]);
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
     *      /managers/(userID)                                  |   display welcome and options
     *      /managers/(userID)/employees                        |   display employee options
     *      /managers/(userID)/employees/(employeeID)           |   display specified employee's profile
     *      /managers/(userID)/employees/all                    |   display all employees
     *      /managers/(userID)/requests                         |   display request options
     *      /managers/(userID)/requests/(requestID)             |   display specified request
     *      /managers/(userID)/requests/all                     |   display all requests
     *      /managers/(userID)/requests/pending                 |   display pending requests
     *      /managers/(userID)/requests/resolved                |   display resolved requests
     *      /managers/(userID)/requests/employee/(employeeID)   |   display all requests belonging to specified employee
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("ManagerServlet doGet called with: " + req.getPathInfo());

        String[] params = req.getPathInfo().split("/");

        // validate the user
        int userID = validate(req, resp);
        if (userID < 0) {
            log.info("Unauthorized request made to ManagerServlet doGet with " + req.getPathInfo());
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the get request
        try {
            if (params.length == 2) {
                // manager/(userID)

                resp.setContentType("text/plain");
                resp.getWriter().println("Successfully logged in!");
                resp.getWriter().println("Enter /employees, /requests, or /logout");
                resp.setStatus(200);
            } else if (params[2].equalsIgnoreCase("employees")) {
                if (params.length == 3) {
                    // manager/(userID)/employees

                    resp.setContentType("text/plain");
                    resp.getWriter().println("Viewing employees");
                    resp.getWriter().println("Enter /new, /all, or /(userID)");
                    resp.setStatus(200);
                } else if (params.length == 4) {
                    if (params[3].equalsIgnoreCase("all")) {
                        // manager/(userID)/employees/all

                        List<Employee> employees = managerController.viewAllEmployees();
                        if (employees == null) {
                            log.warn("Failed to retrieve all employees for an authorized manager");
                            resp.setStatus(404);    // employees not found
                        } else {
                            log.debug("Retrieved all employees for an authorized manager");
                            resp.setContentType("application/json");
                            for (Employee e : employees) {
                                resp.getWriter().write(om.writeValueAsString(e));
                            }
                            resp.setStatus(200);    // employees found
                        }
                    } else {
                        try {
                            // manager/(userID)/employees/(employeeID)

                            int employeeID = Integer.parseInt(params[3]);
                            Employee e = managerController.viewEmployee(employeeID);
                            if (e == null) {
                                log.debug("Employee with id '" + employeeID + "' was not found");
                                resp.setStatus(404);    // employee not found
                            } else {
                                log.debug("Retrieved an employee's profile for a manager");
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(e));
                                resp.setStatus(200);    // employee found
                            }
                        } catch (NumberFormatException e) {
                            log.info("An invalid request was made for an employee profile with specified id '" + params[3] + "'");
                            resp.setStatus(400);        // invalid employeeID entered
                        }
                    }
                } else {
                    log.info("A GET request was made with too many parameters");
                    resp.setStatus(400);
                }
            } else if (params[2].equalsIgnoreCase("requests")) {
                if (params.length == 3) {
                    // manager/(userID)/requests

                    resp.setContentType("text/plain");
                    resp.getWriter().println("Viewing requests");
                    resp.getWriter().println("Enter /all, /pending, /resolved, /(requestID), or update/(requestID)");
                    resp.setStatus(200);
                } else if (params.length == 4) {
                    if (params[3].equalsIgnoreCase("all") || params[3].equalsIgnoreCase("pending") || params[3].equalsIgnoreCase("resolved")) {
                        // manager/(userID)/requests/all or manager/(userID)/requests/pending or manager/(userID)/requests/resolved

                        List<ReimbursementRequest> requests = null;
                        if (params[3].equalsIgnoreCase("pending")) {
                            requests = requestController.viewAllPendingRequests();
                        } else if (params[3].equalsIgnoreCase("resolved")) {
                            requests = requestController.viewAllResolvedRequests();
                        } else {
                            requests = requestController.viewAllRequests();
                        }
                        if (requests == null) {
                            log.warn("Failed to retrieve requests for an authorized manager");
                            resp.setStatus(404);    // reimbursement requests not found
                        } else {
                            log.debug("Retrieved reimbursement requests for an employee");
                            resp.setContentType("application/json");
                            for (ReimbursementRequest r : requests) {
                                resp.getWriter().write(om.writeValueAsString(r));
                            }
                            resp.setStatus(200);    // reimbursement requests found
                        }
                    } else {
                        try {
                            // manager/(userID)/requests/(requestID)

                            int requestID = Integer.parseInt(params[3]);
                            ReimbursementRequest r = requestController.viewRequest(requestID);
                            if (r == null) {
                                log.debug("The reimbursement request with id '" + requestID + "' was not found");
                                resp.setStatus(404);    // reimbursement request not found
                            } else {
                                log.debug("Retrieved a reimbursement request for a manager");
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(r));
                                resp.setStatus(200);    // reimbursement request found
                            }
                        } catch (NumberFormatException e) {
                            log.info("An invalid request was made for a reimbursement request with specified id '" + params[3] + "'");
                            resp.setStatus(400);        // invalid requestID entered
                        }
                    }
                } else if (params.length == 5 && params[3].equalsIgnoreCase("employee")) {
                    try {
                        // manager/(userID)/requests/employee/(employeeID)

                        int employeeID = Integer.parseInt(params[4]);
                        List<ReimbursementRequest> requests = requestController.viewRequests(employeeID);
                        if (requests == null) {
                            log.warn("Failed to retrieve an employee's reimbursement requests for an authorized manager");
                            resp.setStatus(404);        // reimbursement requests not found
                        } else {
                            log.debug("Retrieved an employee's reimbursement requests for a manager");
                            resp.setContentType("application/json");
                            for (ReimbursementRequest r : requests) {
                                resp.getWriter().write(om.writeValueAsString(r));
                            }
                            resp.setStatus(200);        // reimbursement requests found
                        }
                    } catch (NumberFormatException e) {
                        log.info("An invalid request was made for an employee's reimbursement requests with the specified employee id '" + params[4] + "'");
                        resp.setStatus(400);        // invalid employeeID entered
                    }
                } else {
                    log.info("A GET request was made with too many parameters");
                    resp.setStatus(400);
                }
            } else {
                log.info("An invalid GET request was made");
                resp.setStatus(400);
            }
        } catch (IndexOutOfBoundsException e) {
            log.info("An invalid GET request was made");
            resp.setStatus(400);    // invalid get request url
        }
    }

    /**
     *  POST requests are used on endpoints:
     *      /managers/(userID)/employees/new                    |   add a new employee
     *      /managers/(userID)/logout                           |   logout
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("ManagerServlet doPost called with: " + req.getPathInfo());

        String[] params = req.getPathInfo().split("/");

        // validate the user
        int userID = validate(req, resp);
        if (userID < 0) {
            log.info("Unauthorized request made to ManagerServlet doPost with " + req.getPathInfo());
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the post request
        try {
            if (params.length == 3 && params[2].equalsIgnoreCase("logout")) {
                // manager/(userID)/logout

                log.debug("Manager logged out, redirecting to /login");
                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            } else if (params.length == 4 && params[2].equalsIgnoreCase("employees") && params[3].equalsIgnoreCase("new")) {
                // manager/(userID)/employees/new

                // Get employee input
                Employee e = om.readValue(req.getInputStream(), Employee.class);
                try {
                    int employeeID = managerController.addNewEmployee(e.getPassword(), e.getFirstName(), e.getLastName(), e.getDob(), e.getEmail());
                    if (employeeID < 1) {
                        log.warn("Failed to add a valid new employee");
                        resp.setStatus(500);    // failed to add the new employee
                    } else {
                        log.info("Successfully added a new employee");
                        Employee newEmployee = managerController.viewEmployee(employeeID);
                        resp.setContentType("application/json");
                        resp.getWriter().write(om.writeValueAsString(newEmployee));
                        resp.setStatus(200);    // successfully added the new employee
                    }
                } catch (RuntimeException ex) {
                    log.info("An invalid employee profile was not added");
                    resp.setContentType("plain/text");
                    resp.getWriter().println(ex.getMessage());
                    resp.setStatus(409);        // entered email is not available
                }
            } else {
                log.info("An invalid POST request was made");
                resp.setStatus(400);    // invalid post request url
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            log.info("An invalid POST request was made");
            resp.setStatus(400);    // invalid post request url
        }
    }

    /**
     *  PUT requests are used on endpoints:
     *      /managers/(userID)/requests/update/(requestID)      |   approve or deny a request
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("ManagerServlet doPut called with: " + req.getPathInfo());

        String[] params = req.getPathInfo().split("/");

        // validate the user
        int userID = validate(req, resp);
        if (userID < 0) {
            log.info("Unauthorized request made to ManagerServlet doPut with " + req.getPathInfo());
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the put request
        try {
            if (params.length == 5 && params[2].equalsIgnoreCase("requests") && params[3].equalsIgnoreCase("update")) {
                try {
                    // manager/(userID)/requests/update/(requestID)

                    int requestID = Integer.parseInt(params[4]);
                    ReimbursementRequest r = requestController.viewRequest(requestID);
                    if (r == null) {
                        log.debug("The reimbursement request with id '" + requestID + "' was not found");
                        resp.setStatus(404);    // request not found
                    } else if (!r.getStatus().equalsIgnoreCase("Pending")) {
                        log.info("An attempt was made to resolve a reimbursement request that has already been resolved");
                        resp.setContentType("plain/text");
                        resp.getWriter().println("The specified request has already been resolved");
                        resp.setStatus(409);    // request has already been resolved
                    } else {
                        try {
                            boolean resolution = om.readValue(req.getInputStream(), boolean.class);
                            if (requestController.resolveRequest(userID, requestID, resolution)) {
                                log.debug("Reimbursement request successfully resolved");
                                ReimbursementRequest updated = requestController.viewRequest(requestID);
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(updated));
                                resp.setStatus(200);    // successfully updated the reimbursement request status
                            } else {
                                log.warn("Failed to update a reimbursement request");
                                resp.setStatus(500);    // failed to update the reimbursement request status
                            }
                        } catch (IOException e) {
                            log.info("Attempted to resolve a reimbursement request with a non-boolean value");
                            resp.setStatus(400);    // invalid input
                        }
                    }
                } catch (NumberFormatException e) {
                    log.info("An invalid request was made for a reimbursement request with specified id '" + params[3] + "'");
                    resp.setStatus(400);    // invalid requestID entered
                }
            } else {
                log.info("An invalid PUT request was made");
                resp.setStatus(400);        // invalid put request url
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            log.info("An invalid PUT request was made");
            resp.setStatus(400);    // invalid put request url
        }
    }
}