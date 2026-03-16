package services;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActionLogger {

    private static final String SERVER_IP = "192.168.1.158";
    private static final String SERVER_SHARE = "C$\\LOGak";
    private static final String LOG_FILE = "app_actions.log";

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(
            String erabiltzailea,
            String eragiketa,
            String taula,
            String deskripzioa
    ) {
        try {
            String rutaRed = "\\\\" + SERVER_IP + "\\" + SERVER_SHARE;
            Path logPath = Paths.get(rutaRed, LOG_FILE);

            Files.createDirectories(logPath.getParent());

            String linea = String.format(
                    "[%s] ERABILTZAILEA=%s | ERAGIKETA=%s | TAULA=%s | %s%n",
                    LocalDateTime.now().format(FORMAT),
                    erabiltzailea,
                    eragiketa,
                    taula,
                    deskripzioa
            );

            Files.write(
                    logPath,
                    linea.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            System.err.println("Ezin izan da Log-a idatzi: " + e.getMessage());
        }
    }
}

