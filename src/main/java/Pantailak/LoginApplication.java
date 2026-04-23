package Pantailak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        URL fxmlUrl = resolveResourceUrl("Pantailak/login-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);

        stage.setScene(new Scene(loader.load(), 600, 400));
        stage.setTitle("Saioa Hasi");
        stage.getIcons().add(
                new Image(Objects.requireNonNull(
                        resolveResourceUrl("icons/app_icon.png"),
                        "Ezin da aurkitu ikonoa: icons/app_icon.png"
                ).openStream())
        );
        stage.setResizable(false);
        stage.show();
    }

    private static URL resolveResourceUrl(String path) {
        URL url = LoginApplication.class.getResource("/" + path);
        if (url != null) return url;
        url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url != null) return url;
        try {
            Path p1 = Paths.get("src", "main", "resources").resolve(path);
            if (Files.exists(p1)) return p1.toUri().toURL();
            Path p2 = Paths.get("target", "classes").resolve(path);
            if (Files.exists(p2)) return p2.toUri().toURL();
        } catch (Exception ignored) {
        }
        throw new IllegalStateException("Ezin da aurkitu resourcea: " + path);
    }


    public static void main(String[] args) {
        launch();
    }
}
