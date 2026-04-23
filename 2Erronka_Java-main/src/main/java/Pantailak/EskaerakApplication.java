package Pantailak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EskaerakApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(EskaerakApplication.class.getResource("/Pantailak/eskaerak-view.fxml"));

        Scene scene = new Scene(loader.load());

        try {
            scene.getStylesheets().add(
                    EskaerakApplication.class.getResource("/css/osis-suite.css").toExternalForm()
            );
        } catch (NullPointerException e) {
            System.out.println("CSS not found, continuing without it");
        }

        stage.setScene(scene);
        stage.setTitle("Eskaerak");
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/icons/app_icon.png"))
        );
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}