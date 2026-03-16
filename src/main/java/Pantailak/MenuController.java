package Pantailak;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Optional;

public class MenuController {

    @FXML
    private void saioaItxi(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Saioa itxi");
        alert.setHeaderText("Ziur zaude saioa itxi nahi duzula?");
        alert.setContentText("Login pantailara itzuliko zara.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // ✅ NUEVO: Ocultar botón flotante al cerrar sesión
                StageManager.hideFloatingChatButton();

                StageManager.switchToLogin(currentStage);

            } catch (IOException e) {
                erroreaErakutsi("Errorea saioa ixtean: " + e.getMessage());
            }
        }
    }

    private void erroreaErakutsi(String mezua) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errorea");
        alert.setHeaderText(null);
        alert.setContentText(mezua);
        alert.showAndWait();
    }

    private Stage getCurrentStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    @FXML
    private void onLangileakClick(ActionEvent event) {
        try {
            StageManager.switchStage(
                    getCurrentStage(event),
                    "langileak-view.fxml",
                    "Langileak",
                    true
            );
        } catch (IOException e) {
            erroreaErakutsi("Errorea langileak kargatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onOsagaiakClick(ActionEvent event) {
        try {
            StageManager.switchStage(
                    getCurrentStage(event),
                    "osagaiak-view.fxml",
                    "Osagaiak",
                    true
            );
        } catch (IOException e) {
            erroreaErakutsi("Errorea osagaiak kargatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEskaerakClick(ActionEvent event) {
        try {
            StageManager.switchStage(
                    getCurrentStage(event),
                    "eskaerak-view.fxml",
                    "Eskaerak",
                    true
            );
        } catch (IOException e) {
            erroreaErakutsi("Errorea eskaerak kargatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onHornitzaileakClick(ActionEvent event) {
        try {
            StageManager.switchStage(
                    getCurrentStage(event),
                    "hornitzaileak-view.fxml",
                    "Hornitzaileak",
                    true
            );
        } catch (IOException e) {
            erroreaErakutsi("Errorea hornitzaileak kargatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onPlaterakClick(ActionEvent event) {
        try {
            StageManager.switchStage(
                    getCurrentStage(event),
                    "platerak-view.fxml",
                    "Platerak",
                    true
            );
        } catch (IOException e) {
            erroreaErakutsi("Errorea platerak kargatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onMahaiakClick(ActionEvent event) {
        try {
            StageManager.switchStage(
                    getCurrentStage(event),
                    "mahaiak-view.fxml",
                    "Mahaiak",
                    true
            );
        } catch (IOException e) {
            erroreaErakutsi("Errorea mahaiak kargatzean: " + e.getMessage());
            e.printStackTrace();
        }
    }
}