package Pantailak;

import Klaseak.Platera;
import Klaseak.PlaterenOsagaia;
import Klaseak.Osagaia;
import Klaseak.Kategoria;
import services.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.beans.property.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlaterakController {

    private final PlateraService plateraService = new PlateraService();
    private final OsagaiaService osagaiaService = new OsagaiaService();
    private final KategoriaService kategoriaService = new KategoriaService();

    // Datu zerrendak
    private final ObservableList<Platera> platerakList = FXCollections.observableArrayList();
    private final FilteredList<Platera> filteredPlaterak = new FilteredList<>(platerakList, p -> true);
    private final ObservableList<Osagaia> osagaiakList = FXCollections.observableArrayList();
    private final ObservableList<Kategoria> kategoriakList = FXCollections.observableArrayList();
    private final ObservableList<PlaterenOsagaia> platerOsagaiakList = FXCollections.observableArrayList();

    // FXML osagaiak
    @FXML private Button atzeraBotoia;
    @FXML private TextField txtBilaketa;

    @FXML private TableView<Platera> tblPlaterak;
    @FXML private TableColumn<Platera, Integer> colId;
    @FXML private TableColumn<Platera, String> colIzena;
    @FXML private TableColumn<Platera, Double> colPrezioa;
    // Stock zutabea kendu

    // Formularioa
    @FXML private TextField txtId;
    @FXML private TextField txtIzena;
    @FXML private TextArea txtDeskribapena;
    @FXML private TextField txtPrezioa;
    @FXML private ComboBox<Kategoria> cmbKategoriak;
    @FXML private CheckBox chkErabilgarri;
    @FXML private TextField txtIrudia;
    @FXML private Button btnGorde;
    @FXML private Button btnEguneratu;
    @FXML private Label lblEditMode;

    // Osagaiak
    @FXML private TableView<PlaterenOsagaia> tblOsagaiak;
    @FXML private TableColumn<PlaterenOsagaia, String> colOsagaiaIzena;
    @FXML private TableColumn<PlaterenOsagaia, Double> colOsagaiaKopurua;
    @FXML private ComboBox<Osagaia> cmbOsagaiak;
    @FXML private TextField txtOsagaiKopurua;
    @FXML private Label lblGuztiraKostua;  // plateraren kostua (osagaien batura)

    @FXML private Label totalPlaterakLabel;
    @FXML private Label erabilgarriKopuruaLabel;
    @FXML private Label batezBestekoLabel;

    // Egoera
    private boolean editMode = false;
    private Platera platerEditatzen = null;

    @FXML
    public void initialize() {
        System.out.println("INFO: PlaterakController hasieratzen (API egokituta)");

        konfiguratuTaulaNagusia();
        konfiguratuOsagaiakTaula();
        configuratuListeners();
        configuratuBilaketa();

        kargatuPlaterak();
        kargatuOsagaiakCombo();
        kargatuKategoriakCombo();

        aldatuEditMode(false, null);
    }

    private void konfiguratuTaulaNagusia() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colPrezioa.setCellValueFactory(new PropertyValueFactory<>("prezioa"));

        SortedList<Platera> sortedPlaterak = new SortedList<>(filteredPlaterak);
        sortedPlaterak.comparatorProperty().bind(tblPlaterak.comparatorProperty());
        tblPlaterak.setItems(sortedPlaterak);
    }

    private void konfiguratuOsagaiakTaula() {
        // Izena zutabea: osagaiaren izena lortu osagaiakList-etik
        colOsagaiaIzena.setCellValueFactory(cellData -> {
            int osagaiaId = cellData.getValue().getInbentarioaId();
            // Bilatu osagaia izena
            for (Osagaia o : osagaiakList) {
                if (o.getId() == osagaiaId) {
                    return new SimpleStringProperty(o.getIzena());
                }
            }
            return new SimpleStringProperty("Ez da aurkitu");
        });
        colOsagaiaKopurua.setCellValueFactory(new PropertyValueFactory<>("kantitatea"));
        tblOsagaiak.setItems(platerOsagaiakList);
    }

    private void configuratuListeners() {
        platerakList.addListener((javafx.collections.ListChangeListener.Change<? extends Platera> change) -> {
            eguneratuEstatistikak();
        });

        txtBilaketa.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPlaterak.setPredicate(plater -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return plater.getIzena().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(plater.getId()).contains(lowerCaseFilter);
            });
            eguneratuEstatistikak();
        });
    }

    private void configuratuBilaketa() {
        // jada
    }

    private void kargatuOsagaiakCombo() {
        CompletableFuture.runAsync(() -> {
            List<Osagaia> osagaiak = osagaiaService.getOsagaiak();
            Platform.runLater(() -> {
                osagaiakList.clear();
                if (osagaiak != null) osagaiakList.addAll(osagaiak);
                if (cmbOsagaiak != null) {
                    cmbOsagaiak.setItems(osagaiakList);
                    cmbOsagaiak.setCellFactory(param -> new ListCell<Osagaia>() {
                        @Override
                        protected void updateItem(Osagaia item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) setText(null);
                            else setText(item.getIzena() + " (Stock: " + item.getKantitatea() + ")");
                        }
                    });
                    cmbOsagaiak.setButtonCell(new ListCell<Osagaia>() {
                        @Override
                        protected void updateItem(Osagaia item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) setText("Aukeratu osagaia");
                            else setText(item.getIzena());
                        }
                    });
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> System.err.println("ERROR: Osagaiak kargatzean: " + ex.getMessage()));
            return null;
        });
    }

    private void kargatuKategoriakCombo() {
        kategoriaService.getAllKategoriak()
                .thenAccept(kategoriak -> {
                    Platform.runLater(() -> {
                        kategoriakList.clear();
                        if (kategoriak != null) kategoriakList.addAll(kategoriak);
                        if (cmbKategoriak != null) {
                            cmbKategoriak.setItems(kategoriakList);
                            cmbKategoriak.setCellFactory(param -> new ListCell<Kategoria>() {
                                @Override
                                protected void updateItem(Kategoria item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty || item == null) setText(null);
                                    else setText(item.getIzena());
                                }
                            });
                            cmbKategoriak.setButtonCell(new ListCell<Kategoria>() {
                                @Override
                                protected void updateItem(Kategoria item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty || item == null) setText("Aukeratu kategoria");
                                    else setText(item.getIzena());
                                }
                            });
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> erakutsiMezua("Abisua", "Ezin izan dira kategoriak kargatu", "WARNING"));
                    return null;
                });
    }

    @FXML
    private void kargatuPlaterak() {
        PlateraService.getAllPlaterak()
                .thenAccept(platerak -> {
                    Platform.runLater(() -> {
                        platerakList.clear();
                        if (platerak != null) platerakList.addAll(platerak);
                        eguneratuEstatistikak();
                        System.out.println("INFO: " + platerakList.size() + " plater kargatu dira");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> erakutsiMezua("Errorea", "Ezin izan dira platerak kargatu: " + ex.getMessage(), "ERROR"));
                    return null;
                });
    }

    @FXML
    private void onPlaterSelected() {
        Platera hautatuta = tblPlaterak.getSelectionModel().getSelectedItem();
        if (hautatuta != null) {
            System.out.println("INFO: Plater hautatuta: " + hautatuta.getIzena() + " (ID: " + hautatuta.getId() + ")");
            if (txtId != null) txtId.setText(String.valueOf(hautatuta.getId()));
            if (txtIzena != null) txtIzena.setText(hautatuta.getIzena());
            if (txtDeskribapena != null) txtDeskribapena.setText(hautatuta.getDeskribapena());
            if (txtPrezioa != null) txtPrezioa.setText(String.valueOf(hautatuta.getPrezioa()));
            if (chkErabilgarri != null) chkErabilgarri.setSelected("Bai".equals(hautatuta.getErabilgarri()));
            if (txtIrudia != null) txtIrudia.setText(hautatuta.getIrudia());

            if (cmbKategoriak != null) {
                for (Kategoria k : kategoriakList) {
                    if (k.getId() == hautatuta.getKategoriaId()) {
                        cmbKategoriak.getSelectionModel().select(k);
                        break;
                    }
                }
            }

            aldatuEditMode(true, hautatuta);
            kargatuPlaterOsagaiak(hautatuta.getId());
        }
    }

    private void kargatuPlaterOsagaiak(int platerId) {
        PlateraService.getPlaterenOsagaiak(platerId)
                .thenAccept(osagaiak -> {
                    Platform.runLater(() -> {
                        platerOsagaiakList.clear();
                        platerOsagaiakList.addAll(osagaiak);
                    });
                });
    }

    @FXML
    private void platerBerriaSortu() {
        garbituFormularioa();
        aldatuEditMode(false, null);
    }

    @FXML
    private void garbituFormularioa() {
        if (txtId != null) txtId.clear();
        if (txtIzena != null) txtIzena.clear();
        if (txtDeskribapena != null) txtDeskribapena.clear();
        if (txtPrezioa != null) txtPrezioa.clear();
        if (chkErabilgarri != null) chkErabilgarri.setSelected(true);
        if (txtIrudia != null) txtIrudia.clear();
        if (cmbKategoriak != null) cmbKategoriak.getSelectionModel().clearSelection();
        if (txtOsagaiKopurua != null) txtOsagaiKopurua.clear();
        if (cmbOsagaiak != null) cmbOsagaiak.getSelectionModel().clearSelection();

        platerOsagaiakList.clear();
        if (tblOsagaiak != null) tblOsagaiak.setItems(platerOsagaiakList);
    }

    @FXML
    private void gordePlaterra() {
        if (!balidatuFormularioa()) return;

        Kategoria hautatutakoKategoria = cmbKategoriak.getSelectionModel().getSelectedItem();
        if (hautatutakoKategoria == null) {
            erakutsiMezua("Errorea", "Aukeratu kategoria bat", "ERROR");
            return;
        }

        Platera platerBerria = new Platera();
        platerBerria.setIzena(txtIzena.getText());
        platerBerria.setDeskribapena(txtDeskribapena.getText());
        platerBerria.setPrezioa(Double.parseDouble(txtPrezioa.getText()));
        platerBerria.setKategoriaId(hautatutakoKategoria.getId());
        platerBerria.setErabilgarri(chkErabilgarri.isSelected() ? "Bai" : "Ez");
        platerBerria.setIrudia(txtIrudia.getText());

        List<PlaterenOsagaia> osagaiak = new ArrayList<>(platerOsagaiakList);

        PlateraService.createPlatera(platerBerria, osagaiak)
                .thenAccept(gordetakoPlater -> {
                    Platform.runLater(() -> {
                        if (gordetakoPlater != null) {
                            ActionLogger.log(
                                    SessionContext.getCurrentUsername(),
                                    "INSERT",
                                    "platerak",
                                    "Platerra sortu: " + gordetakoPlater.getIzena()
                            );
                            platerakList.add(gordetakoPlater);
                            garbituFormularioa();
                            erakutsiMezua("Arrakasta", "Platerra ondo gorde da!", "SUCCESS");
                            eguneratuEstatistikak();
                            kargatuPlaterak();
                        } else {
                            erakutsiMezua("Errorea", "Ezin izan da platerra gorde", "ERROR");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> erakutsiMezua("Errorea", "Errorea: " + ex.getMessage(), "ERROR"));
                    return null;
                });
    }

    @FXML
    private void eguneratuPlaterra() {
        if (!balidatuFormularioa() || platerEditatzen == null) return;

        Kategoria hautatutakoKategoria = cmbKategoriak.getSelectionModel().getSelectedItem();
        if (hautatutakoKategoria == null) {
            erakutsiMezua("Errorea", "Aukeratu kategoria bat", "ERROR");
            return;
        }

        platerEditatzen.setIzena(txtIzena.getText());
        platerEditatzen.setDeskribapena(txtDeskribapena.getText());
        platerEditatzen.setPrezioa(Double.parseDouble(txtPrezioa.getText()));
        platerEditatzen.setKategoriaId(hautatutakoKategoria.getId());
        platerEditatzen.setErabilgarri(chkErabilgarri.isSelected() ? "Bai" : "Ez");
        platerEditatzen.setIrudia(txtIrudia.getText());

        List<PlaterenOsagaia> osagaiak = new ArrayList<>(platerOsagaiakList);

        PlateraService.updatePlatera(platerEditatzen.getId(), platerEditatzen, osagaiak)
                .thenAccept(arrakasta -> {
                    Platform.runLater(() -> {
                        if (arrakasta) {
                            ActionLogger.log(
                                    SessionContext.getCurrentUsername(),
                                    "UPDATE",
                                    "platerak",
                                    "Platerra eguneratu: ID=" + platerEditatzen.getId()
                            );
                            int index = platerakList.indexOf(platerEditatzen);
                            if (index >= 0) platerakList.set(index, platerEditatzen);
                            erakutsiMezua("Arrakasta", "Platerra eguneratu da!", "SUCCESS");
                            eguneratuEstatistikak();
                        } else {
                            erakutsiMezua("Errorea", "Ezin izan da platerra eguneratu", "ERROR");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> erakutsiMezua("Errorea", "Errorea: " + ex.getMessage(), "ERROR"));
                    return null;
                });
    }

    @FXML
    private void ezabatuPlaterra() {
        Platera hautatuta = tblPlaterak.getSelectionModel().getSelectedItem();
        if (hautatuta == null) {
            erakutsiMezua("Abisua", "Mesedez, hautatu plater bat ezabatzeko", "WARNING");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Platerra Ezabatu");
        alert.setHeaderText("Ziur zaude plater hau ezabatu nahi duzula?");
        alert.setContentText("Platerra: " + hautatuta.getIzena());
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            PlateraService.deletePlatera(hautatuta.getId())
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {
                            if (arrakasta) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUsername(),
                                        "DELETE",
                                        "platerak",
                                        "Platerra ezabatuta: " + hautatuta.getIzena()
                                );
                                platerakList.remove(hautatuta);
                                if (platerEditatzen != null && platerEditatzen.getId() == hautatuta.getId()) {
                                    garbituFormularioa();
                                }
                                erakutsiMezua("Arrakasta", "Platerra ondo ezabatu da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da platerra ezabatu. Erlazioak ditu.", "ERROR");
                            }
                        });
                    });
        }
    }

    @FXML
    private void gehituOsagaia() {
        Osagaia hautatutakoOsagaia = cmbOsagaiak.getSelectionModel().getSelectedItem();
        if (hautatutakoOsagaia == null) {
            erakutsiMezua("Abisua", "Aukeratu osagai bat", "WARNING");
            return;
        }

        try {
            double kopurua = Double.parseDouble(txtOsagaiKopurua.getText());
            if (kopurua <= 0) {
                erakutsiMezua("Errorea", "Kopurua 0 baino handiagoa izan behar da", "ERROR");
                return;
            }

            // Egiaztatu ez dagoeneko
            for (PlaterenOsagaia os : platerOsagaiakList) {
                if (os.getInbentarioaId() == hautatutakoOsagaia.getId()) {
                    erakutsiMezua("Abisua", "Osagaia dagoeneko gehitua dago", "WARNING");
                    return;
                }
            }

            PlaterenOsagaia osagaiaBerria = new PlaterenOsagaia(
                    platerEditatzen != null ? platerEditatzen.getId() : 0,
                    hautatutakoOsagaia.getId(),
                    kopurua
            );
            platerOsagaiakList.add(osagaiaBerria);
            ActionLogger.log(
                    SessionContext.getCurrentUsername(),
                    "INSERT",
                    "plateren_osagaiak",
                    "Osagaia gehitu: " + hautatutakoOsagaia.getIzena() +
                            " | Platerra=" + (platerEditatzen != null ? platerEditatzen.getIzena() : "PLATER BERRIA")
            );
            txtOsagaiKopurua.clear();
        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Sartu kopuru baliodun bat", "ERROR");
        }
    }

    @FXML
    private void ezabatuOsagaia() {
        PlaterenOsagaia hautatuta = tblOsagaiak.getSelectionModel().getSelectedItem();
        if (hautatuta != null) {
            platerOsagaiakList.remove(hautatuta);
        } else {
            erakutsiMezua("Abisua", "Mesedez, hautatu osagai bat ezabatzeko", "WARNING");
        }
    }

    private void eguneratuEstatistikak() {
        int total = filteredPlaterak.size();
        int erabilgarri = (int) filteredPlaterak.stream()
                .filter(p -> "Bai".equals(p.getErabilgarri()))
                .count();
        double batezBestekoa = filteredPlaterak.stream()
                .mapToDouble(Platera::getPrezioa)
                .average()
                .orElse(0.0);

        totalPlaterakLabel.setText(String.valueOf(total));
        erabilgarriKopuruaLabel.setText(String.valueOf(erabilgarri));
        batezBestekoLabel.setText(String.format("%.2f€", batezBestekoa));
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
        if (txtIzena.getText().isEmpty()) {
            erakutsiMezua("Errorea", "Izena bete behar da", "ERROR");
            return false;
        }
        try {
            Double.parseDouble(txtPrezioa.getText());
        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Prezio balioduna sartu behar da", "ERROR");
            return false;
        }
        if (cmbKategoriak.getSelectionModel().getSelectedItem() == null) {
            erakutsiMezua("Errorea", "Aukeratu kategoria bat", "ERROR");
            return false;
        }
        return true;
    }

    private void aldatuEditMode(boolean editatu, Platera plater) {
        editMode = editatu;
        platerEditatzen = plater;
        btnGorde.setVisible(!editatu);
        btnGorde.setManaged(!editatu);
        btnEguneratu.setVisible(editatu);
        btnEguneratu.setManaged(editatu);
        txtId.setVisible(editatu);
        if (editatu && plater != null) {
            lblEditMode.setText("EDITATZEN: " + plater.getIzena());
            lblEditMode.setStyle("-fx-font-weight: bold; -fx-text-fill: #38a169;");
        } else {
            lblEditMode.setText("PLATER BERRIA");
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
}