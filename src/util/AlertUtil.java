package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtil {

    // =========================
    // SUCCESS
    // =========================
    public static void success(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION);

        alert.setTitle("Sukses");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // =========================
    // ERROR
    // =========================
    public static void error(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR);

        alert.setTitle("Error");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // =========================
    // WARNING
    // =========================
    public static void warning(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING);

        alert.setTitle("Peringatan");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // =========================
    // CONFIRMATION
    // =========================
    public static boolean confirm(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION);

        alert.setTitle("Konfirmasi");

        alert.setHeaderText(null);

        alert.setContentText(message);

        return alert.showAndWait()
                .get() == ButtonType.OK;
    }
}