package com.nathanpaternoster.services;

import com.nathanpaternoster.models.requests.ReimbursementRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDAOImplPostgres implements RequestDAO {
    private static final Logger log = LogManager.getLogger(RequestDAOImplPostgres.class);
    private final DataSource dataSource;

    public RequestDAOImplPostgres(DataSource dataSource) {
        this.dataSource = dataSource;
        log.debug("Request DAO created");
    }

    @Override
    public int addRequest(ReimbursementRequest r) {
        String sql =
                "insert into requests (submitterID, resolverID, amount, timeSubmitted, timeResolved, category, description, status) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?) returning requestID;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            connection.setAutoCommit(false);
            ps.setInt(1, r.getSubmitterID());
            if (r.getResolverID() > 0) ps.setInt(2, r.getResolverID());
            else ps.setNull(2, Types.NULL);
            ps.setLong(3, r.getAmount());
            if (r.getTimeSubmitted() == null) ps.setNull(4, Types.NULL);
            else ps.setTimestamp(4, Timestamp.valueOf(r.getTimeSubmitted()));
            if (r.getTimeResolved() == null) ps.setNull(5, Types.NULL);
            else ps.setTimestamp(5, Timestamp.valueOf(r.getTimeResolved()));
            ps.setString(6, r.getCategory());
            ps.setString(7, r.getDescription());
            ps.setString(8, r.getStatus());
            log.debug("Attempting database insert for new reimbursement request");
            ps.executeUpdate();

            // get the generated requestID
            ResultSet keys = ps.getGeneratedKeys();
            int newRequestID = keys.next() ? keys.getInt(1) : -1;

            connection.commit();
            return newRequestID;
        } catch (SQLException e) {
            log.error("Database insert failed");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean resolveRequest(int resolverID, int requestID, String resolution) {
        String sql = "update requests set status=?, resolverID=?, timeResolved=? where requestID=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setString(1, resolution);
            ps.setInt(2, resolverID);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, requestID);
            log.debug("Attempting database update for a request's status");
            int rows = ps.executeUpdate();
            connection.commit();
            return rows > 0;
        } catch (SQLException e) {
            log.error("Database update failed");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ReimbursementRequest getRequest(int requestID) {
        String sql = "select submitterID, resolverID, amount, timeSubmitted, timeResolved, category, description, status from requests where requestID=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setInt(1, requestID);
            log.debug("Attempting database query for existing reimbursement request");
            ResultSet rs = ps.executeQuery();
            connection.commit();

            if (rs.next()) {
                int submitterID = rs.getInt("submitterID");
                int resolverID = rs.getInt("resolverID");
                long amount = rs.getLong("amount");
                Timestamp ts = rs.getTimestamp("timeSubmitted");
                LocalDateTime timeSubmitted = (ts != null) ? ts.toLocalDateTime() : null;
                Timestamp ts2 = rs.getTimestamp("timeResolved");
                LocalDateTime timeResolved = (ts2 != null) ? ts2.toLocalDateTime() : null;
                String category = rs.getString("category");
                String description = rs.getString("description");
                String status = rs.getString("status");
                log.debug("Database query found a reimbursement request");
                return new ReimbursementRequest(requestID, submitterID, resolverID, amount, category, description, timeSubmitted, timeResolved, status);
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Utility method for querying a list of requests
     * @param withStatus One of "Pending", "Resolved", "All", defaults to all
     * @param userID The userID the requests should belong or a negative value for requests belonging to any user
     * @return A list of reimbursement requests
     */
    private List<ReimbursementRequest> queryRequests(String withStatus, int userID) {
        String whereClause = " ";
        if (userID >= 0) {
            if (withStatus.equalsIgnoreCase("Pending")) {
                whereClause = " where submitterID=" + userID + " and status='Pending' ";
            } else if (withStatus.equalsIgnoreCase("Resolved")) {
                whereClause = " where submitterID=" + userID + " and status in ('Approved', 'Denied') ";
            } else {
                whereClause = " where submitterID=" + userID + " ";
            }
        } else {
            if (withStatus.equalsIgnoreCase("Pending")) {
                whereClause = " where status='Pending' ";
            } else if (withStatus.equalsIgnoreCase("Resolved")) {
                whereClause = " where status in ('Approved', 'Denied') ";
            }
        }
        String sql =
                "select requestID, submitterID, resolverID, amount, timeSubmitted, timeResolved, category, description, status from requests" +
                        whereClause + "order by timeSubmitted desc;";
        List<ReimbursementRequest> requests = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            log.debug("Attempting database query for '" + withStatus + "' requests" + (userID >= 0 ? " belonging to one user" : ""));
            ResultSet rs = ps.executeQuery();
            connection.commit();

            while (rs.next()) {
                int requestID = rs.getInt("requestID");
                int submitterID = rs.getInt("submitterID");
                int resolverID = rs.getInt("resolverID");
                long amount = rs.getLong("amount");
                Timestamp ts = rs.getTimestamp("timeSubmitted");
                LocalDateTime timeSubmitted = (ts != null) ? ts.toLocalDateTime() : null;
                Timestamp ts2 = rs.getTimestamp("timeResolved");
                LocalDateTime timeResolved = (ts2 != null) ? ts2.toLocalDateTime() : null;
                String category = rs.getString("category");
                String description = rs.getString("description");
                String status = rs.getString("status");
                log.debug("Database query found a reimbursement request");
                requests.add(new ReimbursementRequest(requestID, submitterID, resolverID, amount, category, description, timeSubmitted, timeResolved, status));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public List<ReimbursementRequest> getRequests(int userID) {
        return queryRequests("All", userID);
    }

    @Override
    public List<ReimbursementRequest> getPendingRequests(int userID) {
        return queryRequests("Pending", userID);
    }

    @Override
    public List<ReimbursementRequest> getResolvedRequests(int userID) {
        return queryRequests("Resolved", userID);
    }

    @Override
    public List<ReimbursementRequest> getAllRequests() {
        return queryRequests("All", -1);
    }

    @Override
    public List<ReimbursementRequest> getAllPendingRequests() {
        return queryRequests("Pending", -1);
    }

    @Override
    public List<ReimbursementRequest> getAllResolvedRequests() {
        return queryRequests("Resolved", -1);
    }
}