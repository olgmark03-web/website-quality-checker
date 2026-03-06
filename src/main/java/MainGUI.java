import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainGUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Φόρτωση του FXML αρχείου
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main/java/MainView.fxml")));

            Scene scene = new Scene(root, 800, 600);

            // Προσθήκη του CSS (αν υπάρχει)
            try {
                String css = Objects.requireNonNull(getClass().getResource("main/java/style.css")).toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("CSS not loaded: " + e.getMessage());
            }

            // Ρύθμιση του παραθύρου
            primaryStage.setTitle("Website Quality Checker");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {

            System.err.println("Error loading application: " + e.getMessage());

        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}


