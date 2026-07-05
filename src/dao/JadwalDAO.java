package dao;

import model.Jadwal;
import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class JadwalDAO {
    public ObservableList<Jadwal> getAllJadwal() {
        ObservableList<Jadwal> list = FXCollections.observableArrayList();
        // JOIN dengan tabel dokter agar bisa menampilkan nama dokter
        String sql = "SELECT j.*, d.nama AS nama_dokter FROM jadwal j JOIN dokter d ON j.id_dokter = d.id_dokter";
        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Jadwal j = new Jadwal();
                j.setIdJadwal(rs.getInt("id_jadwal"));
                j.setIdDokter(rs.getInt("id_dokter"));
                j.setNamaDokter(rs.getString("nama_dokter"));
                j.setHari(rs.getString("hari"));
                j.setJamMulai(rs.getString("jam_mulai"));
                j.setJamSelesai(rs.getString("jam_selesai"));
                list.add(j);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void insert(Jadwal j) throws SQLException {
        String sql = "INSERT INTO jadwal (id_dokter, hari, jam_mulai, jam_selesai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, j.getIdDokter());
            ps.setString(2, j.getHari());
            ps.setString(3, j.getJamMulai());
            ps.setString(4, j.getJamSelesai());
            ps.executeUpdate();
        }
    }
}