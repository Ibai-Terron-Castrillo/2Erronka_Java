package Pantailak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HornitzaileakApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(HornitzaileakApplication.class.getResource("/Pantailak/hornitzaileak-view.fxml"));

        Scene scene = new Scene(loader.load());

        try {
            scene.getStylesheets().add(
                    HornitzaileakApplication.class.getResource("/css/osis-suite.css").toExternalForm()
            );
        } catch (NullPointerException e) {
            System.out.println("CSS not found, continuing without it");
        }

        stage.setScene(scene);
        stage.setTitle("Hornitzaileak");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}