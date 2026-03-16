package Pantailak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Klaseak.Erabiltzailea;
import Klaseak.Langilea;
import Klaseak.Lanpostua;
import services.ActionLogger;
import services.ErabiltzaileaService;
import services.LangileaService;
import services.SessionContext;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class LangileakController {

    @FXML
    private TableView<Langilea> tableLangileak;
    @FXML
    private TableColumn<Langilea, Integer> colId;
    @FXML
    private TableColumn<Langilea, String> colIzena;
    @FXML
    private TableColumn<Langilea, String> colAbizena1;
    @FXML
    private TableColumn<Langilea, String> colAbizena2;
    @FXML
    private TableColumn<Langilea, String> colTelefonoa;
    @FXML
    private TableColumn<Langilea, String> colLanpostua;

    @FXML
    private TextField txtIzena, txtAbizena1, txtAbizena2, txtTelefonoa;
    @FXML
    private ComboBox<Lanpostua> comboLanpostu;
    @FXML
    private CheckBox checkErabiltzaile;
    @FXML
    private VBox boxErabiltzaile;
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPass;
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
    private Erabiltzailea loadedErabiltzailea;

    @FXML
    public void initialize() {
        System.out.println("initialize() deituta");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colAbizena1.setCellValueFactory(new PropertyValueFactory<>("abizena1"));
        colAbizena2.setCellValueFactory(new PropertyValueFactory<>("abizena2"));
        colTelefonoa.setCellValueFactory(new PropertyValueFactory<>("telefonoa"));
        colLanpostua.setCellValueFactory(new PropertyValueFactory<>("lanpostuaName"));

        comboLanpostu.getItems().setAll(LangileaService.getLanpostuak());

        formularioaKonfiguratu();

        filtroakKonfiguratu();

        taulaBirkargatu();

        bilaketaKonfiguratu();

        botoiakKonfiguratu();

        taulaAukera();

        formularioaGarbitu();
    }

    private void formularioaKonfiguratu() {
        checkErabiltzaile.selectedProperty().addListener((obs, old, val) -> {
            boxErabiltzaile.setVisible(val);
            boxErabiltzaile.setManaged(val);
        });

        btnSave.setOnAction(e -> langileaGorde());
        btnCancel.setOnAction(e -> formularioaGarbitu());
    }

    private void filtroakKonfiguratu() {
        lanpostuFilter.getItems().addAll("Guztiak", "Sukaldaria", "Zerbitzaria", "Admin");
        lanpostuFilter.setValue("Guztiak");

        ordenatuFilter.getItems().addAll("ID", "Izena", "Abizena", "Lanpostua");
        ordenatuFilter.setValue("ID");

        lanpostuFilter.setOnAction(e -> filtroakAplikatu());
        ordenatuFilter.setOnAction(e -> ordenaAplikatu());
    }

    private void bilaketaKonfiguratu() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filtroakAplikatu();
            });
        }
    }

    private void botoiakKonfiguratu() {
        btnAdd.setOnAction(e -> {
            formularioaGarbitu();
            langileaEditatzen = null;
            loadedErabiltzailea = null;
        });

        /*
        btnEdit.setOnAction(e -> {
            Langilea selected = tableLangileak.getSelectionModel().getSelectedItem();
            if (selected != null) {
                kargatuFormularioa(selected);
            } else {
                mostrarAlerta("Aukeratu langile bat editatzeko.");
            }
        });*/

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
        txtAbizena1.setText(langilea.getAbizena1());
        txtAbizena2.setText(langilea.getAbizena2());
        txtTelefonoa.setText(langilea.getTelefonoa());
        comboLanpostu.setValue(langilea.getLanpostua());

        loadedErabiltzailea = ErabiltzaileaService.getByLangile(langilea.getId());

        if (loadedErabiltzailea != null) {
            checkErabiltzaile.setSelected(true);
            txtUser.setText(loadedErabiltzailea.getIzena());
            txtPass.setText(loadedErabiltzailea.getPasahitza());
        } else {
            checkErabiltzaile.setSelected(false);
            txtUser.clear();
            txtPass.clear();
        }
    }

    private void formularioaGarbitu() {
        langileaEditatzen = null;
        loadedErabiltzailea = null;

        txtIzena.clear();
        txtAbizena1.clear();
        txtAbizena2.clear();
        txtTelefonoa.clear();
        comboLanpostu.getSelectionModel().clearSelection();
        checkErabiltzaile.setSelected(false);
        txtUser.clear();
        txtPass.clear();

        tableLangileak.getSelectionModel().clearSelection();
    }

    private void langileaGorde() {
        if (txtIzena.getText().isBlank()) {
            alertaErakutsi("Izena jarri behar da.");
            return;
        }

        if (txtTelefonoa.getText().isBlank()) {
            alertaErakutsi("Telefonoa jarri behar da.");
            return;
        }

        String telefono = txtTelefonoa.getText();
        if (!telefono.matches("\\d{9,}")) {
            alertaErakutsi("Telefonoak zenbakiak bakarrik izan behar ditu eta gutxienez 9 digitu.");
            return;
        }

        if (comboLanpostu.getValue() == null) {
            alertaErakutsi("Lanpostu bat aukeratu behar da.");
            return;
        }

        if (checkErabiltzaile.isSelected()) {
            if (txtUser.getText().isBlank()) {
                alertaErakutsi("Erabiltzaile izena jarri behar da.");
                return;
            }

            if (txtPass.getText().isBlank()) {
                alertaErakutsi("Pasahitza jarri behar da.");
                return;
            }
        }

        Langilea langilea = (langileaEditatzen == null ? new Langilea() : langileaEditatzen);

        langilea.setIzena(txtIzena.getText());
        langilea.setAbizena1(txtAbizena1.getText());
        langilea.setAbizena2(txtAbizena2.getText());
        langilea.setTelefonoa(txtTelefonoa.getText());
        langilea.setLanpostua(comboLanpostu.getValue());

        String izenaLog = txtIzena.getText();
        String abizenaLog = txtAbizena1.getText();

        if (langileaEditatzen == null) {
            langilea = LangileaService.create(langilea);

            ActionLogger.log(
                    SessionContext.getCurrentUser(),
                    "INSERT",
                    "langileak",
                    "Langilea sortu: " + izenaLog + " " + abizenaLog
            );

        } else {
            LangileaService.update(langilea);

            ActionLogger.log(
                    SessionContext.getCurrentUser(),
                    "UPDATE",
                    "langileak",
                    "Langilea eguneratu (ID=" + langilea.getId() + ")"
            );
        }


        if (checkErabiltzaile.isSelected()) {
            Erabiltzailea erabiltzailea = loadedErabiltzailea;

            if (erabiltzailea == null) {
                erabiltzailea = new Erabiltzailea();
                erabiltzailea.setLangilea(langilea);
            }

            erabiltzailea.setIzena(txtUser.getText());
            erabiltzailea.setPasahitza(txtPass.getText());

            ErabiltzaileaService.saveOrUpdate(erabiltzailea);
            ActionLogger.log(
                    erabiltzailea.getIzena(),
                    loadedErabiltzailea == null ? "INSERT" : "UPDATE",
                    "erabiltzaileak",
                    "Erabiltzailea lotu langileari (Langile ID=" + langilea.getId() + ")"
            );

        } else {
            if (loadedErabiltzailea != null) {
                ErabiltzaileaService.delete(loadedErabiltzailea.getId());
                ActionLogger.log(
                        loadedErabiltzailea.getIzena(),
                        "DELETE",
                        "erabiltzaileak",
                        "Erabiltzailea ezabatua (ID=" + loadedErabiltzailea.getId() + ")"
                );

            }
        }

        taulaBirkargatu();
        formularioaGarbitu();

        arrakastaErakutsi("Langilea ondo gorde da.");
    }

    private void filtroakAplikatu() {
        if (filteredData == null) return;

        String searchText = searchField.getText().toLowerCase();
        String selectedLangile = lanpostuFilter.getValue();

        filteredData.setPredicate(langilea -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    langilea.getIzena().toLowerCase().contains(searchText) ||
                    langilea.getAbizena1().toLowerCase().contains(searchText) ||
                    langilea.getAbizena2().toLowerCase().contains(searchText) ||
                    langilea.getTelefonoa().toLowerCase().contains(searchText);

            boolean matchesLanpostu = selectedLangile.equals("Guztiak") ||
                    langilea.getLanpostuaName().equals(selectedLangile);

            return matchesSearch && matchesLanpostu;
        });

        langileKopuruaLabel.setText(filteredData.size() + " langile");
    }

    private void ordenaAplikatu() {
        if (filteredData == null) return;

        String orden = ordenatuFilter.getValue();

        Comparator<Langilea> comparator = switch (orden) {
            case "Izena" -> Comparator.comparing(Langilea::getIzena);
            case "Abizena" -> Comparator.comparing(Langilea::getAbizena1);
            case "Lanpostua" -> Comparator.comparing(Langilea::getLanpostuaName);
            default -> Comparator.comparing(Langilea::getId);
        };

        SortedList<Langilea> sortedData = new SortedList<>(filteredData);
        sortedData.setComparator(comparator);
        tableLangileak.setItems(sortedData);
    }

    private void taulaBirkargatu() {
        try {
            List<Langilea> langileak = LangileaService.getAll();

            if (langileak != null) {
                langileakLista = FXCollections.observableArrayList(langileak);
                filteredData = new FilteredList<>(langileakLista);

                ordenaAplikatu();

                System.out.println("Taula birkargatu da " + langileak.size() + " erregistrorekin");
            } else {
                langileakLista = FXCollections.observableArrayList();
                filteredData = new FilteredList<>(langileakLista);
                tableLangileak.setItems(filteredData);
                System.err.println("WARNING: LangileaService.getAll() null bueltatu du");
            }

            kopuruakEguneratu();

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

        for (Langilea langilea : langileakLista) {
            String lanpostu = langilea.getLanpostuaName();
            if (lanpostu.contains("Sukaldari") || lanpostu.contains("sukaldari")) {
                sukaldariak++;
            } else if (lanpostu.contains("Zerbitzari") || lanpostu.contains("zerbitzari")) {
                zerbitzariak++;
            } else if (lanpostu.contains("Admin") || lanpostu.contains("admin")) {
                admin++;
            }
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
        alert.setContentText(selected.getIzena() + " " + selected.getAbizena1() + " betirako ezabatuko da");

        ButtonType bai = new ButtonType("Bai", ButtonBar.ButtonData.OK_DONE);
        ButtonType ez = new ButtonType("Ez", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(bai, ez);

        var result = alert.showAndWait();

        if (result.isPresent() && result.get() == bai) {
            LangileaService.deleteLangile(selected.getId());
            ActionLogger.log(
                    SessionContext.getCurrentUser(),
                    "DELETE",
                    "langileak",
                    "Langilea ezabatua: " + selected.getIzena() + " " + selected.getAbizena1()
            );

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