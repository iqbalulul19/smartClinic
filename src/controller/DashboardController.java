package controller;

import java.net.URL;
import java.util.ResourceBundle;

import database.DBConnection;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Pasien;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import util.SceneUtil;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

public class DashboardController implements Initializable {
    
    @FXML private VBox sidebar;
    @FXML private Label logoTitle;
    @FXML private Button btnDashboard, btnPasien, btnDokter, btnPetugas, btnObat;
    private boolean collapsed = false;
    @FXML private VBox vboxMaster, vboxTransaksi, vboxLaporan;
    @FXML private Label lblMaster, lblTransaksi, lblLaporan;
    @FXML private Button btnPendaftaran, btnPemeriksaan, btnRekam, btnPrediksi, btnRiwayatPrediksi, btnLaporanKlinik, btnPoli, btnJadwal;
    
    @FXML private Label lblTotalPasien;
    @FXML private Label lblTotalObat;
    @FXML private Label lblTotalRekamMedis;
    @FXML private Label lblTotalPrediksi;

    @FXML private TableView<Pasien> tablePasienTerbaru;
    @FXML private TableColumn<Pasien, Integer> colId;
    @FXML private TableColumn<Pasien, String> colNama;
    @FXML private TableColumn<Pasien, Integer> colUmur;
    @FXML private TableColumn<Pasien, String> colGender;
    @FXML private TableColumn<Pasien, String> colAlamat;
    @FXML private Button btnLogout;
    @FXML private TextField txtSearch;
    @FXML private Label lblJamTanggal;
    @FXML private Label lblNamaNavbar;
    @FXML private Label lblRoleNavbar;

    private ObservableList<Pasien> masterDataPasien = FXCollections.observableArrayList();
    private boolean isSidebarExpanded = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hitungData();
        loadPasienTerbaru();
        setupPencarianRealTime();
        hitungTotalPrediksiML();
        setupJamRealTime();
        
        // 1. Menghubungkan kolom dengan variabel di model Pasien.java
        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("idPasien"));
            colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
            colUmur.setCellValueFactory(new PropertyValueFactory<>("umur"));
            colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
            colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
            
            loadPasienTerbaru();
        }

        // =======================================================
        // 2. LOGIKA HAK AKSES (MENYEMBUNYIKAN MENU SESUAI ROLE)
        // =======================================================
        if (LoginController.sessionUser != null) {
            String role = LoginController.sessionUser.getRole();

            if (role.equalsIgnoreCase("Pasien")) {
                // Sembunyikan Kategori Master Data
                lblMaster.setVisible(false); lblMaster.setManaged(false);
                btnPasien.setVisible(false); btnPasien.setManaged(false);
                btnDokter.setVisible(false); btnDokter.setManaged(false);
                btnPetugas.setVisible(false); btnPetugas.setManaged(false);
                btnObat.setVisible(false); btnObat.setManaged(false);
                btnRiwayatPrediksi.setVisible(false); btnRiwayatPrediksi.setManaged(false);

                // Sembunyikan Kategori Khusus Dokter
                btnPemeriksaan.setVisible(false); btnPemeriksaan.setManaged(false);
                btnPrediksi.setVisible(false); btnPrediksi.setManaged(false);
                
                // Pasien hanya sisa menu: Dashboard, Pendaftaran, Rekam Medis (Riwayat)

            } else if (role.equalsIgnoreCase("Dokter")) {
                // Dokter tidak butuh mengatur Petugas
                btnPetugas.setVisible(false); btnPetugas.setManaged(false);
                
                // Dokter tidak melayani pendaftaran awal
                btnPendaftaran.setVisible(false); btnPendaftaran.setManaged(false);
            }
        }
        // Rakit ulang tombol agar presisi (letakkan kode ini di dalam initialize)
        setTombolPresisi(btnDashboard, "🏠", "Dashboard");
        setTombolPresisi(btnPasien, "👨‍⚕", "Pasien");
        setTombolPresisi(btnDokter, "🩺", "Dokter");
        setTombolPresisi(btnPetugas, "👩‍💼", "Petugas");
        setTombolPresisi(btnPoli, "🏥", "Poli");
        setTombolPresisi(btnJadwal, "📅", "Jadwal");
        setTombolPresisi(btnObat, "💊", "Obat");
        setTombolPresisi(btnPendaftaran, "📝", "Pendaftaran");
        setTombolPresisi(btnPemeriksaan, "🩻", "Pemeriksaan");
        setTombolPresisi(btnRekam, "📋", "Rekam Medis");
        setTombolPresisi(btnPrediksi, "🧠", "Prediksi ML");
        setTombolPresisi(btnRiwayatPrediksi, "📊", "Hasil & Riwayat");
        setTombolPresisi(btnLaporanKlinik, "📈", "Laporan Klinik");
        setTombolPresisi(btnLogout, "🚪", "Logout");
    }

    private void setupJamRealTime() {
    // Membuat timeline yang berjalan berulang kali
    Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
        LocalDateTime waktuSekarang = LocalDateTime.now();
        
        // Format: NamaHari, Tanggal Bulan Tahun - Jam:Menit:Detik
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss", new Locale("id", "ID"));
        
        lblJamTanggal.setText(waktuSekarang.format(formatter));
    }), new KeyFrame(Duration.seconds(1))); // Update setiap 1 detik
    
    clock.setCycleCount(Animation.INDEFINITE); // Ulangi terus menerus (infinity)
    clock.play(); // Jalankan!
    }

    public void setPenggunaAktif(String nama, String role) {
        lblNamaNavbar.setText("Halo, " + nama);
        lblRoleNavbar.setText(role);
    }

    @FXML
