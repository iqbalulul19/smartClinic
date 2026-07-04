package controller;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Pasien;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class PasienController implements Initializable {

    @FXML private TableView<Pasien> tablePasien;
    @FXML private TableColumn<Pasien, Integer> colId;
    @FXML private TableColumn<Pasien, String> colNama;
    @FXML private TableColumn<Pasien, Integer> colUmur;
    @FXML private TableColumn<Pasien, String> colGender;
    @FXML private TableColumn<Pasien, String> colHp;
    @FXML private TableColumn<Pasien, String> colAlamat;

    @FXML private TextField txtIdPasien;
    @FXML private TextField txtNama;
    @FXML private TextField txtUmur;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtHp;
    @FXML private TextArea txtAlamat;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGender.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));
        
        colId.setCellValueFactory(new PropertyValueFactory<>("idPasien"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colUmur.setCellValueFactory(new PropertyValueFactory<>("umur"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colHp.setCellValueFactory(new PropertyValueFactory<>("noHp")); // Pastikan di model Pasien ada 'noHp'
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        loadData();
    }

    private void loadData() {
        ObservableList<Pasien> listPasien = FXCollections.observableArrayList();
        String sql = "SELECT * FROM pasien ORDER BY id_pasien DESC";

        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Pasien p = new Pasien();
                p.setIdPasien(rs.getInt("id_pasien"));
                p.setNama(rs.getString("nama"));
                p.setUmur(rs.getInt("umur"));
                p.setGender(rs.getString("gender"));
                p.setNoHp(rs.getString("no_hp"));
                p.setAlamat(rs.getString("alamat"));
                listPasien.add(p);
            }
            tablePasien.setItems(listPasien);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePilihTabel() {
        Pasien selected = tablePasien.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtIdPasien.setText(String.valueOf(selected.getIdPasien()));
            txtNama.setText(selected.getNama());
            txtUmur.setText(String.valueOf(selected.getUmur()));
            cmbGender.setValue(selected.getGender());
            txtHp.setText(selected.getNoHp());
            txtAlamat.setText(selected.getAlamat());
        }
    }

    @FXML
    private void handleSimpan() {
        if (txtNama.getText().isEmpty() || txtUmur.getText().isEmpty() || cmbGender.getValue() == null) {
            showAlert("Peringatan", "Nama, Umur, dan Gender wajib diisi!"); return;
        }
        String sql = "INSERT INTO pasien (nama, umur, gender, no_hp, alamat) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNama.getText());
            ps.setInt(2, Integer.parseInt(txtUmur.getText()));
            ps.setString(3, cmbGender.getValue());
            ps.setString(4, txtHp.getText());
            ps.setString(5, txtAlamat.getText());
            ps.executeUpdate();
            showAlert("Sukses", "Data pasien berhasil ditambahkan!");
            handleBersihkan();
            loadData();
        } catch (Exception e) { showAlert("Error", e.getMessage()); }
    }

    @FXML
    private void handleUbah() {
        if (txtIdPasien.getText().isEmpty()) {
            showAlert("Peringatan", "Pilih data yang akan diubah dari tabel!"); return;
        }
        String sql = "UPDATE pasien SET nama=?, umur=?, gender=?, no_hp=?, alamat=? WHERE id_pasien=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNama.getText());
            ps.setInt(2, Integer.parseInt(txtUmur.getText()));
            ps.setString(3, cmbGender.getValue());
            ps.setString(4, txtHp.getText());
            ps.setString(5, txtAlamat.getText());
            ps.setInt(6, Integer.parseInt(txtIdPasien.getText()));
            ps.executeUpdate();
            showAlert("Sukses", "Data pasien berhasil diubah!");
            handleBersihkan();
            loadData();
        } catch (Exception e) { showAlert("Error", e.getMessage()); }
    }

    @FXML
    private void handleHapus() {
        if (txtIdPasien.getText().isEmpty()) {
            showAlert("Peringatan", "Pilih data yang akan dihapus dari tabel!"); return;
        }
        String sql = "DELETE FROM pasien WHERE id_pasien=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdPasien.getText()));
            ps.executeUpdate();
            showAlert("Sukses", "Data pasien berhasil dihapus!");
            handleBersihkan();
            loadData();
        } catch (Exception e) { showAlert("Error", "Gagal menghapus! Data pasien ini mungkin sedang dipakai di pendaftaran."); }
    }

    @FXML
    private void handleBersihkan() {
        txtIdPasien.clear();
        txtNama.clear();
        txtUmur.clear();
        cmbGender.getSelectionModel().clearSelection();
        txtHp.clear();
        txtAlamat.clear();
        tablePasien.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}