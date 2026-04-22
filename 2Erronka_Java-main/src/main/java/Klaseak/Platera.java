package Klaseak;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Platera {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty izena = new SimpleStringProperty();
    private final StringProperty deskribapena = new SimpleStringProperty();
    private final DoubleProperty prezioa = new SimpleDoubleProperty();
    private final IntegerProperty kategoriaId = new SimpleIntegerProperty();
    private final StringProperty kategoriaIzena = new SimpleStringProperty();
    private final StringProperty erabilgarri = new SimpleStringProperty(); // "Bai"/"Ez"
    private final ObjectProperty<LocalDateTime> sortzeData = new SimpleObjectProperty<>();
    private final StringProperty irudia = new SimpleStringProperty();

    public Platera() {}

    // Getters and setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getIzena() { return izena.get(); }
    public void setIzena(String value) { izena.set(value); }
    public StringProperty izenaProperty() { return izena; }

    public String getDeskribapena() { return deskribapena.get(); }
    public void setDeskribapena(String value) { deskribapena.set(value); }
    public StringProperty deskribapenaProperty() { return deskribapena; }

    public double getPrezioa() { return prezioa.get(); }
    public void setPrezioa(double value) { prezioa.set(value); }
    public DoubleProperty prezioaProperty() { return prezioa; }

    public int getKategoriaId() { return kategoriaId.get(); }
    public void setKategoriaId(int value) { kategoriaId.set(value); }
    public IntegerProperty kategoriaIdProperty() { return kategoriaId; }

    public String getKategoriaIzena() { return kategoriaIzena.get(); }
    public void setKategoriaIzena(String value) { kategoriaIzena.set(value); }
    public StringProperty kategoriaIzenaProperty() { return kategoriaIzena; }

    public String getErabilgarri() { return erabilgarri.get(); }
    public void setErabilgarri(String value) { erabilgarri.set(value); }
    public StringProperty erabilgarriProperty() { return erabilgarri; }

    public LocalDateTime getSortzeData() { return sortzeData.get(); }
    public void setSortzeData(LocalDateTime value) { sortzeData.set(value); }
    public ObjectProperty<LocalDateTime> sortzeDataProperty() { return sortzeData; }

    public String getIrudia() { return irudia.get(); }
    public void setIrudia(String value) { irudia.set(value); }
    public StringProperty irudiaProperty() { return irudia; }

    @Override
    public String toString() {
        return getIzena() + " (" + getPrezioa() + "€)";
    }
}