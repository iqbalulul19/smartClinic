import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(root);
        
        stage.setTitle("Smart Clinic - Login System");
        stage.setScene(scene);
        
        // Pastikan Login awal TIDAK full screen
        stage.setMaximized(false); 
        // (Opsional) Kunci ukuran layar agar form login tidak bisa ditarik-tarik
        stage.setResizable(false); 
        stage.sizeToScene();
        stage.show();
        stage.centerOnScreen();

    }

    public static void main(String[] args) {
        launch();
    }
}