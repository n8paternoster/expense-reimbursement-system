package com.nathanpaternoster.controllers.users;

import static org.junit.Assert.*;

import com.nathanpaternoster.models.users.Employee;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.nathanpaternoster.services.UserDAO;

import java.time.LocalDate;

public class EmployeeControllerTest {
    private EmployeeController testController;
    private UserDAO testDAO;

    @Before
    public void setUp() throws Exception {
        testDAO = Mockito.mock(UserDAO.class);
        testController = new EmployeeController(testDAO);
    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetProfile() {
        Mockito.when(testDAO.getUser(Mockito.anyInt())).thenReturn(null);
        assertNull(testController.getProfile(1));
    }

    @Test
    public void testUpdateProfile() {
        Mockito.when(testDAO.updateUser(Mockito.any())).thenReturn(true);
        Mockito.when(testDAO.getUser(Mockito.anyInt())).thenReturn(Mockito.mock(Employee.class));
        Mockito.when(testDAO.emailIsAvailable(Mockito.anyString())).thenReturn(true);

        assertTrue(testController.updateProfile(3, "password123", "name", "name", LocalDate.now(), "email"));
    }
}