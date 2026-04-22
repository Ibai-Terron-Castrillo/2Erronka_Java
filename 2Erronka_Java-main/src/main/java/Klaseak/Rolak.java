package Klaseak;

public class Rolak {
    private int id;
    private String izena;

    public Rolak() {}

    public Rolak(int id, String izena) {
        this.id = id;
        this.izena = izena;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    @Override
    public String toString() {
        return izena; // ComboBox-ean erakusteko
    }
}