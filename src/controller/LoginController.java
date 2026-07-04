package controller;

import database.DBConnection;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;

    // Session global untuk mencatat user yang sedang aktif di aplikasi
    public static User sessionUser = null;

    @FXML
    private void handleLogin(ActionEvent event) {
        String usernameInput = txtUsername.getText();
        String passwordInput = txtPassword.getText();

        // Sesuaikan nama tabel 'petugas' dan kolom 'nama', 'role' dengan database kamu
        String sql = "SELECT nama, username FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usernameInput);
            ps.setString(2, passwordInput);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // 1. JIKA LOGIN BERHASIL, AMBIL NAMA & ROLE ASLI DARI DATABASE
                String namaAsli = rs.getString("nama");
                String roleAsli = rs.getString("username"); // Misal role disimpan di kolom 'username', sesuaikan dengan database kamu

                System.out.println("Login sukses! Selamat datang, " + namaAsli);

                // 2. BUKA HALAMAN DASHBOARD
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                Parent root = loader.load();

                // 3. AMBIL CONTROLLER DASHBOARD DAN KIRIM DATA ASLINYA
                DashboardController dashboard = loader.getController();
                dashboard.setPenggunaAktif(namaAsli, roleAsli); // <-- INI YANG BIKIN OTOMATIS

                // 4. TAMPILKAN LAYAR DASHBOARD
                Stage stage = new Stage();
                Scene dashboardScene = new Scene(root, 1280, 650);
                stage.setTitle("Smart Clinic - Dashboard");
                stage.setScene(dashboardScene);
                stage.setResizable(false); 

                stage.show();

                // Tutup layar login
                ((Node)(event.getSource())).getScene().getWindow().hide();

            } else {
                // Jika username/password salah
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Gagal");
                alert.setHeaderText(null);
                alert.setContentText("Username atau Password salah!");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleGoToRegister(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka halaman registrasi: " + e.getMessage());
        }
    }
}