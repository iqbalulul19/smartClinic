package database;
import java.sql.Connection;
import java.sql.DriverManager;
import javafx.scene.control.Alert;
public class DBConnection {
    public static Connection connect() {
        try {
            // LOAD DRIVER
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn =
                DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/smart_clinic",
                    "root",
                    ""
                );
            System.out.println("Koneksi berhasil!");
            return conn;
        } catch (Exception e) {
            System.out.println("Koneksi gagal!");
            e.printStackTrace();
            showAlert("ERROR", e.getMessage());
            return null;
        }
    }

    private static void showAlert(
        String title,String msg) {Alert alert =new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
