package controller;

import dao.JadwalDAO;
import dao.DokterDAO;
import model.Jadwal;
import model.Dokter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import java.util.List;

public class JadwalController {
    @FXML private TableView<Jadwal> tableJadwal;
    @FXML private TableColumn<Jadwal, String> colDokter, colHari, colMulai, colSelesai;
    
    @FXML private ComboBox<String> cmbDokter; 
    @FXML private ComboBox<String> cmbHari;
    @FXML private TextField txtJamMulai, txtJamSelesai; 

    private JadwalDAO jadwalDAO = new JadwalDAO();
    private DokterDAO dokterDAO = new DokterDAO();
    private ObservableList<Dokter> listDokter;

    @FXML
    public void initialize() {
        colDokter.setCellValueFactory(new PropertyValueFactory<>("namaDokter"));
        colHari.setCellValueFactory(new PropertyValueFactory<>("hari"));
        colMulai.setCellValueFactory(new PropertyValueFactory<>("jamMulai"));
        colSelesai.setCellValueFactory(new PropertyValueFactory<>("jamSelesai"));
        
        // Isi ComboBox Hari
        cmbHari.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu");
        
        loadDokterToComboBox();
        loadData();
    }

    private void loadDokterToComboBox() {
    // Memanggil fungsi getSemuaDokter() dari DokterDAO
    List<Dokter> listDokter = dokterDAO.getSemuaDokter(); 
    
    // Looping untuk memasukkan nama dokter ke dalam ComboBox
    for (Dokter d : listDokter) {
        cmbDokter.getItems().add(d.getIdDokter() + " - " + d.getNama());
    }
}

    private void loadData() {
        tableJadwal.setItems(jadwalDAO.getAllJadwal());
    }

    @FXML
    private void handleSimpan() {
        try {
            Jadwal j = new Jadwal();
            
            // Mengambil ID Dokter dari ComboBox (Pisahkan string berdasarkan spasi/strip)
            String selectedDokter = cmbDokter.getValue();
            int idDokter = Integer.parseInt(selectedDokter.split(" - ")[0]);
            
            j.setIdDokter(idDokter);
            j.setHari(cmbHari.getValue());
            j.setJamMulai(txtJamMulai.getText() + ":00"); // Tambahkan detik MySQL
            j.setJamSelesai(txtJamSelesai.getText() + ":00");
            
            jadwalDAO.insert(j);
            loadData();
            
            txtJamMulai.clear(); txtJamSelesai.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKembali(javafx.event.ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}