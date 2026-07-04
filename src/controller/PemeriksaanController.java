package controller;

import database.DBConnection;
import javafx.beans.property.SimpleIntegerProperty;
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

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class PemeriksaanController implements Initializable {

    // TABEL ANTRIAN (KIRI - TAB 1)
    @FXML private TableView<Pendaftaran> tableAntrian;
    @FXML private TableColumn<Pendaftaran, Integer> colNoAntrian;
    @FXML private TableColumn<Pendaftaran, String> colNamaPasien;
    @FXML private TableColumn<Pendaftaran, String> colKeluhan;

    // TABEL RIWAYAT SELESAI (KIRI - TAB 2)
    @FXML private TableView<Pemeriksaan> tableRiwayat;
    @FXML private TableColumn<Pemeriksaan, Integer> colNoRiwayat;
    @FXML private TableColumn<Pemeriksaan, String> colNamaRiwayat;
    @FXML private TableColumn<Pemeriksaan, String> colDiagnosaRiwayat;

    // FORM KANAN
    @FXML private TextField txtNamaPasien;
    @FXML private TextField txtTekananDarah;
    @FXML private TextField txtGulaDarah;
    @FXML private TextField txtSuhu;
    @FXML private TextField txtBerat;
    @FXML private TextArea txtDiagnosa;
    @FXML private TextArea txtCatatan;
    @FXML private Button btnBersihkan;
    @FXML private Button btnKembali;
    @FXML private Button btnPilihAntrian;
    @FXML private Button btnPilihRiwayat;
    @FXML private Button btnRefreshAntrian;
    @FXML private Button btnRefreshRiwayat;    
    @FXML private Button btnSimpan; 

    @FXML private ComboBox<model.Obat> cmbObat;
    @FXML private TextField txtJumlahObat;
    @FXML private TextField txtDosis;
    
    @FXML private TableView<model.ResepObat> tableResep;
    @FXML private TableColumn<model.ResepObat, String> colResepNama;
    @FXML private TableColumn<model.ResepObat, Integer> colResepJumlah;
    @FXML private TableColumn<model.ResepObat, String> colResepDosis;

    private ObservableList<model.ResepObat> listKeranjangResep = FXCollections.observableArrayList();
    private int selectedIdDaftar = -1;
    private int selectedIdPasien = -1; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup Kolom Tabel Antrian (Model Pendaftaran)
        colNoAntrian.setCellValueFactory(new PropertyValueFactory<>("noAntrian"));
        colNamaPasien.setCellValueFactory(new PropertyValueFactory<>("namaPasien"));
        colKeluhan.setCellValueFactory(new PropertyValueFactory<>("keluhan"));

        // Setup Kolom Tabel Riwayat (Model Pemeriksaan -> Pendaftaran)
        colNoRiwayat.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPendaftaran().getNoAntrian()).asObject());
        colNamaRiwayat.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPendaftaran().getNamaPasien()));
        colDiagnosaRiwayat.setCellValueFactory(new PropertyValueFactory<>("diagnosa"));

        colResepNama.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getObat().getNamaObat()));
        colResepJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colResepDosis.setCellValueFactory(new PropertyValueFactory<>("dosis"));
        tableResep.setItems(listKeranjangResep);

        // Muat Data Kedua Tabel
        loadAntrianBelumDiperiksa();
        loadRiwayatDiperiksa();
        loadResepObat();
    }

    private void loadResepObat() {
        // Siapkan list kosong untuk menampung data dari database
        ObservableList<model.Obat> listObat = FXCollections.observableArrayList();
        
        // Ambil data obat yang stoknya lebih dari 0 saja
        String query = "SELECT * FROM obat WHERE stok > 0 ORDER BY nama_obat ASC"; 

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.Obat obat = new model.Obat();
                obat.setIdObat(rs.getInt("id_obat"));
                obat.setNamaObat(rs.getString("nama_obat"));
                obat.setStok(rs.getInt("stok"));
                obat.setHarga(rs.getDouble("harga"));
                // Jika ada kolom aturan pakai & kfa, bisa di-set juga di sini
                // obat.setAturanPakai(rs.getString("aturan_pakai"));
                // obat.setKodeKfa(rs.getString("kode_kfa"));

                listObat.add(obat);
            }
            
            // Masukkan data yang sudah diambil ke dalam ComboBox
            cmbObat.setItems(listObat);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Gagal memuat data obat untuk ComboBox: " + e.getMessage());
        }
    }

    private void loadAntrianBelumDiperiksa() {
        ObservableList<Pendaftaran> listAntrian = FXCollections.observableArrayList();
        String query = "SELECT p.*, pas.nama AS nama_pasien FROM pendaftaran p " +
                       "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                       "LEFT JOIN pemeriksaan per ON p.id_daftar = per.id_daftar " +
                       "WHERE per.id_periksa IS NULL AND p.tanggal = CURDATE() ORDER BY p.no_antrian ASC";

        if (LoginController.sessionUser != null && LoginController.sessionUser.getRole().equalsIgnoreCase("Dokter")) {
            query = "SELECT p.*, pas.nama AS nama_pasien FROM pendaftaran p " +
                    "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                    "LEFT JOIN pemeriksaan per ON p.id_daftar = per.id_daftar " +
                    "WHERE per.id_periksa IS NULL AND p.tanggal = CURDATE() " +
                    "AND p.id_dokter = " + LoginController.sessionUser.getIdDokter() + " ORDER BY p.no_antrian ASC";
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.Pasien pas = new model.Pasien(); 
                pas.setIdPasien(rs.getInt("id_pasien")); // AMBIL ID PASIEN
                pas.setNama(rs.getString("nama_pasien"));
                
                Pendaftaran p = new Pendaftaran();
                p.setIdDaftar(rs.getInt("id_daftar"));
                p.setNoAntrian(rs.getInt("no_antrian"));
                p.setKeluhan(rs.getString("keluhan"));
                p.setPasien(pas);
                listAntrian.add(p);
            }
            tableAntrian.setItems(listAntrian);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadRiwayatDiperiksa() {
        ObservableList<Pemeriksaan> listRiwayat = FXCollections.observableArrayList();
        String query = "SELECT per.*, p.no_antrian, pas.nama AS nama_pasien FROM pemeriksaan per " +
                       "JOIN pendaftaran p ON per.id_daftar = p.id_daftar " +
                       "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                       "WHERE p.tanggal = CURDATE() ORDER BY p.no_antrian ASC";

        if (LoginController.sessionUser != null && LoginController.sessionUser.getRole().equalsIgnoreCase("Dokter")) {
            query = "SELECT per.*, p.no_antrian, pas.nama AS nama_pasien FROM pemeriksaan per " +
                    "JOIN pendaftaran p ON per.id_daftar = p.id_daftar " +
                    "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                    "WHERE p.tanggal = CURDATE() AND p.id_dokter = " + LoginController.sessionUser.getIdDokter() + " ORDER BY p.no_antrian ASC";
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.Pasien pas = new model.Pasien(); pas.setNama(rs.getString("nama_pasien"));
                Pendaftaran p = new Pendaftaran();
                p.setNoAntrian(rs.getInt("no_antrian"));
                p.setPasien(pas);

                Pemeriksaan per = new Pemeriksaan();
                per.setIdPeriksa(rs.getInt("id_periksa"));
                per.setDiagnosa(rs.getString("diagnosa"));
                per.setTekananDarah(rs.getDouble("tekanan_darah"));
                per.setGulaDarah(rs.getDouble("gula_darah"));
                per.setSuhu(rs.getDouble("suhu"));
                per.setBeratBadan(rs.getDouble("berat_badan"));
                per.setCatatan(rs.getString("catatan"));
                per.setPendaftaran(p); 
                listRiwayat.add(per);
            }
            tableRiwayat.setItems(listRiwayat);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePilihAntrian() {
        System.out.println("==================================================");
        System.out.println("[DEBUG] Baris Tabel Antrian Berhasil Diklik!");
        
        Pendaftaran selected = tableAntrian.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            System.out.println("[DEBUG] GAGAL: Tidak ada data pasien yang terpilih di model tabel (null).");
            return;
        }
        
        System.out.println("[DEBUG] Pasien dideteksi dari tabel: " + selected.getNamaPasien());
        
        // Pengecekan krusial variabel komponen FXML
        if (txtNamaPasien == null) {
            System.out.println("[DEBUG] ERROR KRUSIAL: Variabel 'txtNamaPasien' bernilai NULL!");
            System.out.println("[DEBUG] Solusi: Periksa file pemeriksaan.fxml, pastikan fx:id=\"txtNamaPasien\" sudah terpasang.");
            return;
        }

        // Proses Pemindahan Data ke Form Kanan
        selectedIdDaftar = selected.getIdDaftar();
        txtNamaPasien.setText(selected.getNamaPasien());
        
        if (selected.getPasien() != null) {
            selectedIdPasien = selected.getPasien().getIdPasien();
            System.out.println("[DEBUG] ID Pasien berhasil dikunci: " + selectedIdPasien);
        } else {
            System.out.println("[DEBUG] PERINGATAN: Objek Relasi Pasien kosong (null).");
        }
        
        // Kosongkan sisa form medis inputan sebelumnya
        txtTekananDarah.clear(); txtGulaDarah.clear(); txtSuhu.clear(); 
        txtBerat.clear(); txtDiagnosa.clear(); txtCatatan.clear();
        
        btnSimpan.setDisable(false); 
        btnSimpan.setText("💾 Simpan Pemeriksaan");
        System.out.println("[DEBUG] Form Kanan Berhasil Diupdate untuk: " + selected.getNamaPasien());
    }

    @FXML
    private void handlePilihRiwayat() {
        System.out.println("==================================================");
        System.out.println("[DEBUG] Baris Tabel Riwayat Berhasil Diklik!");
        
        Pemeriksaan selected = tableRiwayat.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedIdDaftar = -1; 
            selectedIdPasien = -1;
            
            txtNamaPasien.setText(selected.getPendaftaran().getNamaPasien());
            txtTekananDarah.setText(String.valueOf(selected.getTekananDarah()));
            txtGulaDarah.setText(String.valueOf(selected.getGulaDarah()));
            txtSuhu.setText(String.valueOf(selected.getSuhu()));
            txtBerat.setText(String.valueOf(selected.getBeratBadan()));
            txtDiagnosa.setText(selected.getDiagnosa());
            txtCatatan.setText(selected.getCatatan());
            
            btnSimpan.setDisable(true); 
            btnSimpan.setText("🔒 Sudah Diperiksa");
            System.out.println("[DEBUG] Detail riwayat sukses ditampilkan.");
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        if (selectedIdDaftar == -1 || selectedIdPasien == -1) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih pasien dari tabel antrian terlebih dahulu!");
            return;
        }

        // 1. Ambil data medis dari form dengan aman
        double tensi = parseDoubleSafe(txtTekananDarah.getText());
        double gula = parseDoubleSafe(txtGulaDarah.getText());
        double suhu = parseDoubleSafe(txtSuhu.getText());
        double berat = parseDoubleSafe(txtBerat.getText());
        String diagnosa = txtDiagnosa.getText() != null ? txtDiagnosa.getText().trim() : "";
        String catatan = txtCatatan.getText() != null ? txtCatatan.getText().trim() : "";

        Connection conn = DBConnection.connect();
        try {
            conn.setAutoCommit(false); // MULAI TRANSAKSI DATABASE KETAT

            // ========================================================
            // TUGAS 1: Simpan Pemeriksaan & Ambil ID Barunya
            // ========================================================
            // Asumsi query pemeriksaanmu HANYA pakai id_daftar (tanpa tanggal) sesuai perbaikan sebelumnya
            String sqlPemeriksaan = "INSERT INTO pemeriksaan (id_daftar, tanggal_periksa, tekanan_darah, gula_darah, suhu, berat_badan, diagnosa, catatan) VALUES (?, CURDATE(), ?, ?, ?, ?, ?, ?)";
            int idPeriksaBaru = -1;

            // Statement.RETURN_GENERATED_KEYS sangat penting untuk mengambil ID yang baru di-generate MySQL
            try (PreparedStatement ps1 = conn.prepareStatement(sqlPemeriksaan, Statement.RETURN_GENERATED_KEYS)) {
                ps1.setInt(1, selectedIdDaftar);
                ps1.setDouble(2, tensi);
                ps1.setDouble(3, gula);
                ps1.setDouble(4, suhu);
                ps1.setDouble(5, berat);
                ps1.setString(6, diagnosa);
                ps1.setString(7, catatan);
                ps1.executeUpdate();

                // Tarik ID Periksa-nya!
                try (ResultSet rsKeys = ps1.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idPeriksaBaru = rsKeys.getInt(1);
                    }
                }
            }

            if (idPeriksaBaru == -1) {
                throw new SQLException("Gagal mendapatkan ID Pemeriksaan baru dari database.");
            }

            // ========================================================
            // TUGAS 2: Sinkronisasi ke Tabel Pasien (Form ML)
            // ========================================================
            String sqlUpdatePasien = "UPDATE pasien SET gula_darah = ?, tekanan_darah = ? WHERE id_pasien = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlUpdatePasien)) {
                ps2.setDouble(1, gula);
                ps2.setDouble(2, tensi);
                ps2.setInt(3, selectedIdPasien);
                ps2.executeUpdate();
            }

            // ========================================================
            // TUGAS 3: Simpan Resep & Potong Stok Obat Sekaligus
            // ========================================================
            if (!listKeranjangResep.isEmpty()) {
                String sqlResep = "INSERT INTO resep_obat (id_periksa, id_obat, jumlah, dosis) VALUES (?, ?, ?, ?)";
                String sqlPotongStok = "UPDATE obat SET stok = stok - ? WHERE id_obat = ?";

                try (PreparedStatement psResep = conn.prepareStatement(sqlResep);
                     PreparedStatement psStok = conn.prepareStatement(sqlPotongStok)) {

                    // Lakukan perulangan untuk setiap obat yang ada di keranjang layar
                    for (model.ResepObat resep : listKeranjangResep) {
                        
                        // Siapkan antrean simpan resep
                        psResep.setInt(1, idPeriksaBaru); // Pakai ID Periksa yang didapat dari Tugas 1
                        psResep.setInt(2, resep.getObat().getIdObat());
                        psResep.setInt(3, resep.getJumlah());
                        psResep.setString(4, resep.getDosis());
                        psResep.addBatch(); // Masukkan ke keranjang antrean SQL

                        // Siapkan antrean potong stok
                        psStok.setInt(1, resep.getJumlah());
                        psStok.setInt(2, resep.getObat().getIdObat());
                        psStok.addBatch();
                    }

                    // Tembakkan semua query antrean secara bersamaan (Sangat cepat & efisien!)
                    psResep.executeBatch();
                    psStok.executeBatch();
                }
            }

            // JIKA SEMUA TUGAS 1, 2, DAN 3 BERHASIL TANPA ERROR, SIMPAN PERMANEN!
            conn.commit(); 
            
            System.out.println("[DEBUG] TRANSAKSI SUKSES: Pemeriksaan, Update ML, dan Potong Stok Resep berhasil!");
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pemeriksaan selesai dan stok obat telah diperbarui otomatis!");
            
            // Bersihkan form dan keranjang untuk pasien berikutnya
            listKeranjangResep.clear(); 
            loadAntrianBelumDiperiksa();
            loadRiwayatDiperiksa();
            handleBersihkan(); 

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Batalin semuanya kalau ada yang gagal!
            } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Menyimpan", "Terjadi kesalahan sistem: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
    
    @FXML
    private void handleBersihkan() {
        selectedIdDaftar = -1;
        selectedIdPasien = -1;
        txtNamaPasien.clear(); txtTekananDarah.clear(); txtGulaDarah.clear();
        txtSuhu.clear(); txtBerat.clear(); txtDiagnosa.clear(); txtCatatan.clear();
        
        tableResep.getSelectionModel().clearSelection();
        tableRiwayat.getSelectionModel().clearSelection();
        
        btnSimpan.setDisable(false);
        btnSimpan.setText("💾 Simpan Pemeriksaan");
    }

    private double parseDoubleSafe(String input) {
        if (input == null || input.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(input.trim()); } catch (NumberFormatException e) { return 0.0; }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    @FXML
    private void handleKembali(javafx.event.ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleTambahKeKeranjang(ActionEvent event) {
        model.Obat obatTerpilih = cmbObat.getValue();
        if (obatTerpilih == null || txtJumlahObat.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih obat dan isi jumlahnya!");
            return;
        }

        try {
            int jumlah = Integer.parseInt(txtJumlahObat.getText());
            
            // Cek stok (Opsional tapi sangat disarankan)
            if (jumlah > obatTerpilih.getStok()) {
                showAlert(Alert.AlertType.WARNING, "Stok Kurang", "Stok " + obatTerpilih.getNamaObat() + " hanya sisa " + obatTerpilih.getStok());
                return;
            }

            model.ResepObat resep = new model.ResepObat();
            resep.setObat(obatTerpilih);
            resep.setJumlah(jumlah);
            resep.setDosis(txtDosis.getText());
            
            // Masukkan ke keranjang UI
            listKeranjangResep.add(resep);
            
            // Kosongkan field input resep
            cmbObat.getSelectionModel().clearSelection();
            txtJumlahObat.clear();
            txtDosis.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Jumlah obat harus berupa angka!");
        }
    }
}