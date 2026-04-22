package Pantailak;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Klaseak.Osagaia;
import services.ActionLogger;
import services.OsagaiaService;
import services.SessionContext;

import java.io.IOException;
import java.util.List;

public class OsagaiakController {

    @FXML private TableView<Osagaia> osagaiakTable;
    @FXML private TableColumn<Osagaia, Integer> idColumn;
    @FXML private TableColumn<Osagaia, String> izenaColumn;
    @FXML private TableColumn<Osagaia, String> deskribapenaColumn;
    @FXML private TableColumn<Osagaia, Integer> kantitateaColumn;
    @FXML private TableColumn<Osagaia, String> neurriaColumn;
    @FXML private TableColumn<Osagaia, Integer> stockMinimoaColumn;
    @FXML private TableColumn<Osagaia, Boolean> eskatuColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Button newButton, editButton, deleteButton, refreshButton;
    @FXML private Button stockGehituButton, stockKenduButton, eskatuToggleButton;

    @FXML private TextField izenaField;
    @FXML private TextArea deskribapenaArea; // deskribapena eremu gehigarria
    @FXML private TextField kantitateaField;
    @FXML private TextField neurriaField;
    @FXML private TextField stockMinimoaField;
    // @FXML private CheckBox eskatuCheckBox; // kendu (ez dago APIan)

    @FXML private Label totalOsagaiakLabel;
    @FXML private Label stockGutxiLabel;
    @FXML private ProgressBar stockProgressBar;

    private final javafx.collections.ObservableList<Osagaia> osagaiakList = javafx.collections.FXCollections.observableArrayList();
    private final javafx.collections.transformation.FilteredList<Osagaia> filteredData = new javafx.collections.transformation.FilteredList<>(osagaiakList);

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadOsagaiak();
        setupEventHandlers();
        updateStatistics();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        izenaColumn.setCellValueFactory(new PropertyValueFactory<>("izena"));
        deskribapenaColumn.setCellValueFactory(new PropertyValueFactory<>("deskribapena"));
        kantitateaColumn.setCellValueFactory(new PropertyValueFactory<>("kantitatea"));
        neurriaColumn.setCellValueFactory(new PropertyValueFactory<>("neurriaUnitatea"));
        stockMinimoaColumn.setCellValueFactory(new PropertyValueFactory<>("stockMinimoa"));

        // eskatu zutabea (erosiBeharDa erabiliz)
        eskatuColumn.setCellValueFactory(cellData -> {
            boolean eskaera = cellData.getValue().erosiBeharDa();
            return new javafx.beans.property.SimpleBooleanProperty(eskaera);
        });

