package Klaseak;

public class Erabiltzailea {
    private int id;
    private String izena;
    private String pasahitza;
    private Langilea langilea;

    public Erabiltzailea(int id, String izena, String pasahitza, Langilea langilea) {
        this.id = id;
        this.izena = izena;
        this.pasahitza = pasahitza;
        this.langilea = langilea;
    }

    public Erabiltzailea() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIzena() {
        return izena;
    }

    public void setIzena(String izena) {
        this.izena = izena;
    }

    public String getPasahitza() {
        return pasahitza;
    }

    public void setPasahitza(String pasahitza) {
        this.pasahitza = pasahitza;
    }

    public Langilea getLangilea() {
        return langilea;
    }

    public void setLangilea(Langilea langilea) {
        this.langilea = langilea;
    }
}
