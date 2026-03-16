package Pantailak;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StageManager {

    
    private static final Image APP_ICON =
            new Image(StageManager.class.getResourceAsStream("/icons/app_icon.png"));

    
    private static Image CHAT_ICON = loadChatIcon();

    private static final String APP_CSS =
            StageManager.class.getResource("/css/osis-suite.css").toExternalForm();

    
    private static final String COLOR_FUEGO = "#F3863A";
    private static final String COLOR_OZEANOA = "#1D505B";
    private static final String COLOR_BEIGE = "#C19A6B";
    private static final String COLOR_ZURIA = "#F5F5F5";
    private static final String COLOR_GORRIA = "#5B1C1C";

    
    private static Stage floatingStage = null;
    private static StackPane mainContainer = null;
    private static ImageView chatIconView = null;
    private static Circle notificationBadge = null;
    private static Label notificationCount = null;
    private static String erabiltzaileIzena = null;
    private static Stage chatWindow = null;
    private static TxatController currentChatController = null; 
    private static List<String> unreadMessages = new ArrayList<>();
    private static List<String> sessionMessages = new ArrayList<>();
    private static Socket chatSocket = null;
    private static BufferedReader chatReader = null;
    private static PrintWriter chatWriter = null;
    private static boolean isChatServerConnected = false;
    private static boolean isFirstConnection = true;

    
    private static boolean isDragging = false;
    private static double dragStartX, dragStartY;
    private static final double DRAG_THRESHOLD = 5.0;

    private StageManager() {}

    

    private static Image loadChatIcon() {
        try {
            
            Image svgIcon = new Image(StageManager.class.getResourceAsStream("/icons/CHAT.svg"));
            if (!svgIcon.isError()) {
                System.out.println("DEBUG: SVG kargatuta");
                return svgIcon;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Errorea SVG kargatzean: " + e.getMessage());
        }

        try {
            
            Image pngIcon = new Image(StageManager.class.getResourceAsStream("/icons/chat_icon.png"));
            if (!pngIcon.isError()) {
                System.out.println("DEBUG: PNG kargatuta");
                return pngIcon;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Errorea PNG kargatzean: " + e.getMessage());
        }

        
        System.out.println("DEBUG: Ezin izan da ikonoa kargatu, ez dago eskuragarri, emotikonoa kargatuko da ordezkatzeko.");
        return null;
    }

    

    public static void switchToLogin(Stage currentStage) throws IOException {
        hideFloatingChatButton();
        disconnectChatServer();
        sessionMessages.clear();
        unreadMessages.clear();
        currentChatController = null; 
        isFirstConnection = true;
        switchStage(currentStage, "login-view.fxml", "Saioa Hasi", false);
    }

    public static void switchStage(Stage currentStage, String fxml, String title, boolean maximized)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxml));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.getIcons().add(APP_ICON);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(APP_CSS);
        newStage.setScene(scene);

        if (maximized) {
            newStage.setMaximized(true);
        } else {
            newStage.centerOnScreen();
        }

        newStage.setOnCloseRequest(e -> {
            hideFloatingChatButton();
            disconnectChatServer();
            Platform.exit();
            System.exit(0);
        });

        currentStage.close();
        newStage.show();

        if (erabiltzaileIzena != null) {
            updateFloatingButtonPosition();
        }
    }

    public static Stage openStage(String fxml, String title, boolean maximized, int width, int height)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxml));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.getIcons().add(APP_ICON);

        Scene scene;
        if (maximized) {
            scene = new Scene(root);
            stage.setMaximized(true);
        } else {
            scene = new Scene(root, width, height);
        }

        scene.getStylesheets().add(APP_CSS);
        stage.setScene(scene);
        stage.centerOnScreen();

        return stage;
    }

    

    public static void showFloatingChatButton(String username) {
        erabiltzaileIzena = username;

        Platform.runLater(() -> {
            try {
                if (floatingStage != null && floatingStage.isShowing()) {
                    updateFloatingButtonPosition();
                    return;
                }

                createFloatingButton();
                connectToChatServer();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }

    public static void hideFloatingChatButton() {
        Platform.runLater(() -> {
            if (floatingStage != null) {
                floatingStage.hide();
            }
        });
    }

    public static void showFloatingChatButtonIfHidden() {
        if (floatingStage != null && !floatingStage.isShowing()) {
            Platform.runLater(() -> {
                floatingStage.show();
                updateFloatingButtonPosition();
            });
        }
    }

    private static void updateFloatingButtonPosition() {
        if (floatingStage != null && floatingStage.isShowing()) {
            Platform.runLater(() -> {
                try {
                    double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
                    floatingStage.setX((screenWidth - floatingStage.getWidth()) / 2);
                    floatingStage.setY(20);
                } catch (Exception e) {
                    System.err.println("Errorea kokapena eguneratzean: " + e.getMessage());
                }
            });
        }
    }

    

    private static void connectToChatServer() {
        new Thread(() -> {
            try {
                chatSocket = new Socket("192.168.2.101", 5555);
                chatReader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
                chatWriter = new PrintWriter(chatSocket.getOutputStream(), true);
                isChatServerConnected = true;

                
                chatWriter.println(erabiltzaileIzena);
                System.out.println("DEBUG: Erabiltzailea bidalita: " + erabiltzaileIzena);

                
                loadSessionMessages();

                
                if (isFirstConnection) {
                    String welcomeMessage = "SISTEMA: " + erabiltzaileIzena + " konektatu da";
                    
                    saveMessageToSession(welcomeMessage);
                    isFirstConnection = false;

                    
                    if (currentChatController != null) {
                        Platform.runLater(() -> {
                            currentChatController.addStyledMessageToContainer(welcomeMessage);
                        });
                    }
                }

                listenToChatServer();

            } catch (IOException e) {
                System.err.println("Errorea zerbitzarira konektaztean: " + e.getMessage());
                isChatServerConnected = false;

                
                String errorMessage = "SISTEMA: Ezin da zerbitzarira konektatu";
                saveMessageToSession(errorMessage);

                
                if (currentChatController != null) {
                    Platform.runLater(() -> {
                        currentChatController.addStyledMessageToContainer(errorMessage);
                    });
                }
            }
        }).start();
    }

    
    private static void listenToChatServer() {
        try {
            String message;
            while (isChatServerConnected && chatSocket != null && chatSocket.isConnected() &&
                    (message = chatReader.readLine()) != null) {

                final String finalMessage = message;
                System.out.println("DEBUG: Mezua jasota: " + finalMessage);

                Platform.runLater(() -> {
                    
                    saveMessageToSession(finalMessage);

                    
                    
                    boolean isSystemMessage = finalMessage.toLowerCase().contains(" sartu da") ||
                            finalMessage.toLowerCase().contains(" atera egin da") ||
                            finalMessage.toLowerCase().contains(" konektatu da") ||
                            finalMessage.toLowerCase().contains(" deskonektatu da");

                    
                    boolean isOwnMessage = finalMessage.startsWith(erabiltzaileIzena + ": ");

                    
                    if (!isOwnMessage && !isSystemMessage) {
                        if (chatWindow == null || !chatWindow.isShowing()) {
                            addUnreadMessage(finalMessage);
                        }
                    }

                    
                    if (currentChatController != null) {
                        currentChatController.addStyledMessageToContainer(finalMessage);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("DEBUG: Zerbitzariarekin konexioa itxita: " + e.getMessage());
        } finally {
            isChatServerConnected = false;
        }
    }
    private static void disconnectChatServer() {
        isChatServerConnected = false;
        try {
            if (chatWriter != null && chatSocket != null && chatSocket.isConnected()) {
                
                
            }
            if (chatWriter != null) chatWriter.close();
            if (chatReader != null) chatReader.close();
            if (chatSocket != null) chatSocket.close();
        } catch (IOException e) {
            
        }
        saveSessionToFile();
        currentChatController = null; 
    }

    

    public static void sendChatMessage(String message) {
        if (chatWriter != null && isChatServerConnected) {
            
            
            System.out.println("DEBUG: Mezua bidaltzen: " + message);
            chatWriter.println(message);
        } else {
            
            String errorMessage = message + " (ezin bidali - ez dago konexiorik)";
            saveMessageToSession(errorMessage);

            
            if (currentChatController != null) {
                Platform.runLater(() -> {
                    currentChatController.addStyledMessageToContainer(errorMessage);
                });
            }
        }
    }

    public static void sendSystemMessage(String message) {
        String systemMessage = "SISTEMA: " + message;
        saveMessageToSession(systemMessage);

        Platform.runLater(() -> {
            if (currentChatController != null) {
                currentChatController.addStyledMessageToContainer(systemMessage);
            }
        });
    }

    public static List<String> getSessionMessages() {
        return new ArrayList<>(sessionMessages);
    }

    public static void clearSessionMessages() {
        sessionMessages.clear();
        File sessionFile = new File(getSessionFilePath());
        if (sessionFile.exists()) {
            sessionFile.delete();
        }

        
        if (currentChatController != null) {
            Platform.runLater(() -> {
                currentChatController.messagesContainer.getChildren().clear();
            });
        }
    }

    private static void saveMessageToSession(String message) {
        
        if (!sessionMessages.isEmpty()) {
            String lastMessage = sessionMessages.get(sessionMessages.size() - 1);
            if (lastMessage.equals(message)) {
                return;
            }
        }
        sessionMessages.add(message);
        if (sessionMessages.size() > 1000) {
            sessionMessages.remove(0);
        }
        saveSessionToFile();
    }

    private static void loadSessionMessages() {
        File sessionFile = new File(getSessionFilePath());
        if (sessionFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(sessionFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sessionMessages.add(line);
                }
                System.out.println("DEBUG: " + sessionMessages.size() + " mezu kargatuta sesio fitxategitik");
            } catch (IOException e) {
                System.err.println("Errorea sesioko mezuak kargatzean: " + e.getMessage());
            }
        }
    }

    private static void saveSessionToFile() {
        if (!sessionMessages.isEmpty()) {
            File sessionFile = new File(getSessionFilePath());
            try (PrintWriter writer = new PrintWriter(new FileWriter(sessionFile))) {
                for (String message : sessionMessages) {
                    writer.println(message);
                }
            } catch (IOException e) {
                System.err.println("Errorea mezuak gordetzean: " + e.getMessage());
            }
        }
    }

    private static String getSessionFilePath() {
        String tempDir = System.getProperty("java.io.tmpdir");
        String safeUsername = erabiltzaileIzena != null ?
                erabiltzaileIzena.replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
        return tempDir + "osis_chat_" + safeUsername + ".session";
    }

    

    private static void createFloatingButton() {
        try {
            
            mainContainer = new StackPane();
            mainContainer.setPickOnBounds(false);
            mainContainer.setStyle("-fx-background-color: transparent;");
            mainContainer.setPrefSize(70, 70);
            mainContainer.setMaxSize(70, 70);
            mainContainer.setMinSize(70, 70);

            
            StackPane buttonCircle = new StackPane();
            buttonCircle.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 30;" +
                            "-fx-border-color: " + COLOR_FUEGO + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 30;" +
                            "-fx-pref-width: 60;" +
                            "-fx-pref-height: 60;" +
                            "-fx-min-width: 60;" +
                            "-fx-min-height: 60;" +
                            "-fx-max-width: 60;" +
                            "-fx-max-height: 60;"
            );

            
            if (CHAT_ICON != null && !CHAT_ICON.isError()) {
                chatIconView = new ImageView(CHAT_ICON);
                chatIconView.setFitWidth(30);
                chatIconView.setFitHeight(30);
                chatIconView.setPreserveRatio(true);
                chatIconView.setSmooth(true);

                if (CHAT_ICON.getUrl() != null && CHAT_ICON.getUrl().toLowerCase().endsWith(".svg")) {
                    chatIconView.setStyle(
                            "-fx-effect: dropshadow(gaussian, " + COLOR_FUEGO + ", 1, 0.5, 0, 0);"
                    );
                }

                buttonCircle.getChildren().add(chatIconView);
            } else {
                Label fallbackLabel = new Label("💬");
                fallbackLabel.setStyle(
                        "-fx-text-fill: " + COLOR_FUEGO + ";" +
                                "-fx-font-size: 26px;" +
                                "-fx-font-family: 'Segoe UI';"
                );
                buttonCircle.getChildren().add(fallbackLabel);
            }

            
            StackPane notificationContainer = new StackPane();
            notificationContainer.setStyle("-fx-background-color: transparent;");
            notificationContainer.setPrefSize(24, 24);
            notificationContainer.setMaxSize(24, 24);
            notificationContainer.setMinSize(24, 24);

            
            notificationBadge = new Circle(10);
            notificationBadge.setFill(Color.web(COLOR_GORRIA));
            notificationBadge.setStroke(Color.WHITE);
            notificationBadge.setStrokeWidth(2);
            notificationBadge.setVisible(false);

            
            notificationCount = new Label();
            notificationCount.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 10px;" +
                            "-fx-padding: 0;" +
                            "-fx-alignment: center;"
            );
            notificationCount.setVisible(false);

            
            notificationContainer.getChildren().addAll(notificationBadge, notificationCount);
            StackPane.setAlignment(notificationBadge, Pos.CENTER);
            StackPane.setAlignment(notificationCount, Pos.CENTER);

            
            mainContainer.getChildren().addAll(buttonCircle, notificationContainer);

            
            StackPane.setAlignment(notificationContainer, Pos.TOP_RIGHT);
            StackPane.setMargin(notificationContainer, new Insets(-1, -1, 0, 0));

            
            setupMouseEvents(mainContainer, buttonCircle, notificationContainer);

            
            Tooltip tooltip = new Tooltip("Klik: Ireki txata\nArrastratu: Mugitu");
            tooltip.setStyle(
                    "-fx-background-color: " + COLOR_OZEANOA + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-size: 11px;"
            );
            tooltip.setShowDelay(Duration.millis(300));
            Tooltip.install(mainContainer, tooltip);

            
            floatingStage = new Stage();
            floatingStage.initStyle(StageStyle.TRANSPARENT);
            floatingStage.setAlwaysOnTop(true);
            floatingStage.setResizable(false);
            floatingStage.setWidth(75);
            floatingStage.setHeight(75);

            Scene scene = new Scene(mainContainer);
            scene.setFill(null);

            floatingStage.setScene(scene);

            
            updateFloatingButtonPosition();

            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            
            floatingStage.show();
            fadeIn.play();

            
            updateNotificationBadge();

        } catch (Exception e) {
            System.err.println("Errorea botoia sortzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupMouseEvents(StackPane container, StackPane buttonCircle, StackPane notificationContainer) {
        container.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            dragStartX = event.getScreenX();
            dragStartY = event.getScreenY();
            isDragging = false;
            applyPressedStyle(buttonCircle);
        });

        container.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double deltaX = Math.abs(event.getScreenX() - dragStartX);
            double deltaY = Math.abs(event.getScreenY() - dragStartY);

            if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                isDragging = true;
                floatingStage.setX(event.getScreenX() - (floatingStage.getWidth() / 2));
                floatingStage.setY(event.getScreenY() - (floatingStage.getHeight() / 2));
                container.setCursor(javafx.scene.Cursor.MOVE);
                applyDraggingStyle(buttonCircle);
            }

            event.consume();
        });

        container.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            applyNormalStyle(buttonCircle);
            container.setCursor(javafx.scene.Cursor.HAND);

            if (!isDragging) {
                openChatWindow();
            }

            isDragging = false;
            adjustToSafePosition();
            event.consume();
        });

        container.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!isDragging) {
                applyHoverStyle(buttonCircle);
                container.setCursor(javafx.scene.Cursor.HAND);
            }
        });

        container.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (!isDragging) {
                applyNormalStyle(buttonCircle);
                container.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        container.setCursor(javafx.scene.Cursor.HAND);
    }

    private static void applyNormalStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: " + COLOR_FUEGO + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;"
        );
    }

    private static void applyHoverStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: rgba(243, 134, 58, 0.1);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: " + COLOR_FUEGO + ";" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;" +
                        "-fx-effect: dropshadow(gaussian, rgba(243, 134, 58, 0.5), 8, 0, 0, 0);"
        );
    }

    private static void applyPressedStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: rgba(243, 134, 58, 0.2);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #E67E22;" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;"
        );
    }

    private static void applyDraggingStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: rgba(243, 134, 58, 0.15);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #E67E22;" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;" +
                        "-fx-effect: dropshadow(gaussian, rgba(231, 126, 34, 0.6), 10, 0, 0, 0);"
        );
    }

    

    public static void openChatWindow() {
        Platform.runLater(() -> {
            try {
                hideFloatingChatButton();

                if (chatWindow != null && chatWindow.isShowing()) {
                    chatWindow.requestFocus();
                    chatWindow.toFront();
                    markAllMessagesAsRead();
                    return;
                }

                FXMLLoader loader = new FXMLLoader(StageManager.class.getResource("txat-view.fxml"));
                Parent root = loader.load();

                TxatController controller = loader.getController();
                currentChatController = controller; 

                controller.initializeWithData(
                        erabiltzaileIzena,
                        getSessionMessages(),
                        message -> sendChatMessage(message)
                );

                chatWindow = new Stage();
                chatWindow.setTitle("OSIS Txat - " + erabiltzaileIzena);
                chatWindow.getIcons().add(APP_ICON);

                Scene scene = new Scene(root);
                scene.getStylesheets().add(APP_CSS);
                chatWindow.setScene(scene);

                chatWindow.setMinWidth(550);
                chatWindow.setMinHeight(450);
                chatWindow.setWidth(600);
                chatWindow.setHeight(500);

                chatWindow.centerOnScreen();

                chatWindow.setOnHiding(e -> {
                    currentChatController = null; 
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            Platform.runLater(() -> {
                                showFloatingChatButtonIfHidden();
                            });
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });

                chatWindow.setOnCloseRequest(e -> {
                    currentChatController = null; 
                    chatWindow = null;
                });

                chatWindow.show();
                markAllMessagesAsRead();

            } catch (Exception e) {
                System.err.println("Errorea txata irekitzen: " + e.getMessage());
                e.printStackTrace();
                showFloatingChatButtonIfHidden();
            }
        });
    }

    

    public static void addUnreadMessage(String message) {
        
        boolean isOwnMessage = message.startsWith(erabiltzaileIzena + ": ");
        boolean isOwnSystemMessage = message.startsWith("SISTEMA: " + erabiltzaileIzena);

        if (!isOwnMessage && !isOwnSystemMessage) {
            unreadMessages.add(message);
            updateNotificationBadge();

            if (chatWindow == null || !chatWindow.isShowing()) {
                animateNotification();
            }
        }
    }

    public static void markAllMessagesAsRead() {
        unreadMessages.clear();
        updateNotificationBadge();
    }

    private static void updateNotificationBadge() {
        Platform.runLater(() -> {
            int count = unreadMessages.size();
            boolean hasNotifications = count > 0;

            if (notificationBadge != null && notificationCount != null) {
                notificationBadge.setVisible(hasNotifications);
                notificationCount.setVisible(hasNotifications);

                if (hasNotifications) {
                    String text = count > 9 ? "9+" : String.valueOf(count);
                    notificationCount.setText(text);

                    if (count > 9) {
                        notificationBadge.setRadius(11);
                        notificationCount.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-family: 'Segoe UI';" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 9px;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        );
                    } else if (count > 1) {
                        notificationBadge.setRadius(10);
                        notificationCount.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-family: 'Segoe UI';" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 10px;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        );
                    } else {
                        notificationBadge.setRadius(10);
                        notificationCount.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-family: 'Segoe UI';" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        );
                    }
                }
            }
        });
    }

    private static void animateNotification() {
        if (notificationBadge != null) {
            Platform.runLater(() -> {
                ScaleTransition pulse1 = new ScaleTransition(Duration.millis(100), notificationBadge);
                pulse1.setToX(1.3);
                pulse1.setToY(1.3);

                ScaleTransition pulse2 = new ScaleTransition(Duration.millis(100), notificationBadge);
                pulse2.setToX(1.0);
                pulse2.setToY(1.0);

                SequentialTransition pulse = new SequentialTransition(pulse1, pulse2);
                pulse.setCycleCount(1);
                pulse.play();
            });
        }
    }

    private static void adjustToSafePosition() {
        if (floatingStage == null) return;

        Platform.runLater(() -> {
            try {
                double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
                double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

                double x = floatingStage.getX();
                double y = floatingStage.getY();
                double width = floatingStage.getWidth();
                double height = floatingStage.getHeight();

                if (x < 10) x = 10;
                if (x > screenWidth - width - 10) x = screenWidth - width - 10;
                if (y < 10) y = 10;
                if (y > screenHeight - height - 10) y = screenHeight - height - 10;

                floatingStage.setX(x);
                floatingStage.setY(y);

            } catch (Exception e) {
                System.err.println("Errorea kokapena ezartzen: " + e.getMessage());
            }
        });
    }
}