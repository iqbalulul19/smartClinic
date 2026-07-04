package controller;

// Import Model & Database
import database.DBConnection;
import model.Pasien;
import model.Prediksi;
import model.RiwayatPrediksi;
import service.MLService;

// Import JavaFX (UI)
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Import iText PDF
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

// Import Bawaan Java (File, SQL, Waktu, dll)
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


public class PrediksiController implements Initializable {

    @FXML private ComboBox<Pasien> cmbPasien;
    @FXML private Label lblHasil;

    // Deklarasi FXML untuk Penyakit Diabetes (8 TextField)
    @FXML private TextField txtPregnancies, txtGlucose, txtBloodPressure;
    @FXML private TextField txtSkinThickness, txtInsulin, txtBMI, txtPedigree, txtAge;

    // Deklarasi FXML untuk Penyakit Jantung (14 TextField)
    @FXML private TextField txtJantungAge, txtJantungSex, txtJantungDataset, txtJantungCp, txtJantungTrestbps;
    @FXML private TextField txtJantungChol, txtJantungFbs, txtJantungRestecg, txtJantungThalch, txtJantungExang;
    @FXML private TextField txtJantungOldpeak, txtJantungSlope, txtJantungCa, txtJantungThal;

    // Deklarasi FXML untuk Penyakit Ginjal (24 TextField)
    @FXML private TextField txtGinjalAge, txtGinjalBp, txtGinjalSg, txtGinjalAl, txtGinjalSu, txtGinjalRbc;
    @FXML private TextField txtGinjalPc, txtGinjalPcc, txtGinjalBa, txtGinjalBgr, txtGinjalBu, txtGinjalSc;
    @FXML private TextField txtGinjalSod, txtGinjalPot, txtGinjalHemo, txtGinjalPcv, txtGinjalWc, txtGinjalRc;
    @FXML private TextField txtGinjalHtn, txtGinjalDm, txtGinjalCad, txtGinjalAppet, txtGinjalPe, txtGinjalAne;

    @FXML private TableView<RiwayatPrediksi> tableRiwayat;
    @FXML private TableColumn<RiwayatPrediksi, String> colTanggal;
    @FXML private TableColumn<RiwayatPrediksi, String> colNama;
    @FXML private TableColumn<RiwayatPrediksi, String> colHasil;
    @FXML private TableColumn<RiwayatPrediksi, String> colPasien;
    @FXML private TableColumn<RiwayatPrediksi, String> colDetail;
    @FXML private ObservableList<RiwayatPrediksi> listRiwayatPrediksi = FXCollections.observableArrayList();

     // Panggil asisten Machine Learning kita
    private MLService mlService = new MLService();

    @Override
public void initialize(URL url, ResourceBundle rb) {
    setupTabel();
    loadPasien();
    loadRiwayat();

    // FITUR AUTO-FILL: Mengisi otomatis umur, gender, dan data medis
    cmbPasien.setOnAction(e -> {
        Pasien p = cmbPasien.getValue();
        if (p != null) {
            String umurPasien = String.valueOf(p.getUmur());
            
            // 1. Isi Umur ke semua tab
            txtAge.setText(umurPasien);
            txtJantungAge.setText(umurPasien);
            txtGinjalAge.setText(umurPasien);
            
            // 2. Isi Jenis Kelamin khusus ke Tab Jantung
            String jk = p.getGender();
            if (jk != null) {
                if (jk.equalsIgnoreCase("L") || jk.equalsIgnoreCase("Laki-laki") || jk.equalsIgnoreCase("Pria") || jk.equalsIgnoreCase("Male")) {
                    txtJantungSex.setText("1");
                } 
                else if (jk.equalsIgnoreCase("P") || jk.equalsIgnoreCase("Perempuan") || jk.equalsIgnoreCase("Wanita") || jk.equalsIgnoreCase("Female")) {
                    txtJantungSex.setText("0");
                }
            }

            txtGlucose.setText("");
            txtBloodPressure.setText("");

            // 4. Baru tarik data dari database
            tarikDataMedisPasien(p.getIdPasien());
        }
    });
}

private void tarikDataMedisPasien(int idPasien) {
    String sql = "SELECT gula_darah, tekanan_darah FROM pasien WHERE id_pasien = ?";
    
    try (Connection conn = database.DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setInt(1, idPasien);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            String gula = rs.getString("gula_darah");
            String tensi = rs.getString("tekanan_darah");
            
            // Masukkan ke dalam TextField di form Diabetes (jika datanya ada/tidak null)
            if (gula != null && !gula.isEmpty()) {
                txtGlucose.setText(gula);
            }
            if (tensi != null && !tensi.isEmpty()) {
                txtBloodPressure.setText(tensi);
            }
        }
    } catch (Exception e) {
        System.err.println("Gagal menarik data medis: " + e.getMessage());
    }
}

    // Pastikan kamu memiliki ObservableList ini di bagian atas class:
