package dao; // Sesuaikan dengan struktur folder (bisa dao atau controller)

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Dokter;

public class DokterDAO {
    
    // Pastikan kamu sudah punya class koneksi database, misalnya DatabaseConnection
    private Connection conn;

    public DokterDAO(Connection conn) {
        this.conn = conn;
    }

    public DokterDAO() {
        // Otomatis mengambil koneksi sendiri
        this.conn = database.DBConnection.connect(); 
    }

    // 1. Fitur Menambahkan Dokter Baru (CREATE)
    public boolean tambahDokter(Dokter dokter) {
        String sql = "INSERT INTO dokter (nama, spesialis, no_hp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dokter.getNama());
            stmt.setString(2, dokter.getSpesialis());
            stmt.setString(3, dokter.getNoHP());
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.out.println("Error tambah dokter: " + e.getMessage());
            return false;
        }
    }

    // 2. Fitur Menampilkan Semua Dokter (READ) - Berguna untuk mengisi TableView di UI
    public List<Dokter> getSemuaDokter() {
        List<Dokter> listDokter = new ArrayList<>();
        String sql = "SELECT * FROM dokter";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Dokter d = new Dokter();
                d.setIdDokter(rs.getInt("id_dokter"));
                d.setNama(rs.getString("nama"));
                d.setSpesialis(rs.getString("spesialis"));
                d.setNoHP(rs.getString("no_hp"));
                listDokter.add(d);
            }
        } catch (SQLException e) {
            System.out.println("Error ambil data dokter: " + e.getMessage());
        }
        return listDokter;
    }

    // 3. Fitur Update Data Dokter (UPDATE)
    public boolean updateDokter(Dokter dokter) {
        String sql = "UPDATE dokter SET nama = ?, spesialis = ?, no_hp = ? WHERE id_dokter = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dokter.getNama());
            stmt.setString(2, dokter.getSpesialis());
            stmt.setString(3, dokter.getNoHP());
            stmt.setInt(4, dokter.getIdDokter());
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error update dokter: " + e.getMessage());
            return false;
        }
    }

    // 4. Fitur Hapus Data Dokter (DELETE)
    public boolean hapusDokter(int idDokter) {
        String sql = "DELETE FROM dokter WHERE id_dokter = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDokter);
            
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.out.println("Error hapus dokter: " + e.getMessage());
            return false;
        }
    }
}