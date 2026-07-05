package dao;

import model.Poli;
import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class PoliDAO {
    public ObservableList<Poli> getAllPoli() {
        ObservableList<Poli> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM poli";
        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Poli p = new Poli();
                p.setIdPoli(rs.getInt("id_poli"));
                p.setNamaPoli(rs.getString("nama_poli"));
                p.setKeterangan(rs.getString("keterangan"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void insert(Poli p) throws SQLException {
        String sql = "INSERT INTO poli (nama_poli, keterangan) VALUES (?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNamaPoli());
            ps.setString(2, p.getKeterangan());
            ps.executeUpdate();
        }
    }
}