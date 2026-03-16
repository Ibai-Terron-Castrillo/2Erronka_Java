package Pantailak;

import Klaseak.Platera;
import Klaseak.Osagaia;
import Klaseak.Kategoria;
import services.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class PlaterakController {

    // Zerbitzuak
    private final PlateraService plateraService = new PlateraService();
    private final OsagaiaService osagaiaService = new OsagaiaService();
    private final KategoriaService kategoriaService = new KategoriaService();

    // Datu zerrendak
    private final ObservableList<Platera> platerakList = FXCollections.observableArrayList();
    private final FilteredList<Platera> filteredPlaterak = new FilteredList<>(platerakList, p -> true);
    private final ObservableList<Osagaia> osagaiakList = FXCollections.observableArrayList();
    private final ObservableList<Kategoria> kategoriakList = FXCollections.observableArrayList();
    private final ObservableList<OsagaiakTableModel> platerOsagaiakList = FXCollections.observableArrayList();

    // FXML osagaiak
    @FXML private Button atzeraBotoia;
    @FXML private TextField txtBilaketa;

    // Taula nagusia
    @FXML private TableView<Platera> tblPlaterak;

    // Formularioa
    @FXML private TextField txtId;
    @FXML private TextField txtIzena;
    @FXML private TextField txtPrezioa;
    @FXML private TextField txtStock;
    @FXML private ComboBox<Kategoria> cmbKategoriak;
    @FXML private Button btnKategoriaBerria;
    @FXML private Button btnGorde;
    @FXML private Button btnEguneratu;
    @FXML private Label lblEditMode;

    // Osagaiak
    @FXML private TableView<OsagaiakTableModel> tblOsagaiak;
    @FXML private Label lblGuztiraKostua;
    @FXML private ComboBox<Osagaia> cmbOsagaiak;
    @FXML private TextField txtOsagaiKopurua;

    // Estatistikak
    @FXML private Label totalPlaterakLabel;
    @FXML private Label stockGutxiLabel;
    @FXML private Label batezBestekoLabel;

    // Egoera
    private boolean editMode = false;
    private Platera platerEditatzen = null;
    private int stockOriginal;

    @FXML
    public void initialize() {
        System.out.println("INFO: PlaterakController hasieratzen");

        try {
            // 1. Konfiguratu taula nagusia
            konfiguratuTaulaNagusia();

            // 2. Konfiguratu osagaiak taula
            konfiguratuOsagaiakTaula();

            // 3. Listenerrak konfiguratu
            configuratuListeners();

            // 4. Bilaketa konfiguratu
            configuratuBilaketa();

            // 5. Stock sistema konfiguratu
            configuratuStockSistema();

            // 6. Datuak kargatu
            kargatuPlaterak();
            kargatuOsagaiakCombo();
            kargatuKategoriakCombo();

            // 7. Hasierako egoera
            aldatuEditMode(false, null);

            System.out.println("INFO: Controller ondo inicializatuta");
        } catch (Exception e) {
            System.err.println("ERROR: initialize()-n: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void konfiguratuTaulaNagusia() {
        try {
            TableColumn<Platera, Integer> idZutabea = (TableColumn<Platera, Integer>) tblPlaterak.getColumns().get(0);
            TableColumn<Platera, String> izenaZutabea = (TableColumn<Platera, String>) tblPlaterak.getColumns().get(1);
            TableColumn<Platera, Double> prezioaZutabea = (TableColumn<Platera, Double>) tblPlaterak.getColumns().get(2);
            TableColumn<Platera, Integer> stockZutabea = (TableColumn<Platera, Integer>) tblPlaterak.getColumns().get(3);

            idZutabea.setCellValueFactory(new PropertyValueFactory<>("id"));
            izenaZutabea.setCellValueFactory(new PropertyValueFactory<>("izena"));
            prezioaZutabea.setCellValueFactory(new PropertyValueFactory<>("prezioa"));
            stockZutabea.setCellValueFactory(new PropertyValueFactory<>("stock"));

            SortedList<Platera> sortedPlaterak = new SortedList<>(filteredPlaterak);
            sortedPlaterak.comparatorProperty().bind(tblPlaterak.comparatorProperty());
            tblPlaterak.setItems(sortedPlaterak);

            System.out.println("INFO: Taula nagusia konfiguratuta");
        } catch (Exception e) {
            System.err.println("ERROR: Taula konfiguratzen: " + e.getMessage());
        }
    }

    private void konfiguratuOsagaiakTaula() {
        try {
            if (tblOsagaiak.getColumns().size() >= 5) {
                TableColumn<OsagaiakTableModel, Integer> idZutabea = (TableColumn<OsagaiakTableModel, Integer>) tblOsagaiak.getColumns().get(0);
                TableColumn<OsagaiakTableModel, String> izenaZutabea = (TableColumn<OsagaiakTableModel, String>) tblOsagaiak.getColumns().get(1);
                TableColumn<OsagaiakTableModel, Integer> kopuruaZutabea = (TableColumn<OsagaiakTableModel, Integer>) tblOsagaiak.getColumns().get(2);
                TableColumn<OsagaiakTableModel, Double> prezioaZutabea = (TableColumn<OsagaiakTableModel, Double>) tblOsagaiak.getColumns().get(3);
                TableColumn<OsagaiakTableModel, Double> guztiraZutabea = (TableColumn<OsagaiakTableModel, Double>) tblOsagaiak.getColumns().get(4);

                idZutabea.setCellValueFactory(new PropertyValueFactory<>("id"));
                izenaZutabea.setCellValueFactory(new PropertyValueFactory<>("izena"));
                kopuruaZutabea.setCellValueFactory(new PropertyValueFactory<>("kopurua"));
                prezioaZutabea.setCellValueFactory(new PropertyValueFactory<>("prezioa"));
                guztiraZutabea.setCellValueFactory(new PropertyValueFactory<>("guztira"));

                tblOsagaiak.setItems(platerOsagaiakList);
                System.out.println("INFO: Osagaiak taula konfiguratuta");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Osagaiak taula konfiguratzen: " + e.getMessage());
        }
    }

    private void configuratuListeners() {
        platerakList.addListener((javafx.collections.ListChangeListener.Change<? extends Platera> change) -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                    eguneratuEstatistikak();
                }
            }
        });

        txtBilaketa.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPlaterak.setPredicate(plater -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (plater.getIzena().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return String.valueOf(plater.getId()).contains(lowerCaseFilter);
            });
            eguneratuEstatistikak();
        });
    }

    private void configuratuBilaketa() {
        // Jada configuratuListeners()-en dago
    }

    private void configuratuStockSistema() {
        txtStock.setEditable(true);

        txtStock.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                txtStock.setText(oldVal);
            }
        });

        txtStock.setOnAction(event -> {
            eguneratuStockAutomatikoki();
        });

        txtStock.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                eguneratuStockAutomatikoki();
            }
        });
    }

    private void eguneratuStockAutomatikoki() {
        if (!editMode || platerEditatzen == null) {
            return;
        }

        String stockText = txtStock.getText().trim();
        if (stockText.isEmpty()) {
            txtStock.setText(String.valueOf(stockOriginal));
            return;
        }

        try {
            int stockBerria = Integer.parseInt(stockText);

            if (stockBerria < 0) {
                erakutsiMezua("Errorea", "Stock ezin da negatiboa izan", "ERROR");
                txtStock.setText(String.valueOf(stockOriginal));
                return;
            }

            if (stockBerria == platerEditatzen.getStock()) {
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Stock Eguneratu");
            alert.setHeaderText("Stock aldaketa baieztatu");
            alert.setContentText("Stock: " + platerEditatzen.getStock() + " -> " + stockBerria +
                    "\nZiur zaude aldaketa hau egin nahi duzula?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int stockAldaketa = stockBerria - platerEditatzen.getStock();

                plateraService.updateStock(platerEditatzen.getId(), stockAldaketa)
                        .thenAccept(arrakasta -> {
                            Platform.runLater(() -> {
                                if (arrakasta) {
                                    ActionLogger.log(
                                            SessionContext.getCurrentUser(),
                                            "UPDATE",
                                            "platerak",
                                            "Stock aldaketa: " + platerEditatzen.getIzena() +
                                                    " | " + (stockBerria - stockOriginal) +
                                                    " (berria=" + stockBerria + ")"
                                    );

                                    platerEditatzen.setStock(stockBerria);
                                    stockOriginal = stockBerria;

                                    int index = platerakList.indexOf(platerEditatzen);
                                    if (index >= 0) {
                                        platerakList.set(index, platerEditatzen);
                                    }

                                    erakutsiMezua("Arrakasta",
                                            "Stock eguneratu da (" + stockBerria + " unitate)",
                                            "SUCCESS");
                                    eguneratuEstatistikak();
                                } else {
                                    erakutsiMezua("Errorea", "Ezin izan da stocka eguneratu", "ERROR");
                                    txtStock.setText(String.valueOf(stockOriginal));
                                }
                            });
                        });
            } else {
                txtStock.setText(String.valueOf(stockOriginal));
            }

        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Stock zenbaki osoa izan behar da", "ERROR");
            txtStock.setText(String.valueOf(stockOriginal));
        }
    }

    private void kargatuOsagaiakCombo() {
        CompletableFuture.runAsync(() -> {
            List<Osagaia> osagaiak = osagaiaService.getOsagaiak();
            Platform.runLater(() -> {
                osagaiakList.clear();
                if (osagaiak != null) {
                    osagaiakList.addAll(osagaiak);
                }

                if (cmbOsagaiak != null) {
                    cmbOsagaiak.setItems(osagaiakList);
                    cmbOsagaiak.setCellFactory(param -> new ListCell<Osagaia>() {
                        @Override
                        protected void updateItem(Osagaia item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getIzena() + " (Stock: " + item.getStock() + ")");
                            }
                        }
                    });

                    cmbOsagaiak.setButtonCell(new ListCell<Osagaia>() {
                        @Override
                        protected void updateItem(Osagaia item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getIzena());
                            }
                        }
                    });
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("ERROR: Osagaiak kargatzerakoan: " + ex.getMessage());
            });
            return null;
        });
    }

    private void kargatuKategoriakCombo() {
        System.out.println("INFO: Kategoriak ComboBox kargatzen...");

        kategoriaService.getAllKategoriak()
                .thenAccept(kategoriak -> {
                    Platform.runLater(() -> {
                        try {
                            kategoriakList.clear();

                            if (kategoriak != null && !kategoriak.isEmpty()) {
                                kategoriakList.addAll(kategoriak);
                                System.out.println("INFO: " + kategoriak.size() + " kategoria kargatu dira combo-ra");
                            } else {
                                System.out.println("INFO: Ez daude kategoriak edo lista hutsa");
                            }

                            if (cmbKategoriak != null) {
                                cmbKategoriak.setItems(kategoriakList);

                                // Configurar celda personalizada
                                cmbKategoriak.setCellFactory(param -> new ListCell<Kategoria>() {
                                    @Override
                                    protected void updateItem(Kategoria item, boolean empty) {
                                        super.updateItem(item, empty);
                                        if (empty || item == null) {
                                            setText(null);
                                        } else {
                                            setText(item.getIzena() + " (ID: " + item.getId() + ")");
                                        }
                                    }
                                });

                                // Configurar celda del botón
                                cmbKategoriak.setButtonCell(new ListCell<Kategoria>() {
                                    @Override
                                    protected void updateItem(Kategoria item, boolean empty) {
                                        super.updateItem(item, empty);
                                        if (empty || item == null) {
                                            setText("Aukeratu kategoria");
                                        } else {
                                            setText(item.getIzena());
                                        }
                                    }
                                });

                                System.out.println("INFO: ComboBox kategoriak konfiguratuta");
                            }

                        } catch (Exception e) {
                            System.err.println("ERROR: Kategoriak combo konfiguratzerakoan: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("ERROR: Kategoriak kargatzerakoan: " + ex.getMessage());
                        erakutsiMezua("Abisua", "Ezin izan dira kategoriak kargatu", "WARNING");
                    });
                    return null;
                });
    }

    @FXML
    public void kargatuPlaterak() {
        plateraService.getAllPlatera()
                .thenAccept(platerak -> {
                    Platform.runLater(() -> {
                        platerakList.clear();
                        if (platerak != null && !platerak.isEmpty()) {
                            platerakList.addAll(platerak);
                        }
                        eguneratuEstatistikak();
                        System.out.println("INFO: " + platerakList.size() + " plater kargatu dira");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        erakutsiMezua("Errorea", "Ezin izan dira platerak kargatu: " + ex.getMessage(), "ERROR");
                    });
                    return null;
                });
    }

    @FXML
    private void kargatuStockGutxi() {
        plateraService.getPlaterakStockGutxi()
                .thenAccept(platerak -> {
                    Platform.runLater(() -> {
                        platerakList.clear();
                        if (platerak != null && !platerak.isEmpty()) {
                            platerakList.addAll(platerak);
                            erakutsiMezua("Stock Gutxi", platerak.size() + " plater stock gutxirekin", "INFO");
                        } else {
                            erakutsiMezua("Informazioa", "Ez daude platerrak stock gutxirekin", "INFO");
                        }
                        eguneratuEstatistikak();
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        erakutsiMezua("Errorea", "Ezin izan dira stock gutxikoak kargatu: " + ex.getMessage(), "ERROR");
                    });
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
            if (txtPrezioa != null) txtPrezioa.setText(String.valueOf(hautatuta.getPrezioa()));
            if (txtStock != null) txtStock.setText(String.valueOf(hautatuta.getStock()));

            // Kategoria aukeratu
            if (cmbKategoriak != null) {
                for (Kategoria kategoria : kategoriakList) {
                    if (kategoria.getId() == hautatuta.getKategoriaId()) {
                        cmbKategoriak.getSelectionModel().select(kategoria);
                        break;
                    }
                }
            }

            stockOriginal = hautatuta.getStock();
            aldatuEditMode(true, hautatuta);

            kargatuPlaterOsagaiak(hautatuta.getId());
        }
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
        if (txtPrezioa != null) txtPrezioa.clear();
        if (txtStock != null) txtStock.clear();
        if (cmbKategoriak != null) cmbKategoriak.getSelectionModel().clearSelection();
        if (txtOsagaiKopurua != null) txtOsagaiKopurua.clear();
        if (cmbOsagaiak != null) cmbOsagaiak.getSelectionModel().clearSelection();

        platerOsagaiakList.clear();
        if (tblOsagaiak != null) {
            tblOsagaiak.setItems(platerOsagaiakList);
        }
        eguneratuKostuTotala();

        stockOriginal = 0;
    }
    /*
    @FXML
    private void kategoriaBerriaSortu() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Kategoria Berria");
        dialog.setHeaderText("Sartu kategoriaren izena:");
        dialog.setContentText("Izena:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(izena -> {
            if (!izena.trim().isEmpty()) {
                // Kategoria berria zerbitzura bidali
                kategoriaService.createKategoria(izena.trim())
                        .thenAccept(kategoriaSortua -> {
                            Platform.runLater(() -> {
                                if (kategoriaSortua != null) {
                                    ActionLogger.log(
                                            SessionContext.getCurrentUser(),
                                            "INSERT",
                                            "kategoriak",
                                            "Kategoria sortu: " + kategoriaSortua.getIzena()
                                    );

                                    kategoriakList.add(kategoriaSortua);
                                    cmbKategoriak.getSelectionModel().select(kategoriaSortua);
                                    erakutsiMezua("Arrakasta", "Kategoria ondo sortu da", "SUCCESS");
                                } else {
                                    erakutsiMezua("Errorea", "Ezin izan da kategoria sortu", "ERROR");
                                }
                            });
                        });
            }
        });
    }*/

    @FXML
    private void gordePlaterra() {
        if (!balidatuFormularioa()) {
            return;
        }

        try {
            Kategoria hautatutakoKategoria = cmbKategoriak.getSelectionModel().getSelectedItem();
            if (hautatutakoKategoria == null) {
                erakutsiMezua("Errorea", "Aukeratu kategoria bat", "ERROR");
                return;
            }

            Platera platerBerria = new Platera();
            platerBerria.setIzena(txtIzena.getText());
            platerBerria.setPrezioa(Double.parseDouble(txtPrezioa.getText()));
            platerBerria.setStock(Integer.parseInt(txtStock.getText()));
            platerBerria.setKategoriaId(hautatutakoKategoria.getId());

            // Osagaiak prestatu
            List<OsagaiakTableModel> osagaiakKopiatu = new ArrayList<>();
            for (OsagaiakTableModel osagaia : platerOsagaiakList) {
                osagaiakKopiatu.add(new OsagaiakTableModel(
                        osagaia.getId(),
                        osagaia.getIzena(),
                        osagaia.getKopurua(),
                        osagaia.getPrezioa()
                ));
            }

            System.out.println("INFO: Plater berria sortzen");
            System.out.println("INFO: Osagai kopurua: " + osagaiakKopiatu.size());
            System.out.println("INFO: Kategoria: " + hautatutakoKategoria.getIzena() + " (ID: " + hautatutakoKategoria.getId() + ")");

            plateraService.createPlatera(platerBerria, osagaiakKopiatu)
                    .thenAccept(gordetakoPlater -> {
                        Platform.runLater(() -> {
                            if (gordetakoPlater != null) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUser(),
                                        "INSERT",
                                        "platerak",
                                        "Platerra sortu: " + gordetakoPlater.getIzena() +
                                                " | Kategoria=" + hautatutakoKategoria.getIzena() +
                                                " | Osagaiak=" + osagaiakKopiatu.size()
                                );

                                platerakList.add(gordetakoPlater);
                                garbituFormularioa();
                                erakutsiMezua("Arrakasta",
                                        "Platerra ondo gorde da! Osagaiak: " + osagaiakKopiatu.size(),
                                        "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea", "Ezin izan da platerra gorde", "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            erakutsiMezua("Errorea", "Errorea platerra gordetzerakoan: " + ex.getMessage(), "ERROR");
                        });
                        return null;
                    });

        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Zenbakiak sartu behar dira prezioa eta stockerako", "ERROR");
        } catch (Exception e) {
            erakutsiMezua("Errorea", "Errorea platerra gordetzerakoan: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void eguneratuPlaterra() {
        if (!balidatuFormularioa() || platerEditatzen == null) {
            return;
        }

        try {
            Kategoria hautatutakoKategoria = cmbKategoriak.getSelectionModel().getSelectedItem();
            if (hautatutakoKategoria == null) {
                erakutsiMezua("Errorea", "Aukeratu kategoria bat", "ERROR");
                return;
            }

            // Datuak eguneratu
            platerEditatzen.setIzena(txtIzena.getText());
            platerEditatzen.setPrezioa(Double.parseDouble(txtPrezioa.getText()));
            platerEditatzen.setKategoriaId(hautatutakoKategoria.getId());

            // Osagaiak prestatu
            List<OsagaiakTableModel> osagaiakKopiatu = new ArrayList<>();
            for (OsagaiakTableModel osagaia : platerOsagaiakList) {
                osagaiakKopiatu.add(new OsagaiakTableModel(
                        osagaia.getId(),
                        osagaia.getIzena(),
                        osagaia.getKopurua(),
                        osagaia.getPrezioa()
                ));
            }

            System.out.println("INFO: Eguneratzen platera ID: " + platerEditatzen.getId());
            System.out.println("INFO: Osagai kopurua: " + osagaiakKopiatu.size());
            System.out.println("INFO: Kategoria: " + hautatutakoKategoria.getIzena() + " (ID: " + hautatutakoKategoria.getId() + ")");

            // Baieztapena eskatu
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Platerra Eguneratu");
            confirmAlert.setHeaderText("Platerra eta osagaiak eguneratu nahi dituzu?");
            confirmAlert.setContentText("Platerra: " + platerEditatzen.getIzena() +
                    "\nKategoria: " + hautatutakoKategoria.getIzena() +
                    "\nOsagaiak: " + osagaiakKopiatu.size() + " osagai");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            // Platera eguneratu (datuak + osagaiak batera)
            plateraService.updatePlatera(platerEditatzen.getId(), platerEditatzen, osagaiakKopiatu)
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {
                            if (arrakasta) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUser(),
                                        "UPDATE",
                                        "platerak",
                                        "Platerra eguneratu: ID=" + platerEditatzen.getId() +
                                                " | Izena=" + platerEditatzen.getIzena() +
                                                " | Osagaiak=" + osagaiakKopiatu.size()
                                );

                                // Lista lokalak eguneratu
                                int index = platerakList.indexOf(platerEditatzen);
                                if (index >= 0) {
                                    platerakList.set(index, platerEditatzen);
                                }

                                erakutsiMezua("Arrakasta",
                                        "Platerra eta osagaiak eguneratu dira!\n" +
                                                "Kategoria: " + hautatutakoKategoria.getIzena() +
                                                "\nOsagaiak: " + osagaiakKopiatu.size(),
                                        "SUCCESS");

                                eguneratuEstatistikak();

                                // Osagaiak berriro kargatu (berresteko)
                                kargatuPlaterOsagaiak(platerEditatzen.getId());

                            } else {
                                erakutsiMezua("Errorea",
                                        "Ezin izan da platerra eguneratu",
                                        "ERROR");
                            }
                        });
                    }).exceptionally(ex -> {
                        Platform.runLater(() -> {
                            erakutsiMezua("Errorea",
                                    "Errorea: " + ex.getMessage(),
                                    "ERROR");
                        });
                        return null;
                    });

        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Zenbakiak sartu behar dira", "ERROR");
        }
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
        alert.setContentText("Platerra: " + hautatuta.getIzena() + "\nID: " + hautatuta.getId());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            plateraService.deletePlatera(hautatuta.getId())
                    .thenAccept(arrakasta -> {
                        Platform.runLater(() -> {
                            if (arrakasta) {
                                ActionLogger.log(
                                        SessionContext.getCurrentUser(),
                                        "DELETE",
                                        "platerak",
                                        "Platerra ezabatuta: " + hautatuta.getIzena() +
                                                " (ID=" + hautatuta.getId() + ")"
                                );

                                platerakList.remove(hautatuta);
                                if (platerEditatzen != null && platerEditatzen.getId() == hautatuta.getId()) {
                                    garbituFormularioa();
                                }
                                erakutsiMezua("Arrakasta", "Platerra ondo ezabatu da!", "SUCCESS");
                                eguneratuEstatistikak();
                            } else {
                                erakutsiMezua("Errorea",
                                        "Ezin izan da platerra ezabatu. Komanda edo beste erlazio batzuk ditu.",
                                        "ERROR");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            erakutsiMezua("Errorea", "Errorea platerra ezabatzerakoan: " + ex.getMessage(), "ERROR");
                        });
                        return null;
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
            int kopurua = Integer.parseInt(txtOsagaiKopurua.getText());
            if (kopurua <= 0) {
                erakutsiMezua("Errorea", "Kopurua 0 baino handiagoa izan behar da", "ERROR");
                return;
            }

            // Egiaztatu osagaia dagoeneko badagoen
            for (OsagaiakTableModel osagaia : platerOsagaiakList) {
                if (osagaia.getId() == hautatutakoOsagaia.getId()) {
                    erakutsiMezua("Abisua", "Osagaia dagoeneko gehitua dago", "WARNING");
                    return;
                }
            }

            OsagaiakTableModel osagaiaBerria = new OsagaiakTableModel(
                    hautatutakoOsagaia.getId(),
                    hautatutakoOsagaia.getIzena(),
                    kopurua,
                    hautatutakoOsagaia.getAzkenPrezioa()
            );

            platerOsagaiakList.add(osagaiaBerria);
            ActionLogger.log(
                    SessionContext.getCurrentUser(),
                    "INSERT",
                    "platera_osagaiak",
                    "Osagaia gehitua: " + hautatutakoOsagaia.getIzena() +
                            " | Platerra=" + (platerEditatzen != null ? platerEditatzen.getIzena() : "PLATER BERRIA") +
                            " | Kopurua=" + kopurua
            );

            tblOsagaiak.refresh();
            eguneratuKostuTotala();

            txtOsagaiKopurua.clear();

        } catch (NumberFormatException e) {
            erakutsiMezua("Errorea", "Sartu kopuru baliodun bat", "ERROR");
        }
    }

    @FXML
    private void ezabatuOsagaia() {
        OsagaiakTableModel hautatutakoOsagaia = tblOsagaiak.getSelectionModel().getSelectedItem();
        if (hautatutakoOsagaia != null) {
            ActionLogger.log(
                    SessionContext.getCurrentUser(),
                    "DELETE",
                    "platera_osagaiak",
                    "Osagaia kenduta: " + hautatutakoOsagaia.getIzena() +
                            " | Platerra=" + (platerEditatzen != null ? platerEditatzen.getIzena() : "PLATER BERRIA")
            );

            platerOsagaiakList.remove(hautatutakoOsagaia);
            tblOsagaiak.refresh();
            eguneratuKostuTotala();
        } else {
            erakutsiMezua("Abisua", "Mesedez, hautatu osagai bat ezabatzeko", "WARNING");
        }
    }

    private void kargatuPlaterOsagaiak(int platerId) {
        plateraService.getPlateraOsagaiak(platerId)
                .thenAccept(jsonResponse -> {
                    Platform.runLater(() -> {
                        try {
                            platerOsagaiakList.clear();

                            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

                            for (JsonElement element : jsonArray) {
                                JsonObject obj = element.getAsJsonObject();

                                int osagaiId = obj.has("osagaiakId") ? obj.get("osagaiakId").getAsInt() :
                                        obj.has("id") ? obj.get("id").getAsInt() : 0;

                                String izena = obj.has("osagaiaIzena") ? obj.get("osagaiaIzena").getAsString() :
                                        obj.has("izena") ? obj.get("izena").getAsString() : "";

                                int kopurua = obj.has("kopurua") ? obj.get("kopurua").getAsInt() : 0;

                                double prezioa = obj.has("osagaiaPrezioa") ? obj.get("osagaiaPrezioa").getAsDouble() :
                                        obj.has("prezioa") ? obj.get("prezioa").getAsDouble() : 0.0;

                                OsagaiakTableModel model = new OsagaiakTableModel(
                                        osagaiId,
                                        izena,
                                        kopurua,
                                        prezioa
                                );
                                platerOsagaiakList.add(model);
                            }

                            tblOsagaiak.refresh();
                            eguneratuKostuTotala();

                        } catch (Exception e) {
                            System.err.println("ERROR: Osagaiak kargatzerakoan: " + e.getMessage());
                            tblOsagaiak.setItems(platerOsagaiakList);
                        }
                    });
                });
    }

    private void eguneratuKostuTotala() {
        if (lblGuztiraKostua != null) {
            double guztira = 0.0;
            for (OsagaiakTableModel osagaia : platerOsagaiakList) {
                guztira += osagaia.getGuztira();
            }
            lblGuztiraKostua.setText(String.format("Kostu totala: %.2f€", guztira));
        }
    }

    private void eguneratuEstatistikak() {
        if (totalPlaterakLabel != null && stockGutxiLabel != null && batezBestekoLabel != null) {
            int total = filteredPlaterak.size();
            int stockGutxi = (int) filteredPlaterak.stream()
                    .filter(p -> p.getStock() < 10)
                    .count();

            double batezBestekoa = filteredPlaterak.stream()
                    .mapToDouble(Platera::getPrezioa)
                    .average()
                    .orElse(0.0);

            totalPlaterakLabel.setText(String.valueOf(total));
            stockGutxiLabel.setText(String.valueOf(stockGutxi));
            batezBestekoLabel.setText(String.format("%.2f€", batezBestekoa));

            System.out.println("INFO: Estatistikak: Total=" + total + ", StockGutxi=" + stockGutxi + ", BatezBestekoa=" + batezBestekoa);
        }
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
        if (txtIzena == null || txtIzena.getText().isEmpty()) {
            erakutsiMezua("Errorea", "Izena bete behar da", "ERROR");
            return false;
        }
        if (txtPrezioa == null || txtPrezioa.getText().isEmpty() || !txtPrezioa.getText().matches("\\d+(\\.\\d{1,2})?")) {
            erakutsiMezua("Errorea", "Prezio balioduna sartu behar da (0.00 formatuan)", "ERROR");
            return false;
        }
        if (txtStock == null || txtStock.getText().isEmpty() || !txtStock.getText().matches("\\d+")) {
            erakutsiMezua("Errorea", "Stock zenbaki osoa izan behar da", "ERROR");
            return false;
        }
        if (cmbKategoriak == null || cmbKategoriak.getSelectionModel().getSelectedItem() == null) {
            erakutsiMezua("Errorea", "Aukeratu kategoria bat", "ERROR");
            return false;
        }
        return true;
    }

    private void aldatuEditMode(boolean editatu, Platera plater) {
        editMode = editatu;
        platerEditatzen = plater;

        if (btnGorde != null) {
            btnGorde.setVisible(!editatu);
            btnGorde.setManaged(!editatu);
        }
        if (btnEguneratu != null) {
            btnEguneratu.setVisible(editatu);
            btnEguneratu.setManaged(editatu);
        }
        if (txtId != null) {
            txtId.setVisible(editatu);
        }
        if (lblEditMode != null) {
            if (editatu && plater != null) {
                lblEditMode.setText("EDITATZEN: " + plater.getIzena());
                lblEditMode.setStyle("-fx-font-weight: bold; -fx-text-fill: #38a169;");
            } else {
                lblEditMode.setText("PLATER BERRIA");
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

    // Barne klasea
    public static class OsagaiakTableModel {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty izena;
        private final SimpleIntegerProperty kopurua;
        private final SimpleDoubleProperty prezioa;
        private final SimpleDoubleProperty guztira;

        public OsagaiakTableModel(int id, String izena, int kopurua, double prezioa) {
            this.id = new SimpleIntegerProperty(id);
            this.izena = new SimpleStringProperty(izena);
            this.kopurua = new SimpleIntegerProperty(kopurua);
            this.prezioa = new SimpleDoubleProperty(prezioa);
            this.guztira = new SimpleDoubleProperty(kopurua * prezioa);
        }

        public int getId() { return id.get(); }
        public String getIzena() { return izena.get(); }
        public int getKopurua() { return kopurua.get(); }
        public double getPrezioa() { return prezioa.get(); }
        public double getGuztira() { return guztira.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty izenaProperty() { return izena; }
        public SimpleIntegerProperty kopuruaProperty() { return kopurua; }
        public SimpleDoubleProperty prezioaProperty() { return prezioa; }
        public SimpleDoubleProperty guztiraProperty() { return guztira; }
    }
}