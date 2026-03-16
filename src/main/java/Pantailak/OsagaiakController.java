package Pantailak;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.ActionLogger;
import services.OsagaiaService;
import Klaseak.Osagaia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.SessionContext;

import java.io.IOException;
import java.util.List;

public class OsagaiakController {

    @FXML private TableView<Osagaia> osagaiakTable;
    @FXML private TableColumn<Osagaia, Integer> idColumn;
    @FXML private TableColumn<Osagaia, String> izenaColumn;
    @FXML private TableColumn<Osagaia, Double> prezioaColumn;
    @FXML private TableColumn<Osagaia, Integer> stockColumn;
    @FXML private TableColumn<Osagaia, Integer> gutxienekoStockColumn;
    @FXML private TableColumn<Osagaia, Boolean> eskatuColumn;
    @FXML private TableColumn<Osagaia, Double> balioaColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Button newButton, editButton, deleteButton, refreshButton;
    @FXML private Button stockGehituButton, stockKenduButton, eskatuToggleButton;

    @FXML private TextField izenaField;
    @FXML private TextField prezioaField;
    @FXML private TextField stockField;
    @FXML private TextField gutxienekoStockField;
    @FXML private CheckBox eskatuCheckBox;

    @FXML private Label totalOsagaiakLabel;
    @FXML private Label stockGutxiLabel;
    @FXML private Label balioTotalaLabel;
    @FXML private ProgressBar stockProgressBar;

