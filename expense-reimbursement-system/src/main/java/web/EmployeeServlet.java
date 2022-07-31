package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controllers.EmployeeController;
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

@WebServlet("/employee/*")
public class EmployeeServlet extends HttpServlet {
    private ObjectMapper om;
    private EmployeeController employeeController;
    private ReimbursementRequestController requestController;

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
                // employee/(userID)

                resp.setStatus(200);
                resp.setContentType("text/plain");
                resp.getOutputStream().println("Successfully logged in, enter /profile, /update, /requests, or /logout");
            } else if (params.length == 3 && params[2].equalsIgnoreCase("profile")) {
                // employee/(userID)/profile

                Employee e = employeeController.getProfile(userID);
                if (e == null) {
                    resp.setStatus(404);    // employee profile not found
                } else {
                    resp.setStatus(200);    // employee profile found
                    resp.setContentType("application/json");
                    resp.getWriter().write(om.writeValueAsString(e));
                }
            } else if (params[2].equalsIgnoreCase("requests")) {
                if (params.length == 3) {
                    // employee/(userID)/requests

                    resp.setStatus(200);
                    resp.setContentType("text/plain");
                    resp.getOutputStream().println("Viewing requests, enter /all, /pending, /resolved, or /(requestID)");
                } else if (params.length == 4) {
                    if (params[3].equalsIgnoreCase("pending") || params[3].equalsIgnoreCase("resolved") || params[3].equalsIgnoreCase("all")) {
                        // employee/(userID)/requests/all or employee/(userID)/requests/pending or employee/(userID)/requests/resolved

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
                            // employee/(userID)/requests/(requestID)

                            int requestID = Integer.parseInt(params[3]);
                            ReimbursementRequest r = requestController.viewRequest(requestID);
                            if (r == null) {
                                resp.setStatus(404);    // reimbursement request not found
                            } else if (r.getSubmitterID() != userID) {
                                resp.setStatus(403);    // reimbursement request does not belong to this user
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
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
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
                // employee/(userID)/logout

                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            } else if (params.length == 4 && params[2].equalsIgnoreCase("requests") && params[3].equalsIgnoreCase("new")) {
                // employee/(userID)/requests/new

                // Get reimbursement request input
                ReimbursementRequest reimbRequest = om.readValue(req.getInputStream(), ReimbursementRequest.class);
                System.out.println(reimbRequest.getAmount());
                System.out.println(reimbRequest.getCategory());
                System.out.println(reimbRequest.getDescription());

                int requestID = requestController.submitNewRequest(userID, reimbRequest.getAmount(), reimbRequest.getCategory(), reimbRequest.getDescription());
                if (requestID < 0) {
                    resp.setStatus(400);    // failed to add new request
                } else {
                    resp.setStatus(201);    // new request added successfully
                    ReimbursementRequest request = requestController.viewRequest(requestID);
                    resp.setContentType("application/json");
                    resp.getWriter().write(om.writeValueAsString(request));
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
            if (params.length == 3 && params[2].equalsIgnoreCase("update")) {
                // employee/(userID)/update

                // Get new profile info
                Employee updated = om.readValue(req.getInputStream(), Employee.class);
                try {
                    boolean success = employeeController.updateProfile(userID, updated.getPassword(), updated.getFirstName(), updated.getLastName(), updated.getDob(), updated.getEmail());
                    if (!success) {
                        resp.setStatus(400);
                    } else {
                        resp.setStatus(200);
                        Employee e = employeeController.getProfile(userID);
                        resp.setContentType("application/json");
                        resp.getWriter().write(om.writeValueAsString(e));
                    }
                } catch (RuntimeException e) {
                    resp.setStatus(409);    // entered email is not available
                    resp.setContentType("plain/text");
                    resp.getOutputStream().println(e.getMessage());
                }
            } else {
                resp.setStatus(400);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            resp.setStatus(400);    // invalid request
        }
    }
}