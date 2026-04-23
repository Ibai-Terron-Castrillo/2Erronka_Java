package Pantailak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("login-view.fxml"));

        stage.setScene(new Scene(loader.load(), 600, 400));
        stage.setTitle("Saioa Hasi");
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/icons/app_icon.png"))
        );
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
