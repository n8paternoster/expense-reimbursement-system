package com.nathanpaternoster.services;

import com.nathanpaternoster.models.users.Employee;
import com.nathanpaternoster.models.users.Manager;
import com.nathanpaternoster.models.users.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImplPostgres implements UserDAO {
    private static final Logger log = LogManager.getLogger(UserDAOImplPostgres.class);
    private final DataSource dataSource;

    public UserDAOImplPostgres(DataSource dataSource) {
        this.dataSource = dataSource;
        log.debug("PostgreSQL DAO created");
    }

    @Override
    public int addNewUser(User newUser) {
        String sql = "insert into users (password, userType, firstName, lastName, email, dob) values (?, ?, ?, ?, ?, ?);";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            connection.setAutoCommit(false);
            String userType = "";
            if (newUser instanceof Employee) userType = "Employee";
            else if (newUser instanceof Manager) userType = "Manager";
            ps.setString(1, newUser.getPassword());
            ps.setString(2, userType);
            ps.setString(3, newUser.getFirstName());
            ps.setString(4, newUser.getLastName());
            ps.setString(5, (newUser instanceof Employee ? ((Employee) newUser).getEmail() : null));
            ps.setTimestamp(6, Timestamp.valueOf(newUser.getDob().atStartOfDay()));
            log.debug("Attempting database insert for new user");
            ps.executeUpdate();

            // get the generated userID
            ResultSet keys = ps.getGeneratedKeys();
            int newUserID = keys.next() ? keys.getInt(1) : -1;

            connection.commit();
            return newUserID;
        } catch (SQLException e) {
            log.error("Database insert failed");
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean updateUser(User updatedUser) {
        String sql = "update users set password=?, firstName=?, lastName=?, email=?, dob=? where userID=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setString(1, updatedUser.getPassword());
            ps.setString(2, updatedUser.getFirstName());
            ps.setString(3, updatedUser.getLastName());
            if (updatedUser instanceof Employee) ps.setString(4, ((Employee) updatedUser).getEmail());
            else ps.setNull(4, Types.NULL);
            ps.setTimestamp(5, Timestamp.valueOf(updatedUser.getDob().atStartOfDay()));
            ps.setInt(6, updatedUser.getUserID());
            log.debug("Attempting database update for existing user");
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
    public User authenticateUser(int userID, String password) {
        String sql = "select userType, firstName, lastName, email, dob from users where userID=? and password=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setInt(1, userID);
            ps.setString(2, password);
            log.debug("Attempting database query for user authentication");
            ResultSet rs = ps.executeQuery();
            connection.commit();

            if (rs.next()) {
                String replacedPassword = "**********";
                String type = rs.getString("userType");
                String fName = rs.getString("firstName");
                String lName = rs.getString("lastName");
                String email = rs.getString("email");
                Timestamp ts = rs.getTimestamp("dob");
                LocalDate dob = (ts != null) ? ts.toLocalDateTime().toLocalDate() : null;
                email = (email == null) ? "" : email;
                if (type.equalsIgnoreCase("Employee")) {
                    log.debug("Employee successfully authenticated");
                    return new Employee(userID, replacedPassword, fName, lName, dob, email, null);
                } else if (type.equalsIgnoreCase("Manager")) {
                    log.debug("Manager successfully authenticated");
                    return new Manager(userID, replacedPassword, fName, lName, dob);
                }
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUser(int userID) {
        String sql = "select userType, firstName, lastName, email, dob from users where userID=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setInt(1, userID);
            log.debug("Attempting database query for existing user");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String replacedPassword = "**********";
                String type = rs.getString("userType");
                String fName = rs.getString("firstName");
                String lName = rs.getString("lastName");
                String email = rs.getString("email");
                Timestamp ts = rs.getTimestamp("dob");
                LocalDate dob = (ts != null) ? ts.toLocalDateTime().toLocalDate() : null;
                email = (email == null) ? "" : email;
                if (type.equalsIgnoreCase("Employee")) {
                    log.debug("Database query found an employee");
                    return new Employee(userID, replacedPassword, fName, lName, dob, email, null);
                } else if (type.equalsIgnoreCase("Manager")) {
                    log.debug("Database query found a manager");
                    return new Manager(userID, replacedPassword, fName, lName, dob);
                }
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() {
        String sql = "select userID, firstName, lastName, email, dob from users where userType='Employee' order by userID asc;";
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            log.debug("Attempting database query for all employees");
            ResultSet rs = ps.executeQuery();
            connection.commit();

            while (rs.next()) {
                String replacedPassword = "**********";
                int userID = rs.getInt("userID");
                String fName = rs.getString("firstName");
                String lName = rs.getString("lastName");
                String email = rs.getString("email");
                Timestamp ts = rs.getTimestamp("dob");
                LocalDate dob = (ts != null) ? ts.toLocalDateTime().toLocalDate() : null;
                email = (email == null) ? "" : email;
                employees.add(new Employee(userID, replacedPassword, fName, lName, dob, email, null));
            }
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public boolean emailIsAvailable(String email) {
        String sql = "select userID from users where email=?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            connection.setAutoCommit(false);
            ps.setString(1, email);
            log.debug("Attempting database query for email availability");
            ResultSet rs = ps.executeQuery();
            connection.commit();
            return !rs.next();
        } catch (SQLException e) {
            log.error("Database query failed");
            e.printStackTrace();
        }
        return false;
    }
}