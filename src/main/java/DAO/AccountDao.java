package DAO;

import Model.Account;
import Util.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AccountDao implements BaseDao<Account> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDao.class);

    private void handleSQLException(SQLException e, String sql, String errorMessage) {
        LOGGER.error("SQLException Details: {}", e.getMessage());
        LOGGER.error("SQL State: {}", e.getSQLState());
        LOGGER.error("Error Code: {}", e.getErrorCode());
        LOGGER.error("SQL: {}", sql);
        throw new DaoException(errorMessage, e);
    }

    // Get Account by Id

    public Optional<Account> getById(int id) {

        String sql = "SELECT * FROM account WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving the account with id: " + id);
        }
        return Optional.empty();
    }

    // Get all form Account

    public List<Account> getAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password"));
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving all the accounts");
        }
        return accounts;
    }

    // Find Account by User Name

    public Optional<Account> findAccountByUsername(String username) {

        String sql = "SELECT * FROM account WHERE username = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while finding account with username: " + username);
        }
        return Optional.empty();
    }

    public Optional<Account> validateLogin(String username, String password) {
        String sql = "SELECT * FROM account WHERE username = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password"));
                    if (Objects.equals(password, account.getPassword())) {
                        return Optional.of(account);
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while validating login for username: " + username);
        }
        return Optional.empty();
    }

    // Checking for User Existance.

    public boolean doesUsernameExist(String username) {
        String sql = "SELECT COUNT(*) FROM account WHERE username = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                handleSQLException(e, sql, "Error while checking if username exists: " + username);
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while establishing connection");
        }
        return false;
    }

    // Insert to account table.
    public Account insert(Account account) {
        String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedAccountId = generatedKeys.getInt(1);
                    return new Account(generatedAccountId, account.getUsername(), account.getPassword());
                } else {
                    throw new DaoException("Creating account failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Creating account failed due to SQL error", e);
        }
    }

    // Update the account table.

    public boolean update(Account account) {
        String sql = "UPDATE account SET username = ?, password = ? WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.setInt(3, account.getAccount_id());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return true;
            } else {
                throw new DaoException("Updating account failed, no such account found.");
            }
        } catch (SQLException e) {
            throw new DaoException("Updating account failed due to SQL error", e);
        }
    }

    // Delete from account table.

    public boolean delete(Account account) {
        String sql = "DELETE FROM account WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, account.getAccount_id());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DaoException("Deleting account failed due to SQL error", e);
        }
    }
}