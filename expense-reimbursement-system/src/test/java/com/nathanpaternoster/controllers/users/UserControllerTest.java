package com.nathanpaternoster.controllers.users;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.nathanpaternoster.services.UserDAO;

import java.time.LocalDate;

public class UserControllerTest {
    private UserController testContoller;
    private UserDAO testDAO;

    @Before
    public void setUp() throws Exception {
        testDAO = Mockito.mock(UserDAO.class);
        testContoller = new UserController(testDAO);
    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLogin() {
        Mockito.when(testDAO.authenticateUser(Mockito.anyInt(), Mockito.anyString())).thenReturn(null);
        assertNull(testContoller.login(1, ""));
    }

    @Test
    public void testValidateName() {
        String[] validNames = new String[]{"Robert", "1", "Mary Ann", "alsekb2", "M M M M"};
        String[] invalidNames = new String[]{"", "   ", " Robert", "A     B", "123 ", null, "abc!"};
        for (String name : validNames) {
            testContoller.validateName(name, name); // should not throw
        }
        for (String name: invalidNames) {
            assertThrows(RuntimeException.class, () -> testContoller.validateName(name, name));
        }
    }

    @Test
    public void testValidatePassword() {
        String[] validPasswords = new String[]{"afsdfsad1", "la2L$  fasd", "aaaa5aaa", "43541312%a"};
        String[] invalidPasswords = new String[]{"", "         ", "aaaaaaaaaa", "1111111111", "Abcdefgh*#"};
        for (String pass : validPasswords) {
            testContoller.validatePassword(pass); // should not throw
        }
        for (String pass: invalidPasswords) {
            assertThrows(RuntimeException.class, () -> testContoller.validatePassword(pass));
        }
    }

    @Test
    public void testValidateEmail() {
        Mockito.when(testDAO.emailIsAvailable(Mockito.anyString())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> testContoller.validateEmail("inuse"));
    }

    @Test
    public void testValidateDOB() {
        LocalDate invalid = LocalDate.of(3000, 1, 1);
        assertThrows(RuntimeException.class, () -> testContoller.validateDOB(invalid));

        testContoller.validateDOB(null);    // null is acceptable
    }
}