package services;

import Klaseak.Langilea;

public class SessionContext {
    private static Langilea currentUser;

    public static void setCurrentUser(Langilea user) {
        currentUser = user;
    }

    public static Langilea getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    // Lagungarria: erabiltzailearen izena lortzeko
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getErabiltzailea() : null;
    }

    // Lagungarria: rolaren izena lortzeko
    public static String getCurrentUserRole() {
        if (currentUser != null && currentUser.getRola() != null) {
            return currentUser.getRola().getIzena();
        }
        return null;
    }

    // Lagungarria: ID lortzeko
    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    // Txat baimena egiaztatzeko metodoa
    public static boolean txatBaimenaDauka() {
        boolean baimena = currentUser != null && currentUser.getTxat_baimena();
        System.out.println("DEBUG: txatBaimenaDauka() -> " + baimena + " (currentUser: " + currentUser + ")");
        return baimena;
    }
}