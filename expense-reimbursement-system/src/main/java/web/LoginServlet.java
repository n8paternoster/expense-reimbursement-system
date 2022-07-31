package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.UserController;
import models.users.Employee;
import models.users.Manager;
import models.users.User;
import services.UserDAO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private ObjectMapper om;
    private UserController userController;

    @Override
    public void init() throws ServletException {
        om = new ObjectMapper();
        UserDAO userDao = (UserDAO) getServletContext().getAttribute("userDAO");
        userController = new UserController(userDao);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doGet called on LoginServlet with: " + req.getPathInfo());

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doPost called on LoginServlet with: " + req.getPathInfo());

        // Get client login input
        LoginRequest loginRequest = om.readValue(req.getInputStream(), LoginRequest.class);
        System.out.println(loginRequest.getUserID());
        System.out.println(loginRequest.getPassword());

        // Authenticate login
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        User user = userController.login(loginRequest.getUserID(), loginRequest.getPassword());
        if (user != null) {
            session = req.getSession();
            session.setAttribute("user", user.getUserID());
            session.setMaxInactiveInterval(30*60);  // session expires in 30 minutes
            if (user instanceof Employee) {
                String path = req.getContextPath() + "/employee/" + user.getUserID();
                System.out.println("Redirecting to: " + path);
                resp.sendRedirect(path);
                return;
            } else if (user instanceof Manager) {
                String path = req.getContextPath() + "/managers/" + user.getUserID();
                System.out.println("Redirecting to " + path);
                resp.sendRedirect(path);
                return;
            } else {
                session.invalidate();
                resp.setContentType("plain/text");
                resp.getWriter().println("Invalid user type");
                resp.setStatus(500);    // authenticated user is not a recognized type
            }
        } else {
            resp.setContentType("plain/text");
            resp.getWriter().println("Invalid username or password entered");
            resp.setStatus(401);        // invalid login credentials
        }
    }
}

class LoginRequest {
    private int userID;
    private String password;
    public LoginRequest() { }
    public LoginRequest(int userID, String password) {
        this.userID = userID;
        this.password = password;
    }
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}