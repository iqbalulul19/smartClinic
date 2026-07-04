package controller;

import database.DBConnection;
import model.Dokter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DokterController implements Initializable {

    @FXML private TableView<Dokter> tableDokter;
    @FXML private TableColumn<Dokter, Integer> colID;
    @FXML private TableColumn<Dokter, String> colNama;
    @FXML private TableColumn<Dokter, String> colSpesialis;
    @FXML private TableColumn<Dokter, String> colNoHP;

    @FXML private TextField txtNama;
    @FXML private TextField txtSpesialis;
    @FXML private TextField txtNoHP;
    @FXML private TextField txtCari;

    private ObservableList<Dokter> listDokter = FXCollections.observableArrayList();
    private int selectedId = -1; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(new PropertyValueFactory<>("idDokter"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama")); 
        colSpesialis.setCellValueFactory(new PropertyValueFactory<>("spesialis"));
        colNoHP.setCellValueFactory(new PropertyValueFactory<>("noHP"));
        
        loadData();
    }

    private void loadData() {
        listDokter.clear();
        String query = "SELECT * FROM dokter";
        
        try (Connection conn = database.DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                listDokter.add(new Dokter(
                        rs.getInt("id_dokter"),
                        rs.getString("nama"),
                        rs.getString("spesialis"),
                        rs.getString("no_hp")
                ));
            }
            tableDokter.setItems(listDokter);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleSimpan() {
        String nama = txtNama.getText();
        String spesialis = txtSpesialis.getText();
        String noHP = txtNoHP.getText();

        if (nama.isEmpty() || spesialis.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama dan Spesialis tidak boleh kosong!");
            return;
        }

        String sql;
        if (selectedId == -1) {
            sql = "INSERT INTO dokter (nama, spesialis, no_hp) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE dokter SET nama = ?, spesialis = ?, no_hp = ? WHERE id_dokter = ?";
        }

        try (Connection conn = database.DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setString(2, spesialis);
            ps.setString(3, noHP);
            
            if (selectedId != -1) {
                ps.setInt(4, selectedId);
            }

            ps.executeUpdate();
            loadData();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil disimpan!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        Dokter selected = tableDokter.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data di tabel untuk dihapus!");
            return;
        }

        try (Connection conn = database.DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM dokter WHERE id_dokter = ?")) {
            
            ps.setInt(1, selected.getIdDokter());
            ps.executeUpdate();
            loadData();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil dihapus!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus: " + e.getMessage());
        }
    }

    @FXML
    private void handleCari() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        listDokter.clear();
        String query = "SELECT * FROM dokter WHERE nama LIKE ? OR spesialis LIKE ?";
        
        try (Connection conn = database.DBConnection.connect(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listDokter.add(new Dokter(
                        rs.getInt("id_dokter"),
                        rs.getString("nama"),
                        rs.getString("spesialis"),
                        rs.getString("no_hp")
                ));
            }
            tableDokter.setItems(listDokter);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Pencarian gagal: " + e.getMessage());
        }
    }

    @FXML
    private void handleTableClick() {
        Dokter selected = tableDokter.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedId = selected.getIdDokter();
            txtNama.setText(selected.getNama() != null ? selected.getNama() : ""); 
            txtSpesialis.setText(selected.getSpesialis());
            txtNoHP.setText(selected.getNoHP());
        }
    }

    @FXML
    private void clearFields() {
        selectedId = -1;
        txtNama.clear();
        txtSpesialis.clear();
        txtNoHP.clear();
        tableDokter.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleKembali(javafx.event.ActionEvent event) {
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
}