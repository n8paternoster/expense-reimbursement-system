package com.nathanpaternoster.web;

import com.nathanpaternoster.services.RequestDAO;
import com.nathanpaternoster.services.RequestDAOImplPostgres;
import com.nathanpaternoster.services.UserDAO;
import com.nathanpaternoster.services.UserDAOImplPostgres;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

/**
 * When the servletContext is initialized, set context variables for a userDAO and requestDAO provide database access to the servlets
 */
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
