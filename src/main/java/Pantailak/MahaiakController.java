package Pantailak;

import Klaseak.Mahaia;
import services.MahaiaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import services.SessionContext;
import services.ActionLogger;
import java.io.IOException;
import java.util.Optional;

public class MahaiakController {

    private final ObservableList<MahaiaTableModel> mahaiakList = FXCollections.observableArrayList();
    private final ObservableList<Integer> pertsonaMaxAukerak = FXCollections.observableArrayList(2, 4, 6, 8, 10, 12, 15, 20);
    private final ObservableList<String> egoeraAukerak = FXCollections.observableArrayList("Dena", "Libre", "Okupatuta");
    private final ObservableList<String> ordenatuAukerak = FXCollections.observableArrayList(
            "Zenbakia (goraka)", "Zenbakia (beheraka)", "Pertsona max (goraka)", "Pertsona max (beheraka)");

    @FXML private Button atzeraBotoia;
    @FXML private TextField txtBilaketa;
    @FXML private ComboBox<String> egoeraFilter;
    @FXML private ComboBox<String> ordenatuFilter;
    @FXML private Label mahaiKopuruaLabel;

    @FXML private TableView<MahaiaTableModel> tblMahaiak;
    @FXML private TableColumn<MahaiaTableModel, Integer> colId;
    @FXML private TableColumn<MahaiaTableModel, Integer> colZenbakia;
    @FXML private TableColumn<MahaiaTableModel, Integer> colPertsonaMax;
    @FXML private TableColumn<MahaiaTableModel, String> colEgoera;

    @FXML private TextField txtId;
    @FXML private TextField txtZenbakia;
    @FXML private ComboBox<Integer> cmbPertsonaMax;
    @FXML private Label lblEgoera;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private Button btnEguneratu;
    @FXML private Label lblEditMode;

    @FXML private Label totalMahaiakLabel;
    @FXML private Label okupatutaLabel;
    @FXML private Label libreLabel;
    @FXML private Label gehienekoLabel;
    @FXML private Button refreshButton;

    private boolean editMode = false;
    private MahaiaTableModel mahaiEditatzen = null;

    @FXML
    public void initialize() {
        konfiguratuTaulaNagusia();
        konfiguratuComboBox();
        configuratuListeners();
        kargatuMahaiak();
        aldatuEditMode(false, null);
    }

