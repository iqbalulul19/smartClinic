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
import model.Pemeriksaan;
import model.RekamMedis;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RekamMedisController implements Initializable {

    // TAB 1: PEMERIKSAAN BARU (Belum ada Rekam Medis)
    @FXML private TableView<Pemeriksaan> tablePemeriksaan;
    @FXML private TableColumn<Pemeriksaan, String> colTglPeriksa;
    @FXML private TableColumn<Pemeriksaan, String> colNamaPasien;
    @FXML private TableColumn<Pemeriksaan, String> colDiagnosa;

    // TAB 2: ARSIP REKAM MEDIS
    @FXML private TableView<RekamMedis> tableArsip;
    @FXML private TableColumn<RekamMedis, String> colTglArsip;
    @FXML private TableColumn<RekamMedis, String> colNamaArsip;
    @FXML private TableColumn<RekamMedis, String> colRingkasan;

    // FORM KANAN
    @FXML private TextField txtNama;
    @FXML private TextArea txtDiagnosa;
    @FXML private TextArea txtCatatan;
    @FXML private TextArea txtRingkasan;
    @FXML private Button btnSimpan;

    private int selectedIdPeriksa = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // ========================================================
        // Setup Kolom Tab 1 (Sistem Anti-Error / Null-Safe)
        // ========================================================
        colTglPeriksa.setCellValueFactory(cell -> {
            if (cell.getValue().getTanggalPeriksa() != null) {
                return new SimpleStringProperty(cell.getValue().getTanggalPeriksa().toString());
            } else {
                return new SimpleStringProperty("-"); // Tampilkan strip jika kosong
            }
        });
        
        colNamaPasien.setCellValueFactory(cell -> {
            try {
                // Mencoba mengambil nama dari relasi yang panjang
                String nama = cell.getValue().getPendaftaran().getPasien().getNama();
                return new SimpleStringProperty(nama != null ? nama : "Tanpa Nama");
            } catch (NullPointerException e) {
                // Jika relasi terputus/kosong di tengah jalan, tampilkan ini agar tidak crash
                return new SimpleStringProperty("Data Tidak Lengkap");
            }
        });
        
        colDiagnosa.setCellValueFactory(new PropertyValueFactory<>("diagnosa"));

        // ========================================================
        // Setup Kolom Tab 2 (Sistem Anti-Error / Null-Safe)
        // ========================================================
        colTglArsip.setCellValueFactory(cell -> {
            if (cell.getValue().getTanggal() != null) {
                return new SimpleStringProperty(cell.getValue().getTanggal().toString());
            } else {
                return new SimpleStringProperty("-");
            }
        });
        
        colNamaArsip.setCellValueFactory(cell -> {
            try {
                // Mencoba mengambil nama dari relasi yang super panjang
                String nama = cell.getValue().getPemeriksaan().getPendaftaran().getPasien().getNama();
                return new SimpleStringProperty(nama != null ? nama : "Tanpa Nama");
            } catch (NullPointerException e) {
                return new SimpleStringProperty("Data Tidak Lengkap");
            }
        });
        
        colRingkasan.setCellValueFactory(new PropertyValueFactory<>("ringkasan"));

        // Muat data dari database
        loadPemeriksaanBaru();
        loadArsipTersimpan();
    }

    private void loadPemeriksaanBaru() {
        ObservableList<Pemeriksaan> list = FXCollections.observableArrayList();
        // Mengambil Pemeriksaan yang BELUM ada di tabel rekam_medis
        String sql = "SELECT per.*, pas.nama AS nama_pasien FROM pemeriksaan per " +
                     "JOIN pendaftaran p ON per.id_daftar = p.id_daftar " +
                     "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                     "LEFT JOIN rekam_medis rm ON per.id_periksa = rm.id_periksa " +
                     "WHERE rm.id_rekam IS NULL ORDER BY per.tanggal_periksa DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.Pasien pas = new model.Pasien(); pas.setNama(rs.getString("nama_pasien"));
                Pendaftaran p = new Pendaftaran(); p.setPasien(pas);
                
                Pemeriksaan per = new Pemeriksaan();
                per.setIdPeriksa(rs.getInt("id_periksa"));
                per.setTanggalPeriksa(rs.getDate("tanggal_periksa"));
                per.setDiagnosa(rs.getString("diagnosa"));
                per.setCatatan(rs.getString("catatan"));
                per.setPendaftaran(p);
                
                list.add(per);
            }
            tablePemeriksaan.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadArsipTersimpan() {
        ObservableList<RekamMedis> list = FXCollections.observableArrayList();
        String sql = "SELECT rm.*, per.diagnosa, per.catatan, pas.nama AS nama_pasien " +
                     "FROM rekam_medis rm " +
                     "JOIN pemeriksaan per ON rm.id_periksa = per.id_periksa " +
                     "JOIN pendaftaran p ON per.id_daftar = p.id_daftar " +
                     "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                     "ORDER BY rm.tanggal DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.Pasien pas = new model.Pasien(); pas.setNama(rs.getString("nama_pasien"));
                Pendaftaran p = new Pendaftaran(); p.setPasien(pas);
                Pemeriksaan per = new Pemeriksaan();
                per.setDiagnosa(rs.getString("diagnosa"));
                per.setCatatan(rs.getString("catatan"));
                per.setPendaftaran(p);
                
                RekamMedis rm = new RekamMedis();
                rm.setIdRekam(rs.getInt("id_rekam"));
                rm.setTanggal(rs.getDate("tanggal"));
                rm.setRingkasan(rs.getString("ringkasan"));
                rm.setPemeriksaan(per);
                
                list.add(rm);
            }
            tableArsip.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePilihPemeriksaan() {
        Pemeriksaan selected = tablePemeriksaan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleBersihkan();
            selectedIdPeriksa = selected.getIdPeriksa();
            txtNama.setText(selected.getPendaftaran().getPasien().getNama());
            txtDiagnosa.setText(selected.getDiagnosa());
            txtCatatan.setText(selected.getCatatan());
            
            txtRingkasan.setEditable(true);
            btnSimpan.setDisable(false);
            btnSimpan.setText("📥 Arsipkan Rekam Medis");
        }
    }

    @FXML
    private void handlePilihArsip() {
        RekamMedis selected = tableArsip.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedIdPeriksa = -1; // Kunci agar tidak bisa disave ulang
            
            txtNama.setText(selected.getPemeriksaan().getPendaftaran().getPasien().getNama());
            txtDiagnosa.setText(selected.getPemeriksaan().getDiagnosa());
            txtCatatan.setText(selected.getPemeriksaan().getCatatan());
            txtRingkasan.setText(selected.getRingkasan());
            
            txtRingkasan.setEditable(false);
            btnSimpan.setDisable(true);
            btnSimpan.setText("🔒 Sudah Diarsipkan");
        }
    }

    @FXML
    private void handleSimpan() {
        if (selectedIdPeriksa == -1) {
            showAlert("Peringatan", "Pilih data dari tab 'Perlu Diarsipkan' terlebih dahulu!"); return;
        }
        if (txtRingkasan.getText().trim().isEmpty()) {
            showAlert("Peringatan", "Ringkasan akhir tidak boleh kosong!"); return;
        }

        String sql = "INSERT INTO rekam_medis (id_periksa, tanggal, ringkasan) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, selectedIdPeriksa);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(3, txtRingkasan.getText().trim());
            
            ps.executeUpdate();
            showAlert("Sukses", "Rekam medis berhasil diarsipkan permanen!");
            
            handleBersihkan();
            loadPemeriksaanBaru();
            loadArsipTersimpan();
            
        } catch (SQLException e) {
            showAlert("Error", "Gagal menyimpan rekam medis: " + e.getMessage());
        }
    }

    @FXML
    private void handleBersihkan() {
        selectedIdPeriksa = -1;
        txtNama.clear(); txtDiagnosa.clear(); txtCatatan.clear(); txtRingkasan.clear();
        tablePemeriksaan.getSelectionModel().clearSelection();
        tableArsip.getSelectionModel().clearSelection();
        txtRingkasan.setEditable(true);
        btnSimpan.setDisable(false);
        btnSimpan.setText("📥 Arsipkan Rekam Medis");
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}