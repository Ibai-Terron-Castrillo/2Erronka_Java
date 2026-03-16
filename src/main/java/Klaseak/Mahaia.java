package Klaseak;

import javafx.beans.property.*;
import com.google.gson.annotations.SerializedName;

public class Mahaia {
    @SerializedName("id")
    private final IntegerProperty id = new SimpleIntegerProperty();

    @SerializedName("zenbakia")
    private final IntegerProperty zenbakia = new SimpleIntegerProperty();

    @SerializedName("pertsonaMax")
    private final IntegerProperty pertsonaMax = new SimpleIntegerProperty();

    @SerializedName("occupied")  // Esto asegura que Gson mapee "occupied" del JSON
    private final BooleanProperty occupied = new SimpleBooleanProperty();

    public Mahaia() {}

    public Mahaia(int id, int zenbakia, int pertsonaMax, boolean occupied) {
        this.id.set(id);
        this.zenbakia.set(zenbakia);
        this.pertsonaMax.set(pertsonaMax);
        this.occupied.set(occupied);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty zenbakiaProperty() { return zenbakia; }
    public IntegerProperty pertsonaMaxProperty() { return pertsonaMax; }
    public BooleanProperty occupiedProperty() { return occupied; }

    // Getters
    public int getId() { return id.get(); }
    public int getZenbakia() { return zenbakia.get(); }
    public int getPertsonaMax() { return pertsonaMax.get(); }
    public boolean isOccupied() { return occupied.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setZenbakia(int zenbakia) { this.zenbakia.set(zenbakia); }
    public void setPertsonaMax(int pertsonaMax) { this.pertsonaMax.set(pertsonaMax); }
    public void setOccupied(boolean occupied) { this.occupied.set(occupied); }

    @Override
    public String toString() {
        return "Mahai " + zenbakia.get() + " (ID: " + id.get() + ", Occupied: " + occupied.get() + ")";
    }
}