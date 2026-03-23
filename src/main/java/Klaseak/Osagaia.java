package Klaseak;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Osagaia {
    private int id;
    private String izena;
    private String deskribapena;
    private int kantitatea;          // stock
    private String neurriaUnitatea;  // kg, l, unitate...
    private int stockMinimoa;        // gutxieneko stock
    private LocalDateTime azkenEguneratzea;

    public Osagaia() {
        this.azkenEguneratzea = LocalDateTime.now();
    }

    public Osagaia(String izena, int kantitatea, int stockMinimoa) {
        this();
        this.izena = izena;
        this.kantitatea = kantitatea;
        this.stockMinimoa = stockMinimoa;
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getDeskribapena() { return deskribapena; }
    public void setDeskribapena(String deskribapena) { this.deskribapena = deskribapena; }

    public int getKantitatea() { return kantitatea; }
    public void setKantitatea(int kantitatea) { this.kantitatea = kantitatea; }

    public String getNeurriaUnitatea() { return neurriaUnitatea; }
    public void setNeurriaUnitatea(String neurriaUnitatea) { this.neurriaUnitatea = neurriaUnitatea; }

    public int getStockMinimoa() { return stockMinimoa; }
    public void setStockMinimoa(int stockMinimoa) { this.stockMinimoa = stockMinimoa; }

    public LocalDateTime getAzkenEguneratzea() { return azkenEguneratzea; }
    public void setAzkenEguneratzea(LocalDateTime azkenEguneratzea) { this.azkenEguneratzea = azkenEguneratzea; }

    public boolean erosiBeharDa() {
        return kantitatea <= stockMinimoa;
    }

    @Override
    public String toString() {
        return izena + " (Stock: " + kantitatea + " " + neurriaUnitatea + ")";
    }
}