        // Koloreak stock gutxirako
        kantitateaColumn.setCellFactory(column -> new TableCell<Osagaia, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    Osagaia osagaia = getTableView().getItems().get(getIndex());
                    if (osagaia.erosiBeharDa()) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item <= osagaia.getStockMinimoa() * 2) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        eskatuColumn.setCellFactory(column -> new TableCell<Osagaia, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "BAI" : "EZ");
                    setStyle(item ? "-fx-text-fill: red; -fx-font-weight: bold;" : "-fx-text-fill: green;");
                }
            }
        });

        javafx.collections.transformation.SortedList<Osagaia> sortedData = new javafx.collections.transformation.SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(osagaiakTable.comparatorProperty());
        osagaiakTable.setItems(sortedData);

        osagaiakTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadOsagaiaDetails(newSelection);
                    }
                });
    }

    private void setupFilters() {
        filterCombo.getItems().addAll("Guztiak", "Stock gutxi dutenak", "Stock normala");
        filterCombo.setValue("Guztiak");
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    private void applyFilter(String filter) {
        filteredData.setPredicate(osagaia -> {
            if (filter == null || filter.isEmpty() || filter.equals("Guztiak")) return true;
            if (filter.equals("Stock gutxi dutenak")) return osagaia.erosiBeharDa();
            if (filter.equals("Stock normala")) return !osagaia.erosiBeharDa();
            return true;
        });
    }

    private void loadOsagaiak() {
        new Thread(() -> {
            List<Osagaia> osagaiak = OsagaiaService.getOsagaiak();
            javafx.application.Platform.runLater(() -> {
                osagaiakList.clear();
                osagaiakList.addAll(osagaiak);
                updateStatistics();
            });
        }).start();
    }

    private void loadOsagaiaDetails(Osagaia osagaia) {
        izenaField.setText(osagaia.getIzena());
        deskribapenaArea.setText(osagaia.getDeskribapena());
        kantitateaField.setText(String.valueOf(osagaia.getKantitatea()));
        neurriaField.setText(osagaia.getNeurriaUnitatea());
        stockMinimoaField.setText(String.valueOf(osagaia.getStockMinimoa()));
        updateStockProgress(osagaia);
    }

    private void updateStockProgress(Osagaia osagaia) {
        double progress = osagaia.getStockMinimoa() > 0 ?
                Math.min(1.0, (double) osagaia.getKantitatea() / (osagaia.getStockMinimoa() * 3)) : 0.5;
        stockProgressBar.setProgress(progress);
        if (progress < 0.25) stockProgressBar.setStyle("-fx-accent: red;");
        else if (progress < 0.5) stockProgressBar.setStyle("-fx-accent: orange;");
        else stockProgressBar.setStyle("-fx-accent: green;");
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((obs, oldText, newText) -> filterBySearch(newText));
    }

    private void filterBySearch(String searchText) {
        filteredData.setPredicate(osagaia ->
                searchText == null || searchText.isEmpty() ||
                        osagaia.getIzena().toLowerCase().contains(searchText.toLowerCase()) ||
                        (osagaia.getDeskribapena() != null && osagaia.getDeskribapena().toLowerCase().contains(searchText.toLowerCase())));
    }

    private void updateStatistics() {
        int total = osagaiakList.size();
        long stockGutxi = osagaiakList.stream().filter(Osagaia::erosiBeharDa).count();
        totalOsagaiakLabel.setText(String.valueOf(total));
        stockGutxiLabel.setText(String.valueOf(stockGutxi));
    }

    @FXML
    private void handleNewOsagaia() { clearForm(); }

    @FXML
    private void handleSaveOsagaia() {
        try {
            Osagaia selected = osagaiakTable.getSelectionModel().getSelectedItem();

            // Balidazioak
            if (izenaField.getText().isBlank()) {
                showAlert("Errorea", "Izena ezin da hutsik egon", Alert.AlertType.ERROR);
                return;
            }
            int kantitatea;
            int stockMinimoa;
            try {
                kantitatea = Integer.parseInt(kantitateaField.getText());
                stockMinimoa = Integer.parseInt(stockMinimoaField.getText());
            } catch (NumberFormatException e) {
                showAlert("Errorea", "Kopurua eta stock minimoa zenbakiak izan behar dira", Alert.AlertType.ERROR);
                return;
            }

            Osagaia osagaia;
            if (selected != null) {
                osagaia = selected;
                osagaia.setIzena(izenaField.getText());
                osagaia.setDeskribapena(deskribapenaArea.getText());
                osagaia.setKantitatea(kantitatea);
                osagaia.setNeurriaUnitatea(neurriaField.getText());
                osagaia.setStockMinimoa(stockMinimoa);
            } else {
                osagaia = new Osagaia();
                osagaia.setIzena(izenaField.getText());
                osagaia.setDeskribapena(deskribapenaArea.getText());
                osagaia.setKantitatea(kantitatea);
                osagaia.setNeurriaUnitatea(neurriaField.getText());
                osagaia.setStockMinimoa(stockMinimoa);
            }

            boolean isUpdate = selected != null;
            String izenaLog = izenaField.getText();

            new Thread(() -> {
                boolean success = isUpdate ?
                        OsagaiaService.updateOsagaia(osagaia) :
                        OsagaiaService.createOsagaia(osagaia);
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        ActionLogger.log(
                                SessionContext.getCurrentUsername(),
                                isUpdate ? "UPDATE" : "INSERT",
                                "inbentarioa",
                                (isUpdate ? "Eguneratu: " : "Sortu: ") + izenaLog
                        );
                        showAlert("Arrakasta", "Osagaia ondo gordeta", Alert.AlertType.INFORMATION);
                        loadOsagaiak();
                        clearForm();
                    } else {
                        showAlert("Errorea", "Ezin izan da osagaia gorde", Alert.AlertType.ERROR);
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errorea", "Errorea gordetzean: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteOsagaia() {
        Osagaia selected = osagaiakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Abisua", "Hautatu ezazu ezabatu nahi duzun osagaia", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Berrespena");
        confirm.setHeaderText("Osagaia ezabatu");
        confirm.setContentText("Ziur zaude '" + selected.getIzena() + "' osagaia ezabatu nahi duzula?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String izenaLog = selected.getIzena();
            int idLog = selected.getId();
            new Thread(() -> {
                boolean success = OsagaiaService.deleteOsagaia(selected.getId());
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        ActionLogger.log(
                                SessionContext.getCurrentUsername(),
                                "DELETE",
                                "inbentarioa",
                                "Ezabatua: " + izenaLog + " (ID=" + idLog + ")"
                        );
                        showAlert("Arrakasta", "Osagaia ondo ezabatuta", Alert.AlertType.INFORMATION);
                        loadOsagaiak();
                        clearForm();
                    } else {
                        showAlert("Errorea", "Ezin izan da osagaia ezabatu", Alert.AlertType.ERROR);
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleAddStock() {
        Osagaia selected = osagaiakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Abisua", "Hautatu ezazu stock gehitu nahi diozun osagaia", Alert.AlertType.WARNING);
            return;
        }
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Stock Gehitu");
        dialog.setHeaderText("Gehitu stock '" + selected.getIzena() + "' osagaiari");
        dialog.setContentText("Sartu gehitu nahi duzun kopurua:");
        dialog.showAndWait().ifPresent(quantity -> {
            try {
                int kopurua = Integer.parseInt(quantity);
                new Thread(() -> {
                    boolean success = OsagaiaService.updateStock(selected.getId(), selected.getKantitatea() + kopurua);
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            ActionLogger.log(
                                    SessionContext.getCurrentUsername(),
                                    "UPDATE",
                                    "inbentarioa",
                                    "Stock gehitu: " + selected.getIzena() + " +" + kopurua
                            );
                            loadOsagaiak();
                            showAlert("Arrakasta", "Stock ondo eguneratuta", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Errorea", "Ezin izan da stock-a eguneratu", Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            } catch (NumberFormatException e) {
                showAlert("Errorea", "Zenbaki baliagarria sartu behar da", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleRemoveStock() {
        Osagaia selected = osagaiakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Abisua", "Hautatu ezazu stock kendu nahi diozun osagaia", Alert.AlertType.WARNING);
            return;
        }
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Stock Kendu");
        dialog.setHeaderText("Kendu stock '" + selected.getIzena() + "' osagaiatik");
        dialog.setContentText("Sartu kendu nahi duzun kopurua:");
        dialog.showAndWait().ifPresent(quantity -> {
            try {
                int kopurua = Integer.parseInt(quantity);
                int newStock = selected.getKantitatea() - kopurua;
                if (newStock < 0) {
                    showAlert("Errorea", "Ezin da stock negatiboa izan", Alert.AlertType.ERROR);
                    return;
                }
                new Thread(() -> {
                    boolean success = OsagaiaService.updateStock(selected.getId(), newStock);
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            ActionLogger.log(
                                    SessionContext.getCurrentUsername(),
                                    "UPDATE",
                                    "inbentarioa",
                                    "Stock kendu: " + selected.getIzena() + " -" + kopurua
                            );
                            loadOsagaiak();
                            showAlert("Arrakasta", "Stock ondo eguneratuta", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Errorea", "Ezin izan da stock-a eguneratu", Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            } catch (NumberFormatException e) {
                showAlert("Errorea", "Zenbaki baliagarria sartu behar da", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleRefresh() { loadOsagaiak(); }

    @FXML
    private void handleGenerateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== INBENTARIOAREN INFORMEA ===\n");
        report.append("Data: ").append(java.time.LocalDate.now()).append("\n");
        report.append("Osagai kopurua: ").append(osagaiakList.size()).append("\n");
        long stockGutxi = osagaiakList.stream().filter(Osagaia::erosiBeharDa).count();
        report.append("Stock gutxi dutenak: ").append(stockGutxi).append("\n\n");
        report.append("=== OSAGAIAK ZERRENDATUA ===\n");
        for (Osagaia osagaia : osagaiakList) {
            report.append(String.format("- %s: %d %s (min: %d) %s\n",
                    osagaia.getIzena(),
                    osagaia.getKantitatea(),
                    osagaia.getNeurriaUnitatea() != null ? osagaia.getNeurriaUnitatea() : "",
                    osagaia.getStockMinimoa(),
                    osagaia.erosiBeharDa() ? "[STOCK GUTXI]" : ""));
            if (osagaia.getDeskribapena() != null && !osagaia.getDeskribapena().isBlank()) {
                report.append("  Deskribapena: ").append(osagaia.getDeskribapena()).append("\n");
            }
        }
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inbentarioaren Informea");
        alert.setHeaderText("Osagaien inbentarioaren informea");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void clearForm() {
        izenaField.clear();
        deskribapenaArea.clear();
        kantitateaField.clear();
        neurriaField.clear();
        stockMinimoaField.clear();
        osagaiakTable.getSelectionModel().clearSelection();
        stockProgressBar.setProgress(0);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void atzeraBueltatu(ActionEvent actionEvent) {
        try {
            Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            StageManager.switchStage(currentStage, "menu-view.fxml", "Menu Nagusia", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}