package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.UserController;
import models.users.Employee;
import models.users.Manager;
import models.users.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import services.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet to process login requests and redirect when successful
 */
@WebServlet("/login")
public class AuthenticationServlet extends HttpServlet {
    private static final Logger log = LogManager.getLogger(AuthenticationServlet.class.getName());
    private ObjectMapper om;
    private UserController userController;

    static class LoginRequest {
        public int userID;
        public String password;
    }

    @Override
    public void init() throws ServletException {
        om = new ObjectMapper();
        UserDAO userDao = (UserDAO) getServletContext().getAttribute("userDAO");
        userController = new UserController(userDao);
        log.debug("AuthenticationServlet initialized");
    }

    /**
     * Prompt for login credentials
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("plain/text");
        resp.getWriter().println("Please input your employeeID and password");
        log.debug("AuthenticationServlet doGet called");
    }

    /**
     * Process a login request
     * When a user is authenticated:
     *      - create an HTTPSession and set a session attribute "user" containing their userID
     *      - redirect to either /employees/(userID) or /managers/(userID)
     * When an authentication fails:
     *      - invalidate the current HTTPSession
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("AuthenticationServlet doPost called with: " + req.getPathInfo());

        // Get client login input
        LoginRequest loginRequest;
        try {
            loginRequest = om.readValue(req.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            resp.getWriter().println("Invalid login parameters");
            resp.setStatus(400);
            return;
        }

        // Authenticate login
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        User user = userController.login(loginRequest.userID, loginRequest.password);
        if (user != null) {
            session = req.getSession();
            session.setAttribute("user", user.getUserID());
            session.setMaxInactiveInterval(30*60);  // session expires in 30 minutes
            if (user instanceof Employee) {
                String path = req.getContextPath() + "/employees/" + user.getUserID();
                log.info("Employee authentication succeeded, redirecting to " + path);
                resp.sendRedirect(path);
                return;
            } else if (user instanceof Manager) {
                String path = req.getContextPath() + "/managers/" + user.getUserID();
                log.info("Manager authentication succeeded, redirecting to " + path);
                resp.sendRedirect(path);
                return;
            } else {
                session.invalidate();
                log.warn("Authentication succeeded for an unknown user type");
                resp.setContentType("plain/text");
                resp.getWriter().println("Invalid user type");
                resp.setStatus(500);    // authenticated user is not a recognized type
            }
        } else {
            log.info("Authentication attempt failed");
            resp.setContentType("plain/text");
            resp.getWriter().println("Invalid username or password entered");
            resp.setStatus(401);        // invalid login credentials
        }
    }
}