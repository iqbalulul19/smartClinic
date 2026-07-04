package controller;

import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Pendaftaran;
import javafx.application.Platform;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PendaftaranController implements Initializable {

    @FXML private TextField txtNama, txtUmur, txtNoHP;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextArea txtAlamat, txtKeluhan;
    @FXML private ComboBox<String> cmbDokter;

    @FXML private TableView<Pendaftaran> tableAntrian;
    @FXML private TableColumn<Pendaftaran, Integer> colNoAntrian;
    @FXML private TableColumn<Pendaftaran, String> colNamaPasien;
    @FXML private TableColumn<Pendaftaran, String> colDokter;
    @FXML private TableColumn<Pendaftaran, String> colKeluhan;

    private Map<String, Integer> mapDokter = new HashMap<>();
    
    // Variabel penyimpan ID untuk fitur Update & Delete
    private int selectedIdDaftar = -1;
    private int selectedIdPasien = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGender.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));

        colNoAntrian.setCellValueFactory(new PropertyValueFactory<>("noAntrian"));
        colKeluhan.setCellValueFactory(new PropertyValueFactory<>("keluhan"));
        colNamaPasien.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPasien().getNama()));
        colDokter.setCellValueFactory(cellData -> new SimpleStringProperty("dr. " + cellData.getValue().getDokter().getNama()));

        loadDokter();
        loadAntrianHariIni();
    }

    private void loadDokter() {
        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_dokter, nama FROM dokter")) {
            while (rs.next()) {
                String namaDokter = "Dr. " + rs.getString("nama");
                mapDokter.put(namaDokter, rs.getInt("id_dokter"));
                cmbDokter.getItems().add(namaDokter);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAntrianHariIni() {
        ObservableList<Pendaftaran> listAntrian = FXCollections.observableArrayList();
        String query = "SELECT p.id_daftar, p.no_antrian, p.keluhan, p.id_dokter, " +
                       "pas.id_pasien, pas.nama AS nama_pasien, pas.umur, pas.gender, pas.no_hp, pas.alamat, " +
                       "dok.nama AS nama_dokter " +
                       "FROM pendaftaran p " +
                       "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                       "JOIN dokter dok ON p.id_dokter = dok.id_dokter " +
                       "LEFT JOIN pemeriksaan per ON p.id_daftar = per.id_daftar " +
                       "WHERE p.tanggal = CURDATE() AND per.id_periksa IS NULL " +
                       "ORDER BY p.no_antrian ASC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.Pasien pas = new model.Pasien();
                pas.setIdPasien(rs.getInt("id_pasien"));
                pas.setNama(rs.getString("nama_pasien"));
                pas.setUmur(rs.getInt("umur"));
                pas.setGender(rs.getString("gender"));
                pas.setNoHp(rs.getString("no_hp"));
                pas.setAlamat(rs.getString("alamat"));

                model.Dokter dok = new model.Dokter();
                dok.setIdDokter(rs.getInt("id_dokter"));
                dok.setNama(rs.getString("nama_dokter"));

                Pendaftaran p = new Pendaftaran();
                p.setIdDaftar(rs.getInt("id_daftar"));
                p.setNoAntrian(rs.getInt("no_antrian"));
                p.setKeluhan(rs.getString("keluhan"));
                p.setPasien(pas);
                p.setDokter(dok);
                
                listAntrian.add(p);
            }
            tableAntrian.setItems(listAntrian);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePilihAntrian() {
        Pendaftaran selected = tableAntrian.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedIdDaftar = selected.getIdDaftar();
            selectedIdPasien = selected.getPasien().getIdPasien();

            txtNama.setText(selected.getPasien().getNama());
            txtUmur.setText(String.valueOf(selected.getPasien().getUmur()));
            cmbGender.setValue(selected.getPasien().getGender());
            txtNoHP.setText(selected.getPasien().getNoHp());
            txtAlamat.setText(selected.getPasien().getAlamat());
            txtKeluhan.setText(selected.getKeluhan());
            
            // Format disamakan dengan loadDokter agar combo box terpilih
            cmbDokter.setValue("Dr. " + selected.getDokter().getNama());
        }
    }

    @FXML
    private int generateNoAntrian(Connection conn, int idDokter) throws Exception {
        int antrian = 1;
        String sql = "SELECT MAX(no_antrian) AS max_antrian FROM pendaftaran WHERE tanggal = CURDATE() AND id_dokter = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDokter);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && !rs.wasNull()) {
                antrian = rs.getInt("max_antrian") + 1;
            }
        }
        return antrian;
    }

    @FXML
    private void handleDaftar(ActionEvent event) {
        try {
            // 1. Validasi Input Dasar
            if (txtNama.getText().isEmpty() || cmbDokter.getValue() == null) {
                System.out.println("DEBUG: Nama atau Dokter masih kosong!");
                return;
            }

            int noAntrianBaru = generateNoAntrian();
            int idDokter = mapDokter.get(cmbDokter.getValue());
            String keluhan = txtKeluhan.getText();

            Connection conn = DBConnection.connect();
            try {
                conn.setAutoCommit(false); 

                // =======================================================
                // TAHAP 1: SIMPAN PASIEN BARU & AMBIL ID-NYA
                // =======================================================
                String sqlPasien = "INSERT INTO pasien (nama, umur, gender, no_hp, alamat) VALUES (?, ?, ?, ?, ?)";
                int idPasienBaru = -1;

                try (PreparedStatement psPasien = conn.prepareStatement(sqlPasien, Statement.RETURN_GENERATED_KEYS)) {
                    psPasien.setString(1, txtNama.getText());
                    psPasien.setInt(2, Integer.parseInt(txtUmur.getText())); 
                    psPasien.setString(3, cmbGender.getValue() != null ? cmbGender.getValue().toString() : "-");
                    psPasien.setString(4, txtNoHP.getText());
                    psPasien.setString(5, txtAlamat.getText());
                    psPasien.executeUpdate();

                    try (ResultSet rs = psPasien.getGeneratedKeys()) {
                        if (rs.next()) {
                            idPasienBaru = rs.getInt(1);
                        }
                    }
                }

                if (idPasienBaru == -1) {
                    throw new SQLException("Gagal mendapatkan ID Pasien dari database.");
                }

                // =======================================================
                // TAHAP 2: SIMPAN KE ANTREAN PENDAFTARAN
                // =======================================================
                String sqlDaftar = "INSERT INTO pendaftaran (no_antrian, id_pasien, id_dokter, keluhan, tanggal) VALUES (?, ?, ?, ?, CURDATE())";

                try (PreparedStatement psDaftar = conn.prepareStatement(sqlDaftar)) {
                    psDaftar.setInt(1, noAntrianBaru);
                    psDaftar.setInt(2, idPasienBaru); 
                    psDaftar.setInt(3, idDokter);
                    psDaftar.setString(4, keluhan);
                    psDaftar.executeUpdate();
                }

                // KEDUANYA BERHASIL, SIMPAN PERMANEN KELUAR DARI DATABASE
                conn.commit(); 
                System.out.println("SUKSES: Data berhasil masuk MySQL!");

                Platform.runLater(() -> {
                    loadAntrianHariIni(); // 1. Data masuk ke tabel antrean
                    bersihkanForm();      // 2. Otomatis form langsung bersih
                });

            } catch (SQLException ex) {
                if (conn != null) conn.rollback(); 
                System.err.println("Gagal transaksi, data dibatalkan: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Input Umur harus berupa angka!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUbah() {
        if (selectedIdDaftar == -1) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data dari tabel antrian terlebih dahulu!");
            return;
        }
        if (!validateInput()) return;

        int idDokterTerpilih = mapDokter.get(cmbDokter.getValue());

        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); 

            // 1. Update Biodata Pasien
            String sqlPasien = "UPDATE pasien SET nama=?, umur=?, gender=?, alamat=?, no_hp=? WHERE id_pasien=?";
            try (PreparedStatement psPasien = conn.prepareStatement(sqlPasien)) {
                psPasien.setString(1, txtNama.getText());
                psPasien.setInt(2, Integer.parseInt(txtUmur.getText()));
                psPasien.setString(3, cmbGender.getValue());
                psPasien.setString(4, txtAlamat.getText());
                psPasien.setString(5, txtNoHP.getText());
                psPasien.setInt(6, selectedIdPasien);
                psPasien.executeUpdate();
            }

            // 2. Update Keluhan & Dokter di Pendaftaran
            String sqlDaftar = "UPDATE pendaftaran SET keluhan=?, id_dokter=? WHERE id_daftar=?";
            try (PreparedStatement psDaftar = conn.prepareStatement(sqlDaftar)) {
                psDaftar.setString(1, txtKeluhan.getText());
                psDaftar.setInt(2, idDokterTerpilih);
                psDaftar.setInt(3, selectedIdDaftar);
                psDaftar.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data pendaftaran berhasil diperbarui!");
            handleBersihkan();
            loadAntrianHariIni();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            showAlert(Alert.AlertType.ERROR, "Gagal", "Error: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ex) {}
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedIdDaftar == -1) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data dari tabel antrian terlebih dahulu!");
            return;
        }

        // Fitur Batal Periksa: KITA HANYA MENGHAPUS PENDAFTARANNYA SAJA (Biodata pasien tetap ada di arsip klinik)
        String sql = "DELETE FROM pendaftaran WHERE id_daftar = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedIdDaftar);
            ps.executeUpdate();
            
            showAlert(Alert.AlertType.INFORMATION, "Dibatalkan", "Antrian pasien berhasil dibatalkan/dihapus!");
            handleBersihkan();
            loadAntrianHariIni();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Error membatalkan pendaftaran: " + e.getMessage());
        }
    }

    @FXML
    private void handleBersihkan() {
        bersihkanForm();
    }

    private boolean validateInput() {
        if (txtNama.getText().trim().isEmpty() || txtUmur.getText().trim().isEmpty() || 
            cmbGender.getValue() == null || txtKeluhan.getText().trim().isEmpty() || cmbDokter.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Data bertanda penting wajib diisi!");
            return false;
        }
        try { Integer.parseInt(txtUmur.getText().trim()); } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Error Input", "Umur harus berupa angka!"); return false;
        }
        return true;
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int generateNoAntrian() {
    int nextAntrian = 1; // Default jika belum ada antrean hari ini
    
    // Gunakan MAX + 1 agar aman, filter berdasarkan tanggal hari ini
    String sql = "SELECT MAX(no_antrian) AS max_antrian FROM pendaftaran WHERE tanggal = CURDATE()";
    
    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        if (rs.next()) {
            int max = rs.getInt("max_antrian");
            if (max > 0) {
                nextAntrian = max + 1;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return nextAntrian;
}

    private void bersihkanForm() {
    try {
        if (txtNama != null) txtNama.setText("");
        if (txtUmur != null) txtUmur.setText("");
        if (txtNoHP != null) txtNoHP.setText("");
        if (txtAlamat != null) txtAlamat.setText("");
        if (txtKeluhan != null) txtKeluhan.setText("");
        System.out.println("[1/3] DEBUG: Semua TextField & TextArea berhasil dikosongkan.");

        // Pengecekan aman untuk ComboBox Gender
        if (cmbGender != null) {
            cmbGender.getSelectionModel().clearSelection();
            cmbGender.setValue(null);
            System.out.println("[2/3] DEBUG: ComboBox Gender berhasil di-reset.");
        }

        // Pengecekan aman untuk ComboBox Dokter
        if (cmbDokter != null) {
            cmbDokter.getSelectionModel().clearSelection();
            cmbDokter.setValue(null);
            System.out.println("[3/3] DEBUG: ComboBox Dokter berhasil di-reset.");
        }
        
        selectedIdPasien = -1; 
        System.out.println("👉 DEBUG STATUS: Form BERHASIL dibersihkan total tanpa error!");
        
    } catch (Exception e) {
        System.err.println("❌ ERROR TERJADI DI DALAM bersihkanForm(): " + e.getMessage());
        e.printStackTrace();
    }
    System.out.println("=========================================");
}
}