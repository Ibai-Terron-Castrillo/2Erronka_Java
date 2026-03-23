package Klaseak;

public class PlaterenOsagaia {
    private int plateraId;
    private int inbentarioaId;  // Osagaia-ren id (inbentarioa)
    private double kantitatea;

    public PlaterenOsagaia() {}

    public PlaterenOsagaia(int plateraId, int inbentarioaId, double kantitatea) {
        this.plateraId = plateraId;
        this.inbentarioaId = inbentarioaId;
        this.kantitatea = kantitatea;
    }

    public int getPlateraId() { return plateraId; }
    public void setPlateraId(int plateraId) { this.plateraId = plateraId; }

    public int getInbentarioaId() { return inbentarioaId; }
    public void setInbentarioaId(int inbentarioaId) { this.inbentarioaId = inbentarioaId; }

    public double getKantitatea() { return kantitatea; }
    public void setKantitatea(double kantitatea) { this.kantitatea = kantitatea; }
}