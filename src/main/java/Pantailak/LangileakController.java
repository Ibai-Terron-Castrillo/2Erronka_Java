package Pantailak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Klaseak.Langilea;
import Klaseak.Rolak;
import services.ActionLogger;
import services.LangileaService;
import services.SessionContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class LangileakController {

    @FXML
    private TableView<Langilea> tableLangileak;
    @FXML
    private TableColumn<Langilea, Integer> colId;
    @FXML
    private TableColumn<Langilea, String> colIzena;          // izen osoa
    @FXML
    private TableColumn<Langilea, String> colErabiltzailea;  // username
    @FXML
    private TableColumn<Langilea, String> colRola;           // rol izena
    @FXML
    private TableColumn<Langilea, Boolean> colTxatBaimena;   // txat baimena

    @FXML
    private TextField txtIzena;          // izen osoa
    @FXML
    private TextField txtErabiltzailea;   // username
    @FXML
    private PasswordField txtPasahitza;   // password
    @FXML
    private ComboBox<Rolak> comboLanpostu; // rol aukeratzailea
    @FXML
    private CheckBox chkTxatBaimena;     // txat baimena checkbox

    @FXML
    private Button btnSave, btnCancel;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> lanpostuFilter;
    @FXML
    private ComboBox<String> ordenatuFilter;
    @FXML
    private Label langileKopuruaLabel;

    @FXML
    private Label totalLangileakLabel, sukaldariakLabel, zerbitzariakLabel, adminLabel;

    @FXML
    private Button btnAdd, btnEdit, btnDelete, atzeraBotoia, refreshButton;

    private ObservableList<Langilea> langileakLista;
    private FilteredList<Langilea> filteredData;
    private Langilea langileaEditatzen;

    @FXML
    public void initialize() {
        System.out.println("initialize() deituta (API egokituta)");

        // Taularen zutabeak konfiguratu
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colErabiltzailea.setCellValueFactory(new PropertyValueFactory<>("erabiltzailea"));
        colRola.setCellValueFactory(new PropertyValueFactory<>("rolaIzena"));
        colTxatBaimena.setCellValueFactory(new PropertyValueFactory<>("txat_baimena"));

        // Txat baimena zutabea formateatu (✓ edo ✗)
        colTxatBaimena.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✓" : "✗");
                    setStyle("-fx-alignment: CENTER; -fx-font-size: 14; -fx-text-fill: " + (item ? "#38a169;" : "#e53e3e;"));
                }
            }
        });

        // Rolak kargatu ComboBox-ean
        List<Rolak> rolList = LangileaService.getAllRolak();
        comboLanpostu.setItems(FXCollections.observableArrayList(rolList));

        // Filtroen konfigurazioa: dinamikoa rolekin
        lanpostuFilter.getItems().clear();
        lanpostuFilter.getItems().add("Guztiak");
        for (Rolak r : rolList) {
            lanpostuFilter.getItems().add(r.getIzena());
        }
        lanpostuFilter.setValue("Guztiak");

        ordenatuFilter.getItems().addAll("ID", "Izena", "Erabiltzailea", "Lanpostua");
        ordenatuFilter.setValue("ID");

        formularioaKonfiguratu();
        taulaBirkargatu();
        bilaketaKonfiguratu();
        botoiakKonfiguratu();
        taulaAukera();
        formularioaGarbitu();
    }

    private void formularioaKonfiguratu() {
        btnSave.setOnAction(e -> langileaGorde());
        btnCancel.setOnAction(e -> formularioaGarbitu());
    }

    private void bilaketaKonfiguratu() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filtroakAplikatu());
        }
        lanpostuFilter.setOnAction(e -> filtroakAplikatu());
        ordenatuFilter.setOnAction(e -> ordenaAplikatu());
    }

    private void botoiakKonfiguratu() {
        btnAdd.setOnAction(e -> {
            formularioaGarbitu();
            langileaEditatzen = null;
        });

        btnDelete.setOnAction(e -> deleteSelected());

        if (refreshButton != null) {
            refreshButton.setOnAction(e -> taulaBirkargatu());
        }
    }

    private void taulaAukera() {
        tableLangileak.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        kargatuFormularioa(newSelection);
                    }
                });
    }

    private void kargatuFormularioa(Langilea langilea) {
        langileaEditatzen = langilea;
        txtIzena.setText(langilea.getIzena());
        txtErabiltzailea.setText(langilea.getErabiltzailea());
        txtPasahitza.setText(langilea.getPasahitza());
        chkTxatBaimena.setSelected(langilea.getTxat_baimena());

        if (langilea.getRola() != null) {
            comboLanpostu.setValue(langilea.getRola());
        } else {
            // rolaId-tik bilatu
            comboLanpostu.getItems().stream()
                    .filter(r -> r.getId() == langilea.getRolaId())
                    .findFirst()
                    .ifPresent(comboLanpostu::setValue);
        }
    }

    private void formularioaGarbitu() {
        langileaEditatzen = null;
        txtIzena.clear();
        txtErabiltzailea.clear();
        txtPasahitza.clear();
        comboLanpostu.getSelectionModel().clearSelection();
        chkTxatBaimena.setSelected(false);
        tableLangileak.getSelectionModel().clearSelection();
    }

    private void langileaGorde() {
        if (txtIzena.getText().isBlank()) {
            alertaErakutsi("Izena jarri behar da.");
            return;
        }
        if (txtErabiltzailea.getText().isBlank()) {
            alertaErakutsi("Erabiltzaile izena jarri behar da.");
            return;
        }
        if (txtPasahitza.getText().isBlank()) {
            alertaErakutsi("Pasahitza jarri behar da.");
            return;
        }

        Rolak selectedRol = comboLanpostu.getValue();
        if (selectedRol == null) {
            alertaErakutsi("Rol bat aukeratu behar da.");
            return;
        }

        Langilea langilea = (langileaEditatzen == null) ? new Langilea() : langileaEditatzen;

        langilea.setIzena(txtIzena.getText().trim());
        langilea.setErabiltzailea(txtErabiltzailea.getText().trim());
        langilea.setPasahitza(txtPasahitza.getText().trim());
        langilea.setAktibo("Bai");
        if (langilea.getErregistroData() == null) {
            langilea.setErregistroData(LocalDateTime.now());
        }
        langilea.setRolaId(selectedRol.getId());
        langilea.setRola(selectedRol);
        langilea.setTxat_baimena(chkTxatBaimena.isSelected());

        if (langileaEditatzen == null) {
            Langilea created = LangileaService.create(langilea);
            if (created != null) {
                ActionLogger.log(SessionContext.getCurrentUsername(), "INSERT", "langileak",
                        "Langilea sortu: " + langilea.getIzena());
            } else {
                alertaErakutsi("Errorea langilea sortzean.");
                return;
            }
        } else {
            boolean ok = LangileaService.update(langilea);
            if (ok) {
                ActionLogger.log(SessionContext.getCurrentUsername(), "UPDATE", "langileak",
                        "Langilea eguneratu (ID=" + langilea.getId() + ")");
            } else {
                alertaErakutsi("Errorea langilea eguneratzean.");
                return;
            }
        }

        taulaBirkargatu();
        formularioaGarbitu();
        arrakastaErakutsi("Langilea ondo gorde da.");
    }

    private void filtroakAplikatu() {
        if (filteredData == null) return;

        String searchText = searchField.getText().toLowerCase();
        String selectedRolName = lanpostuFilter.getValue();

        filteredData.setPredicate(langilea -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    langilea.getIzena().toLowerCase().contains(searchText) ||
                    langilea.getErabiltzailea().toLowerCase().contains(searchText);

            boolean matchesRol = selectedRolName.equals("Guztiak") ||
                    (langilea.getRola() != null && langilea.getRola().getIzena().equals(selectedRolName));

            return matchesSearch && matchesRol;
        });

        langileKopuruaLabel.setText(filteredData.size() + " langile");
    }

    private void ordenaAplikatu() {
        if (filteredData == null) return;

        String orden = ordenatuFilter.getValue();
        Comparator<Langilea> comparator = switch (orden) {
            case "Izena" -> Comparator.comparing(Langilea::getIzena);
            case "Erabiltzailea" -> Comparator.comparing(Langilea::getErabiltzailea);
            case "Lanpostua" -> Comparator.comparing(l -> l.getRola() != null ? l.getRola().getIzena() : "");
            default -> Comparator.comparing(Langilea::getId);
        };

        SortedList<Langilea> sortedData = new SortedList<>(filteredData);
        sortedData.setComparator(comparator);
        tableLangileak.setItems(sortedData);
    }

    private void taulaBirkargatu() {
        try {
            List<Langilea> langileak = LangileaService.getAll();
            langileakLista = FXCollections.observableArrayList(langileak);
            filteredData = new FilteredList<>(langileakLista);
            ordenaAplikatu();
            kopuruakEguneratu();
            System.out.println("Taula birkargatu da " + langileak.size() + " erregistrorekin");
        } catch (Exception e) {
            e.printStackTrace();
            langileakLista = FXCollections.observableArrayList();
            filteredData = new FilteredList<>(langileakLista);
            tableLangileak.setItems(filteredData);
        }
    }

    private void kopuruakEguneratu() {
        if (langileakLista == null) return;

        int total = langileakLista.size();
        int sukaldariak = 0;
        int zerbitzariak = 0;
        int admin = 0;

        for (Langilea l : langileakLista) {
            String rol = l.getRolaIzena().toLowerCase();
            if (rol.contains("sukaldari")) sukaldariak++;
            else if (rol.contains("zerbitzari")) zerbitzariak++;
            else if (rol.contains("admin")) admin++;
        }

        totalLangileakLabel.setText(String.valueOf(total));
        sukaldariakLabel.setText(String.valueOf(sukaldariak));
        zerbitzariakLabel.setText(String.valueOf(zerbitzariak));
        adminLabel.setText(String.valueOf(admin));
        langileKopuruaLabel.setText(total + " langile");
    }

    private void deleteSelected() {
        Langilea selected = tableLangileak.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertaErakutsi("Aukeratu langile bat ezabatzeko.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("KONTUZ!");
        alert.setHeaderText("Ziur zaude erregistro hau ezabatu nahi duzula?");
        alert.setContentText(selected.getIzena() + " betirako ezabatuko da");

        ButtonType bai = new ButtonType("Bai", ButtonBar.ButtonData.OK_DONE);
        ButtonType ez = new ButtonType("Ez", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(bai, ez);

        if (alert.showAndWait().orElse(ez) == bai) {
            LangileaService.deleteLangile(selected.getId());
            ActionLogger.log(SessionContext.getCurrentUsername(), "DELETE", "langileak",
                    "Langilea ezabatua: " + selected.getIzena());
            taulaBirkargatu();
            formularioaGarbitu();
        }
    }

    private void alertaErakutsi(String mezua) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Abisua");
        alert.setHeaderText(null);
        alert.setContentText(mezua);
        alert.showAndWait();
    }

    private void arrakastaErakutsi(String mezua) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ondo");
        alert.setHeaderText(null);
        alert.setContentText(mezua);
        alert.showAndWait();
    }

    @FXML
    public void atzeraBueltatu(ActionEvent actionEvent) {
        try {
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            StageManager.switchStage(currentStage, "menu-view.fxml", "Menu Nagusia", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}