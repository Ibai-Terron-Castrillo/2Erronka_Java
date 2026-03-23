package Klaseak;

import javafx.beans.property.*;
import com.google.gson.annotations.SerializedName;

public class Mahaia {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty mahaiaZbk = new SimpleIntegerProperty();   // mahai zenbakia
    private final IntegerProperty edukiera = new SimpleIntegerProperty();    // pertsona max
    private final StringProperty egoera = new SimpleStringProperty();        // "Libre" / "Okupatuta"

    public Mahaia() {}

    public Mahaia(int id, int mahaiaZbk, int edukiera, String egoera) {
        this.id.set(id);
        this.mahaiaZbk.set(mahaiaZbk);
        this.edukiera.set(edukiera);
        this.egoera.set(egoera);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty mahaiaZbkProperty() { return mahaiaZbk; }
    public IntegerProperty edukieraProperty() { return edukiera; }
    public StringProperty egoeraProperty() { return egoera; }

    // Getters
    public int getId() { return id.get(); }
    public int getMahaiaZbk() { return mahaiaZbk.get(); }
    public int getEdukiera() { return edukiera.get(); }
    public String getEgoera() { return egoera.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setMahaiaZbk(int mahaiaZbk) { this.mahaiaZbk.set(mahaiaZbk); }
    public void setEdukiera(int edukiera) { this.edukiera.set(edukiera); }
    public void setEgoera(String egoera) { this.egoera.set(egoera); }

    public boolean isOkupatuta() {
        return "Okupatuta".equals(egoera.get());
    }

    @Override
    public String toString() {
        return "Mahai " + mahaiaZbk.get() + " (ID: " + id.get() + ", Egoera: " + egoera.get() + ")";
    }
}