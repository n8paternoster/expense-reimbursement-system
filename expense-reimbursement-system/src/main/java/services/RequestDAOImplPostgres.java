package services;

import models.ReimbursementRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDAOImplPostgres implements RequestDAO {
    private static final Logger log = LogManager.getLogger(RequestDAOImplPostgres.class.getName());
    DataSource dataSource;

    RequestDAOImplPostgres(DataSource dataSource) {
        this.dataSource = dataSource;
        log.debug("Request DAO created");
    }

    /**
     * Add a new reimbursement request to the database
     * @param r The reimbursement request to insert
     * @return True if the new request was added, false if not
     */
    @Override
    public boolean addRequest(ReimbursementRequest r) {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "insert into requests (submitterID, resolverID, amount, timeSubmitted, category, description, status) values (?, ?, ?, ?, ?, ?, ?) returning requestID;";
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

            // get the generated userID and add to the request
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newRequestID = keys.getInt(1);
                r.setRequestID(newRequestID);
            } else {
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            log.error("Database insert failed");
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
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "select submitterID, resolverID, amount, timeSubmitted, category, description, status from requests where requestID=?;";
            PreparedStatement ps = connection.prepareStatement(sql);
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
     * Get all pending requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of pending reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getPendingRequests(int userID) {
        List<ReimbursementRequest> requests = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "select requestID, resolverID, amount, timeSubmitted, category, description from requests where submitterID=? and status='Pending';";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userID);
            log.debug("Attempting database query for all pending requests belonging to one user");
            ResultSet rs = ps.executeQuery();
            connection.commit();

            while (rs.next()) {
                int requestID = rs.getInt("requestID");
                int resolverID = rs.getInt("resolverID");
                long amount = rs.getLong("amount");
                Timestamp ts = rs.getTimestamp("timeSubmitted");
                LocalDateTime timeSubmitted = (ts != null) ? ts.toLocalDateTime() : null;
                String category = rs.getString("category");
                String description = rs.getString("description");
                log.debug("Database query found a pending reimbursement request");
                requests.add(new ReimbursementRequest(requestID, userID, resolverID, amount, category, description, timeSubmitted, "Pending"));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Get all resolved requests belonging to the user with the specified userID
     * @param userID The specified user's id
     * @return A list of resolved reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getResolvedRequests(int userID) {
        List<ReimbursementRequest> requests = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "select requestID, resolverID, amount, timeSubmitted, category, description, status from requests where submitterID=? and status in ('Approved', 'Denied');";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userID);
            log.debug("Attempting database query for all resolved requests belonging to one user");
            ResultSet rs = ps.executeQuery();
            connection.commit();

            while (rs.next()) {
                int requestID = rs.getInt("requestID");
                int resolverID = rs.getInt("resolverID");
                long amount = rs.getLong("amount");
                Timestamp ts = rs.getTimestamp("timeSubmitted");
                LocalDateTime timeSubmitted = (ts != null) ? ts.toLocalDateTime() : null;
                String category = rs.getString("category");
                String description = rs.getString("description");
                String status = rs.getString("status");
                log.debug("Database query found a resolved reimbursement request");
                requests.add(new ReimbursementRequest(requestID, userID, resolverID, amount, category, description, timeSubmitted, status));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Get all pending requests
     * @return A list of pending reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getAllPendingRequests() {
        List<ReimbursementRequest> requests = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "select requestID, submitterID, resolverID, amount, timeSubmitted, category, description from requests where status='Pending';";
            PreparedStatement ps = connection.prepareStatement(sql);
            log.debug("Attempting database query for all pending requests");
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
                log.debug("Database query found a pending reimbursement request");
                requests.add(new ReimbursementRequest(requestID, submitterID, resolverID, amount, category, description, timeSubmitted, "Pending"));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Get all resolved requests
     * @return A list of resolved reimbursement requests
     */
    @Override
    public List<ReimbursementRequest> getAllResolvedRequests() {
        List<ReimbursementRequest> requests = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "select requestID, submitterID, resolverID, amount, timeSubmitted, category, description, status from requests where status in ('Approved', 'Denied');";
            PreparedStatement ps = connection.prepareStatement(sql);
            log.debug("Attempting database query for all resolved requests");
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
                log.debug("Database query found a resolved reimbursement request");
                requests.add(new ReimbursementRequest(requestID, submitterID, resolverID, amount, category, description, timeSubmitted, status));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Update a request's status
     * @param requestID The reimbursement request's id
     * @param resolution The updated status
     * @return True if the request was successfully updated, false otherwise
     */
    @Override
    public boolean resolveRequest(int requestID, String resolution) {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "update requests set status=? where requestID=?;";
            PreparedStatement ps = connection.prepareStatement(sql);
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
}
