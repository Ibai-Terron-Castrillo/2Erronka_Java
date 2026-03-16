package Pantailak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PlaterakApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(PlaterakApplication.class.getResource("/Pantailak/platerak-view.fxml"));

        Scene scene = new Scene(loader.load());

        try {
            scene.getStylesheets().add(
                    PlaterakApplication.class.getResource("/css/osis-suite.css").toExternalForm()
            );
        } catch (NullPointerException e) {
            System.out.println("CSS not found, continuing without it");
        }

        stage.setScene(scene);
        stage.setTitle("Platerak");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}