package controller;

import database.DBConnection;
import model.User;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class PetugasController implements Initializable {

    @FXML private TableView<User> tableUser;
    @FXML private TableColumn<User, Integer> colID;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    
    @FXML private TextField txtCari;

    private ObservableList<User> listUser = FXCollections.observableArrayList();
    private int selectedId = -1; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(cellData -> {
            int index = listUser.indexOf(cellData.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        
        colUsername.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername())
        );
        
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        loadData();
    }

    private void loadData() {
        listUser.clear();
        String query = "SELECT * FROM users"; 
        try (Connection conn = DBConnection.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                String roleName = "Tidak Diketahui";
                int idRoleDb = rs.getInt("id_role");
                
                if (idRoleDb == 1) {
                    roleName = "Admin";
                } else if (idRoleDb == 2) {
                    roleName = "Petugas";
                } else if (idRoleDb == 3) {
                    roleName = "Dokter";
                } else if (idRoleDb == 4) { // Asumsi ID 4 adalah Pasien di database
                    roleName = "Pasien";
                }
                
                // PERBAIKAN: Tarik data nama dan id_dokter dari database
                String namaUser = rs.getString("nama");
                int idDokterUser = rs.getInt("id_dokter");

                // PERBAIKAN: Masukkan ke-6 data ke dalam Constructor User
                listUser.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("password"),
                        roleName,
                        namaUser,
                        idDokterUser
                ));
            }
            tableUser.setItems(listUser);
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

        listUser.clear();
        String query = "SELECT * FROM users WHERE username LIKE ?";
        
        try (Connection conn = DBConnection.connect(); 
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, "%" + keyword + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String roleName = "Tidak Diketahui";
                int idRoleDb = rs.getInt("id_role");

                if (idRoleDb == 1) {
                    roleName = "Admin";
                } else if (idRoleDb == 2) {
                    roleName = "Petugas";
                } else if (idRoleDb == 3) {
                    roleName = "Dokter";
                } else if (idRoleDb == 4) {
                    roleName = "Pasien";
                }

                // PERBAIKAN: Tarik data nama dan id_dokter dari database
                String namaUser = rs.getString("nama");
                int idDokterUser = rs.getInt("id_dokter");

                // PERBAIKAN: Masukkan ke-6 data ke dalam Constructor User
                listUser.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("password"),
                        roleName,
                        namaUser,
                        idDokterUser
                ));
            }
            tableUser.setItems(listUser);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Pencarian gagal: " + e.getMessage());
        }
    }

    @FXML
    private void handleSimpan() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String role = cmbRole.getValue();

        if (username.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Username dan Role harus diisi!");
            return;
        }

        int idRole = 2; // Default Petugas
        if (role.equals("Admin")) {
            idRole = 1;
        } else if (role.equals("Dokter")) {
            idRole = 3;
        } else if (role.equals("Pasien")) {
            idRole = 4;  
        }

        String sql;
        if (selectedId == -1) {
            if (password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Password wajib diisi!");
                return;
            }
            sql = "INSERT INTO users (username, password, id_role, nama) VALUES (?, ?, ?, ?)";
        } else {
            if (password.isEmpty()) {
                sql = "UPDATE users SET username = ?, id_role = ? WHERE id_user = ?";
            } else {
                sql = "UPDATE users SET username = ?, password = ?, id_role = ? WHERE id_user = ?";
            }
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            if (selectedId == -1) {
                ps.setString(2, password);
                ps.setInt(3, idRole);
                ps.setString(4, username); 
            } else {
                if (password.isEmpty()) {
                    ps.setInt(2, idRole);
                    ps.setInt(3, selectedId);
                } else {
                    ps.setString(2, password);
                    ps.setInt(3, idRole);
                    ps.setInt(4, selectedId);
                }
            }

            ps.executeUpdate();
            loadData();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data petugas berhasil disimpan!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan data: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedId == -1) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih akun petugas dari tabel terlebih dahulu!");
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id_user = ?")) {
            
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            loadData();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun berhasil dihapus!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    @FXML
    private void handleTableClick() {
        User selected = tableUser.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedId = selected.getIdUser();
            txtUsername.setText(selected.getUsername());
            cmbRole.setValue(selected.getRole());
            txtPassword.clear(); 
        }
    }

    @FXML
    private void clearFields() {
        selectedId = -1;
        txtUsername.clear();
        txtPassword.clear();
        cmbRole.setValue(null);
        tableUser.getSelectionModel().clearSelection();
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