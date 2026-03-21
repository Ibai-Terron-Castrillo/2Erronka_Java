package Pantailak;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import Klaseak.Langilea;
import Klaseak.Rolak;
import services.LangileaService;

import java.time.LocalDateTime;
import java.util.List;

public class LangileakForm {

    @FXML private TextField txtIzena;
    @FXML private TextField txtErabiltzailea;
    @FXML private PasswordField txtPasahitza;
    @FXML private ComboBox<Rolak> comboLanpostu;

    private static Langilea editing;
    private static Runnable refreshCallback;

    public static void show(Langilea langile, Runnable onRefresh) {
        try {
            editing = langile;
            refreshCallback = onRefresh;

            FXMLLoader loader = new FXMLLoader(LangileakForm.class.getResource("langileak-form.fxml"));
            VBox root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(langile == null ? "Langile berria" : "Aldatu langilea");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        List<Rolak> rolList = LangileaService.getAllRolak();
        comboLanpostu.setItems(javafx.collections.FXCollections.observableArrayList(rolList));

        if (editing != null) {
            txtIzena.setText(editing.getIzena());
            txtErabiltzailea.setText(editing.getErabiltzailea());
            txtPasahitza.setText(editing.getPasahitza());

            if (editing.getRola() != null) {
                comboLanpostu.setValue(editing.getRola());
            } else {
                comboLanpostu.getItems().stream()
                        .filter(r -> r.getId() == editing.getRolaId())
                        .findFirst()
                        .ifPresent(comboLanpostu::setValue);
            }
        }
    }

    @FXML
    private void onSave() {
        if (txtIzena.getText().isBlank()) {
            showError("Izena jarri behar da.");
            return;
        }
        if (txtErabiltzailea.getText().isBlank()) {
            showError("Erabiltzaile izena jarri behar da.");
            return;
        }
        if (txtPasahitza.getText().isBlank()) {
            showError("Pasahitza jarri behar da.");
            return;
        }

        Rolak selectedRol = comboLanpostu.getValue();
        if (selectedRol == null) {
            showError("Rol bat aukeratu behar da.");
            return;
        }

        Langilea l = (editing == null) ? new Langilea() : editing;

        l.setIzena(txtIzena.getText().trim());
        l.setErabiltzailea(txtErabiltzailea.getText().trim());
        l.setPasahitza(txtPasahitza.getText().trim());
        l.setAktibo("Bai");
        if (l.getErregistroData() == null) {
            l.setErregistroData(LocalDateTime.now());
        }
        l.setRolaId(selectedRol.getId());
        l.setRola(selectedRol);

        if (editing == null) {
            LangileaService.create(l);
        } else {
            LangileaService.update(l);
        }

        refreshCallback.run();
        close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Errorea");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) txtIzena.getScene().getWindow();
        stage.close();
    }
}