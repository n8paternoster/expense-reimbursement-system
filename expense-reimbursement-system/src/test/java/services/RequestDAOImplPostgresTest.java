package services;

import static org.junit.Assert.*;

import models.requests.ReimbursementRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RequestDAOImplPostgresTest {
    private RequestDAOImplPostgres dao;
    private ReimbursementRequest testRequest;

    @Mock private DataSource dataSource;
    @Mock private Connection con;
    @Mock private PreparedStatement ps;
    @Mock private ResultSet rs;

    @Before
    public void setUp() throws Exception {
        assertNotNull(dataSource);
        dao = new RequestDAOImplPostgres(dataSource);
        testRequest = new ReimbursementRequest(56,
                123,
                987,
                5050,
                "Food",
                "Test description",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Pending");
        Mockito.when(con.prepareStatement(Mockito.anyString())).thenReturn(ps);
        Mockito.when(con.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(ps);
        Mockito.when(dataSource.getConnection()).thenReturn(con);
        Mockito.when(ps.executeQuery()).thenReturn(rs);
        Mockito.when(ps.executeUpdate()).thenReturn(1);
        Mockito.when(ps.getGeneratedKeys()).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true, false);
        Mockito.when(rs.getInt("requestID")).thenReturn(testRequest.getRequestID());
        Mockito.when(rs.getInt("submitterID")).thenReturn(testRequest.getSubmitterID());
        Mockito.when(rs.getInt("resolverID")).thenReturn(testRequest.getResolverID());
        Mockito.when(rs.getLong("amount")).thenReturn(testRequest.getAmount());
        Mockito.when(rs.getTimestamp("timeSubmitted")).thenReturn(Timestamp.valueOf(testRequest.getTimeSubmitted()));
        Mockito.when(rs.getTimestamp("timeResolved")).thenReturn(Timestamp.valueOf(testRequest.getTimeResolved()));
        Mockito.when(rs.getString("category")).thenReturn(testRequest.getCategory());
        Mockito.when(rs.getString("description")).thenReturn(testRequest.getDescription());
        Mockito.when(rs.getString("status")).thenReturn(testRequest.getStatus());
    }
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddRequest() throws SQLException {
        Mockito.when(rs.getInt(1)).thenReturn(56);
        assertEquals(56, dao.addRequest(testRequest));
    }

    @Test
    public void testInvalidAddRequest() throws SQLException {
        Mockito.when(ps.executeUpdate()).thenThrow(SQLException.class);
        assertTrue(dao.addRequest(testRequest) < 0);
    }

    @Test
    public void testGetRequest() {
        ReimbursementRequest r = dao.getRequest(testRequest.getRequestID());
        assertEquals(testRequest.getRequestID(), r.getRequestID());
        assertEquals(testRequest.getSubmitterID(), r.getSubmitterID());
        assertEquals(testRequest.getResolverID(), r.getResolverID());
        assertEquals(testRequest.getAmount(), r.getAmount());
        assertEquals(testRequest.getTimeSubmitted(), r.getTimeSubmitted());
        assertEquals(testRequest.getCategory(), r.getCategory());
        assertEquals(testRequest.getDescription(), r.getDescription());
        assertEquals(testRequest.getStatus(), r.getStatus());
    }

    @Test
    public void testInvalidGetRequest() throws SQLException {
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        assertNull(dao.getRequest(testRequest.getRequestID()));
    }

    @Test
    public void testGetPendingRequests() throws SQLException {
        List<ReimbursementRequest> requests = dao.getPendingRequests(123);
        assertEquals(1, requests.size());
        ReimbursementRequest r = requests.get(0);
        assertEquals(testRequest.getRequestID(), r.getRequestID());
        assertEquals(testRequest.getSubmitterID(), r.getSubmitterID());
        assertEquals(testRequest.getResolverID(), r.getResolverID());
        assertEquals(testRequest.getAmount(), r.getAmount());
        assertEquals(testRequest.getTimeSubmitted(), r.getTimeSubmitted());
        assertEquals(testRequest.getCategory(), r.getCategory());
        assertEquals(testRequest.getDescription(), r.getDescription());
        assertEquals(testRequest.getStatus(), r.getStatus());
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        requests = dao.getPendingRequests(123);
        assertEquals(0, requests.size());
    }

    @Test
    public void testGetResolvedRequests() throws SQLException {
        List<ReimbursementRequest> requests = dao.getResolvedRequests(123);
        assertEquals(1, requests.size());
        ReimbursementRequest r = requests.get(0);
        assertEquals(testRequest.getRequestID(), r.getRequestID());
        assertEquals(testRequest.getSubmitterID(), r.getSubmitterID());
        assertEquals(testRequest.getResolverID(), r.getResolverID());
        assertEquals(testRequest.getAmount(), r.getAmount());
        assertEquals(testRequest.getTimeSubmitted(), r.getTimeSubmitted());
        assertEquals(testRequest.getCategory(), r.getCategory());
        assertEquals(testRequest.getDescription(), r.getDescription());
        assertEquals(testRequest.getStatus(), r.getStatus());
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        requests = dao.getResolvedRequests(123);
        assertEquals(0, requests.size());
    }

    @Test
    public void testGetAllPendingRequests() throws SQLException {
        List<ReimbursementRequest> requests = dao.getAllPendingRequests();
        assertEquals(1, requests.size());
        ReimbursementRequest r = requests.get(0);
        assertEquals(testRequest.getRequestID(), r.getRequestID());
        assertEquals(testRequest.getSubmitterID(), r.getSubmitterID());
        assertEquals(testRequest.getResolverID(), r.getResolverID());
        assertEquals(testRequest.getAmount(), r.getAmount());
        assertEquals(testRequest.getTimeSubmitted(), r.getTimeSubmitted());
        assertEquals(testRequest.getCategory(), r.getCategory());
        assertEquals(testRequest.getDescription(), r.getDescription());
        assertEquals(testRequest.getStatus(), r.getStatus());
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        requests = dao.getAllPendingRequests();
        assertEquals(0, requests.size());
    }

    @Test
    public void testGetAllResolvedRequests() throws SQLException {
        List<ReimbursementRequest> requests = dao.getAllResolvedRequests();
        assertEquals(1, requests.size());
        ReimbursementRequest r = requests.get(0);
        assertEquals(testRequest.getRequestID(), r.getRequestID());
        assertEquals(testRequest.getSubmitterID(), r.getSubmitterID());
        assertEquals(testRequest.getResolverID(), r.getResolverID());
        assertEquals(testRequest.getAmount(), r.getAmount());
        assertEquals(testRequest.getTimeSubmitted(), r.getTimeSubmitted());
        assertEquals(testRequest.getCategory(), r.getCategory());
        assertEquals(testRequest.getDescription(), r.getDescription());
        assertEquals(testRequest.getStatus(), r.getStatus());
        Mockito.when(ps.executeQuery()).thenThrow(SQLException.class);
        requests = dao.getAllResolvedRequests();
        assertEquals(0, requests.size());
    }

    @Test
    public void testResolveRequest() throws SQLException {
        assertTrue(dao.resolveRequest(123, 45,"Approved"));
        Mockito.when(ps.executeUpdate()).thenThrow(SQLException.class);
        assertFalse(dao.resolveRequest(123, 45,"Approved"));
    }
}