    private void konfiguratuTaulaNagusia() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colZenbakia.setCellValueFactory(new PropertyValueFactory<>("zenbakia"));
        colPertsonaMax.setCellValueFactory(new PropertyValueFactory<>("pertsonaMax"));
        colEgoera.setCellValueFactory(new PropertyValueFactory<>("egoera"));
        tblMahaiak.setItems(mahaiakList);
    }

    private void konfiguratuComboBox() {
        if (cmbPertsonaMax != null) {
            cmbPertsonaMax.setItems(pertsonaMaxAukerak);
            cmbPertsonaMax.setCellFactory(param -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item + " pertsona");
                }
            });
            cmbPertsonaMax.setButtonCell(new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText("Aukeratu pertsona maximoak");
                    else setText(item + " pertsona");
                }
            });
            if (!pertsonaMaxAukerak.isEmpty()) cmbPertsonaMax.getSelectionModel().selectFirst();
        }

        if (egoeraFilter != null) {
            egoeraFilter.setItems(egoeraAukerak);
            egoeraFilter.getSelectionModel().selectFirst();
        }

        if (ordenatuFilter != null) {
            ordenatuFilter.setItems(ordenatuAukerak);
            ordenatuFilter.getSelectionModel().selectFirst();
        }
    }

    private void configuratuListeners() {
        mahaiakList.addListener((javafx.collections.ListChangeListener.Change<? extends MahaiaTableModel> change) -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                    eguneratuEstatistikak();
                }
            }
        });

        txtBilaketa.textProperty().addListener((obs, old, val) -> aplikatuFiltroak());
        if (egoeraFilter != null) egoeraFilter.valueProperty().addListener((obs, old, val) -> aplikatuFiltroak());
        if (ordenatuFilter != null) ordenatuFilter.valueProperty().addListener((obs, old, val) -> aplikatuOrdenazioa());
    }

    private void aplikatuFiltroak() {
        ObservableList<MahaiaTableModel> filteredList = FXCollections.observableArrayList();
        String bilaketa = txtBilaketa.getText();
        String egoera = egoeraFilter.getValue();

        for (MahaiaTableModel mahai : mahaiakList) {
            boolean pasa = true;
            if (bilaketa != null && !bilaketa.isEmpty()) {
                if (!String.valueOf(mahai.getZenbakia()).contains(bilaketa)) pasa = false;
            }
            if (egoera != null && !egoera.equals("Dena")) {
                if (egoera.equals("Libre") && mahai.isOkupatuta()) pasa = false;
                else if (egoera.equals("Okupatuta") && !mahai.isOkupatuta()) pasa = false;
            }
            if (pasa) filteredList.add(mahai);
        }
        tblMahaiak.setItems(filteredList);
        eguneratuEstatistikak();
    }

    private void aplikatuOrdenazioa() {
        String orden = ordenatuFilter.getValue();
        if (orden == null) return;
        ObservableList<MahaiaTableModel> lista = tblMahaiak.getItems();
        switch (orden) {
            case "Zenbakia (goraka)": lista.sort((a,b) -> Integer.compare(a.getZenbakia(), b.getZenbakia())); break;
            case "Zenbakia (beheraka)": lista.sort((a,b) -> Integer.compare(b.getZenbakia(), a.getZenbakia())); break;
            case "Pertsona max (goraka)": lista.sort((a,b) -> Integer.compare(a.getPertsonaMax(), b.getPertsonaMax())); break;
            case "Pertsona max (beheraka)": lista.sort((a,b) -> Integer.compare(b.getPertsonaMax(), a.getPertsonaMax())); break;
        }
        tblMahaiak.setItems(lista);
    }

    @FXML
    public void kargatuMahaiak() {
        MahaiaService.getAllMahaiak()
                .thenAccept(mahaiak -> {
                    Platform.runLater(() -> {
                        mahaiakList.clear();
                        if (mahaiak != null) {
                            for (Mahaia m : mahaiak) {
                                mahaiakList.add(new MahaiaTableModel(m.getId(), m.getMahaiaZbk(), m.getEdukiera(), m.isOkupatuta()));
                            }
                        }
                        aplikatuFiltroak();
                        aplikatuOrdenazioa();
                        System.out.println("INFO: " + mahaiakList.size() + " mahai kargatu dira");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> erakutsiMezua("Errorea", "Ezin izan dira mahaiak kargatu: " + ex.getMessage(), "ERROR"));
                    return null;
                });
    }

    @FXML
    private void onMahaiSelected() {
        MahaiaTableModel hautatuta = tblMahaiak.getSelectionModel().getSelectedItem();
        if (hautatuta != null) {
            txtId.setText(String.valueOf(hautatuta.getId()));
            txtZenbakia.setText(String.valueOf(hautatuta.getZenbakia()));
            cmbPertsonaMax.getSelectionModel().select(Integer.valueOf(hautatuta.getPertsonaMax()));

            if (hautatuta.isOkupatuta()) {
                lblEgoera.setText("Okupatuta");
                lblEgoera.setStyle("-fx-text-fill: #e53e3e; -fx-background-color: #fed7d7; -fx-border-color: #fc8181;");
            } else {
                lblEgoera.setText("Libre");
                lblEgoera.setStyle("-fx-text-fill: #38a169; -fx-background-color: #c6f6d5; -fx-border-color: #9ae6b4;");
            }
            aldatuEditMode(true, hautatuta);
        }
    }

    @FXML
    private void mahaiBerriaSortu() {
        garbituFormularioa();
        aldatuEditMode(false, null);
    }

    @FXML
    private void garbituFormularioa() {
        txtId.clear();
        txtZenbakia.clear();
        if (!pertsonaMaxAukerak.isEmpty()) cmbPertsonaMax.getSelectionModel().selectFirst();
        lblEgoera.setText("Libre");
        lblEgoera.setStyle("-fx-text-fill: #38a169; -fx-background-color: #f0fff4; -fx-border-color: #c6f6d5;");
        tblMahaiak.getSelectionModel().clearSelection();
        aldatuEditMode(false, null);
    }

    @FXML
    private void gordeMahai() {
        if (!balidatuFormularioa()) return;

        try {
            Mahaia mahaiBerria = new Mahaia();
            mahaiBerria.setMahaiaZbk(Integer.parseInt(txtZenbakia.getText()));
            mahaiBerria.setEdukiera(cmbPertsonaMax.getValue());
            mahaiBerria.setEgoera("Libre");  // sortzean beti libre

            MahaiaService.createMahai(mahaiBerria)
                    .thenAccept(gordetako -> {
                        Platform.runLater(() -> {
                            if (gordetako != null) {
                                ActionLogger.log(SessionContext.getCurrentUsername(), "INSERT", "mahaiak",
                                        "Mahaia sortu: Zenbakia=" + gordetako.getMahaiaZbk() + ", Edukiera=" + gordetako.getEdukiera());
                                mahaiakList.add(new MahaiaTableModel(gordetako.getId(), gordetako.getMahaiaZbk(), gordetako.getEdukiera(), false));
                                garbituFormularioa();
                                erakutsiMezua("Arrakasta", "Mahaia ondo gorde da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da mahai gorde", "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> erakutsiMezua("Errorea", "Errorea: " + ex.getMessage(), "ERROR"));
                        return null;
                    });
        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Zenbakiak sartu behar dira", "ERROR");
        }
    }

    @FXML
    private void eguneratuMahai() {
        if (!balidatuFormularioa() || mahaiEditatzen == null) return;

        try {
            Mahaia mahaiEguneratu = new Mahaia();
            mahaiEguneratu.setId(mahaiEditatzen.getId());
            mahaiEguneratu.setMahaiaZbk(Integer.parseInt(txtZenbakia.getText()));
            mahaiEguneratu.setEdukiera(cmbPertsonaMax.getValue());
            mahaiEguneratu.setEgoera(mahaiEditatzen.isOkupatuta() ? "Okupatuta" : "Libre");

            MahaiaService.updateMahai(mahaiEditatzen.getId(), mahaiEguneratu)
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {
                            if (arrakasta) {
                                ActionLogger.log(SessionContext.getCurrentUsername(), "UPDATE", "mahaiak",
                                        "Mahaia eguneratu: ID=" + mahaiEditatzen.getId());
                                mahaiEditatzen.setZenbakia(mahaiEguneratu.getMahaiaZbk());
                                mahaiEditatzen.setPertsonaMax(mahaiEguneratu.getEdukiera());
                                int index = mahaiakList.indexOf(mahaiEditatzen);
                                if (index >= 0) mahaiakList.set(index, mahaiEditatzen);
                                erakutsiMezua("Arrakasta", "Mahaia ondo eguneratu da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da mahai eguneratu", "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> erakutsiMezua("Errorea", "Errorea: " + ex.getMessage(), "ERROR"));
                        return null;
                    });
        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Zenbakiak sartu behar dira", "ERROR");
        }
    }

    @FXML
    private void ezabatuMahaia(MahaiaTableModel mahai) {
        if (mahai == null) {
            erakutsiMezua("Abisua", "Mesedez, hautatu mahai bat ezabatzeko", "WARNING");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mahaia Ezabatu");
        alert.setHeaderText("Ziur zaude mahai hau ezabatu nahi duzula?");
        alert.setContentText("Mahai zenbakia: " + mahai.getZenbakia());
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.YES) {
            MahaiaService.deleteMahai(mahai.getId())
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {
                            if (arrakasta) {
                                ActionLogger.log(SessionContext.getCurrentUsername(), "DELETE", "mahaiak",
                                        "Mahaia ezabatu: ID=" + mahai.getId());
                                mahaiakList.remove(mahai);
                                if (mahaiEditatzen != null && mahaiEditatzen.getId() == mahai.getId()) garbituFormularioa();
                                erakutsiMezua("Arrakasta", "Mahaia ondo ezabatu da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da mahai ezabatu. Erlazioak ditu.", "ERROR");
                            }
                        });
                    });
        }
    }

    @FXML
    private void ezabatuMahaiBotoia() {
        ezabatuMahaia(tblMahaiak.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void eguneratuEstatistikak() {
        ObservableList<MahaiaTableModel> aktuLista = tblMahaiak.getItems();
        int total = aktuLista.size();
        int okupatuta = (int) aktuLista.stream().filter(MahaiaTableModel::isOkupatuta).count();
        int libre = total - okupatuta;
        int gehieneko = aktuLista.stream().mapToInt(MahaiaTableModel::getPertsonaMax).sum();

        totalMahaiakLabel.setText(String.valueOf(total));
        okupatutaLabel.setText(String.valueOf(okupatuta));
        libreLabel.setText(String.valueOf(libre));
        gehienekoLabel.setText(String.valueOf(gehieneko));
        mahaiKopuruaLabel.setText(total + " mahai");
    }

    @FXML
    public void atzeraBueltatu(javafx.event.ActionEvent actionEvent) {
        try {
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            StageManager.switchStage(currentStage, "menu-view.fxml", "Menu Nagusia", true);
        } catch (IOException e) {
            e.printStackTrace();
            erakutsiMezua("Errorea", "Ezin izan da atzera itzuli: " + e.getMessage(), "ERROR");
        }
    }

    private boolean balidatuFormularioa() {
        if (txtZenbakia.getText().isEmpty() || !txtZenbakia.getText().matches("\\d+")) {
            erakutsiMezua("Errorea", "Mahai zenbakia zenbaki osoa izan behar da", "ERROR");
            return false;
        }
        int zenbakia = Integer.parseInt(txtZenbakia.getText());
        if (zenbakia <= 0) {
            erakutsiMezua("Errorea", "Mahai zenbakia 0 baino handiagoa izan behar da", "ERROR");
            return false;
        }
        if (!editMode || (mahaiEditatzen != null && zenbakia != mahaiEditatzen.getZenbakia())) {
            for (MahaiaTableModel mahai : mahaiakList) {
                if (mahai.getZenbakia() == zenbakia) {
                    erakutsiMezua("Errorea", "Mahai zenbakia dagoeneko existitzen da", "ERROR");
                    return false;
                }
            }
        }
        if (cmbPertsonaMax.getValue() == null) {
            erakutsiMezua("Errorea", "Aukeratu pertsona maximo kopurua", "ERROR");
            return false;
        }
        return true;
    }

    private void aldatuEditMode(boolean editatu, MahaiaTableModel mahai) {
        editMode = editatu;
        mahaiEditatzen = mahai;
        btnSave.setVisible(!editatu);
        btnSave.setManaged(!editatu);
        btnEguneratu.setVisible(editatu);
        btnEguneratu.setManaged(editatu);
        txtId.setVisible(editatu);
        if (editatu && mahai != null) {
            lblEditMode.setText("EDITATZEN: Mahaia " + mahai.getZenbakia());
            lblEditMode.setStyle("-fx-font-weight: bold; -fx-text-fill: #38a169;");
        } else {
            lblEditMode.setText("MAHAI BERRIA");
            lblEditMode.setStyle("-fx-font-weight: bold; -fx-text-fill: #3182ce;");
        }
    }

    private void erakutsiMezua(String titulua, String mezua, String mota) {
        Alert.AlertType alertType;
        switch (mota) {
            case "ERROR": alertType = Alert.AlertType.ERROR; break;
            case "WARNING": alertType = Alert.AlertType.WARNING; break;
            default: alertType = Alert.AlertType.INFORMATION;
        }
        Alert alert = new Alert(alertType);
        alert.setTitle(titulua);
        alert.setHeaderText(null);
        alert.setContentText(mezua);
        alert.showAndWait();
    }

    public static class MahaiaTableModel {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty zenbakia;
        private final SimpleIntegerProperty pertsonaMax;
        private final SimpleStringProperty egoera;
        private final SimpleBooleanProperty okupatuta;

        public MahaiaTableModel(int id, int zenbakia, int pertsonaMax, boolean okupatuta) {
            this.id = new SimpleIntegerProperty(id);
            this.zenbakia = new SimpleIntegerProperty(zenbakia);
            this.pertsonaMax = new SimpleIntegerProperty(pertsonaMax);
            this.okupatuta = new SimpleBooleanProperty(okupatuta);
            this.egoera = new SimpleStringProperty(okupatuta ? "Okupatuta" : "Libre");
        }

        public int getId() { return id.get(); }
        public int getZenbakia() { return zenbakia.get(); }
        public int getPertsonaMax() { return pertsonaMax.get(); }
        public String getEgoera() { return egoera.get(); }
        public boolean isOkupatuta() { return okupatuta.get(); }

        public void setId(int id) { this.id.set(id); }
        public void setZenbakia(int zenbakia) { this.zenbakia.set(zenbakia); }
        public void setPertsonaMax(int pertsonaMax) { this.pertsonaMax.set(pertsonaMax); }
        public void setOkupatuta(boolean okupatuta) {
            this.okupatuta.set(okupatuta);
            this.egoera.set(okupatuta ? "Okupatuta" : "Libre");
        }
    }
}