package Pantailak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class TxatController implements Initializable {

    @FXML private TextField txtInput;
    @FXML private Label lblErabiltzaile;
    @FXML private Label lblKontaktua;
    @FXML private Button btnBidali;
    @FXML private Button btnEmoji;
    @FXML private Button btnFitxategia;
    @FXML private Button btnGarbitu;
    @FXML private Button btnItxi;
    @FXML private ScrollPane scrollPane;
    @FXML
    VBox messagesContainer;

    private String erabiltzaileIzena = null;
    private Consumer<String> sendMessageCallback = null;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final String FILE_PREFIX = "[FILE]|";
    private static final long MAX_FILE_BYTES = 2L * 1024L * 1024L;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG: TxatController inicializado");

                configureScrollPane();

        applyDesignStyles();

        txtInput.setOnAction(e -> bidaliMezua());
        btnBidali.setOnAction(e -> bidaliMezua());
        btnEmoji.setOnAction(e -> showEmojiPicker());
        btnFitxategia.setOnAction(e -> aukeratuEtaBidaliFitxategia());
        btnGarbitu.setOnAction(e -> garbituTxata());
        btnItxi.setOnAction(e -> itxiTxata());
    }

    private void configureScrollPane() {
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);

                messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                scrollPane.setVvalue(1.0);             });
        });
    }

        public void initializeWithData(String username, List<String> sessionMessages,
                                   Consumer<String> sendCallback) {
        this.erabiltzaileIzena = username;
        this.sendMessageCallback = sendCallback;

        Platform.runLater(() -> {
            lblErabiltzaile.setText(username);
            lblKontaktua.setText("Konektatuta");
            lblKontaktua.setStyle(lblKontaktua.getStyle().replace("#95a5a6", "#1C5F2B"));

                        displayStyledMessages(sessionMessages);
            txtInput.requestFocus();
        });
    }

    private void displayStyledMessages(List<String> messages) {
                messagesContainer.getChildren().clear();

        for (String message : messages) {
            addStyledMessageToContainer(message);
        }

                Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
        });
    }

        public void addStyledMessageToContainer(String rawMessage) {
        Platform.runLater(() -> {
            try {
                if (rawMessage != null) {
                    String msg = rawMessage.trim();
                    if (msg.startsWith("FILECHUNK|") || msg.contains("|FILECHUNK|")) {
                        return;
                    }
                    if ((msg.startsWith("[SISTEMA]") || msg.startsWith("SISTEMA:")) && msg.toLowerCase().contains("ezezaguna") &&
                            (msg.toLowerCase().contains("irten da") || msg.toLowerCase().contains("sartu da"))) {
                        return;
                    }
                    if ((msg.startsWith("[SISTEMA]") || msg.startsWith("SISTEMA:")) &&
                            msg.toLowerCase().contains("fitxategia") && msg.toLowerCase().contains("bidalita")) {
                        return;
                    }
                }
                                MessageType messageType = determineMessageType(rawMessage);
                String sender = extractSender(rawMessage);
                String content = extractContent(rawMessage, messageType);
                String time = LocalDateTime.now().format(timeFormatter);

                                HBox messageContainer = new HBox();
                messageContainer.setPadding(new Insets(5, 10, 5, 10));
                messageContainer.setMaxWidth(Double.MAX_VALUE);

                                VBox messageBubble = new VBox();
                messageBubble.setPadding(new Insets(8, 12, 8, 12));
                messageBubble.setMaxWidth(400);
                messageBubble.setSpacing(2);

                boolean isFileMessage = messageType != MessageType.SYSTEM && isFileMessageContent(content);

                                switch (messageType) {
                    case SELF:
                                                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                        HBox.setHgrow(messageContainer, Priority.ALWAYS);

                        messageBubble.setStyle(
                                "-fx-background-color: #F3863A;" +                                         "-fx-background-radius: 18 18 4 18;" +                                         "-fx-effect: dropshadow(gaussian, rgba(243, 134, 58, 0.2), 5, 0, 0, 2);"
                        );

                        if (isFileMessage) {
                            FilePayload payload = parseFilePayload(content);
                            VBox fileBox = buildFileMessageBox(payload, true);
                            messageBubble.getChildren().add(fileBox);
                        } else {
                            Text selfContent = new Text(content);
                            selfContent.setFill(Color.WHITE);
                            selfContent.setFont(Font.font("Segoe UI", 13));
                            selfContent.setWrappingWidth(380);

                            messageBubble.getChildren().add(selfContent);
                        }

                        Text selfMeta = new Text("Zu • " + time);
                        selfMeta.setFill(Color.rgb(255, 255, 255, 0.8));
                        selfMeta.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));

                        messageBubble.getChildren().add(selfMeta);
                        break;

                    case OTHER:
                                                messageContainer.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(messageContainer, Priority.ALWAYS);

                        messageBubble.setStyle(
                                "-fx-background-color: #FFFFFF;" +                                         "-fx-background-radius: 18 18 18 4;" +                                         "-fx-border-color: #E0E0E0;" +
                                        "-fx-border-width: 1;" +
                                        "-fx-border-radius: 18 18 18 4;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 3, 0, 0, 1);"
                        );

                                                Text otherName = new Text(sender);
                        otherName.setFill(Color.web("#1D505B"));                         otherName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

                        if (isFileMessage) {
                            FilePayload payload = parseFilePayload(content);
                            VBox fileBox = buildFileMessageBox(payload, false);
                            messageBubble.getChildren().add(fileBox);
                        } else {
                            Text otherContent = new Text(content);
                            otherContent.setFill(Color.web("#2C3E50"));                         otherContent.setFont(Font.font("Segoe UI", 13));
                            otherContent.setWrappingWidth(380);
                            messageBubble.getChildren().add(otherContent);
                        }

                                                Text otherTime = new Text(" • " + time);
                        otherTime.setFill(Color.rgb(149, 165, 166, 0.8));                         otherTime.setFont(Font.font("Segoe UI", 10));

                                                TextFlow otherMeta = new TextFlow(otherName, otherTime);

                        messageBubble.getChildren().add(0, otherMeta);
                        break;

                    case SYSTEM:
                                                messageContainer.setAlignment(Pos.CENTER);
                        HBox.setHgrow(messageContainer, Priority.ALWAYS);

                        messageBubble.setStyle(
                                "-fx-background-color: #F5F5F5;" +                                         "-fx-background-radius: 12;" +                                         "-fx-border-color: #E0E0E0;" +
                                        "-fx-border-width: 1;" +
                                        "-fx-border-radius: 12;" +
                                        "-fx-padding: 6 12 6 12;" +
                                        "-fx-max-width: 350;"                         );

                                                Text systemContent = new Text(content);
                        systemContent.setFill(Color.web("#7F8C8D"));                         systemContent.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
                        systemContent.setStyle("-fx-font-style: italic;");
                        systemContent.setWrappingWidth(330);

                                                Text systemTime = new Text(time);
                        systemTime.setFill(Color.rgb(149, 165, 166, 0.6));
                        systemTime.setFont(Font.font("Segoe UI", 9));

                        VBox systemContentBox = new VBox(2);
                        systemContentBox.setAlignment(Pos.CENTER);
                        systemContentBox.getChildren().addAll(systemContent, systemTime);

                        messageBubble.getChildren().add(systemContentBox);
                        break;
                }

                                messageContainer.getChildren().add(messageBubble);

                                messagesContainer.getChildren().add(messageContainer);

                                Platform.runLater(() -> {
                    scrollPane.setVvalue(1.0);
                });

            } catch (Exception e) {
                System.err.println("Error añadiendo mensaje estilizado: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private MessageType determineMessageType(String message) {
        System.out.println("DEBUG: Determinando tipo de mensaje: " + message);

                                                
        String lowerMessage = message.toLowerCase();

                if (message.startsWith("SISTEMA:")) {
            System.out.println("DEBUG: Detectado como mensaje del sistema (formato SISTEMA:)");
            return MessageType.SYSTEM;
        }
        else if (message.startsWith("[SISTEMA]")) {
            return MessageType.SYSTEM;
        }
                else if (lowerMessage.contains(" sartu da") ||
                lowerMessage.contains(" atera egin da") ||
                lowerMessage.contains(" konektatu da") ||
                lowerMessage.contains(" deskonektatu da") ||
                lowerMessage.contains("sartu da") ||
                lowerMessage.contains("atera egin da")) {
            System.out.println("DEBUG: Detectado como mensaje del sistema (conexión)");
            return MessageType.SYSTEM;
        }
                else if (erabiltzaileIzena != null && message.startsWith(erabiltzaileIzena + ": ")) {
            System.out.println("DEBUG: Detectado como mensaje propio");
            return MessageType.SELF;
        }
                else {
            System.out.println("DEBUG: Detectado como mensaje de otro usuario");
            return MessageType.OTHER;
        }
    }

    private String extractSender(String message) {
                MessageType type = determineMessageType(message);
        if (type == MessageType.SYSTEM) {
            return "";         }
                else if (message.contains(": ")) {
            return message.substring(0, message.indexOf(": "));
        }
        return "Ezezaguna";
    }

    private String extractContent(String message, MessageType messageType) {
        if (messageType == MessageType.SYSTEM) {
                                                if (message.startsWith("SISTEMA:")) {
                return message.substring(8).trim();
            }
            if (message.startsWith("[SISTEMA]")) {
                return message.substring(8).trim();
            }
            return message;         } else if (message.contains(": ")) {
            return message.substring(message.indexOf(": ") + 2);
        }
        return message;
    }

        private String extractContent(String message) {
        MessageType type = determineMessageType(message);
        return extractContent(message, type);
    }

    private enum MessageType {
        SELF, OTHER, SYSTEM
    }

    private void applyDesignStyles() {
                lblErabiltzaile.setStyle(
                "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 13px; " +
                        "-fx-text-fill: #F3863A;" +
                        "-fx-font-weight: bold;"
        );

        lblKontaktua.setStyle(
                "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 11px; " +
                        "-fx-text-fill: #95a5a6;"
        );

                txtInput.setStyle(
                "-fx-background-color: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #2C3E50;" +
                        "-fx-border-color: #1B345D;" +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-padding: 10 14 10 14;"
        );

        btnEmoji.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-family: 'Segoe UI Emoji';" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #1D505B;" +
                        "-fx-border-color: #1B345D;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-cursor: hand;"
        );

        btnFitxategia.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-family: 'Segoe UI Emoji';" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #1D505B;" +
                        "-fx-border-color: #1B345D;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-cursor: hand;"
        );

                btnBidali.setStyle(
                "-fx-background-color: #F3863A;" +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 10 28 10 28; " +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(243, 134, 58, 0.3), 5, 0, 0, 1);"
        );

        btnGarbitu.setStyle(
                "-fx-background-color: #1D505B;" +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-weight: normal; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(29, 80, 91, 0.3), 5, 0, 0, 1);"
        );

        btnItxi.setStyle(
                "-fx-background-color: #5B1C1C;" +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-weight: normal; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(91, 28, 28, 0.3), 5, 0, 0, 1);"
        );

                setupHoverEffects();
    }

    private void setupHoverEffects() {
                btnBidali.setOnMouseEntered(e -> {
            btnBidali.setStyle(
                    "-fx-background-color: #E67E22;" +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 10 28 10 28; " +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(230, 126, 34, 0.4), 6, 0, 0, 2);"
            );
        });

        btnBidali.setOnMouseExited(e -> {
            btnBidali.setStyle(
                    "-fx-background-color: #F3863A;" +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 10 28 10 28; " +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(243, 134, 58, 0.3), 5, 0, 0, 1);"
            );
        });

        btnEmoji.setOnMouseEntered(e -> btnEmoji.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-family: 'Segoe UI Emoji';" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #1D505B;" +
                        "-fx-border-color: #F3863A;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-cursor: hand;"
        ));
        btnEmoji.setOnMouseExited(e -> btnEmoji.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-family: 'Segoe UI Emoji';" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #1D505B;" +
                        "-fx-border-color: #1B345D;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-cursor: hand;"
        ));

        btnFitxategia.setOnMouseEntered(e -> btnFitxategia.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-family: 'Segoe UI Emoji';" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #1D505B;" +
                        "-fx-border-color: #F3863A;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-cursor: hand;"
        ));
        btnFitxategia.setOnMouseExited(e -> btnFitxategia.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-family: 'Segoe UI Emoji';" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #1D505B;" +
                        "-fx-border-color: #1B345D;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-cursor: hand;"
        ));

                btnGarbitu.setOnMouseEntered(e -> {
            btnGarbitu.setStyle(
                    "-fx-background-color: #2C698D;" +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-weight: normal; " +
                            "-fx-font-size: 13px; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 8 20 8 20; " +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(44, 105, 141, 0.4), 6, 0, 0, 2);"
            );
        });

        btnGarbitu.setOnMouseExited(e -> {
            btnGarbitu.setStyle(
                    "-fx-background-color: #1D505B;" +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-weight: normal; " +
                            "-fx-font-size: 13px; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 8 20 8 20; " +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(29, 80, 91, 0.3), 5, 0, 0, 1);"
            );
        });

                btnItxi.setOnMouseEntered(e -> {
            btnItxi.setStyle(
                    "-fx-background-color: #7A2C2C;" +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-weight: normal; " +
                            "-fx-font-size: 13px; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 8 20 8 20; " +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(122, 44, 44, 0.4), 6, 0, 0, 2);"
            );
        });

        btnItxi.setOnMouseExited(e -> {
            btnItxi.setStyle(
                    "-fx-background-color: #5B1C1C;" +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-weight: normal; " +
                            "-fx-font-size: 13px; " +
                            "-fx-background-radius: 4; " +
                            "-fx-padding: 8 20 8 20; " +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(91, 28, 28, 0.3), 5, 0, 0, 1);"
            );
        });

                txtInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtInput.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-font-family: 'Segoe UI'; " +
                                "-fx-font-size: 14px; " +
                                "-fx-text-fill: #2C3E50;" +
                                "-fx-border-color: #F3863A;" +                                 "-fx-border-width: 2; " +
                                "-fx-border-radius: 4; " +
                                "-fx-padding: 10 14 10 14;" +
                                "-fx-effect: dropshadow(gaussian, rgba(243, 134, 58, 0.1), 3, 0, 0, 0);"
                );
            } else {
                txtInput.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-font-family: 'Segoe UI'; " +
                                "-fx-font-size: 14px; " +
                                "-fx-text-fill: #2C3E50;" +
                                "-fx-border-color: #1B345D;" +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 4; " +
                                "-fx-padding: 10 14 10 14;"
                );
            }
        });
    }

    private void showEmojiPicker() {
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);

        String[] emojis = new String[]{
                "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎",
                "🤔", "😢", "😡", "👍", "👎", "🙌", "🙏", "🎉",
                "❤️", "🔥", "✅", "❌"
        };

        for (String emoji : emojis) {
            MenuItem item = new MenuItem(emoji);
            item.setOnAction(e -> insertAtCaret(emoji));
            menu.getItems().add(item);
        }

        menu.show(btnEmoji, javafx.geometry.Side.TOP, 0, 0);
    }

    private void insertAtCaret(String text) {
        int caret = txtInput.getCaretPosition();
        String current = txtInput.getText();
        if (current == null) current = "";
        String next = current.substring(0, caret) + text + current.substring(caret);
        txtInput.setText(next);
        txtInput.positionCaret(caret + text.length());
        txtInput.requestFocus();
    }

    private void aukeratuEtaBidaliFitxategia() {
        Stage stage = (Stage) txtInput.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Fitxategia bidali");
        File selected = chooser.showOpenDialog(stage);
        if (selected == null) return;

        try {
            byte[] bytes = Files.readAllBytes(selected.toPath());
            if (bytes.length > MAX_FILE_BYTES) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Fitxategia handiegia");
                alert.setHeaderText("Fitxategia ezin da bidali");
                alert.setContentText("Gehienezko tamaina: " + formatBytes(MAX_FILE_BYTES));
                alert.showAndWait();
                return;
            }

            String mime = Files.probeContentType(selected.toPath());
            if (mime == null || mime.isBlank()) {
                mime = "application/octet-stream";
            }

            String nameEncoded = URLEncoder.encode(selected.getName(), StandardCharsets.UTF_8);
            String fileId = UUID.randomUUID().toString();
            int chunkSize = 8 * 1024;
            int totalChunks = (int) Math.ceil(bytes.length / (double) chunkSize);

            if (sendMessageCallback != null) {
                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(start + chunkSize, bytes.length);
                    byte[] chunk = java.util.Arrays.copyOfRange(bytes, start, end);
                    String chunkB64 = Base64.getEncoder().encodeToString(chunk);
                    int index = i + 1;
                    String payload = "FILECHUNK|" + fileId + "|" + nameEncoded + "|" + mime + "|" + index + "|" + totalChunks + "|" + chunkB64;
                    sendMessageCallback.accept(payload);
                }
            }

            String dataB64 = Base64.getEncoder().encodeToString(bytes);
            String fullPayload = FILE_PREFIX + nameEncoded + "|" + mime + "|" + bytes.length + "|" + dataB64;
            addStyledMessageToContainer(erabiltzaileIzena + ": " + fullPayload);

            txtInput.requestFocus();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errorea");
            alert.setHeaderText("Ezin izan da fitxategia bidali");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private boolean isFileMessageContent(String content) {
        return content != null && content.startsWith(FILE_PREFIX);
    }

    private FilePayload parseFilePayload(String content) {
        String raw = content.substring(FILE_PREFIX.length());
        String[] parts = raw.split("\\|", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Fitxategi formatua okerra da");
        }

        String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
        String mime = parts[1];
        long size = Long.parseLong(parts[2]);
        String dataB64 = parts[3];
        return new FilePayload(name, mime, size, dataB64);
    }

    private VBox buildFileMessageBox(FilePayload payload, boolean isSelf) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📎 " + payload.name);
        title.setWrapText(true);
        title.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + (isSelf ? "#FFFFFF" : "#1D505B") + ";"
        );

        Label meta = new Label(formatBytes(payload.size) + " • " + payload.mime);
        meta.setWrapText(true);
        meta.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 11px;" +
                        "-fx-text-fill: " + (isSelf ? "rgba(255,255,255,0.85)" : "rgba(149,165,166,0.9)") + ";"
        );

        Button btnGorde = new Button("Gorde");
        btnGorde.setStyle(
                "-fx-background-color: " + (isSelf ? "rgba(255,255,255,0.18)" : "#F5F5F5") + ";" +
                        "-fx-text-fill: " + (isSelf ? "#FFFFFF" : "#1D505B") + ";" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;"
        );
        btnGorde.setOnAction(e -> saveFile(payload));

        Button btnIreki = new Button("Ireki");
        btnIreki.setStyle(
                "-fx-background-color: " + (isSelf ? "rgba(255,255,255,0.18)" : "#F5F5F5") + ";" +
                        "-fx-text-fill: " + (isSelf ? "#FFFFFF" : "#1D505B") + ";" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;"
        );
        btnIreki.setOnAction(e -> openFile(payload));

        HBox actions = new HBox(8, btnGorde, btnIreki);
        actions.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, meta, actions);
        return box;
    }

    private void saveFile(FilePayload payload) {
        Stage stage = (Stage) txtInput.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Fitxategia gorde");
        chooser.setInitialFileName(payload.name);
        File dest = chooser.showSaveDialog(stage);
        if (dest == null) return;

        try {
            byte[] bytes = Base64.getDecoder().decode(payload.dataB64);
            Files.write(dest.toPath(), bytes);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errorea");
            alert.setHeaderText("Ezin izan da fitxategia gorde");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void openFile(FilePayload payload) {
        try {
            byte[] bytes = Base64.getDecoder().decode(payload.dataB64);
            String safeName = payload.name.replaceAll("[\\\\/:*?\"<>|]+", "_");
            File temp = File.createTempFile("osis-chat-", "-" + safeName);
            Files.write(temp.toPath(), bytes);
            temp.deleteOnExit();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(temp);
            }
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errorea");
            alert.setHeaderText("Ezin izan da fitxategia ireki");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(java.util.Locale.ROOT, "%.1f KB", kb);
        double mb = kb / 1024.0;
        return String.format(java.util.Locale.ROOT, "%.1f MB", mb);
    }

    private static final class FilePayload {
        private final String name;
        private final String mime;
        private final long size;
        private final String dataB64;

        private FilePayload(String name, String mime, long size, String dataB64) {
            this.name = name;
            this.mime = mime;
            this.size = size;
            this.dataB64 = dataB64;
        }
    }

    @FXML
    private void bidaliMezua() {
        String mezua = txtInput.getText().trim();
        if (mezua.isEmpty() || erabiltzaileIzena == null) return;

        String displayMessage = erabiltzaileIzena + ": " + mezua;
        addStyledMessageToContainer(displayMessage);

                if (sendMessageCallback != null) {
            sendMessageCallback.accept(mezua);
        }

                txtInput.clear();
        txtInput.requestFocus();
    }

    @FXML
    private void garbituTxata() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Txata garbitu");
        alert.setHeaderText("Ziur zaude txat-historia garbitu nahi duzula?");
        alert.setContentText("Ekintza hau ezin da desegin.");

                DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-border-color: #C19A6B;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 5;"
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                messagesContainer.getChildren().clear();
                                StageManager.clearSessionMessages();
            }
        });
    }

    @FXML
    private void itxiTxata() {
        Stage stage = (Stage) txtInput.getScene().getWindow();
        stage.close();
    }

    public void updateConnectionStatus(boolean connected, String text) {
        Platform.runLater(() -> {
            if (lblKontaktua == null) return;
            lblKontaktua.setText(text != null ? text : (connected ? "Konektatuta" : "Deskonektatuta"));
            String color = connected ? "#1C5F2B" : "#5B1C1C";
            lblKontaktua.setStyle(lblKontaktua.getStyle().replace("#95a5a6", color));
        });
    }
}
