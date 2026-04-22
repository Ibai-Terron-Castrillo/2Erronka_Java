package Pantailak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Klaseak.Langilea;
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

        Langilea loggedUser = LoginService.login(user, pass);

        if (loggedUser != null) {
            SessionContext.setCurrentUser(loggedUser);
            StageManager.hideFloatingChatButton();

            // DEBUG: erakutsi baimenaren balioa
            System.out.println("DEBUG: Saioa hasi du " + loggedUser.getErabiltzailea() +
                    ", txat_baimena = " + loggedUser.getTxat_baimena());

            Platform.runLater(() -> {
                // Txat botoia erakutsi baimena badu bakarrik
                if (SessionContext.txatBaimenaDauka()) {
                    System.out.println("DEBUG: txatBaimenaDauka() -> true, botoia erakutsiko da.");
                    StageManager.showFloatingChatButton(loggedUser.getErabiltzailea());
                } else {
                    System.out.println("DEBUG: txatBaimenaDauka() -> false, botoia EZ da erakutsiko.");
                }
                menuNagusiaIreki();
            });
        } else {
            erroreaErakutsi("Login errorea: erabiltzailea edo pasahitza okerra.");
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
        SessionContext.logout();
        Platform.exit();
        System.exit(0);
    }
}