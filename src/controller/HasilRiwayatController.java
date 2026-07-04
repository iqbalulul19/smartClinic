package controller;

import database.DBConnection;
import model.RiwayatPrediksi;
import java.sql.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HasilRiwayatController implements Initializable {

    @FXML private TableView<RiwayatPrediksi> tableHasilML;
    @FXML private TableColumn<RiwayatPrediksi, String> colTanggal;
    @FXML private TableColumn<RiwayatPrediksi, String> colPasien;
    @FXML private TableColumn<RiwayatPrediksi, String> colHasil;
    @FXML private TextField txtCari;

    // UI Baru untuk Detail Pasien
    @FXML private Label lblDetailNama;
    @FXML private Label lblDetailTanggal;
    @FXML private Label lblDetailHasil;
    @FXML private Button btnCetakIndividu;

    // Menyimpan data pasien yang sedang di-klik
    private RiwayatPrediksi pasienTerpilih;

    private final ObservableList<RiwayatPrediksi> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colPasien.setCellValueFactory(new PropertyValueFactory<>("namaPasien"));
        colHasil.setCellValueFactory(new PropertyValueFactory<>("hasilPrediksi"));
        
        loadDataRiwayatML();
        setupPencarian();
        setupKlikTabel(); // Aktifkan pendeteksi klik tabel
    }

    private void loadDataRiwayatML() {
        masterData.clear();
        // Menggabungkan parameter medis menggunakan pemisah "|" agar mudah dipecah nanti
        String sql = "SELECT pr.tanggal, p.nama, pr.hasil_prediksi, " +
                     "CONCAT('Kehamilan (Pregnancies): ', pr.pregnancies, '|', " +
                     "'Gula Darah (Glucose): ', pr.glucose, '|', " +
                     "'Tekanan Darah: ', pr.blood_pressure, '|', " +
                     "'Ketebalan Kulit: ', pr.skin_thickness, '|', " +
                     "'Insulin: ', pr.insulin, '|', " +
                     "'BMI (Indeks Massa Tubuh): ', pr.bmi, '|', " +
                     "'Riwayat Keluarga (Pedigree): ', pr.pedigree, '|', " +
                     "'Umur: ', pr.age) AS detail_medis " +
                     "FROM prediksi_hasil pr " +
                     "JOIN pasien p ON pr.id_pasien = p.id_pasien ORDER BY pr.tanggal DESC";
        
        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                String tgl = rs.getString("tanggal") != null ? rs.getString("tanggal") : "-";
                String nama = rs.getString("nama") != null ? rs.getString("nama") : "Tanpa Nama";
                String hasil = rs.getString("hasil_prediksi") != null ? rs.getString("hasil_prediksi") : "-";
                String detail = rs.getString("detail_medis") != null ? rs.getString("detail_medis") : "";
                
                // Memasukkan detail medis ke parameter ke-4
                masterData.add(new RiwayatPrediksi(tgl, nama, hasil, detail));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void setupPencarian() {
        FilteredList<RiwayatPrediksi> filteredData = new FilteredList<>(masterData, b -> true);
        txtCari.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(riwayat -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (riwayat.getNamaPasien().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (riwayat.getHasilPrediksi().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<RiwayatPrediksi> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableHasilML.comparatorProperty());
        tableHasilML.setItems(sortedData);
    }

    // =======================================================
    // LOGIKA BARU: DETEKSI KLIK TABEL
    // =======================================================
    private void setupKlikTabel() {
        tableHasilML.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                pasienTerpilih = newSelection;
                lblDetailNama.setText("Nama : " + pasienTerpilih.getNamaPasien());
                lblDetailTanggal.setText("Waktu : " + pasienTerpilih.getTanggal());
                lblDetailHasil.setText("Hasil  : " + pasienTerpilih.getHasilPrediksi());
                
                // Baris ini yang membuat tombol bisa dipencet:
                btnCetakIndividu.setDisable(false); 
            }
        });
    }

    // =======================================================
    // LOGIKA BARU: CETAK PDF 1 PASIEN
    // =======================================================
    @FXML
    private void handleCetakPDFIndividu(ActionEvent event) {
        if (pasienTerpilih == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Surat Hasil Prediksi");
        fileChooser.setInitialFileName("Hasil_" + pasienTerpilih.getNamaPasien().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF", "*.pdf"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A5, 30, 30, 30, 30);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Font Styling
                Font fontJudul = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
                Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
                Font fontBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
                Font fontHasil = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED);

                // Kop Surat
                Paragraph kop = new Paragraph("SMART CLINIC SYSTEM", fontJudul);
                kop.setAlignment(Element.ALIGN_CENTER);
                document.add(kop);
                
                document.add(new Paragraph("________________________________________________________________\n\n", fontNormal));

                Paragraph judulDokumen = new Paragraph("SURAT KETERANGAN HASIL PREDIKSI (ARSIP)", fontBold);
                judulDokumen.setAlignment(Element.ALIGN_CENTER);
                document.add(judulDokumen);
                document.add(new Paragraph("\n", fontNormal));

                // Detail Pasien
                PdfPTable tableData = new PdfPTable(2);
                tableData.setWidthPercentage(100);
                tableData.setWidths(new float[]{30f, 70f});
                tableData.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                
                tableData.addCell(new Phrase("Nama Pasien", fontNormal));
                tableData.addCell(new Phrase(": " + pasienTerpilih.getNamaPasien(), fontNormal));
                
                tableData.addCell(new Phrase("Tanggal Periksa", fontNormal));
                tableData.addCell(new Phrase(": " + pasienTerpilih.getTanggal(), fontNormal));

                document.add(tableData);
                document.add(new Paragraph("\n", fontNormal));

                // TABEL PARAMETER MEDIS
                if (pasienTerpilih.getDetailMedis() != null && !pasienTerpilih.getDetailMedis().isEmpty()) {
                    document.add(new Paragraph("DATA KLINIS PASIEN SAAT PEMERIKSAAN:", fontBold));
                    document.add(new Paragraph(" ", fontNormal));

                    PdfPTable tableMedis = new PdfPTable(2);
                    tableMedis.setWidthPercentage(100);
                    tableMedis.setWidths(new float[]{60f, 40f}); // Proporsi lebar kolom 60:40

                    // Pecah data yang tadinya digabung pakai "|"
                    String[] parameters = pasienTerpilih.getDetailMedis().split("\\|");
                    for (String param : parameters) {
                        String[] splitParam = param.split(": ");
                        if (splitParam.length == 2) {
                            PdfPCell cellKey = new PdfPCell(new Phrase(splitParam[0], fontNormal));
                            cellKey.setPadding(6);
                            PdfPCell cellVal = new PdfPCell(new Phrase(splitParam[1], fontBold));
                            cellVal.setPadding(6);
                            
                            tableMedis.addCell(cellKey);
                            tableMedis.addCell(cellVal);
                        }
                    }
                    document.add(tableMedis);
                    document.add(new Paragraph("\n", fontNormal));
                }

                // Kotak Hasil Prediksi Mesin
                PdfPTable tableHasil = new PdfPTable(1);
                tableHasil.setWidthPercentage(100);
                
                PdfPCell cellHasilHeader = new PdfPCell(new Phrase("KESIMPULAN SISTEM", fontBold));
                cellHasilHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cellHasilHeader.setPadding(8);
                cellHasilHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableHasil.addCell(cellHasilHeader);

                PdfPCell cellHasilKonten = new PdfPCell(new Phrase(pasienTerpilih.getHasilPrediksi().toUpperCase(), fontHasil));
                cellHasilKonten.setPadding(15);
                cellHasilKonten.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableHasil.addCell(cellHasilKonten);

                document.add(tableHasil);
                document.add(new Paragraph("\n\n", fontNormal));

                // Tanda Tangan
                String tanggalCetak = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm"));
                Paragraph tgl = new Paragraph("Semarang, " + tanggalCetak, fontNormal);
                tgl.setAlignment(Element.ALIGN_RIGHT);
                document.add(tgl);
                
                document.add(new Paragraph("\n\n\n", fontNormal)); 
                
                Paragraph namaTtd = new Paragraph("( Admin Klinik )", fontBold);
                namaTtd.setAlignment(Element.ALIGN_RIGHT);
                document.add(namaTtd);

                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sukses");
                alert.setHeaderText(null);
                alert.setContentText("Surat berhasil dibuat untuk " + pasienTerpilih.getNamaPasien());
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}