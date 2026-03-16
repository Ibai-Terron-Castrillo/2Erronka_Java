package Pantailak;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import Klaseak.Erabiltzailea;
import Klaseak.Langilea;
import Klaseak.Lanpostua;

import services.ErabiltzaileaService;
import services.LangileaService;

public class LangileakForm {

    @FXML private TextField txtIzena, txtAbizena1, txtAbizena2, txtTelefonoa;
    @FXML private ComboBox<Lanpostua> comboLanpostu;
    @FXML private CheckBox checkErabiltzaile;
    @FXML private VBox boxErabiltzaile;
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    private static Langilea editing;
    private static Runnable refreshCallback;

    private Erabiltzailea loadedErabiltzailea;

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

        comboLanpostu.getItems().setAll(LangileaService.getLanpostuak());

        checkErabiltzaile.selectedProperty().addListener((obs, old, val) -> {
            boxErabiltzaile.setVisible(val);
            boxErabiltzaile.setManaged(val);
        });

        if (editing != null) {

            txtIzena.setText(editing.getIzena());
            txtAbizena1.setText(editing.getAbizena1());
            txtAbizena2.setText(editing.getAbizena2());
            txtTelefonoa.setText(editing.getTelefonoa());
            comboLanpostu.setValue(editing.getLanpostua());

            loadedErabiltzailea = ErabiltzaileaService.getByLangile(editing.getId());

            if (loadedErabiltzailea != null) {
                checkErabiltzaile.setSelected(true);
                txtUser.setText(loadedErabiltzailea.getIzena());
                txtPass.setText(loadedErabiltzailea.getPasahitza());
            }
        }
    }

    @FXML
    private void onSave() {

        if (txtIzena.getText().isBlank()) {
            showError("Izena jarri behar da.");
            return;
        }

        if (txtTelefonoa.getText().isBlank()) {
            showError("Telefonoa jarri behar da.");
            return;
        }

        String telefono = txtTelefonoa.getText();
        if (!telefono.matches("\\d{9,}")) {
            showError("Telefonoak zenbakiak bakarrik izan behar ditu eta gutxienez 9 digitu.");
            return;
        }

        if (comboLanpostu.getValue() == null) {
            showError("Lanpostu bat aukeratu behar da.");
            return;
        }

        if (checkErabiltzaile.isSelected()) {

            if (txtUser.getText().isBlank()) {
                showError("Erabiltzaile izena jarri behar da.");
                return;
            }

            if (txtPass.getText().isBlank()) {
                showError("Pasahitza jarri behar da.");
                return;
            }
        }

        Langilea l = (editing == null ? new Langilea() : editing);

        l.setIzena(txtIzena.getText());
        l.setAbizena1(txtAbizena1.getText());
        l.setAbizena2(txtAbizena2.getText());
        l.setTelefonoa(txtTelefonoa.getText());
        l.setLanpostua(comboLanpostu.getValue());

        if (editing == null) {
            l = LangileaService.create(l);
        } else {
            LangileaService.update(l);
        }

        if (checkErabiltzaile.isSelected()) {

            Erabiltzailea er = loadedErabiltzailea;

            if (er == null) {
                er = new Erabiltzailea();
                er.setLangilea(l);
            }

            er.setIzena(txtUser.getText());
            er.setPasahitza(txtPass.getText());

            ErabiltzaileaService.saveOrUpdate(er);

        } else {
            if (loadedErabiltzailea != null) {
                ErabiltzaileaService.delete(loadedErabiltzailea.getId());
            }
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
