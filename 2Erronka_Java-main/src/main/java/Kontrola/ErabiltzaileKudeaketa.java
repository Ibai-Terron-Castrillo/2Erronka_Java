package Kontrola;

import DB.DBConnection;
import Klaseak.Erabiltzailea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ErabiltzaileKudeaketa {
    public boolean balidatu(Erabiltzailea erabiltzailea) throws SQLException {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        conn = DBConnection.getConnection();

        String sql = "SELECT * FROM Erabiltzaileak WHERE erabiltzailea=? AND pasahitza=?";

        pst = conn.prepareStatement(sql);
        pst.setString(1, erabiltzailea.getIzena());
        pst.setString(2, erabiltzailea.getPasahitza());
        rs = pst.executeQuery();
        boolean exists = rs.next();
        rs.close();
        pst.close();
        return exists;
    }
}
