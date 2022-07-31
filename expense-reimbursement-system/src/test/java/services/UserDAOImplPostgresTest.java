package services;

import static org.junit.Assert.*;

import models.users.Employee;
import models.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class UserDAOImplPostgresTest {
    private UserDAOImplPostgres dao;
    private Employee testEmployee;

    @Mock private DataSource dataSource;
    @Mock private Connection con;
    @Mock private PreparedStatement ps;
    @Mock private ResultSet rs;

    @Before
    public void setUp() throws Exception {
        assertNotNull(dataSource);
        dao = new UserDAOImplPostgres(dataSource);
        testEmployee = new Employee(123,
                "password!123",
                "Morgan",
                "Freeman",
                LocalDate.of(1970, 1, 1),
                "mfreeman@gmail.com",
                null);

        Mockito.when(con.prepareStatement(Mockito.anyString())).thenReturn(ps);
        Mockito.when(con.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(ps);
        Mockito.when(dataSource.getConnection()).thenReturn(con);
        Mockito.when(ps.executeQuery()).thenReturn(rs);
        Mockito.when(ps.executeUpdate()).thenReturn(1);
        Mockito.when(ps.getGeneratedKeys()).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true, false);
        Mockito.when(rs.getInt("userID")).thenReturn(testEmployee.getUserID());
        Mockito.when(rs.getString("userType")).thenReturn("Employee");
        Mockito.when(rs.getString("firstName")).thenReturn(testEmployee.getFirstName());
        Mockito.when(rs.getString("lastName")).thenReturn(testEmployee.getLastName());
        Mockito.when(rs.getString("email")).thenReturn(testEmployee.getEmail());
        Mockito.when(rs.getTimestamp("dob")).thenReturn(Timestamp.valueOf(testEmployee.getDob().atStartOfDay()));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAuthenticateUser() {
        User u = dao.authenticateUser(testEmployee.getUserID(), testEmployee.getPassword());
        assertEquals(Employee.class, u.getClass());
        assertEquals(testEmployee.getUserID(), u.getUserID());
        assertEquals("**********", u.getPassword()); // replaced password
        assertEquals(testEmployee.getFirstName(), u.getFirstName());
        assertEquals(testEmployee.getLastName(), u.getLastName());
        assertEquals(testEmployee.getDob(), u.getDob());
        assertEquals(testEmployee.getEmail(), ((Employee) u).getEmail());
        assertNull(((Employee) u).getRequests());   // UserDAO does not query for a user's reimbursement requests
    }

    @Test
    public void testInvalidAuthenticateUser() throws SQLException {
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        assertNull(dao.authenticateUser(-980, "invalidpassword"));
    }

    @Test
    public void testAddNewUser() throws SQLException {
        Mockito.when(rs.getInt(1)).thenReturn(23);  // new user ID = 23
        assertEquals(23, dao.addNewUser(testEmployee));
    }

    @Test
    public void testInvalidAddNewUser() throws SQLException {
        Mockito.when(ps.executeUpdate()).thenThrow(SQLException.class);
        assertEquals(-1, dao.addNewUser(testEmployee));
    }

    @Test
    public void testGetUser() {
        User u = dao.getUser(testEmployee.getUserID());
        assertEquals(Employee.class, u.getClass());
        assertEquals(testEmployee.getUserID(), u.getUserID());
        assertEquals("**********", u.getPassword()); // replaced password
        assertEquals(testEmployee.getFirstName(), u.getFirstName());
        assertEquals(testEmployee.getLastName(), u.getLastName());
        assertEquals(testEmployee.getDob(), u.getDob());
        assertEquals(testEmployee.getEmail(), ((Employee) u).getEmail());
        assertNull(((Employee) u).getRequests()); // UserDAO does not query for a user's reimbursement requests
    }

    @Test
    public void testInvalidGetUser() throws SQLException {
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        assertNull(dao.getUser(testEmployee.getUserID()));
    }

    @Test
    public void testUpdateUser() throws SQLException {
        assertTrue(dao.updateUser(testEmployee));
        Mockito.when(ps.executeUpdate()).thenThrow(SQLException.class);
        assertFalse(dao.updateUser(testEmployee));
    }

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = dao.getAllEmployees();
        assertEquals(1, employees.size());
        Employee e = employees.get(0);
        assertEquals(Employee.class, e.getClass());
        assertEquals(testEmployee.getUserID(), e.getUserID());
        assertEquals("**********", e.getPassword()); // replaced password
        assertEquals(testEmployee.getFirstName(), e.getFirstName());
        assertEquals(testEmployee.getLastName(), e.getLastName());
        assertEquals(testEmployee.getDob(), e.getDob());
        assertEquals(testEmployee.getEmail(), e.getEmail());
        assertNull(e.getRequests()); // UserDAO does not query for a user's reimbursement requests
    }

    @Test
    public void testEmailIsAvailable() throws SQLException {
        Mockito.when(rs.next()).thenReturn(true);
        assertFalse(dao.emailIsAvailable(testEmployee.getEmail()));

        Mockito.when(rs.next()).thenReturn(false);
        assertTrue(dao.emailIsAvailable("fakeemailaddress"));
    }
}