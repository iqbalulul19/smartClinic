package controller;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class RegisterController implements Initializable { // <--- Tambahan Initializable

    @FXML private ComboBox<String> comboRole; // <--- Deklarasi ComboBox
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnRegister;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mengisi pilihan di dalam Dropdown saat halaman dibuka
        comboRole.setItems(FXCollections.observableArrayList("Pasien", "Dokter"));
    }

    @FXML private TextField txtNama; // Pastikan fx:id ini sudah di-declare di atas bersama komponen lain

    @FXML
    void handleRegister(ActionEvent event) {
        String selectedRole = comboRole.getValue();
        String namaLengkap = txtNama.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPass = txtConfirmPassword.getText().trim();

        if (selectedRole == null || namaLengkap.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom harus diisi lengkap!");
            return;
        }

        if (!password.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Password dan Konfirmasi Password tidak cocok!");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            // Cek duplikasi username
            String checkQuery = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Username sudah digunakan!");
                    return;
                }
            }

            Integer idDokterBaru = null;

            // JIKA DAFTAR SEBAGAI DOKTER: Buat data di tabel dokter dulu secara otomatis
            if (selectedRole.equalsIgnoreCase("Dokter")) {
                String insertDokter = "INSERT INTO dokter (nama) VALUES (?)";
                try (PreparedStatement psDokter = conn.prepareStatement(insertDokter, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psDokter.setString(1, namaLengkap);
                    psDokter.executeUpdate();
                    ResultSet rsKey = psDokter.getGeneratedKeys();
                    if (rsKey.next()) {
                        idDokterBaru = rsKey.getInt(1); // Dapatkan ID Dokter yang baru terbuat
                    }
                }
            }

            // Simpan akun ke tabel users lengkap dengan nama asli dan id_dokter (jika ada)
            String insertQuery = "INSERT INTO users (nama, username, password, id_role, id_dokter) " +
                                 "VALUES (?, ?, ?, (SELECT id_role FROM roles WHERE nama_role = ?), ?)";
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, namaLengkap);
                insertStmt.setString(2, username);
                insertStmt.setString(3, password);
                insertStmt.setString(4, selectedRole);
                
                if (idDokterBaru != null) {
                    insertStmt.setInt(5, idDokterBaru);
                } else {
                    insertStmt.setNull(5, java.sql.Types.INTEGER);
                }
                
                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Registrasi Berhasil sebagai " + selectedRole + "!");
                    handleGoToLogin(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Sistem", "Terjadi kegagalan: " + e.getMessage());
        }
    }

    @FXML
    void handleGoToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}