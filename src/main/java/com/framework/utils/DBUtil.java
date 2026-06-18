package com.framework.utils;

import java.sql.*;
import java.util.*;

import static com.framework.config.ConfigurationManager.config;

/**
 * Thread-safe JDBC helper for executing queries and updates.
 *
 * <p>Connections are created on demand and closed automatically after each
 * operation; there is no connection pool — add HikariCP if high-frequency
 * DB calls are needed.
 *
 * <pre>
 *   List&lt;Map&lt;String,String&gt;&gt; rows = DBUtil.executeQuery(
 *       "SELECT * FROM orders WHERE status = ?", "OPEN");
 * </pre>
 */
public final class DBUtil {

    private DBUtil() {}

    /**
     * Executes a SELECT query and returns all rows as a list of column→value maps.
     *
     * @param sql    parameterised SQL (use {@code ?} placeholders)
     * @param params positional parameter values
     */
    public static List<Map<String, String>> executeQuery(String sql, Object... params) {
        List<Map<String, String>> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = prepare(conn, sql, params);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            LogUtils.error("DB query failed: " + e.getMessage(), e);
            throw new RuntimeException("DB query failed", e);
        }
        return results;
    }

    /**
     * Executes an INSERT / UPDATE / DELETE statement.
     *
     * @return the number of affected rows
     */
    public static int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = prepare(conn, sql, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            LogUtils.error("DB update failed: " + e.getMessage(), e);
            throw new RuntimeException("DB update failed", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            config().dbUrl(),
            config().dbUsername(),
            config().dbPassword()
        );
    }

    private static PreparedStatement prepare(Connection conn, String sql, Object[] params)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }
}
