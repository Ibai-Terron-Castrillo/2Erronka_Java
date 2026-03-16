package Pantailak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.LoginService;
import services.SessionContext;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField erabiltzailea;

    @FXML
    private PasswordField pasahitza;

    @FXML
    private void saioaHasi() {
        String user = erabiltzailea.getText();
        String pass = pasahitza.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            erroreaErakutsi("Mesedez, bete erabiltzailea eta pasahitza.");
            return;
        }

        String result = LoginService.login(user, pass);

        if ("OK".equals(result)) {
            SessionContext.setCurrentUser(user);
            StageManager.hideFloatingChatButton();

            Platform.runLater(() -> {
                StageManager.showFloatingChatButton(user);
                menuNagusiaIreki();
            });
        } else {
            erroreaErakutsi("Login errorea: " + result);
        }
    }

    @FXML
    private void menuNagusiaIreki() {
        try {
            Stage menuStage = StageManager.openStage(
                    "menu-view.fxml",
                    "OSIS Suite - Menu Nagusia",
                    true,
                    0,
                    0
            );

            Stage loginStage = (Stage) erabiltzailea.getScene().getWindow();
            loginStage.close();

            menuStage.setOnCloseRequest(e -> {
                StageManager.hideFloatingChatButton();
                Platform.exit();
                System.exit(0);
            });

            menuStage.show();

        } catch (IOException e) {
            erroreaErakutsi("Errorea menua irekitzean: " + e.getMessage());
        }
    }

    @FXML
    private void erroreaErakutsi(String mezua) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errorea");
            alert.setHeaderText(null);
            alert.setContentText(mezua);
            alert.showAndWait();
        });
    }

    @FXML
    protected void irten() {
        StageManager.hideFloatingChatButton();
        SessionContext.clear();
        Platform.exit();
        System.exit(0);
    }
}