    private final ObservableList<Osagaia> osagaiakList = FXCollections.observableArrayList();
    private final FilteredList<Osagaia> filteredData = new FilteredList<>(osagaiakList);

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
        prezioaColumn.setCellValueFactory(new PropertyValueFactory<>("azkenPrezioa"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        gutxienekoStockColumn.setCellValueFactory(new PropertyValueFactory<>("gutxienekoStock"));
        eskatuColumn.setCellValueFactory(new PropertyValueFactory<>("eskatu"));

        balioaColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(
                        cellData.getValue().stockBalioaLortu()
                ).asObject()
        );

        prezioaColumn.setCellFactory(column -> new TableCell<Osagaia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                }
            }
        });

        balioaColumn.setCellFactory(column -> new TableCell<Osagaia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                }
            }
        });

        stockColumn.setCellFactory(column -> new TableCell<Osagaia, Integer>() {
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
                    } else if (item <= osagaia.getGutxienekoStock() * 2) {
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
                    if (item) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        SortedList<Osagaia> sortedData = new SortedList<>(filteredData);
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
        filterCombo.getItems().addAll("Guztiak", "Stock gutxi dutenak", "Eskatzeko markatuta", "Stock normala");
        filterCombo.setValue("Guztiak");
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    private void applyFilter(String filter) {
        filteredData.setPredicate(osagaia -> {
            if (filter == null || filter.isEmpty() || filter.equals("Guztiak")) return true;
            switch (filter) {
                case "Stock gutxi dutenak": return osagaia.erosiBeharDa();
                case "Eskatzeko markatuta": return osagaia.isEskatu();
                case "Stock normala": return !osagaia.erosiBeharDa();
                default: return true;
            }
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
        prezioaField.setText(String.format("%.2f", osagaia.getAzkenPrezioa()));
        stockField.setText(String.valueOf(osagaia.getStock()));
        gutxienekoStockField.setText(String.valueOf(osagaia.getGutxienekoStock()));
        eskatuCheckBox.setSelected(osagaia.isEskatu());
        updateStockProgress(osagaia);
    }

    private void updateStockProgress(Osagaia osagaia) {
        double progress = osagaia.getGutxienekoStock() > 0 ?
                Math.min(1.0, (double) osagaia.getStock() / (osagaia.getGutxienekoStock() * 3)) : 0.5;
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
                        osagaia.getIzena().toLowerCase().contains(searchText.toLowerCase()));
    }

    private void updateStatistics() {
        int total = osagaiakList.size();
        long stockGutxi = osagaiakList.stream().filter(Osagaia::erosiBeharDa).count();
        double balioTotala = osagaiakList.stream().mapToDouble(Osagaia::stockBalioaLortu).sum();
        totalOsagaiakLabel.setText(String.valueOf(total));
        stockGutxiLabel.setText(String.valueOf(stockGutxi));
        balioTotalaLabel.setText(String.format("%.2f €", balioTotala));
    }

    @FXML
    private void handleNewOsagaia() { clearForm(); }

    @FXML
    private void handleSaveOsagaia() {
        try {
            Osagaia selected = osagaiakTable.getSelectionModel().getSelectedItem();

            String prezioaTexto = prezioaField.getText().replace(',', '.');
            String stockTexto = stockField.getText();
            String gutxienekoStockTexto = gutxienekoStockField.getText();

            Osagaia osagaia;
            if (selected != null) {
                osagaia = selected;
                osagaia.setIzena(izenaField.getText());
                osagaia.setAzkenPrezioa(Double.parseDouble(prezioaTexto));
                osagaia.setStock(Integer.parseInt(stockTexto));
                osagaia.setGutxienekoStock(Integer.parseInt(gutxienekoStockTexto));
                osagaia.setEskatu(eskatuCheckBox.isSelected());
            } else {
                osagaia = new Osagaia();
                osagaia.setIzena(izenaField.getText());
                osagaia.setAzkenPrezioa(Double.parseDouble(prezioaTexto));
                osagaia.setStock(Integer.parseInt(stockTexto));
                osagaia.setGutxienekoStock(Integer.parseInt(gutxienekoStockTexto));
                osagaia.setEskatu(eskatuCheckBox.isSelected());
            }

            boolean isUpdate = selected != null;
            String izenaLog = izenaField.getText();
            double prezioaLog = Double.parseDouble(prezioaTexto);
            int stockLog = Integer.parseInt(stockTexto);

            new Thread(() -> {
                boolean success = selected != null ?
                        OsagaiaService.updateOsagaia(osagaia) :
                        OsagaiaService.createOsagaia(osagaia);
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        ActionLogger.log(
                                SessionContext.getCurrentUser(),
                                isUpdate ? "UPDATE" : "INSERT",
                                "osagaiak",
                                (isUpdate
                                        ? "Osagaia eguneratu: "
                                        : "Osagaia sortu: ")
                                        + izenaLog
                                        + " | Prezioa=" + prezioaLog
                                        + " | Stock=" + stockLog
                        );

                        showAlert("Arrakasta", "Osagaia ondo gordeta", Alert.AlertType.INFORMATION);
                        loadOsagaiak();
                        clearForm();

                    } else {
                        showAlert("Errorea", "Ezin izan da osagaia gorde", Alert.AlertType.ERROR);
                    }

                });
            }).start();
        } catch (NumberFormatException e) {
            showAlert("Errorea", "Zenbakiak sartu behar dira prezio eta stock eremuetarako", Alert.AlertType.ERROR);
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
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String izenaLog = selected.getIzena();
            int idLog = selected.getId();
            new Thread(() -> {
                boolean success = OsagaiaService.deleteOsagaia(selected.getId());
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        ActionLogger.log(
                                SessionContext.getCurrentUser(),
                                "DELETE",
                                "osagaiak",
                                "Osagaia ezabatua: " + izenaLog + " (ID=" + idLog + ")"
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
                    boolean success = OsagaiaService.updateStock(selected.getId(), kopurua);
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            ActionLogger.log(
                                    SessionContext.getCurrentUser(),
                                    "UPDATE",
                                    "osagaiak",
                                    "Stock gehitua: " + selected.getIzena() + " +" + kopurua
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
                int kopurua = -Integer.parseInt(quantity);
                new Thread(() -> {
                    boolean success = OsagaiaService.updateStock(selected.getId(), kopurua);
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            ActionLogger.log(
                                    SessionContext.getCurrentUser(),
                                    "UPDATE",
                                    "osagaiak",
                                    "Stock kendua: " + selected.getIzena() + " " + kopurua
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
    private void handleToggleEskatu() {
        Osagaia selected = osagaiakTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Abisua", "Hautatu ezazu eskatu/ez eskatu aldatu nahi diozun osagaia", Alert.AlertType.WARNING);
            return;
        }

        new Thread(() -> {
            boolean success = OsagaiaService.toggleEskatu(selected.getId());
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    ActionLogger.log(
                            SessionContext.getCurrentUser(),
                            "UPDATE",
                            "osagaiak",
                            "Eskatu egoera aldatu: " + selected.getIzena()
                    );
                    showAlert("Arrakasta", "Eskatu egoera aldatu da", Alert.AlertType.INFORMATION);
                    loadOsagaiak();
                } else {
                    showAlert("Errorea", "Ezin izan da eskatu egoera aldatu", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    @FXML
    private void handleRefresh() { loadOsagaiak(); }

    @FXML
    private void handleGenerateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== INVENTARIOaren INFORMEA ===\n");
        report.append("Data: ").append(java.time.LocalDate.now()).append("\n");
        report.append("Osagai kopurua: ").append(osagaiakList.size()).append("\n");
        long stockGutxi = osagaiakList.stream().filter(Osagaia::erosiBeharDa).count();
        report.append("Stock gutxi dutenak: ").append(stockGutxi).append("\n");
        double balioTotala = osagaiakList.stream().mapToDouble(Osagaia::stockBalioaLortu).sum();
        report.append("Balio totala: ").append(String.format("%.2f", balioTotala)).append(" €\n\n");
        report.append("=== OSAGAIAK ZERRENDATUA ===\n");
        for (Osagaia osagaia : osagaiakList) {
            report.append(String.format("- %s: %d unitate @ %.2f€ = %.2f€ %s\n",
                    osagaia.getIzena(), osagaia.getStock(), osagaia.getAzkenPrezioa(),
                    osagaia.stockBalioaLortu(), osagaia.erosiBeharDa() ? "[STOCK GUTXI]" : ""));
        }
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventarioaren Informea");
        alert.setHeaderText("Osagaien inventarioaren informea");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void clearForm() {
        izenaField.clear();
        prezioaField.clear();
        stockField.clear();
        gutxienekoStockField.clear();
        eskatuCheckBox.setSelected(false);
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