package controller;

import database.DBConnection;
import model.Obat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ObatController implements Initializable {

    @FXML private TableView<Obat> tableObat;
    @FXML private TableColumn<Obat, Integer> colID;
    @FXML private TableColumn<Obat, String> colNama;
    @FXML private TableColumn<Obat, Integer> colStok;
    @FXML private TableColumn<Obat, Double> colHarga;
    @FXML private TableColumn<Obat, String> colAturan;
    @FXML private TableColumn<Obat, String> colKfa;

    @FXML private TextField txtCari;
    @FXML private TextField txtNama;
    @FXML private TextField txtStok;
    @FXML private TextField txtHarga;
    @FXML private TextField txtAturan;
    @FXML private TextField txtKfa;

    private ObservableList<Obat> listObat = FXCollections.observableArrayList();
    private int selectedId = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Pemetaan langsung ke fungsi getter di model Obat.java
        colID.setCellValueFactory(new PropertyValueFactory<>("idObat"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaObat"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colAturan.setCellValueFactory(new PropertyValueFactory<>("AturanPakai"));
        colKfa.setCellValueFactory(new PropertyValueFactory<>("KodeKfa"));

        // Memaksa tabel melebar otomatis memakan ruang kosong
        tableObat.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadData();
    }

    private void loadData() {
        listObat.clear();
        String query = "SELECT * FROM obat";
        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                // Memanggil Constructor 4 parameter sesuai dengan Obat.java
                listObat.add(new Obat(
                        rs.getInt("id_obat"),
                        rs.getString("nama_obat"),
                        rs.getInt("stok"),
                        rs.getDouble("harga"),
                        rs.getString("aturan_pakai"),
                        rs.getString("kode_kfa")
                ));
            }
            tableObat.setItems(listObat);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        txtCari.clear();
        loadData();
    }

    @FXML
    private void handleCari() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        listObat.clear();
        String query = "SELECT * FROM obat WHERE nama_obat LIKE ?";
        
        try (Connection conn = DBConnection.connect(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, "%" + keyword + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listObat.add(new Obat(
                        rs.getInt("id_obat"),
                        rs.getString("nama_obat"),
                        rs.getInt("stok"),
                        rs.getDouble("harga"),
                        rs.getString("aturan_pakai"),
                        rs.getString("kode_kfa")
                ));
            }
            tableObat.setItems(listObat);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Pencarian gagal: " + e.getMessage());
        }
    }

    @FXML
    private void handleSimpan() {
        String nama = txtNama.getText().trim();
        String strStok = txtStok.getText().trim();
        String strHarga = txtHarga.getText().trim();
        String aturanPakai = txtAturan.getText().trim();
        String kfa = txtKfa.getText().trim();

        if (nama.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama Obat harus diisi!");
            return;
        }

        int stok = 0;
        double harga = 0.0;

        try {
            if (!strStok.isEmpty()) stok = Integer.parseInt(strStok);
            if (!strHarga.isEmpty()) harga = Double.parseDouble(strHarga);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Peringatan", "Stok dan Harga harus berupa angka valid!");
            return;
        }

        String sql;
        if (selectedId == -1) {
            sql = "INSERT INTO obat (nama_obat, stok, harga, aturan_pakai, kode_kfa) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE obat SET nama_obat = ?, stok = ?, harga = ?, aturan_pakai = ?, kode_kfa = ? WHERE id_obat = ?";
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setInt(2, stok);
            ps.setDouble(3, harga);
            ps.setString(4, aturanPakai);
            ps.setString(5, txtKfa.getText().trim());

            if (selectedId != -1) {
                ps.setInt(6, selectedId);
            }

            ps.executeUpdate();
            loadData();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Obat berhasil disimpan!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan data: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedId == -1) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data obat dari tabel terlebih dahulu!");
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM obat WHERE id_obat = ?")) {
            
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            loadData();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Obat berhasil dihapus!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    @FXML
    private void handleTableClick() {
        Obat selected = tableObat.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedId = selected.getIdObat();
            txtNama.setText(selected.getNamaObat());
            txtStok.setText(String.valueOf(selected.getStok()));
            txtAturan.setText(selected.getAturanPakai());
            txtKfa.setText(selected.getKodeKfa());

            // Format agar tampilan harga rapi jika bilangan bulat
            if (selected.getHarga() == (long) selected.getHarga()) {
                txtHarga.setText(String.format("%d", (long) selected.getHarga()));
            } else {
                txtHarga.setText(String.format("%s", selected.getHarga()));
            }
        }
    }

    @FXML
    private void clearFields() {
        selectedId = -1;
        txtNama.clear();
        txtStok.clear();
        txtHarga.clear();
        tableObat.getSelectionModel().clearSelection();
        txtAturan.clear();
        txtKfa.clear();
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