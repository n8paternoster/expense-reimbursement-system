package com.nathanpaternoster.controllers.users;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.nathanpaternoster.services.UserDAO;

import java.time.LocalDate;

public class ManagerControllerTest {
    private ManagerController testController;
    private UserDAO testDAO;

    @Before
    public void setUp() throws Exception {
        testDAO = Mockito.mock(UserDAO.class);
        testController = new ManagerController(testDAO);
    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddNewEmployee() {
        Mockito.when(testDAO.addNewUser(Mockito.any())).thenReturn(10);
        Mockito.when(testDAO.emailIsAvailable(Mockito.anyString())).thenReturn(true);
        assertEquals(10, testController.addNewEmployee("name", "name", LocalDate.now(), "email"));
        LocalDate invalid = LocalDate.of(3000, 12, 12);
        assertThrows(RuntimeException.class, () -> testController.addNewEmployee("name", "name", invalid, "email"));
    }
}