// private ObservableList<RiwayatPrediksi> listRiwayatPrediksi = FXCollections.observableArrayList();

private void setupTabel() {
    // Hubungkan kolom dengan model RiwayatPrediksi
    // Pastikan fx:id kolom di prediksi.fxml sudah di-inject dengan @FXML di bagian atas
    if (colTanggal != null) colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
    if (colPasien != null) colPasien.setCellValueFactory(new PropertyValueFactory<>("namaPasien"));
    if (colHasil != null) colHasil.setCellValueFactory(new PropertyValueFactory<>("hasilPrediksi"));
    if (colDetail != null) colDetail.setCellValueFactory(new PropertyValueFactory<>("detail"));
}

private void loadRiwayat() {
    if (listRiwayatPrediksi == null) {
        listRiwayatPrediksi = FXCollections.observableArrayList();
    }
    listRiwayatPrediksi.clear();
    
    String sql = "SELECT pr.tanggal, p.nama, pr.hasil_prediksi FROM prediksi_hasil pr " +
                 "JOIN pasien p ON pr.id_pasien = p.id_pasien ORDER BY pr.tanggal DESC";
    
    try (Connection conn = database.DBConnection.connect(); 
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        
        while (rs.next()) {
            String tgl = rs.getString("tanggal") != null ? rs.getString("tanggal") : "-";
            String nama = rs.getString("nama") != null ? rs.getString("nama") : "Tanpa Nama";
            String hasil = rs.getString("hasil_prediksi") != null ? rs.getString("hasil_prediksi") : "-";
            
            // Masukkan data ke model. Karena model meminta 4 parameter (efek perubahan kemarin),
            // cukup isi parameter ke-4 dengan string kosong "" saja, tidak perlu ambil pr.detail dari DB.
            listRiwayatPrediksi.add(new RiwayatPrediksi(tgl, nama, hasil, ""));
        }
        
        // Pasang ke tabel
        if (tableRiwayat != null) {
            tableRiwayat.setItems(listRiwayatPrediksi);
        }
        
    } catch (Exception e) { 
        System.err.println("Gagal memuat riwayat di halaman Prediksi: " + e.getMessage());
    }
}

    private void loadPasien() {
    ObservableList<Pasien> listPasien = FXCollections.observableArrayList();
    
    // Query ini HANYA akan mengambil pasien yang:
    // 1. Mendaftar pada hari ini (CURDATE)
    // 2. Sudah memiliki data di tabel 'pemeriksaan'
    String sql = "SELECT DISTINCT p.id_pasien, p.nama, p.umur, p.gender " +
                 "FROM pasien p " +
                 "JOIN pendaftaran pd ON p.id_pasien = pd.id_pasien " +
                 "JOIN pemeriksaan pm ON pd.id_daftar = pm.id_daftar " +
                 "WHERE pd.tanggal = CURDATE()";
                 
    try (Connection conn = DBConnection.connect();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
         
        while (rs.next()) {
            Pasien p = new Pasien();
            p.setIdPasien(rs.getInt("id_pasien"));
            p.setNama(rs.getString("nama"));
            p.setUmur(rs.getInt("umur"));
            
            // Jika struktur DB kamu menggunakan nama kolom 'jenis_kelamin', 
            // pastikan ini diganti dari 'gender' menjadi 'jenis_kelamin' sesuai DB aslimu.
            p.setGender(rs.getString("gender")); 
            
            listPasien.add(p);
        }
        
        // Memasukkan daftar pasien yang sudah tersaring ke dalam ComboBox
        cmbPasien.setItems(listPasien);
        
    } catch (Exception e) { 
        System.err.println("Gagal memuat pasien untuk ML: " + e.getMessage());
        e.printStackTrace(); 
    }
}

    @FXML
    private void handlePrediksiDiabetes() {
        Pasien selectedPasien = cmbPasien.getValue();
        
        if (selectedPasien == null) {
            lblHasil.setText("Pilih pasien terlebih dahulu!");
            lblHasil.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // 1. Buat Objek Prediksi berdasarkan pasien yang dipilih (Penerapan OOP!)
        Prediksi prediksiPasien = new Prediksi(selectedPasien);

        // 2. Minta objek tersebut memproses datanya ke ML
        prediksiPasien.jalankanPrediksiML("diabetes", 
            txtPregnancies.getText(), txtGlucose.getText(), txtBloodPressure.getText(), 
            txtSkinThickness.getText(), txtInsulin.getText(), txtBMI.getText(), 
            txtPedigree.getText(), txtAge.getText()
        );

        // 3. Ambil hasilnya dari objek
        String hasil = prediksiPasien.getHasilPrediksi();
        
        // Cetak log di terminal (Memanggil method tampilHasil sesuai UML)
        prediksiPasien.tampilHasil();

        if (hasil != null && !hasil.startsWith("Error")) {
            // Menyimpan ke tabel internal riwayat prediksi
            simpanKeRiwayat(selectedPasien.getIdPasien(), hasil);
            updateTabelPemeriksaanOtomatis(selectedPasien.getIdPasien(), hasil);
        }

        // 5. Tampilkan ke layar
        if (hasil != null) {
            lblHasil.setText(hasil);
            lblHasil.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
        } else {
            lblHasil.setText("Gagal mendapatkan hasil dari mesin.");
            lblHasil.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handlePrediksiJantung() {
        String hasil = mlService.predict("jantung", 
            txtJantungAge.getText(), txtJantungSex.getText(), txtJantungDataset.getText(), txtJantungCp.getText(), 
            txtJantungTrestbps.getText(), txtJantungChol.getText(), txtJantungFbs.getText(), txtJantungRestecg.getText(),
            txtJantungThalch.getText(), txtJantungExang.getText(), txtJantungOldpeak.getText(), txtJantungSlope.getText(),
            txtJantungCa.getText(), txtJantungThal.getText()
        );
        
        if (hasil != null && !hasil.startsWith("Error")) {
            Pasien selectedPasien = cmbPasien.getValue();
            if (selectedPasien != null) {
                simpanKeRiwayat(selectedPasien.getIdPasien(), hasil);
            }
        }
    }

    @FXML
    private void handlePrediksiGinjal() {
        String hasil = mlService.predict("ginjal", 
            txtGinjalAge.getText(), txtGinjalBp.getText(), txtGinjalSg.getText(), txtGinjalAl.getText(), txtGinjalSu.getText(), 
            txtGinjalRbc.getText(), txtGinjalPc.getText(), txtGinjalPcc.getText(), txtGinjalBa.getText(), txtGinjalBgr.getText(), 
            txtGinjalBu.getText(), txtGinjalSc.getText(), txtGinjalSod.getText(), txtGinjalPot.getText(), txtGinjalHemo.getText(), 
            txtGinjalPcv.getText(), txtGinjalWc.getText(), txtGinjalRc.getText(), txtGinjalHtn.getText(), txtGinjalDm.getText(), 
            txtGinjalCad.getText(), txtGinjalAppet.getText(), txtGinjalPe.getText(), txtGinjalAne.getText()
        );

        if (hasil != null && !hasil.startsWith("Error")) {
            Pasien selectedPasien = cmbPasien.getValue();
            if (selectedPasien != null) {
                simpanRiwayatGinjal(selectedPasien.getIdPasien(), 
                    new String[]{
                        txtGinjalAge.getText(), txtGinjalBp.getText(), txtGinjalSg.getText(), txtGinjalAl.getText(), txtGinjalSu.getText(), 
                        txtGinjalRbc.getText(), txtGinjalPc.getText(), txtGinjalPcc.getText(), txtGinjalBa.getText(), txtGinjalBgr.getText(), 
                        txtGinjalBu.getText(), txtGinjalSc.getText(), txtGinjalSod.getText(), txtGinjalPot.getText(), txtGinjalHemo.getText(), 
                        txtGinjalPcv.getText(), txtGinjalWc.getText(), txtGinjalRc.getText(), txtGinjalHtn.getText(), txtGinjalDm.getText(), 
                        txtGinjalCad.getText(), txtGinjalAppet.getText(), txtGinjalPe.getText(), txtGinjalAne.getText(), hasil
                    }
                );
            }
        }
    }


    private void simpanKeRiwayat(int idPasien, String hasil) {
        String sql = "INSERT INTO prediksi_hasil (id_pasien, tanggal, pregnancies, glucose, blood_pressure, skin_thickness, insulin, bmi, pedigree, age, hasil_prediksi) " +
                     "VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPasien);
            
            // Ambil data dari TextField, gunakan helper agar tidak error jika kosong
            // PENTING: Sesuaikan nama variabel txtGlucose, dll dengan nama fx:id milikmu
            ps.setDouble(2, parseDoubleSafe(txtPregnancies.getText())); 
            ps.setDouble(3, parseDoubleSafe(txtGlucose.getText()));
            ps.setDouble(4, parseDoubleSafe(txtBloodPressure.getText()));
            ps.setDouble(5, parseDoubleSafe(txtSkinThickness.getText()));
            ps.setDouble(6, parseDoubleSafe(txtInsulin.getText()));
            ps.setDouble(7, parseDoubleSafe(txtBMI.getText()));
            ps.setDouble(8, parseDoubleSafe(txtPedigree.getText()));
            ps.setDouble(9, parseDoubleSafe(txtAge.getText()));
            
            ps.setString(10, hasil); // Hasil prediksi Python
            
            ps.executeUpdate();
            System.out.println("[DEBUG] Data parameter medis dan hasil prediksi berhasil disimpan utuh!");
            
            // Refresh tabel riwayat
            loadRiwayat(); 
            
        } catch (Exception e) {
            System.err.println("Gagal menyimpan ke riwayat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double parseDoubleSafe(String input) {
        if (input == null || input.trim().isEmpty()) return 0.0;
        try { 
            return Double.parseDouble(input.trim()); 
        } catch (NumberFormatException e) { 
            return 0.0; 
        }
    }

    @FXML
    private void simpanRiwayatJantung(int idPasien, String[] params) {
        // params urutannya: age, sex, dataset, cp, trestbps, chol, fbs, restecg, thalch, exang, oldpeak, slope, ca, thal, HASIL
        String sql = "INSERT INTO prediksi_jantung (id_pasien, age, sex, dataset, cp, trestbps, chol, fbs, restecg, thalch, exang, oldpeak, slope, ca, thal, hasil_prediksi) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPasien);
            for(int i = 0; i < 14; i++) {
                ps.setDouble(i + 2, Double.parseDouble(params[i]));
            }
            ps.setString(16, params[14]); // Index ke-14 adalah hasil prediksinya
            ps.executeUpdate();
            loadRiwayat(); 
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void simpanRiwayatGinjal(int idPasien, String[] params) {
        // params isinya 24 parameter + 1 hasil prediksi
        String sql = "INSERT INTO prediksi_ginjal (id_pasien, age, bp, sg, al, su, rbc, pc, pcc, ba, bgr, bu, sc, sod, pot, hemo, pcv, wc, rc, htn, dm, cad, appet, pe, ane, hasil_prediksi) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPasien);
            for(int i = 0; i < 24; i++) {
                ps.setDouble(i + 2, Double.parseDouble(params[i]));
            }
            ps.setString(26, params[24]); // Hasil prediksi
            ps.executeUpdate();
            loadRiwayat(); 
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleBersihkan() {
        // Kosongkan ComboBox
        cmbPasien.getSelectionModel().clearSelection();
        
        // Kosongkan Form Diabetes
        txtPregnancies.clear(); txtGlucose.clear(); txtBloodPressure.clear();
        txtSkinThickness.clear(); txtInsulin.clear(); txtBMI.clear(); 
        txtPedigree.clear(); txtAge.clear();
        
        // Kosongkan Form Jantung
        txtJantungAge.clear(); txtJantungSex.clear(); txtJantungDataset.clear(); txtJantungCp.clear();
        txtJantungTrestbps.clear(); txtJantungChol.clear(); txtJantungFbs.clear(); txtJantungRestecg.clear();
        txtJantungThalch.clear(); txtJantungExang.clear(); txtJantungOldpeak.clear(); txtJantungSlope.clear();
        txtJantungCa.clear(); txtJantungThal.clear();
        
        // Kosongkan Form Ginjal (Contoh sebagian, tambahkan sisanya jika diperlukan)
        txtGinjalAge.clear(); txtGinjalBp.clear(); txtGinjalSg.clear(); txtGinjalAl.clear(); txtGinjalSu.clear();
        txtGinjalRbc.clear(); txtGinjalPc.clear(); txtGinjalPcc.clear(); txtGinjalBa.clear(); txtGinjalBgr.clear();
        txtGinjalBu.clear(); txtGinjalSc.clear(); txtGinjalSod.clear(); txtGinjalPot.clear(); txtGinjalHemo.clear();
        txtGinjalPcv.clear(); txtGinjalWc.clear(); txtGinjalRc.clear(); txtGinjalHtn.clear(); txtGinjalDm.clear();
        txtGinjalCad.clear(); txtGinjalAppet.clear(); txtGinjalPe.clear(); txtGinjalAne.clear();

        // Reset Label Hasil
        lblHasil.setText("Belum ada prediksi diproses");
        lblHasil.setStyle("-fx-text-fill: #2c3e50;");
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }

    @FXML
    private void handleCetakHasilPasien(ActionEvent event) {
        // 1. Validasi: Pastikan pasien sudah dipilih
        Pasien pasienTerpilih = cmbPasien.getValue();
        if (pasienTerpilih == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Silakan pilih data pasien terlebih dahulu!");
            alert.showAndWait();
            return;
        }

        // 2. Validasi: Pastikan sudah melakukan prediksi
        String hasilPrediksi = lblHasil.getText();
        if (hasilPrediksi.equals("Belum ada prediksi diproses...") || hasilPrediksi.startsWith("Error")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Lakukan proses prediksi terlebih dahulu sebelum mencetak hasil!");
            alert.showAndWait();
            return;
        }

        // 3. Siapkan FileChooser untuk menyimpan PDF
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Surat Hasil Prediksi");
        fileChooser.setInitialFileName("Hasil_Prediksi_" + pasienTerpilih.getNama().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF", "*.pdf"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // 4. Inisialisasi Dokumen iText PDF
                Document document = new Document(PageSize.A5, 30, 30, 30, 30); // Ukuran A5 cocok untuk surat keterangan
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Font Styles
                Font fontJudul = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
                Font fontSubJudul = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.DARK_GRAY);
                Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
                Font fontBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
                Font fontHasil = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED);

                // 5. KOP SURAT KLINIK
                Paragraph kop = new Paragraph("SMART CLINIC SYSTEM", fontJudul);
                kop.setAlignment(Element.ALIGN_CENTER);
                document.add(kop);

                Paragraph subKop = new Paragraph("Modul Analisis & Prediksi Kesehatan Otomatis", fontSubJudul);
                subKop.setAlignment(Element.ALIGN_CENTER);
                document.add(subKop);

                document.add(new Paragraph("______________________________________________________\n\n", fontNormal));

                // 6. JUDUL DOKUMEN
                Paragraph judulDokumen = new Paragraph("SURAT HASIL PEMERIKSAAN SISTEM", fontBold);
                judulDokumen.setAlignment(Element.ALIGN_CENTER);
                document.add(judulDokumen);
                document.add(new Paragraph("\n", fontNormal));

                // 7. TABEL DATA PASIEN
                PdfPTable tablePasien = new PdfPTable(2);
                tablePasien.setWidthPercentage(100);
                tablePasien.setWidths(new float[]{30f, 70f}); // Proporsi lebar kolom

                // Hilangkan border agar terlihat seperti form bersih
                tablePasien.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                tablePasien.addCell(new Phrase("ID Pasien", fontNormal));
                tablePasien.addCell(new Phrase(": " + pasienTerpilih.getIdPasien(), fontNormal));

                tablePasien.addCell(new Phrase("Nama Pasien", fontNormal));
                tablePasien.addCell(new Phrase(": " + pasienTerpilih.getNama(), fontNormal));

                tablePasien.addCell(new Phrase("Umur", fontNormal));
                tablePasien.addCell(new Phrase(": " + pasienTerpilih.getUmur() + " Tahun", fontNormal));

                tablePasien.addCell(new Phrase("Jenis Kelamin", fontNormal));
                String jkTeks = pasienTerpilih.getGender().equalsIgnoreCase("1") || pasienTerpilih.getGender().equalsIgnoreCase("Laki-laki") ? "Laki-laki" : "Perempuan";
                tablePasien.addCell(new Phrase(": " + jkTeks, fontNormal));

                document.add(tablePasien);
                document.add(new Paragraph("\n\n", fontNormal));

                // 8. KOTAK HASIL PREDIKSI MACHINE LEARNING
                PdfPTable tableHasil = new PdfPTable(1);
                tableHasil.setWidthPercentage(100);

                PdfPCell cellHasilHeader = new PdfPCell(new Phrase("KESIMPULAN PREDIKSI MACHINE LEARNING", fontBold));
                cellHasilHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cellHasilHeader.setPadding(8);
                cellHasilHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableHasil.addCell(cellHasilHeader);

                PdfPCell cellHasilKonten = new PdfPCell(new Phrase(hasilPrediksi.toUpperCase(), fontHasil));
                cellHasilKonten.setPadding(15);
                cellHasilKonten.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableHasil.addCell(cellHasilKonten);

                document.add(tableHasil);
                document.add(new Paragraph("\n\n", fontNormal));

                // 9. TANDA TANGAN DOKTER / PETUGAS
                String tanggalCetak = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm"));
                Paragraph tgl = new Paragraph("Semarang, " + tanggalCetak, fontNormal);
                tgl.setAlignment(Element.ALIGN_RIGHT);
                document.add(tgl);

                document.add(new Paragraph("\n\n\n", fontNormal)); // Ruang tanda tangan

                Paragraph namaTtd = new Paragraph("( Petugas Medis Smart Clinic )", fontBold);
                namaTtd.setAlignment(Element.ALIGN_RIGHT);
                document.add(namaTtd);

                // Closing Dokumen
                document.close();

                // Tampilkan Notifikasi Sukses
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sukses");
                alert.setHeaderText(null);
                alert.setContentText("Surat hasil prediksi pasien berhasil dicetak!");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Gagal mencetak dokumen");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void updateTabelPemeriksaanOtomatis(int idPasien, String hasilML) {
        String tingkatResiko = "RENDAH"; 
        if (hasilML.toUpperCase().contains("TINGGI")) {
            tingkatResiko = "TINGGI";
        } else if (hasilML.toUpperCase().contains("SEDANG") || hasilML.toUpperCase().contains("MODERATE")) {
            tingkatResiko = "SEDANG";
        }

        // 2. Query SQL untuk mengupdate tabel pemeriksaan hari ini milik pasien tersebut
        String sqlUpdate = "UPDATE pemeriksaan per " +
                           "JOIN pendaftaran p ON per.id_daftar = p.id_daftar " +
                           "SET per.hasil_prediksi = ?, per.tingkat_resiko = ? " +
                           "WHERE p.id_pasien = ? AND p.tanggal = CURDATE()";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            
            ps.setString(1, hasilML);        
            ps.setString(2, tingkatResiko);   
            ps.setInt(3, idPasien);
            
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("[DEBUG] Sukses! Hasil ML & Tingkat Risiko berhasil disinkronkan ke rekam medis pemeriksaan hari ini.");
            } else {
                System.out.println("[DEBUG] Peringatan: Hasil ML tidak disalin karena pasien belum didaftarkan di antrean pemeriksaan hari ini.");
            }

        } catch (SQLException e) {
            System.err.println("[DEBUG] Gagal memperbarui data tabel pemeriksaan: " + e.getMessage());
            e.printStackTrace();
        }
    }
}