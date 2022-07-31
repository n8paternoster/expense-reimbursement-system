package web;

import services.RequestDAO;
import services.RequestDAOImplPostgres;
import services.UserDAO;
import services.UserDAOImplPostgres;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

public class ContextListener implements ServletContextListener {

    @Resource(name="jdbc/ersDB")
    private DataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        UserDAO userDAO = new UserDAOImplPostgres(dataSource);
        RequestDAO requestDAO = new RequestDAOImplPostgres(dataSource);
        servletContextEvent.getServletContext().setAttribute("userDAO", userDAO);
        servletContextEvent.getServletContext().setAttribute("requestDAO", requestDAO);
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
