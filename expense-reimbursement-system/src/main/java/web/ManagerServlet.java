package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controllers.ManagerController;
import controllers.ReimbursementRequestController;
import models.requests.ReimbursementRequest;
import models.users.Employee;
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

@WebServlet("/manager/*")
public class ManagerServlet extends HttpServlet {
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
    }

    private int getUserID(HttpServletRequest req, HttpServletResponse resp) {
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        String[] params = path.split("/");

        // validate the user
        int userID = getUserID(req, resp);
        if (userID < 0) {
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the get request
        try {
            if (params.length == 2) {
                // manager/(userID)

                resp.setStatus(200);
                resp.setContentType("text/plain");
                resp.getOutputStream().println("Successfully logged in, enter /employees, /requests, or /logout");
            } else if (params[2].equalsIgnoreCase("employees")) {
                if (params.length == 3) {
                    // manager/(userID)/employees

                    resp.setStatus(200);
                    resp.setContentType("text/plain");
                    resp.getOutputStream().println("Viewing employees, enter /new, /all, or /(userID)");
                } else if (params.length == 4) {
                    if (params[3].equalsIgnoreCase("all")) {
                        // manager/(userID)/employees/all

                        List<Employee> employees = managerController.viewAllEmployees();
                        if (employees == null) {
                            resp.setStatus(404);    // employees not found
                        } else {
                            resp.setStatus(200);    // employees found
                            resp.setContentType("application/json");
                            for (Employee e : employees) {
                                resp.getWriter().write(om.writeValueAsString(e));
                            }
                        }
                    } else {
                        try {
                            // manager/(userID)/employees/(employeeID)

                            int employeeID = Integer.parseInt(params[3]);
                            Employee e = managerController.viewEmployee(employeeID);
                            if (e == null) {
                                resp.setStatus(404);    // employee not found
                            } else {
                                resp.setStatus(200);
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(e));
                            }
                        } catch (NumberFormatException e) {
                            resp.setStatus(400);
                        }
                    }
                } else {
                    resp.setStatus(400);
                }
            } else if (params[2].equalsIgnoreCase("requests")) {
                if (params.length == 3) {
                    // manager/(userID)/requests

                    resp.setStatus(200);
                    resp.setContentType("text/plain");
                    resp.getOutputStream().println("Viewing requests, enter /all, /pending, /resolved, /(requestID), or /(requestID)/update");
                } else if (params.length == 4) {
                    if (params[3].equalsIgnoreCase("all") || params[3].equalsIgnoreCase("pending") || params[3].equalsIgnoreCase("resolved")) {
                        // manager/(userID)/requests/all or manager/(userID)/requests/pending or manager/(userID)/requests/resolved

                        List<ReimbursementRequest> requests = null;
                        if (params[3].equalsIgnoreCase("pending")) {
                            requests = requestController.viewPendingRequests(userID);
                        } else if (params[3].equalsIgnoreCase("resolved")) {
                            requests = requestController.viewResolvedRequests(userID);
                        } else {
                            requests = requestController.viewRequests(userID);
                        }
                        if (requests == null) {
                            resp.setStatus(404);    // reimbursement requests not found
                        } else {
                            resp.setStatus(200);    // reimbursement requests found
                            resp.setContentType("application/json");
                            for (ReimbursementRequest r : requests) {
                                resp.getWriter().write(om.writeValueAsString(r));
                            }
                        }
                    } else {
                        try {
                            // manager/(userID)/requests/(requestID)

                            int requestID = Integer.parseInt(params[3]);
                            ReimbursementRequest r = requestController.viewRequest(requestID);
                            if (r == null) {
                                resp.setStatus(404);    // reimbursement request nto found
                            } else {
                                resp.setStatus(200);    // reimbursement request found
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(r));
                            }
                        } catch (NumberFormatException e) {
                            resp.setStatus(400);
                        }
                    }
                } else {
                    resp.setStatus(400);
                }
            } else {
                resp.setStatus(400);
            }
        } catch (IndexOutOfBoundsException e) {
            resp.setStatus(400);    // invalid request
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        String[] params = path.split("/");

        // validate the user
        int userID = getUserID(req, resp);
        if (userID < 0) {
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the post request
        try {
            if (params.length == 3 && params[2].equalsIgnoreCase("logout")) {
                // manager/(userID)/logout

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
                        resp.setStatus(400);
                    } else {
                        resp.setStatus(200);
                        Employee newEmployee = managerController.viewEmployee(employeeID);
                        resp.setContentType("application/json");
                        resp.getWriter().write(om.writeValueAsString(newEmployee));
                    }
                } catch (RuntimeException ex) {
                    resp.setStatus(409);    // entered email is not available
                    resp.setContentType("plain/text");
                    resp.getOutputStream().println(ex.getMessage());
                }
            } else {
                resp.setStatus(400);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            resp.setStatus(400);    // invalid request
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        String[] params = path.split("/");

        // validate the user
        int userID = getUserID(req, resp);
        if (userID < 0) {
            resp.setStatus(403);        // unauthorized
            return;
        }

        // handle the put request
        try {
            if (params.length == 5 && params[2].equalsIgnoreCase("requests") && params[4].equalsIgnoreCase("update")) {
                try {
                    // manager/(userID)/requests/(requestID)/update

                    int requestID = Integer.parseInt(params[3]);
                    ReimbursementRequest r = requestController.viewRequest(requestID);
                    if (r == null) {
                        resp.setStatus(404);    // request not found
                    } else if (!r.getStatus().equalsIgnoreCase("Pending")) {
                        resp.setStatus(409);    // request has already been resolved
                        resp.setContentType("plain/text");
                        resp.getOutputStream().println("The specified request has already been resolved");
                    } else {
                        String resolution = om.readValue(req.getInputStream(), String.class);
                        if (resolution.equalsIgnoreCase("Approved") || resolution.equalsIgnoreCase("Denied")) {
                            if (resolution.equalsIgnoreCase("Approved")) resolution = "Approved";
                            else resolution = "Denied";
                            if (requestController.resolveRequest(requestID, resolution)) {
                                resp.setStatus(400);
                            } else {
                                resp.setStatus(200);
                                ReimbursementRequest updated = requestController.viewRequest(requestID);
                                resp.setContentType("application/json");
                                resp.getWriter().write(om.writeValueAsString(updated));
                            }
                        } else {
                            resp.setStatus(400);
                            resp.setContentType("plain/text");
                            resp.getOutputStream().println("You must enter 'Approved' or 'Denied'");
                        }
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                }
            } else {
                resp.setStatus(400);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            resp.setStatus(400);    // invalid request
        }
    }
}
