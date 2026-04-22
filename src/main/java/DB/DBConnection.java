package DB;

import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/erronka1";
    private static final String USER = "root";
    private static final String PASSWORD = "1MG2024";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
