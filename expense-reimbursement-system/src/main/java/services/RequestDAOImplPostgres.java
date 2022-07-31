package services;

import models.requests.ReimbursementRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDAOImplPostgres implements RequestDAO {
    private static final Logger log = LogManager.getLogger(RequestDAOImplPostgres.class.getName());
    private final DataSource dataSource;

    public RequestDAOImplPostgres(DataSource dataSource) {
        this.dataSource = dataSource;
        log.debug("Request DAO created");
    }

    /**
     * Add a new reimbursement request to the database
     * @param r The reimbursement request to insert
     * @return The generated requestID or -1 if the request wasn't inserted
     */
    @Override
    public int addRequest(ReimbursementRequest r) {
        String sql =
                "insert into requests (submitterID, resolverID, amount, timeSubmitted, category, description, status) " +
                "values (?, ?, ?, ?, ?, ?, ?) returning requestID;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            connection.setAutoCommit(false);
            ps.setInt(1, r.getSubmitterID());
            if (r.getResolverID() > 0) ps.setInt(2, r.getResolverID());
            else ps.setNull(2, Types.NULL);
            ps.setLong(3, r.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(r.getTimeSubmitted()));
            ps.setString(5, r.getCategory());
            ps.setString(6, r.getDescription());
            ps.setString(7, r.getStatus());
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

    /**
     * Update a request's status
     * @param requestID The reimbursement request's id
     * @param resolution The updated status
     * @return True if the request was successfully updated, false otherwise
     */
    @Override
    public boolean resolveRequest(int requestID, String resolution) {
        String sql = "update requests set status=? where requestID=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setString(1, resolution);
            ps.setInt(2, requestID);
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

    /**
     * Search the database for a reimbursement request with the given id
     * @param requestID The request's id
     * @return A new reimbursement request object if found, null otherwise
     */
    @Override
    public ReimbursementRequest getRequest(int requestID) {
        String sql = "select submitterID, resolverID, amount, timeSubmitted, category, description, status from requests where requestID=?;";
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
                String category = rs.getString("category");
                String description = rs.getString("description");
                String status = rs.getString("status");
                log.debug("Database query found a reimbursement request");
                return new ReimbursementRequest(requestID, submitterID, resolverID, amount, category, description, timeSubmitted, status);
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
                "select requestID, submitterID, resolverID, amount, timeSubmitted, category, description, status from requests" +
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
                String category = rs.getString("category");
                String description = rs.getString("description");
                String status = rs.getString("status");
                log.debug("Database query found a reimbursement request");
                requests.add(new ReimbursementRequest(requestID, submitterID, resolverID, amount, category, description, timeSubmitted, status));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Get all requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getRequests(int userID) {
        return queryRequests("All", userID);
    }

    /**
     * Get all pending requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of pending reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getPendingRequests(int userID) {
        return queryRequests("Pending", userID);
    }

    /**
     * Get all resolved requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of resolved reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getResolvedRequests(int userID) {
        return queryRequests("Resolved", userID);
    }

    /**
     * Get all requests
     * @return A list of reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getAllRequests() {
        return queryRequests("All", -1);
    }

    /**
     * Get all pending requests
     * @return A list of pending reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getAllPendingRequests() {
        return queryRequests("Pending", -1);
    }

    /**
     * Get all resolved requests
     * @return A list of resolved reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getAllResolvedRequests() {
        return queryRequests("Resolved", -1);
    }
}