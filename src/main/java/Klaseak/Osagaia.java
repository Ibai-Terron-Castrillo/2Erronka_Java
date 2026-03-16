package Klaseak;

import java.util.ArrayList;
import java.util.List;

public class Osagaia {
    private int id;
    private String izena;
    private double azkenPrezioa;
    private int stock;
    private int gutxienekoStock;
    private boolean eskatu;
    private List<Hornitzailea> hornitzaileak;

    public Osagaia() {
        this.hornitzaileak = new ArrayList<>();
        this.eskatu = false;
    }

    public Osagaia(String izena, double azkenPrezioa, int stock, int gutxienekoStock) {
        this();
        this.izena = izena;
        this.azkenPrezioa = azkenPrezioa;
        this.stock = stock;
        this.gutxienekoStock = gutxienekoStock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public double getAzkenPrezioa() { return azkenPrezioa; }
    public void setAzkenPrezioa(double azkenPrezioa) { this.azkenPrezioa = azkenPrezioa; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getGutxienekoStock() { return gutxienekoStock; }
    public void setGutxienekoStock(int gutxienekoStock) { this.gutxienekoStock = gutxienekoStock; }

    public boolean isEskatu() { return eskatu; }
    public void setEskatu(boolean eskatu) { this.eskatu = eskatu; }

    public List<Hornitzailea> getHornitzaileak() { return hornitzaileak; }
    public void setHornitzaileak(List<Hornitzailea> hornitzaileak) {
        this.hornitzaileak = hornitzaileak;
    }

    public void addHornitzailea(Hornitzailea hornitzailea) {
        this.hornitzaileak.add(hornitzailea);
    }

    public boolean erosiBeharDa() {
        return stock <= gutxienekoStock;
    }

    public double stockBalioaLortu() {
        return stock * azkenPrezioa;
    }

    @Override
    public String toString() {
        return izena + " (Stock: " + stock + ", Prezioa: " + azkenPrezioa + "€)";
    }
}