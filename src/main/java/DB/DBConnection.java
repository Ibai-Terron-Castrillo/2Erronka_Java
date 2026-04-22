package DB;

import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://192.168.115.162:3306/erronka1";
    private static final String USER = "2Taldea";
    private static final String PASSWORD = "2Taldea2";

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