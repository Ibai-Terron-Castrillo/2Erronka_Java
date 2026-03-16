package Pantailak;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import services.*;
import Klaseak.Eskaera;
import Klaseak.EskaeraOsagaia;
import Klaseak.Osagaia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EskaerakController {

    @FXML private TableView<Eskaera> eskaerakTable;
    @FXML private TableColumn<Eskaera, Integer> zenbakiaColumn;
    @FXML private TableColumn<Eskaera, String> dataColumn;
    @FXML private TableColumn<Eskaera, Double> totalaEskaeraColumn;
    @FXML private TableColumn<Eskaera, String> egoeraColumn;
    @FXML private TableColumn<Eskaera, String> pdfColumn;
    @FXML private TableColumn<Eskaera, Void> akzioakColumn;

    @FXML private TableView<EskaeraOsagaia> eskaeraOsagaiakTable;
    @FXML private TableColumn<EskaeraOsagaia, String> osagaiaColumn;
    @FXML private TableColumn<EskaeraOsagaia, Integer> kopuruaColumn;
    @FXML private TableColumn<EskaeraOsagaia, Double> prezioaColumn;
    @FXML private TableColumn<EskaeraOsagaia, Double> totalaColumn;
    @FXML private TableColumn<EskaeraOsagaia, Void> ezabatuColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private ComboBox<Osagaia> osagaiaCombo;
    @FXML private TextField eskaeraZenbakiaField;
    @FXML private TextField kopuruaField;
    @FXML private TextField prezioaField;

    @FXML private Button refreshButton, gehituButton, garbituButton, sortuButton;
    @FXML private Button ikusiButton, bukatuButton, ezabatuEskaeraButton;
    @FXML private Button atzeraBotoia;

    @FXML private Label guztiraLabel;
    @FXML private Label kontaketaLabel;

    private final ObservableList<Eskaera> eskaeraList = FXCollections.observableArrayList();
    private final ObservableList<EskaeraOsagaia> eskaeraOsagaiakList = FXCollections.observableArrayList();
    private final ObservableList<Osagaia> osagaiaList = FXCollections.observableArrayList();
    private final DecimalFormat decimalFormat;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Osagaia currentSelectedOsagaia = null;
    private boolean userEditedPrice = false;

    public EskaerakController() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        decimalFormat = new DecimalFormat("#,##0.00", symbols);
    }

    @FXML
    public void initialize() {
        taulaPrestatu();
        filtroakEzarri();
        datuakKargatu();
        setupEventHandlers();
        prezioaEditableaZiurtatu();
    }

    private void prezioaEditableaZiurtatu() {
        
        prezioaField.setEditable(true);
        prezioaField.setDisable(false);

        prezioaField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");
    }

    private void taulaPrestatu() {
        zenbakiaColumn.setCellValueFactory(new PropertyValueFactory<>("eskaeraZenbakia"));

        dataColumn.setCellValueFactory(cellData -> {
            Eskaera eskaera = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    dateFormat.format(eskaera.getData())
            );
        });

        totalaEskaeraColumn.setCellValueFactory(new PropertyValueFactory<>("totala"));
        totalaEskaeraColumn.setCellFactory(column -> new TableCell<Eskaera, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(item) + " €");
                }
            }
        });

        egoeraColumn.setCellValueFactory(cellData -> {
            Eskaera eskaera = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    eskaera.isEgoera() ? "Bukatua" : "Pendiente"
            );
        });

        egoeraColumn.setCellFactory(column -> new TableCell<Eskaera, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Bukatua")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });

        pdfColumn.setCellValueFactory(new PropertyValueFactory<>("eskaeraPdf"));

        akzioakColumn.setCellFactory(param -> new TableCell<Eskaera, Void>() {
            private final Button ikusiBtn = new Button("Ikusi");
            private final Button bukatuBtn = new Button("Bukatu");
            private final Button pdfSortuBtn = new Button("PDF Sortu");

            {
                ikusiBtn.setStyle("-fx-font-size: 11px;");
                bukatuBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #27ae60; -fx-text-fill: white;");
                pdfSortuBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #9b59b6; -fx-text-fill: white;");

                ikusiBtn.setOnAction(event -> {
                    Eskaera eskaera = getTableView().getItems().get(getIndex());
                    showEskaeraDetails(eskaera);
                });

                bukatuBtn.setOnAction(event -> {
                    Eskaera eskaera = getTableView().getItems().get(getIndex());
                    handleBukatuEskaera(eskaera);
                });

                pdfSortuBtn.setOnAction(event -> {
                    Eskaera eskaera = getTableView().getItems().get(getIndex());
                    handleSortuPdfEskaerarako(eskaera);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Eskaera eskaera = getTableView().getItems().get(getIndex());

                    HBox buttons = new HBox(5, ikusiBtn);

                    if (!eskaera.isEgoera()) {
                        buttons.getChildren().add(bukatuBtn);
                    }

                    buttons.getChildren().add(pdfSortuBtn);
                    setGraphic(buttons);
                }
            }
        });

        eskaerakTable.setItems(eskaeraList);

        osagaiaColumn.setCellValueFactory(new PropertyValueFactory<>("osagaiaIzena"));
        kopuruaColumn.setCellValueFactory(new PropertyValueFactory<>("kopurua"));

        prezioaColumn.setCellValueFactory(new PropertyValueFactory<>("prezioa"));
        prezioaColumn.setCellFactory(column -> new TableCell<EskaeraOsagaia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(item) + " €");
                }
            }
        });

        totalaColumn.setCellValueFactory(new PropertyValueFactory<>("totala"));
        totalaColumn.setCellFactory(column -> new TableCell<EskaeraOsagaia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(item) + " €");
                }
            }
        });

        ezabatuColumn.setCellFactory(param -> new TableCell<EskaeraOsagaia, Void>() {
            private final Button ezabatuBtn = new Button("Ezabatu");

            {
                ezabatuBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                ezabatuBtn.setOnAction(event -> {
                    EskaeraOsagaia eskaeraOsagaia = getTableView().getItems().get(getIndex());
                    eskaeraOsagaiakList.remove(eskaeraOsagaia);
                    updateGuztira();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(ezabatuBtn);
                }
            }
        });

        eskaeraOsagaiakTable.setItems(eskaeraOsagaiakList);
    }

    private void filtroakEzarri() {
        filterCombo.getItems().addAll("Guztiak", "Pendienteak", "Bukatuak");
        filterCombo.setValue("Guztiak");
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtroaAplikatu(newVal));
    }

    private void filtroaAplikatu(String filter) {
        if (filter.equals("Guztiak")) {
            loadEskaerak();
        } else if (filter.equals("Pendienteak")) {
            loadPendienteak();
        } else if (filter.equals("Bukatuak")) {
            loadBukatuak();
        }
    }

    private void datuakKargatu() {
        loadNextEskaeraZenbakia();
        loadOsagaiak();
        loadEskaerak();
    }

    private void loadNextEskaeraZenbakia() {
        new Thread(() -> {
            int nextZenbakia = EskaeraService.getNextEskaeraZenbakia();
            Platform.runLater(() -> {
                eskaeraZenbakiaField.setText(String.valueOf(nextZenbakia));
            });
        }).start();
    }

    private void loadOsagaiak() {
        new Thread(() -> {
            List<Osagaia> osagaiak = OsagaiaService.getOsagaiak();
            Platform.runLater(() -> {
                osagaiaList.clear();
                osagaiaList.addAll(osagaiak);
                osagaiaCombo.setItems(osagaiaList);
            });
        }).start();
    }

    private void loadEskaerak() {
        new Thread(() -> {
            List<Eskaera> eskaerak = EskaeraService.getEskaerak();
            Platform.runLater(() -> {
                eskaeraList.clear();
                eskaeraList.addAll(eskaerak);
                kontaketaLabel.setText(eskaeraList.size() + " eskaera");
            });
        }).start();
    }

    private void loadPendienteak() {
        new Thread(() -> {
            List<Eskaera> eskaerak = EskaeraService.getPendienteak();
            Platform.runLater(() -> {
                eskaeraList.clear();
                eskaeraList.addAll(eskaerak);
                kontaketaLabel.setText(eskaeraList.size() + " eskaera");
            });
        }).start();
    }

    private void loadBukatuak() {
        new Thread(() -> {
            List<Eskaera> eskaerak = EskaeraService.getBukatuak();
            Platform.runLater(() -> {
                eskaeraList.clear();
                eskaeraList.addAll(eskaerak);
                kontaketaLabel.setText(eskaeraList.size() + " eskaera");
            });
        }).start();
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(event -> datuakKargatu());

        osagaiaCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentSelectedOsagaia = newVal;

                if (!userEditedPrice || prezioaField.getText().isEmpty()) {
                    prezioaField.setText(decimalFormat.format(newVal.getAzkenPrezioa()));
                    userEditedPrice = false;
                }

                kopuruaField.requestFocus();
            }
        });

        
        prezioaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentSelectedOsagaia != null && !newVal.isEmpty()) {
                String defaultPrice = decimalFormat.format(currentSelectedOsagaia.getAzkenPrezioa());
                if (!newVal.equals(defaultPrice)) {
                    userEditedPrice = true;
                }
            }
        });

        
        prezioaField.setOnMouseClicked(event -> {
            
            Platform.runLater(() -> {
                prezioaField.selectAll();
            });
        });

        
        prezioaField.setOnKeyPressed(event -> {
            userEditedPrice = true;
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterBySearch(newText);
        });

        ikusiButton.setOnAction(event -> handleIkusiEskaera());
        bukatuButton.setOnAction(event -> handleBukatuSelectedEskaera());
        ezabatuEskaeraButton.setOnAction(event -> handleEzabatuEskaera());
        gehituButton.setOnAction(event -> handleGehituOsagaia());
        garbituButton.setOnAction(event -> handleGarbitu());
        sortuButton.setOnAction(event -> handleSortuEskaera());

        prezioaField.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem resetPrice = new MenuItem("Berrezarri azken preziora");
            resetPrice.setOnAction(e -> {
                if (currentSelectedOsagaia != null) {
                    prezioaField.setText(decimalFormat.format(currentSelectedOsagaia.getAzkenPrezioa()));
                    userEditedPrice = false;
                }
            });

            contextMenu.getItems().add(resetPrice);
            prezioaField.setContextMenu(contextMenu);
        });
    }

    private void filterBySearch(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            eskaerakTable.setItems(eskaeraList);
        } else {
            ObservableList<Eskaera> filtered = FXCollections.observableArrayList();
            String lowerCaseFilter = searchText.toLowerCase();

            for (Eskaera eskaera : eskaeraList) {
                if (String.valueOf(eskaera.getEskaeraZenbakia()).contains(lowerCaseFilter) ||
                        dateFormat.format(eskaera.getData()).toLowerCase().contains(lowerCaseFilter)) {
                    filtered.add(eskaera);
                }
            }

            eskaerakTable.setItems(filtered);
        }
    }

    @FXML
    private void handleGehituOsagaia() {
        try {
            Osagaia selectedOsagaia = osagaiaCombo.getSelectionModel().getSelectedItem();
            if (selectedOsagaia == null) {
                alertaErakutsi("Abisua", "Hautatu ezazu osagaia", Alert.AlertType.WARNING);
                return;
            }

            String kopuruaText = kopuruaField.getText();
            String prezioaText = prezioaField.getText().replace(',', '.');

            if (kopuruaText.isEmpty() || prezioaText.isEmpty()) {
                alertaErakutsi("Abisua", "Bete ezazu kopurua eta prezioa", Alert.AlertType.WARNING);
                return;
            }

            int kopurua = Integer.parseInt(kopuruaText);
            double prezioa = Double.parseDouble(prezioaText);
            double azkenPrezioa = selectedOsagaia.getAzkenPrezioa();

            if (kopurua <= 0) {
                alertaErakutsi("Abisua", "Kopurua positiboa izan behar da", Alert.AlertType.WARNING);
                kopuruaField.requestFocus();
                return;
            }

            if (prezioa <= 0) {
                alertaErakutsi("Abisua", "Prezioa positiboa izan behar da", Alert.AlertType.WARNING);
                prezioaField.requestFocus();
                return;
            }

            
            if (azkenPrezioa > 0) {
                double aldaketa = Math.abs((prezioa - azkenPrezioa) / azkenPrezioa) * 100;

                
                if (aldaketa > 200) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Prezio aldaketa handia");
                    confirm.setHeaderText("Prezioa asko aldatu da");
                    confirm.setContentText(String.format(
                            "Azken prezioa: %s €\nSartutako prezioa: %s €\nAldaketa: %.1f%%\n\nJarraitu nahi duzu?",
                            decimalFormat.format(azkenPrezioa),
                            decimalFormat.format(prezioa),
                            aldaketa
                    ));

                    if (confirm.showAndWait().get() != ButtonType.OK) {
                        prezioaField.requestFocus();
                        prezioaField.selectAll();
                        return;
                    }
                }
            }

            EskaeraOsagaia eskaeraOsagaia = new EskaeraOsagaia();
            eskaeraOsagaia.setOsagaiakId(selectedOsagaia.getId());
            eskaeraOsagaia.setOsagaiaIzena(selectedOsagaia.getIzena());
            eskaeraOsagaia.setKopurua(kopurua);
            eskaeraOsagaia.setPrezioa(prezioa);
            eskaeraOsagaia.setTotala(kopurua * prezioa);

            eskaeraOsagaiakList.add(eskaeraOsagaia);
            updateGuztira();

            kopuruaField.clear();
            prezioaField.clear();

            
            osagaiaCombo.getSelectionModel().clearSelection();
            currentSelectedOsagaia = null;
            userEditedPrice = false;

            kopuruaField.requestFocus();

        } catch (NumberFormatException e) {
            alertaErakutsi("Errorea", "Zenbaki baliagarriak sartu behar dira", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGarbitu() {
        eskaeraOsagaiakList.clear();
        updateGuztira();
    }

    @FXML
    private void handleSortuEskaera() {
        if (eskaeraOsagaiakList.isEmpty()) {
            alertaErakutsi("Abisua", "Gehitu ezazu gutxienez osagai bat", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Berrespena");
        confirm.setHeaderText("Eskaera sortu");
        confirm.setContentText("Ziur zaude eskaera hau sortu nahi duzula?\nPDF bat automatikoki sortuko da.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    double guztira = guztiraKalkulatu();
                    int eskaeraZenbakia = Integer.parseInt(eskaeraZenbakiaField.getText());

                    boolean eskaeraCreated = EskaeraService.createEskaera(eskaeraZenbakia, guztira);

                    if (eskaeraCreated) {
                        List<Eskaera> eskaerak = EskaeraService.getEskaerak();
                        int createdEskaeraId = -1;
                        Eskaera createdEskaera = null;

                        for (Eskaera e : eskaerak) {
                            if (e.getEskaeraZenbakia() == eskaeraZenbakia) {
                                createdEskaeraId = e.getId();
                                createdEskaera = e;
                                break;
                            }
                        }

                        if (createdEskaeraId > 0 && createdEskaera != null) {
                            boolean allAdded = true;

                            for (EskaeraOsagaia eskaeraOsagaia : eskaeraOsagaiakList) {
                                boolean osagaiaAdded = EskaeraService.addOsagaiaToEskaera(
                                        createdEskaeraId,
                                        eskaeraOsagaia.getOsagaiakId(),
                                        eskaeraOsagaia.getKopurua(),
                                        eskaeraOsagaia.getPrezioa()
                                );

                                if (!osagaiaAdded) {
                                    allAdded = false;
                                }
                            }

                            if (allAdded) {
                                List<EskaeraOsagaia> osagaiak = EskaeraService.getEskaeraOsagaiak(createdEskaeraId);
                                createdEskaera = EskaeraService.getEskaeraById(createdEskaeraId);

                                if (createdEskaera != null && osagaiak != null) {
                                    File pdfFitxategia = PDFSortzailea.sortuEskaeraPdfZerbitzarian(createdEskaera, osagaiak);

                                    boolean finalAllAdded = allAdded;
                                    File finalPdfFitxategia = pdfFitxategia;

                                    Platform.runLater(() -> {
                                        if (finalAllAdded) {
                                            String mezua = "Eskaera ondo sortu da!";
                                            ActionLogger.log(
                                                    SessionContext.getCurrentUser(),
                                                    "INSERT",
                                                    "eskaerak",
                                                    "Eskaera sortu: #" + eskaeraZenbakia + " | Guztira: " + decimalFormat.format(guztira) + " €"
                                            );

                                            if (finalPdfFitxategia != null && finalPdfFitxategia.exists()) {
                                                mezua += "\nPDF fitxategia: " + finalPdfFitxategia.getName();

                                                Alert pdfAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                                pdfAlert.setTitle("PDF sortuta");
                                                pdfAlert.setHeaderText("Eskaeraren PDF-a ondo sortu da");
                                                pdfAlert.setContentText("PDF fitxategia ireki nahi duzu?");

                                                if (pdfAlert.showAndWait().get() == ButtonType.OK) {
                                                    PDFSortzailea.irekiPdf(finalPdfFitxategia);
                                                }
                                            }

                                            alertaErakutsi("Arrakasta", mezua, Alert.AlertType.INFORMATION);
                                            handleGarbitu();
                                            loadNextEskaeraZenbakia();
                                            loadEskaerak();
                                        } else {
                                            alertaErakutsi("Abisua", "Eskaera sortu da baina osagai batzuk ezin izan dira gehitu",
                                                    Alert.AlertType.WARNING);
                                        }
                                    });
                                }
                            } else {
                                Platform.runLater(() -> {
                                    alertaErakutsi("Abisua", "Eskaera sortu da baina osagai batzuk ezin izan dira gehitu",
                                            Alert.AlertType.WARNING);
                                });
                            }
                        } else {
                            Platform.runLater(() -> {
                                alertaErakutsi("Errorea", "Ezin izan da eskaeraren ID-a lortu", Alert.AlertType.ERROR);
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            alertaErakutsi("Errorea", "Ezin izan da eskaera sortu", Alert.AlertType.ERROR);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        alertaErakutsi("Errorea", "Errorea gertatu da: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }

    private void handleSortuPdfEskaerarako(Eskaera eskaera) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("PDF sortu");
        confirm.setHeaderText("Eskaeraren PDF-a sortu");
        confirm.setContentText("Ziur zaude eskaera #" + eskaera.getEskaeraZenbakia() + "-ren PDF bat sortu nahi duzula?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    List<EskaeraOsagaia> osagaiak = EskaeraService.getEskaeraOsagaiak(eskaera.getId());
                    File pdfFitxategia = PDFSortzailea.sortuEskaeraPdfZerbitzarian(eskaera, osagaiak);

                    Platform.runLater(() -> {
                        if (pdfFitxategia != null && pdfFitxategia.exists()) {

                            ActionLogger.log(
                                    SessionContext.getCurrentUser(),
                                    "PDF",
                                    "eskaerak",
                                    "PDF sortua eskaerarentzat: #" + eskaera.getEskaeraZenbakia()
                            );


                            String mezua = "PDF fitxategia ondo sortu da!\n";
                            mezua += "Izena: " + pdfFitxategia.getName() + "\n";
                            mezua += "Kokapena: " + pdfFitxategia.getParent();

                            Alert pdfAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            pdfAlert.setTitle("PDF sortuta");
                            pdfAlert.setHeaderText("PDF-a ondo sortu da");
                            pdfAlert.setContentText(mezua + "\n\nPDF fitxategia ireki nahi duzu?");

                            if (pdfAlert.showAndWait().get() == ButtonType.OK) {
                                PDFSortzailea.irekiPdf(pdfFitxategia);
                            }
                        } else {
                            alertaErakutsi("Errorea", "Ezin izan da PDF fitxategia sortu", Alert.AlertType.ERROR);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        alertaErakutsi("Errorea", "Errorea PDF sortzean: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleIkusiEskaera() {
        Eskaera selected = eskaerakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertaErakutsi("Abisua", "Hautatu ezazu ikusi nahi duzun eskaera", Alert.AlertType.WARNING);
            return;
        }

        showEskaeraDetails(selected);
    }

    private void showEskaeraDetails(Eskaera eskaera) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Eskaera Xehetasunak");
        dialog.setHeaderText("Eskaera #" + eskaera.getEskaeraZenbakia() + " - " +
                (eskaera.isEgoera() ? "Bukatua" : "Pendiente"));

        StringBuilder content = new StringBuilder();
        content.append("Eskaera Zenbakia: ").append(eskaera.getEskaeraZenbakia()).append("\n");
        content.append("Data: ").append(dateFormat.format(eskaera.getData())).append("\n");
        content.append("Totala: ").append(decimalFormat.format(eskaera.getTotala())).append(" €\n");
        content.append("Egoera: ").append(eskaera.isEgoera() ? "Bukatua" : "Pendiente").append("\n\n");

        new Thread(() -> {
            List<EskaeraOsagaia> osagaiak = EskaeraService.getEskaeraOsagaiak(eskaera.getId());

            if (!osagaiak.isEmpty()) {
                content.append("=== O S A G A I A K ===\n");
                for (EskaeraOsagaia eo : osagaiak) {
                    content.append(String.format("- %s: %d x %s = %s\n",
                            eo.getOsagaiaIzena(),
                            eo.getKopurua(),
                            decimalFormat.format(eo.getPrezioa()),
                            decimalFormat.format(eo.getTotala())
                    ));
                }
            } else {
                content.append("Ez dago osagairik eskaera honetan\n");
            }

            Platform.runLater(() -> {
                TextArea textArea = new TextArea(content.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefSize(500, 300);

                dialog.getDialogPane().setContent(textArea);
                dialog.showAndWait();
            });
        }).start();
    }

    @FXML
    private void handleBukatuSelectedEskaera() {
        Eskaera selected = eskaerakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertaErakutsi("Abisua", "Hautatu ezazu bukatu nahi duzun eskaera", Alert.AlertType.WARNING);
            return;
        }

        handleBukatuEskaera(selected);
    }

    private void handleBukatuEskaera(Eskaera eskaera) {
        if (eskaera.isEgoera()) {
            alertaErakutsi("Abisua", "Eskaera hau dagoeneko bukatuta dago", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Berrespena");
        confirm.setHeaderText("Eskaera bukatu");
        confirm.setContentText("Ziur zaude eskaera #" + eskaera.getEskaeraZenbakia() + " bukatu nahi duzula?\n" +
                "Honek stock-a ETA prezioak eguneratuko ditu.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = EskaeraService.markAsCompleted(eskaera.getId());

                if (success) {
                    ActionLogger.log(
                            SessionContext.getCurrentUser(),
                            "UPDATE",
                            "eskaerak",
                            "Eskaera bukatu: #" + eskaera.getEskaeraZenbakia()
                    );

                    List<EskaeraOsagaia> osagaiak = EskaeraService.getEskaeraOsagaiak(eskaera.getId());
                    Eskaera eguneratutakoEskaera = EskaeraService.getEskaeraById(eskaera.getId());

                    if (eguneratutakoEskaera != null) {
                        PDFSortzailea.sortuEskaeraPdfZerbitzarian(eguneratutakoEskaera, osagaiak);
                    }
                }

                Platform.runLater(() -> {
                    if (success) {
                        alertaErakutsi("Arrakasta",
                                "Eskaera ondo bukatu da!\n" +
                                        "Stock-a eguneratuta\n" +
                                        "Azken prezioak eguneratuak\n" +
                                        "PDF berria sortuta",
                                Alert.AlertType.INFORMATION);
                        loadEskaerak();
                    } else {
                        alertaErakutsi("Errorea", "Ezin izan da eskaera bukatu", Alert.AlertType.ERROR);
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleEzabatuEskaera() {
        Eskaera selected = eskaerakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertaErakutsi("Abisua", "Hautatu ezazu ezabatu nahi duzun eskaera", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Berrespena");
        confirm.setHeaderText("Eskaera ezabatu");
        confirm.setContentText("Ziur zaude eskaera #" + selected.getEskaeraZenbakia() + " ezabatu nahi duzula?\n" +
                "Ekintza hau ezin da desegin.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = EskaeraService.deleteEskaera(selected.getId());
                Platform.runLater(() -> {
                    if (success) {
                        ActionLogger.log(
                                SessionContext.getCurrentUser(),
                                "DELETE",
                                "eskaerak",
                                "Eskaera ezabatuta: #" + selected.getEskaeraZenbakia()
                        );

                        alertaErakutsi("Arrakasta", "Eskaera ondo ezabatuta!", Alert.AlertType.INFORMATION);
                        loadEskaerak();
                    } else {
                        alertaErakutsi("Errorea", "Ezin izan da eskaera ezabatu", Alert.AlertType.ERROR);
                    }
                });
            }).start();
        }
    }

    private double guztiraKalkulatu() {
        return eskaeraOsagaiakList.stream()
                .mapToDouble(EskaeraOsagaia::getTotala)
                .sum();
    }

    private void updateGuztira() {
        double guztira = guztiraKalkulatu();
        guztiraLabel.setText(decimalFormat.format(guztira) + " €");
    }

    private void alertaErakutsi(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void atzeraBueltatu(ActionEvent actionEvent) {
        try {
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            StageManager.switchStage(
                    currentStage,
                    "menu-view.fxml",
                    "Menu Nagusia",
                    true
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}