private void toggleSidebar() {
    if (!collapsed) {
        sidebar.setPrefWidth(80);
        
        // Sembunyikan semua label dan hapus ruang kosongnya
        if (logoTitle != null) { logoTitle.setVisible(false); logoTitle.setManaged(false); }
        if (lblMaster != null) { lblMaster.setVisible(false); lblMaster.setManaged(false); }
        if (lblTransaksi != null) { lblTransaksi.setVisible(false); lblTransaksi.setManaged(false); }
        if (lblLaporan != null) { lblLaporan.setVisible(false); lblLaporan.setManaged(false); }
        
        // Ubah tombol menjadi ikon saja
        btnDashboard.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnPasien.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnDokter.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnPetugas.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnPoli.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnJadwal.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnObat.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnPendaftaran.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnPemeriksaan.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnRekam.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnPrediksi.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnRiwayatPrediksi.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        btnLaporanKlinik.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        
        collapsed = true;
    } else {
        sidebar.setPrefWidth(240); 
        
        // Tampilkan kembali judul aplikasi dan grup menu utama
        if (logoTitle != null) { logoTitle.setVisible(true); logoTitle.setManaged(true); }
        if (lblTransaksi != null) { lblTransaksi.setVisible(true); lblTransaksi.setManaged(true); }
        if (lblLaporan != null) { lblLaporan.setVisible(true); lblLaporan.setManaged(true); }
        
        // =======================================================
        // SOLUSI: Pastikan MASTER DATA selalu ditampilkan di sini
        // =======================================================
        if (lblMaster != null) { 
            lblMaster.setVisible(true); 
            lblMaster.setManaged(true); 
        }
        
        // Kembalikan tombol ke mode normal (Ikon + Teks di kanan)
        btnDashboard.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnPasien.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnDokter.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnPetugas.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnObat.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnPoli.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnJadwal.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnPendaftaran.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnPemeriksaan.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnRekam.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnPrediksi.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnRiwayatPrediksi.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnLaporanKlinik.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        
        collapsed = false;
    }
    
    sidebar.requestLayout();
}

    @FXML
    private void openPasien() {
        SceneUtil.openMaximizedWindow("/view/pasien.fxml", "Data Pasien");
    }

    @FXML
    private void openDokter() {
        SceneUtil.openMaximizedWindow("/view/dokter.fxml", "Data Dokter");
    }

    @FXML
    private void openPendaftaran() {
        SceneUtil.openMaximizedWindow("/view/pendaftaran.fxml", "Pendaftaran");
    }

    @FXML
    private void openPemeriksaan() {
        SceneUtil.openMaximizedWindow("/view/pemeriksaan.fxml", "Pemeriksaan");
    }

    @FXML
    private void openPetugas() {
        SceneUtil.openMaximizedWindow("/view/petugas.fxml", "Data Petugas");
    }

    @FXML
    private void openObat() {
        SceneUtil.openMaximizedWindow("/view/obat.fxml", "Data Obat");
    }

    @FXML
    private void openRekam() {
        SceneUtil.openMaximizedWindow("/view/rekam_medis.fxml", "Arsip Rekam Medis");
    }

    @FXML
    private void openPrediksi() {
        SceneUtil.openMaximizedWindow("/view/prediksi.fxml", "Prediksi ML");
    }

    @FXML
    private void openPoli() {
        SceneUtil.openMaximizedWindow("/view/poli.fxml", "Data Poli");
    }

    @FXML
    private void openJadwal() {
        SceneUtil.openMaximizedWindow("/view/jadwal.fxml", "Jadwal");
    }
    
    @FXML
    private void openRiwayatPrediksi() {
        String path = "/view/hasil_riwayat.fxml";
        URL url = getClass().getResource(path);
        
        if (url == null) {
            // Ini akan mencetak lokasi asli folder saat aplikasi berjalan
            System.err.println("GAGAL: File tidak ditemukan di path: " + path);
            System.err.println("Mencoba mencari di: " + System.getProperty("user.dir"));
        } else {
            SceneUtil.openMaximizedWindow(path, "Riwayat Prediksi ML");
        }
    } 

    @FXML
    private void openLaporanKlinik() {
        SceneUtil.openMaximizedWindow("/view/laporan_klinik.fxml", "Laporan Klinik");
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        if (LoginController.sessionUser != null) {
            LoginController.sessionUser.logouta();
            LoginController.sessionUser = null;
        }

        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            javafx.scene.Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setMaximized(false); 
            stage.setResizable(false); 
            stage.sizeToScene();
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hitungData() {
        // 1. Menghitung Total Obat
        String queryObat = "SELECT COUNT(*) AS total FROM obat";
        try (Connection conn = database.DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(queryObat)) {
            if (rs.next()) {
                if (lblTotalObat != null) {
                    lblTotalObat.setText(String.valueOf(rs.getInt("total")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal menghitung obat: " + e.getMessage());
        }

        // 2. Menghitung Total Pasien
        String queryPasien = "SELECT COUNT(*) AS total FROM pasien";
        try (Connection conn = database.DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(queryPasien)) {
            if (rs.next()) {
                if (lblTotalPasien != null) {
                    lblTotalPasien.setText(String.valueOf(rs.getInt("total")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal menghitung pasien: " + e.getMessage());
        }

        // 3. Menghitung Total Rekam Medis
        String queryRekam = "SELECT COUNT(*) AS total FROM rekam_medis";
        try (Connection conn = database.DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(queryRekam)) {
            if (rs.next()) {
                if (lblTotalRekamMedis != null) {
                    lblTotalRekamMedis.setText(String.valueOf(rs.getInt("total")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal menghitung rekam medis: " + e.getMessage());
        }
    }

    private void loadPasienTerbaru() {
    masterDataPasien.clear(); 
    String sql = "SELECT * FROM pasien ORDER BY id_pasien DESC LIMIT 10"; 
    
    try (Connection conn = DBConnection.connect();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
         
        while (rs.next()) {
            Pasien p = new Pasien();
            p.setIdPasien(rs.getInt("id_pasien"));
            p.setNama(rs.getString("nama"));
            p.setUmur(rs.getInt("umur"));
            p.setGender(rs.getString("gender")); 
            p.setAlamat(rs.getString("alamat"));
            
            masterDataPasien.add(p); 
        }
        
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // Fungsi khusus untuk memastikan ikon memiliki "kotak" dengan lebar yang sama (35 pixel)
    private void setTombolPresisi(Button btn, String emoji, String teks) {
        if (btn != null) {
            javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(emoji);
            iconLabel.setPrefWidth(35); 
            iconLabel.setAlignment(javafx.geometry.Pos.CENTER);
            iconLabel.setStyle("-fx-font-size: 18px;"); 
            btn.setGraphic(iconLabel);
            btn.setText(teks);
            btn.setGraphicTextGap(5); 
            btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT); 
        }
    }


    private void setupPencarianRealTime() {
    // 1. Gunakan masterDataPasien sebagai sumber data filter
    FilteredList<Pasien> filteredData = new FilteredList<>(masterDataPasien, b -> true);

    // 2. Pasang pendeteksi ketikan di TextField
    txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(pasien -> {
            if (newValue == null || newValue.isEmpty()) {
                return true; 
            }
            
            String lowerCaseFilter = newValue.toLowerCase();
            
            if (pasien.getNama().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(pasien.getIdPasien()).contains(lowerCaseFilter)) {
                return true;
            } else if (pasien.getAlamat() != null && pasien.getAlamat().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            
            return false;
        });
    });

    // 3. Bungkus ke SortedList agar tabel tetap bisa di-klik untuk diurutkan
    SortedList<Pasien> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(tablePasienTerbaru.comparatorProperty());

    // 4. Masukkan hasil akhirnya ke tabel
    tablePasienTerbaru.setItems(sortedData);
}

    private void hitungTotalPrediksiML() {
    // Query untuk menjumlahkan total baris dari ketiga tabel riwayat prediksi
    String sql = "SELECT " +
                 "(SELECT COUNT(*) FROM prediksi_hasil) + " +
                 "(SELECT COUNT(*) FROM prediksi_jantung) + " +
                 "(SELECT COUNT(*) FROM prediksi_ginjal) AS total_semua_prediksi";
                 
    try (Connection conn = DBConnection.connect();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
         
        if (rs.next()) {
            int total = rs.getInt("total_semua_prediksi");
            lblTotalPrediksi.setText(String.valueOf(total));
        }
        
    } catch (Exception e) {
        System.err.println("Gagal menghitung total prediksi: " + e.getMessage());
        lblTotalPrediksi.setText("0"); 
    }
}
}