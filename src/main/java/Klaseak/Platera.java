package Klaseak;

import javafx.beans.property.*;

public class Platera {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty izena = new SimpleStringProperty();
    private final DoubleProperty prezioa = new SimpleDoubleProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();
    private final IntegerProperty kategoriaId = new SimpleIntegerProperty();
    private final StringProperty kategoriaIzena = new SimpleStringProperty();

    // Constructor
    public Platera() {}

    public Platera(int id, String izena, double prezioa, int stock, int kategoriaId, String kategoriaIzena) {
        setId(id);
        setIzena(izena);
        setPrezioa(prezioa);
        setStock(stock);
        setKategoriaId(kategoriaId);
        setKategoriaIzena(kategoriaIzena);
    }

    // Getters y Setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getIzena() { return izena.get(); }
    public void setIzena(String value) { izena.set(value); }
    public StringProperty izenaProperty() { return izena; }

    public double getPrezioa() { return prezioa.get(); }
    public void setPrezioa(double value) { prezioa.set(value); }
    public DoubleProperty prezioaProperty() { return prezioa; }

    public int getStock() { return stock.get(); }
    public void setStock(int value) { stock.set(value); }
    public IntegerProperty stockProperty() { return stock; }

    public int getKategoriaId() { return kategoriaId.get(); }
    public void setKategoriaId(int value) { kategoriaId.set(value); }
    public IntegerProperty kategoriaIdProperty() { return kategoriaId; }

    public String getKategoriaIzena() { return kategoriaIzena.get(); }
    public void setKategoriaIzena(String value) { kategoriaIzena.set(value); }
    public StringProperty kategoriaIzenaProperty() { return kategoriaIzena; }

    public void sincronizarPropiedades() {
        id.set(id.get());
        izena.set(izena.get());
        prezioa.set(prezioa.get());
        stock.set(stock.get());
        kategoriaId.set(kategoriaId.get());
        kategoriaIzena.set(kategoriaIzena.get());
    }

    @Override
    public String toString() {
        return getIzena() + " (Stock: " + getStock() + ")";
    }
}