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
        System.out.println("Login doGet called");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("DoPost called on LoginServlet");

        // Get client login input
        LoginRequest loginRequest = om.readValue(req.getInputStream(), LoginRequest.class);
        System.out.println(loginRequest.getUserID());
        System.out.println(loginRequest.getPassword());

        // Authenticate login
        User user = userController.login(loginRequest.getUserID(), loginRequest.getPassword());
        HttpSession session = req.getSession(false);
        System.out.println("Got user");
        if (session != null) session.invalidate();
        if (user != null) {
            System.out.println("User is not null");
            session = req.getSession();
            session.setAttribute("user", user.getUserID());
            System.out.println("Setting user attribute to: " + user.getUserID());
            session.setMaxInactiveInterval(30*60);  // session expires in 30 minutes
            if (user instanceof Employee) {
                String path = req.getContextPath() + "/employee/" + user.getUserID();
                System.out.println("Redirecting to: " + path);
                resp.sendRedirect(path);
                return;
            } else if (user instanceof Manager) {
                String path = req.getContextPath() + "/manager/" + user.getUserID();
                resp.sendRedirect(path);
                return;
            } else {
                // invalid login type
                session.invalidate();
                // report error
            }
        } else {
            // Report outcome to client
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