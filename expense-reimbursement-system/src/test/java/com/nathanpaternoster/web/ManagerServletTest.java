package com.nathanpaternoster.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nathanpaternoster.controllers.ManagerController;
import com.nathanpaternoster.controllers.ReimbursementRequestController;
import com.nathanpaternoster.models.requests.ReimbursementRequest;
import com.nathanpaternoster.models.users.Employee;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagerServletTest {
    private testingServlet testServlet;
    private ManagerController testManCon;
    private ReimbursementRequestController testReqCon;
    private HttpServletRequest testRequest;
    private HttpServletResponse testResponse;
    private testingWriter testWriter;
    private ObjectMapper om;
    private ReimbursementRequest testReimbRequest;
    private Employee testEmployee;

    @Before
    public void setUp() throws Exception {
        testManCon = Mockito.mock(ManagerController.class);
        testReqCon = Mockito.mock(ReimbursementRequestController.class);
        om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        testServlet = new testingServlet(testManCon, testReqCon, om);
        testWriter = new testingWriter(System.out);

        testReimbRequest = new ReimbursementRequest(
                999,
                34,
                65,
                500,
                "Food",
                "burgers",
                LocalDateTime.parse("2022-01-01T12:42:47"),
                LocalDateTime.parse("2022-02-02T07:14:58"),
                "Pending");
        testEmployee = new Employee(
                777,
                "password123",
                "emp",
                "last",
                LocalDate.now(),
                "email",
                null);

        testRequest = Mockito.mock(HttpServletRequest.class);
        testResponse = Mockito.mock(HttpServletResponse.class);
        HttpSession testSession = Mockito.mock(HttpSession.class);
        Mockito.when(testRequest.getSession(Mockito.anyBoolean())).thenReturn(testSession);
        Mockito.when(testSession.getAttribute("user")).thenReturn(123);
        Mockito.when(testResponse.getWriter()).thenReturn(testWriter);
    }
    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getRequest() throws ServletException, IOException {
        Mockito.when(testRequest.getPathInfo()).thenReturn("/123/requests/999");
        Mockito.when(testReqCon.viewRequest(999)).thenReturn(testReimbRequest);
        testServlet.doGet(testRequest, testResponse);
        String actual = testWriter.data;
        String expected = om.writeValueAsString(testReimbRequest);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getEmployee() throws ServletException, IOException {
        Mockito.when(testRequest.getPathInfo()).thenReturn("/123/employees/777");
        Mockito.when(testManCon.viewEmployee(777)).thenReturn(testEmployee);
        testServlet.doGet(testRequest, testResponse);
        String actual = testWriter.data;
        String expected = om.writeValueAsString(testEmployee);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getAllEmployees() throws ServletException, IOException {
        Mockito.when(testRequest.getPathInfo()).thenReturn("/123/employees/all");
        List<Employee> employees = Collections.singletonList(testEmployee);
        ManagerServlet.EmployeeArray arr = new ManagerServlet.EmployeeArray(employees);
        String expected = om.writeValueAsString(arr);

        Mockito.when(testManCon.viewAllEmployees()).thenReturn(employees);
        testServlet.doGet(testRequest, testResponse);
        String actual = testWriter.data;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getAllPendingRequests() throws ServletException, IOException {
        Mockito.when(testRequest.getPathInfo()).thenReturn("/123/requests/pending");
        List<ReimbursementRequest> requests = Collections.singletonList(testReimbRequest);
        ManagerServlet.RequestArray arr = new ManagerServlet.RequestArray(requests);
        String expected = om.writeValueAsString(arr);

        Mockito.when(testReqCon.viewAllPendingRequests()).thenReturn(requests);
        testServlet.doGet(testRequest, testResponse);
        String actual = testWriter.data;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getAllEmployeeRequests() throws IOException, ServletException {
        Mockito.when(testRequest.getPathInfo()).thenReturn("/123/requests/employee/555");
        List<ReimbursementRequest> requests = new ArrayList<>();
        ManagerServlet.RequestArray arr = new ManagerServlet.RequestArray(requests);
        String expected = om.writeValueAsString(arr);

        Mockito.when(testReqCon.viewRequests(Mockito.anyInt())).thenReturn(requests);
        testServlet.doGet(testRequest, testResponse);
        String actual = testWriter.data;
        Assert.assertEquals(expected, actual);
    }
}

class testingWriter extends PrintWriter {
    public String data;
    testingWriter(OutputStream stream) {
        super(stream);
    }
    public void write(String passThrough) {
        data = passThrough;
    }
}

class testingServlet extends ManagerServlet {
    public testingServlet(ManagerController manCon, ReimbursementRequestController reqCon, ObjectMapper om) {
        managerController = manCon;
        requestController = reqCon;
        this.om = om;
    }
}