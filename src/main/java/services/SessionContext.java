package services;

public class SessionContext {

    private static String currentUser;

    private SessionContext() {}

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static String getCurrentUser() {
        return currentUser != null ? currentUser : "EZEZAGUNA";
    }

    public static void clear() {
        currentUser = null;
    }
}

