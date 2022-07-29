package web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="TestServlet", initParams = {})
public class TestServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
        System.out.println("Test servlet servicing");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("Initializing test servlet");
    }

    @Override
    public void destroy() {
        System.out.println("Destroying test servlet");
    }
}
