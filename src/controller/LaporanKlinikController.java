package controller;

import database.DBConnection;

import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.LaporanRiwayat;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import javafx.stage.FileChooser;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LaporanKlinikController implements Initializable {

    // Label untuk Summary Cards
    @FXML private Label lblTotalPasien;
    @FXML private Label lblTotalPrediksi;
    @FXML private Label lblTotalObat;

    // Tabel Laporan
    @FXML private TableView<?> tableLaporan;
    @FXML private TableColumn<?, ?> colTanggal;
    @FXML private TableColumn<?, ?> colPasien;
    @FXML private TableColumn<?, ?> colAktivitas;
    @FXML private TableView<LaporanRiwayat> tableRiwayat;
    @FXML private TableColumn<LaporanRiwayat, String> colNama;
    @FXML private TableColumn<LaporanRiwayat, String> colHasil;

    @FXML private DatePicker dpMulai;
    @FXML private DatePicker dpSelesai;
    @FXML private BarChart<String, Number> barChartKunjungan;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hitungStatistikKlinik();
        loadRiwayatTerbaru(); // Memanggil metode untuk memuat riwayat terbaru
        // loadTabelLaporan(); // Kita buat nanti setelah kamu tentukan isi tabelnya
        dpMulai.setValue(LocalDate.now().minusDays(7));
        dpSelesai.setValue(LocalDate.now());
    
    // Tarik data grafik pertama kali halaman dibuka
    loadChartData(dpMulai.getValue(), dpSelesai.getValue());
    }

    private void hitungStatistikKlinik() {
        // Query untuk menghitung jumlah baris di masing-masing tabel
        String sqlPasien = "SELECT COUNT(*) AS total FROM pasien";
        String sqlPrediksi = "SELECT COUNT(*) AS total FROM prediksi_hasil";
        String sqlObat = "SELECT COUNT(*) AS total FROM obat"; // Sesuaikan nama tabel obatmu

        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement()) {

            // Hitung Pasien
            ResultSet rsPasien = st.executeQuery(sqlPasien);
            if (rsPasien.next()) lblTotalPasien.setText(String.valueOf(rsPasien.getInt("total")));

            // Hitung Prediksi/Pemeriksaan
            ResultSet rsPrediksi = st.executeQuery(sqlPrediksi);
            if (rsPrediksi.next()) lblTotalPrediksi.setText(String.valueOf(rsPrediksi.getInt("total")));

            // Hitung Obat
            ResultSet rsObat = st.executeQuery(sqlObat);
            if (rsObat.next()) lblTotalObat.setText(String.valueOf(rsObat.getInt("total")));

        } catch (Exception e) {
            System.err.println("Gagal menghitung statistik: " + e.getMessage());
        }
    }

    @FXML
    private void handleCetakLaporanUtama(ActionEvent event) {
        // Logika iText PDF untuk Laporan Global Klinik akan diletakkan di sini
        System.out.println("Tombol Cetak PDF Laporan Global ditekan!");
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void loadRiwayatTerbaru() {
    ObservableList<LaporanRiwayat> listRiwayat = FXCollections.observableArrayList();
    
    // Ambil 15 riwayat terbaru dengan mengurutkan tanggal secara descending (terbaru di atas)
    String query = "SELECT p.tanggal, pas.nama, per.diagnosa " +
                   "FROM pemeriksaan per " +
                   "JOIN pendaftaran p ON per.id_daftar = p.id_daftar " +
                   "JOIN pasien pas ON p.id_pasien = pas.id_pasien " +
                   "ORDER BY p.tanggal DESC, per.id_periksa DESC LIMIT 15";

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(query);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String tgl = rs.getString("tanggal");
            String nama = rs.getString("nama");
            String diagnosa = rs.getString("diagnosa");
            
            // Jika diagnosa kosong, bisa diganti dengan teks default
            if (diagnosa == null || diagnosa.isEmpty()) diagnosa = "Menunggu Hasil";
            
            listRiwayat.add(new LaporanRiwayat(tgl, nama, diagnosa));
        }

        // Sambungkan ke tabel UI
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPasien"));
        colHasil.setCellValueFactory(new PropertyValueFactory<>("hasil"));
        
        tableRiwayat.setItems(listRiwayat);

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    @FXML
private void handleCetakPDF(ActionEvent event) {
    // 1. Tentukan nama dan lokasi file PDF disimpan (misal di folder proyek)
    String dest = "Laporan_Klinik_Bulanan.pdf"; 
    
    try {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        
        document.open();
        
        // 2. Tambahkan Judul Laporan
        document.add(new Paragraph("SMART CLINIC - LAPORAN PEMERIKSAAN\n\n"));
        document.add(new Paragraph("Data 15 Pemeriksaan Terbaru:\n\n"));
        
        // 3. Buat Tabel PDF (3 Kolom)
        PdfPTable pdfTable = new PdfPTable(3);
        
        // Lebar kolom
        pdfTable.setWidths(new float[] { 2, 4, 4 }); 
        
        // Header Tabel
        pdfTable.addCell("Tanggal");
        pdfTable.addCell("Nama Pasien");
        pdfTable.addCell("Hasil Pemeriksaan");
        
        // 4. Looping data dari TableView JavaFX untuk dimasukkan ke tabel PDF
        for (LaporanRiwayat item : tableRiwayat.getItems()) {
            pdfTable.addCell(item.getTanggal());
            pdfTable.addCell(item.getNamaPasien());
            pdfTable.addCell(item.getHasil());
        }
        
        document.add(pdfTable);
        document.close();
        
        // Tampilkan notifikasi sukses
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sukses");
        alert.setHeaderText(null);
        alert.setContentText("Laporan PDF berhasil dicetak dan disimpan sebagai: " + dest);
        alert.showAndWait();
        
    } catch (Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText("Gagal mencetak PDF: " + e.getMessage());
        alert.showAndWait();
    }
}

    // Fungsi ketika tombol "Terapkan Filter" ditekan
@FXML
private void handleFilterTanggal(ActionEvent event) {
    LocalDate start = dpMulai.getValue();
    LocalDate end = dpSelesai.getValue();
    
    if (start != null && end != null) {
        loadChartData(start, end);
        // Jika kamu ingin tabel riwayat juga ikut terfilter, 
        // kamu bisa memodifikasi fungsi loadRiwayatTerbaru() untuk menerima parameter tanggal juga.
    }
}

// Fungsi menggambar grafik
private void loadChartData(LocalDate start, LocalDate end) {
    // Bersihkan grafik lama sebelum menggambar yang baru
    barChartKunjungan.getData().clear();
    
    // Buat kelompok data baru
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Total Pendaftaran Pasien");

    // Query menghitung jumlah pendaftaran dikelompokkan berdasarkan tanggal
    String query = "SELECT tanggal, COUNT(id_daftar) AS total_pasien " +
                   "FROM pendaftaran " +
                   "WHERE tanggal BETWEEN ? AND ? " +
                   "GROUP BY tanggal " +
                   "ORDER BY tanggal ASC";

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(query)) {

        ps.setDate(1, java.sql.Date.valueOf(start));
        ps.setDate(2, java.sql.Date.valueOf(end));

        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            String tgl = rs.getString("tanggal");
            int total = rs.getInt("total_pasien");
            
            // Masukkan titik koordinat ke dalam grafik (Sumbu X: Tanggal, Sumbu Y: Jumlah)
            series.getData().add(new XYChart.Data<>(tgl, total));
        }

        // Tampilkan grafik ke layar
        barChartKunjungan.getData().add(series);

    } catch (SQLException e) {
        System.err.println("Gagal memuat data grafik: " + e.getMessage());
        e.printStackTrace();
    }
}
   
    @FXML
    private void handleCetakLaporan(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan Operasional Klinik");
        fileChooser.setInitialFileName("Laporan_Klinik_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF", "*.pdf"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Set Desain Font
            Font fontJudul = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
            Font fontSubJudul = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.DARK_GRAY);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font fontBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font fontTableHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

            Paragraph kop = new Paragraph("SMART CLINIC SYSTEM", fontJudul);
            kop.setAlignment(Element.ALIGN_CENTER);
            document.add(kop);

            Paragraph subKop = new Paragraph("Laporan Ringkasan Operasional & Manajemen Data Klinik", fontSubJudul);
            subKop.setAlignment(Element.ALIGN_CENTER);
            document.add(subKop);

            document.add(new Paragraph("____________________________________________________________________________________________\n\n", fontNormal));

            String tanggalCetak = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy, HH:mm"));
            Paragraph infoTgl = new Paragraph("Tanggal Cetak Laporan : " + tanggalCetak, fontNormal);
            document.add(infoTgl);
            
            Paragraph infoPeriode = new Paragraph("Periode Data          : Sampai Hari Ini", fontNormal);
            document.add(infoPeriode);
            document.add(new Paragraph("\n", fontNormal));

            Paragraph judulLaporan = new Paragraph("1. RINGKASAN DATA STATISTIK", fontBold);
            document.add(judulLaporan);
            document.add(new Paragraph("\n", fontNormal));

            int totalPasien = 0, totalRekam = 0, totalObat = 0, totalPrediksi = 0;
            
            String sqlCount = "SELECT " +
                    "(SELECT COUNT(*) FROM pasien) AS pasien, " +
                    "(SELECT COUNT(*) FROM rekam_medis) AS rekam, " +
                    "(SELECT COUNT(*) FROM obat) AS obat, " +
                    "((SELECT COUNT(*) FROM prediksi_hasil) + (SELECT COUNT(*) FROM prediksi_jantung) + (SELECT COUNT(*) FROM prediksi_ginjal)) AS prediksi";

            try (Connection conn = DBConnection.connect();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlCount)) {
                if (rs.next()) {
                    totalPasien = rs.getInt("pasien");
                    totalRekam = rs.getInt("rekam");
                    totalObat = rs.getInt("obat");
                    totalPrediksi = rs.getInt("prediksi");
                }
            } catch (Exception e) {
                System.err.println("Gagal query statistik untuk PDF: " + e.getMessage());
            }

            PdfPTable tableStatistik = new PdfPTable(3);
            tableStatistik.setWidthPercentage(100);
            tableStatistik.setWidths(new float[]{10f, 60f, 30f}); // Proporsi lebar kolom (No, Parameter, Jumlah)

            String[] headers = {"No", "Indikator / Parameter Operasional", "Total Data Terintegrasi"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontTableHeader));
                cell.setBackgroundColor(new BaseColor(44, 62, 80)); // Warna navy mewah (#2c3e50)
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableStatistik.addCell(cell);
            }

            addCustomRow(tableStatistik, "1", "Jumlah Pasien Terdaftar", totalPasien + " Orang", fontNormal);
            addCustomRow(tableStatistik, "2", "Jumlah Berkas Rekam Medis Masuk", totalRekam + " Berkas", fontNormal);
            addCustomRow(tableStatistik, "3", "Jumlah Analisis Prediksi Machine Learning (Diabetes/Jantung/Ginjal)", totalPrediksi + " Kali Prediksi", fontNormal);
            addCustomRow(tableStatistik, "4", "Jumlah Item Obat Tersedia", totalObat + " Jenis Obat", fontNormal);

            document.add(tableStatistik);
            document.add(new Paragraph("\n\n\n", fontNormal));

            Paragraph tglSrg = new Paragraph("Semarang, " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), fontNormal);
            tglSrg.setAlignment(Element.ALIGN_RIGHT);
            document.add(tglSrg);

            document.add(new Paragraph("\n\n\n", fontNormal)); 

            Paragraph namaTtd = new Paragraph("( Manajemen Smart Clinic System )", fontBold);
            namaTtd.setAlignment(Element.ALIGN_RIGHT);
            document.add(namaTtd);

            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukses Cetak PDF");
            alert.setHeaderText(null);
            alert.setContentText("Laporan operasional klinik berhasil dicetak dan disimpan ke komputer!");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gagal Cetak");
            alert.setHeaderText("Terjadi kesalahan sistem saat membuat PDF");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void addCustomRow(PdfPTable table, String no, String parameter, String jumlah, Font font) {
        PdfPCell cellNo = new PdfPCell(new Phrase(no, font));
        cellNo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellNo.setPadding(6);
        table.addCell(cellNo);

        PdfPCell cellParam = new PdfPCell(new Phrase(parameter, font));
        cellParam.setPadding(6);
        table.addCell(cellParam);

        PdfPCell cellJumlah = new PdfPCell(new Phrase(jumlah, font));
        cellJumlah.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellJumlah.setPadding(6);
        table.addCell(cellJumlah);
    }
}
