package util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ValidationUtil {

    // =========================
    // TEXTFIELD
    // =========================
    public static boolean isEmpty(
            TextField field,
            String message) {

        if (field.getText() == null
                || field.getText()
                .trim()
                .isEmpty()) {

            AlertUtil.warning(message);

            field.requestFocus();

            return true;
        }

        return false;
    }

    // =========================
    // TEXTAREA
    // =========================
    public static boolean isEmpty(
            TextArea field,
            String message) {

        if (field.getText() == null
                || field.getText()
                .trim()
                .isEmpty()) {

            AlertUtil.warning(message);

            field.requestFocus();

            return true;
        }

        return false;
    }

    // =========================
    // COMBOBOX
    // =========================
    public static boolean isEmpty(
            ComboBox<?> comboBox,
            String message) {

        if (comboBox.getValue() == null) {

            AlertUtil.warning(message);

            comboBox.requestFocus();

            return true;
        }

        return false;
    }

    // =========================
    // DATEPICKER
    // =========================
    public static boolean isEmpty(
            DatePicker datePicker,
            String message) {

        if (datePicker.getValue() == null) {

            AlertUtil.warning(message);

            datePicker.requestFocus();

            return true;
        }

        return false;
    }
}