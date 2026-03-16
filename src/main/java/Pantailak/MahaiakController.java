package Pantailak;

import Klaseak.Mahaia;
import services.MahaiaService;
import com.google.gson.*;
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

    private final MahaiaService mahaiService = new MahaiaService();

    private final ObservableList<MahaiaTableModel> mahaiakList = FXCollections.observableArrayList();
    private final ObservableList<Integer> pertsonaMaxAukerak = FXCollections.observableArrayList(
            2, 4, 6, 8, 10, 12, 15, 20
    );
    private final ObservableList<String> egoeraAukerak = FXCollections.observableArrayList(
            "Dena", "Libre", "Okupatuta"
    );
    private final ObservableList<String> ordenatuAukerak = FXCollections.observableArrayList(
            "Zenbakia (goraka)", "Zenbakia (beheraka)", "Pertsona max (goraka)", "Pertsona max (beheraka)"
    );

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
    @FXML private TableColumn<MahaiaTableModel, String> colEkintzak;

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
        System.out.println("INFO: MahaiakController hasieratzen");

        try {
            konfiguratuTaulaNagusia();

            konfiguratuComboBox();

            configuratuListeners();

            kargatuMahaiak();

            aldatuEditMode(false, null);

            System.out.println("INFO: Controller ondo hasieratuta");
        } catch (Exception e) {
            System.err.println("ERROR: initialize()-n: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void konfiguratuTaulaNagusia() {
        try {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colZenbakia.setCellValueFactory(new PropertyValueFactory<>("zenbakia"));
            colPertsonaMax.setCellValueFactory(new PropertyValueFactory<>("pertsonaMax"));
            colEgoera.setCellValueFactory(new PropertyValueFactory<>("egoera"));

            tblMahaiak.setItems(mahaiakList);
            System.out.println("INFO: Taula nagusia konfiguratuta");
        } catch (Exception e) {
            System.err.println("ERROR: Taula konfiguratzen: " + e.getMessage());
        }
    }

    private void konfiguratuComboBox() {
        if (cmbPertsonaMax != null) {
            cmbPertsonaMax.setItems(pertsonaMaxAukerak);

            cmbPertsonaMax.setCellFactory(param -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item + " pertsona");
                    }
                }
            });

            cmbPertsonaMax.setButtonCell(new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Aukeratu pertsona maximoak");
                    } else {
                        setText(item + " pertsona");
                    }
                }
            });

            if (!pertsonaMaxAukerak.isEmpty()) {
                cmbPertsonaMax.getSelectionModel().selectFirst();
            }
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

        txtBilaketa.textProperty().addListener((observable, oldValue, newValue) -> {
            aplikatuFiltroak();
        });

        if (egoeraFilter != null) {
            egoeraFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                aplikatuFiltroak();
            });
        }

        if (ordenatuFilter != null) {
            ordenatuFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                aplikatuOrdenazioa();
            });
        }
    }

    private void aplikatuFiltroak() {
        ObservableList<MahaiaTableModel> filteredList = FXCollections.observableArrayList();

        for (MahaiaTableModel mahai : mahaiakList) {
            boolean pasa = true;

            String bilaketa = txtBilaketa.getText();
            if (bilaketa != null && !bilaketa.isEmpty()) {
                if (!String.valueOf(mahai.getZenbakia()).contains(bilaketa)) {
                    pasa = false;
                }
            }

            String egoera = egoeraFilter.getValue();
            if (egoera != null && !egoera.equals("Dena")) {
                if (egoera.equals("Libre") && mahai.isOkupatuta()) {
                    pasa = false;
                } else if (egoera.equals("Okupatuta") && !mahai.isOkupatuta()) {
                    pasa = false;
                }
            }

            if (pasa) {
                filteredList.add(mahai);
            }
        }

        tblMahaiak.setItems(filteredList);
        eguneratuEstatistikak();
    }

    private void aplikatuOrdenazioa() {
        String ordenatu = ordenatuFilter.getValue();
        if (ordenatu == null) return;

        ObservableList<MahaiaTableModel> lista = tblMahaiak.getItems();

        switch (ordenatu) {
            case "Zenbakia (goraka)":
                lista.sort((a, b) -> Integer.compare(a.getZenbakia(), b.getZenbakia()));
                break;
            case "Zenbakia (beheraka)":
                lista.sort((a, b) -> Integer.compare(b.getZenbakia(), a.getZenbakia()));
                break;
            case "Pertsona max (goraka)":
                lista.sort((a, b) -> Integer.compare(a.getPertsonaMax(), b.getPertsonaMax()));
                break;
            case "Pertsona max (beheraka)":
                lista.sort((a, b) -> Integer.compare(b.getPertsonaMax(), a.getPertsonaMax()));
                break;
        }

        tblMahaiak.setItems(lista);
    }

    @FXML
    public void kargatuMahaiak() {
        System.out.println("DEBUG: kargatuMahaiak() dei egiten");

        mahaiService.getAllMahai()
                .thenAccept(mahaiak -> {
                    Platform.runLater(() -> {
                        System.out.println("DEBUG: API-tik erantzuna jaso da");
                        System.out.println("DEBUG: mahaiak null al da? " + (mahaiak == null));
                        System.out.println("DEBUG: Jasotako mahai kopurua: " + (mahaiak != null ? mahaiak.size() : 0));

                        mahaiakList.clear();
                        if (mahaiak != null && !mahaiak.isEmpty()) {
                            int kontadorea = 0;
                            for (Mahaia mahai : mahaiak) {
                                kontadorea++;
                                System.out.println("DEBUG Mahai " + kontadorea + ":");
                                System.out.println("  - ID: " + mahai.getId());
                                System.out.println("  - Zenbakia: " + mahai.getZenbakia());
                                System.out.println("  - PertsonaMax: " + mahai.getPertsonaMax());
                                System.out.println("  - Occupied: " + mahai.isOccupied());

                                MahaiaTableModel model = new MahaiaTableModel(
                                        mahai.getId(),
                                        mahai.getZenbakia(),
                                        mahai.getPertsonaMax(),
                                        mahai.isOccupied()
                                );

                                System.out.println("  - Modeloa - ID: " + model.getId() +
                                        ", Zenbakia: " + model.getZenbakia() +
                                        ", Okupatuta: " + model.isOkupatuta());

                                mahaiakList.add(model);
                            }
                        } else {
                            System.out.println("DEBUG: Ez da mahairik jaso edo zerrenda hutsik dago");
                        }

                        System.out.println("DEBUG: Kargatu ondoren mahaiakList tamaina: " + mahaiakList.size());

                        System.out.println("DEBUG: tblMahaiak null al da? " + (tblMahaiak == null));

                        if (tblMahaiak != null) {
                            tblMahaiak.refresh();
                            System.out.println("DEBUG: Taula freskatu da");

                            System.out.println("DEBUG: Zutabe kopurua: " + tblMahaiak.getColumns().size());
                            for (TableColumn col : tblMahaiak.getColumns()) {
                                System.out.println("DEBUG Zutabea: " + col.getText());
                            }
                        }

                        aplikatuFiltroak();
                        aplikatuOrdenazioa();
                        System.out.println("INFO: " + mahaiakList.size() + " mahai kargatu dira");
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("ERROR kargatuMahaiak-en: " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        erakutsiMezua("Errorea", "Ezin izan dira mahaiak kargatu: " + ex.getMessage(), "ERROR");
                    });
                    return null;
                });
    }

    @FXML
    private void onMahaiSelected() {
        MahaiaTableModel hautatuta = tblMahaiak.getSelectionModel().getSelectedItem();
        if (hautatuta != null) {
            System.out.println("INFO: Mahaia hautatuta: " + hautatuta.getZenbakia() + " (ID: " + hautatuta.getId() + ")");

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

            System.out.println("INFO: Mahaia hautatuta. Erabili formularioa editatzeko.");
        }
    }

    private void editatuMahaia(MahaiaTableModel mahai) {
        if (mahai != null) {
            tblMahaiak.getSelectionModel().select(mahai);
            onMahaiSelected();
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
        if (!pertsonaMaxAukerak.isEmpty()) {
            cmbPertsonaMax.getSelectionModel().selectFirst();
        }

        lblEgoera.setText("Libre");
        lblEgoera.setStyle("-fx-text-fill: #38a169; -fx-background-color: #f0fff4; -fx-border-color: #c6f6d5;");

        tblMahaiak.getSelectionModel().clearSelection();

        aldatuEditMode(false, null);
    }

    @FXML
    private void gordeMahai() {
        if (!balidatuFormularioa()) {
            return;
        }

        try {
            Mahaia mahaiBerria = new Mahaia();
            mahaiBerria.setZenbakia(Integer.parseInt(txtZenbakia.getText()));
            mahaiBerria.setPertsonaMax(cmbPertsonaMax.getValue());

            mahaiService.createMahai(mahaiBerria)
                    .thenAccept(gordetakoMahaia -> {
                        Platform.runLater(() -> {
                            if (gordetakoMahaia != null) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUser(),
                                        "INSERT",
                                        "mahaiak",
                                        "Mahaia sortu: Zenbakia=" + gordetakoMahaia.getZenbakia() +
                                                ", PertsonaMax=" + gordetakoMahaia.getPertsonaMax()
                                );

                                mahaiakList.add(new MahaiaTableModel(
                                        gordetakoMahaia.getId(),
                                        gordetakoMahaia.getZenbakia(),
                                        gordetakoMahaia.getPertsonaMax(),
                                        false
                                ));
                                garbituFormularioa();
                                erakutsiMezua("Arrakasta", "Mahaia ondo gorde da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da mahai gorde", "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            erakutsiMezua("Errorea", "Errorea mahai gordetzean: " + ex.getMessage(), "ERROR");
                        });
                        return null;
                    });

        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Zenbakiak sartu behar dira", "ERROR");
        } catch (Exception e) {
            erakutsiMezua("Errorea", "Errorea mahai gordetzean: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void eguneratuMahai() {
        if (!balidatuFormularioa() || mahaiEditatzen == null) {
            return;
        }

        try {
            Mahaia mahaiEguneratu = new Mahaia();
            mahaiEguneratu.setId(mahaiEditatzen.getId());
            mahaiEguneratu.setZenbakia(Integer.parseInt(txtZenbakia.getText()));
            mahaiEguneratu.setPertsonaMax(cmbPertsonaMax.getValue());

            mahaiService.updateMahai(mahaiEditatzen.getId(), mahaiEguneratu)
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {
                            if (arrakasta) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUser(),
                                        "UPDATE",
                                        "mahaiak",
                                        "Mahaia eguneratu (ID=" + mahaiEditatzen.getId() +
                                                ", Zenbakia=" + mahaiEditatzen.getZenbakia() +
                                                ", PertsonaMax=" + mahaiEditatzen.getPertsonaMax() + ")"
                                );

                                mahaiEditatzen.setZenbakia(Integer.parseInt(txtZenbakia.getText()));
                                mahaiEditatzen.setPertsonaMax(cmbPertsonaMax.getValue());

                                int index = mahaiakList.indexOf(mahaiEditatzen);
                                if (index >= 0) {
                                    mahaiakList.set(index, mahaiEditatzen);
                                }

                                erakutsiMezua("Arrakasta", "Mahaia ondo eguneratu da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da mahai eguneratu", "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            erakutsiMezua("Errorea", "Errorea mahai eguneratzean: " + ex.getMessage(), "ERROR");
                        });
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
        alert.setContentText(
                "Mahaia informazioa:\n" +
                        "• ID: " + mahai.getId() + "\n" +
                        "• Mahai Zenbakia: " + mahai.getZenbakia() + "\n" +
                        "• Pertsona Maximoak: " + mahai.getPertsonaMax() + "\n" +
                        "• Egoera: " + (mahai.isOkupatuta() ? "Okupatuta" : "Libre") + "\n\n" +
                        "OHARRA: Mahaiak erreserbarik baditu, ezin izango da ezabatu."
        );

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {

            mahaiService.deleteMahai(mahai.getId())
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {

                            if (arrakasta) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUser(),
                                        "DELETE",
                                        "mahaiak",
                                        "Mahaia ezabatu (ID=" + mahai.getId() +
                                                ", Zenbakia=" + mahai.getZenbakia() + ")"
                                );

                                mahaiakList.remove(mahai);

                                if (mahaiEditatzen != null && mahaiEditatzen.getId() == mahai.getId()) {
                                    garbituFormularioa();
                                }

                                tblMahaiak.getSelectionModel().clearSelection();

                                erakutsiMezua("Arrakasta",
                                        "Mahaia ondo ezabatu da!\n" +
                                                "Zenbakia: " + mahai.getZenbakia() + "\n" +
                                                "ID: " + mahai.getId(),
                                        "SUCCESS");

                                eguneratuEstatistikak();

                            } else {
                                erakutsiMezua("Errorea",
                                        "Ezin izan da mahai ezabatu. Posible arrazoiak:\n\n" +
                                                "1. Mahaiak erreserba aktiboak ditu\n" +
                                                "2. Mahai historiko batekin erlazionatuta dago\n" +
                                                "3. Sistema errore bat du (jarri harremanetan administratzailearekin)",
                                        "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            ActionLogger.log(
                                    SessionContext.getCurrentUser(),
                                    "ERROR",
                                    "mahaiak",
                                    "Errorea mahaia ezabatzean (ID=" + mahai.getId() + "): " + ex.getMessage()
                            );

                            String errorMsg = ex.getMessage();
                            if (errorMsg.contains("Unable to cast") && errorMsg.contains("Int64")) {
                                erakutsiMezua("Errore Teknikoa",
                                        "APIak errore tekniko bat du:\n\n" +
                                                "'Unable to cast object of type System.Int64 to System.Int32'\n\n" +
                                                "Hau APIaren akats bat da. Administratzaileari jakinarazi behar zaio.",
                                        "ERROR");
                            } else {
                                erakutsiMezua("Errorea",
                                        "Errorea mahai ezabatzean:\n" + errorMsg,
                                        "ERROR");
                            }
                        });
                        return null;
                    });
        }
    }

    @FXML
    private void ezabatuMahaiBotoia() {
        MahaiaTableModel hautatuta = tblMahaiak.getSelectionModel().getSelectedItem();
        ezabatuMahaia(hautatuta);
    }

    @FXML
    private void eguneratuEstatistikakIkusi() {
        kargatuMahaiak();
    }

    @FXML
    private void eguneratuEstatistikak() {
        ObservableList<MahaiaTableModel> aktuLista = tblMahaiak.getItems();

        int total = aktuLista.size();
        int okupatuta = (int) aktuLista.stream()
                .filter(MahaiaTableModel::isOkupatuta)
                .count();
        int libre = total - okupatuta;

        int gehieneko = aktuLista.stream()
                .mapToInt(MahaiaTableModel::getPertsonaMax)
                .sum();

        totalMahaiakLabel.setText(String.valueOf(total));
        okupatutaLabel.setText(String.valueOf(okupatuta));
        libreLabel.setText(String.valueOf(libre));
        gehienekoLabel.setText(String.valueOf(gehieneko));

        mahaiKopuruaLabel.setText(total + " mahai");

        System.out.println("INFO: Mahaia estatistikak: Total=" + total + ", Okupatuta=" + okupatuta + ", Libre=" + libre);
    }

    @FXML
    public void atzeraBueltatu(javafx.event.ActionEvent actionEvent) {
        try {
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            StageManager.switchStage(
                    currentStage,
                    "menu-view.fxml",
                    "Menu Nagusia",
                    true
            );
        } catch (IOException e) {
            e.printStackTrace();
            erakutsiMezua("Errorea", "Ezin izan da atzera itzuli: " + e.getMessage(), "ERROR");
        }
    }

    private boolean balidatuFormularioa() {
        if (txtZenbakia == null || txtZenbakia.getText().isEmpty() || !txtZenbakia.getText().matches("\\d+")) {
            erakutsiMezua("Errorea", "Mahaia zenbakia zenbaki osoa izan behar da", "ERROR");
            return false;
        }

        int zenbakia = Integer.parseInt(txtZenbakia.getText());
        if (zenbakia <= 0) {
            erakutsiMezua("Errorea", "Mahaia zenbakia 0 baino handiagoa izan behar da", "ERROR");
            return false;
        }

        if (!editMode || (mahaiEditatzen != null && zenbakia != mahaiEditatzen.getZenbakia())) {
            for (MahaiaTableModel mahai : mahaiakList) {
                if (mahai.getZenbakia() == zenbakia) {
                    erakutsiMezua("Errorea", "Mahaia zenbakia dagoeneko existitzen da", "ERROR");
                    return false;
                }
            }
        }

        if (cmbPertsonaMax == null || cmbPertsonaMax.getValue() == null) {
            erakutsiMezua("Errorea", "Aukeratu pertsona maximo kopurua", "ERROR");
            return false;
        }

        int pertsonaMax = cmbPertsonaMax.getValue();
        if (pertsonaMax <= 0 || pertsonaMax > 20) {
            erakutsiMezua("Errorea", "Pertsona maximoak 1 eta 20 artean izan behar da", "ERROR");
            return false;
        }

        return true;
    }

    private void aldatuEditMode(boolean editatu, MahaiaTableModel mahai) {
        editMode = editatu;
        mahaiEditatzen = mahai;

        if (btnSave != null) {
            btnSave.setVisible(!editatu);
            btnSave.setManaged(!editatu);
        }
        if (btnEguneratu != null) {
            btnEguneratu.setVisible(editatu);
            btnEguneratu.setManaged(editatu);
        }
        if (txtId != null) {
            txtId.setVisible(editatu);
        }
        if (lblEditMode != null) {
            if (editatu && mahai != null) {
                lblEditMode.setText("EDITATZEN: Mahaia " + mahai.getZenbakia());
                lblEditMode.setStyle("-fx-font-weight: bold; -fx-text-fill: #38a169;");
            } else {
                lblEditMode.setText("MAHAI BERRIA");
                lblEditMode.setStyle("-fx-font-weight: bold; -fx-text-fill: #3182ce;");
            }
        }
    }

    private void erakutsiMezua(String titulua, String mezua, String mota) {
        Alert.AlertType alertType = Alert.AlertType.INFORMATION;

        switch (mota) {
            case "ERROR":
                alertType = Alert.AlertType.ERROR;
                break;
            case "WARNING":
                alertType = Alert.AlertType.WARNING;
                break;
            case "SUCCESS":
                alertType = Alert.AlertType.INFORMATION;
                break;
            case "INFO":
                alertType = Alert.AlertType.INFORMATION;
                break;
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

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleIntegerProperty zenbakiaProperty() { return zenbakia; }
        public SimpleIntegerProperty pertsonaMaxProperty() { return pertsonaMax; }
        public SimpleStringProperty egoeraProperty() { return egoera; }
        public SimpleBooleanProperty okupatutaProperty() { return okupatuta; }
    }
}