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
        // Bestela, rolaId-tik bilatu (behar izanez gero)
        return null;
    }

    // Lagungarria: ID lortzeko
    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}