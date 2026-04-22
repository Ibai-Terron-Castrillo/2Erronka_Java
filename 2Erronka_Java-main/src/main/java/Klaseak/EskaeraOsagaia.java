package Klaseak;

public class EskaeraOsagaia {
    private int id;
    private int eskaerakId;
    private int osagaiakId;
    private int kopurua;
    private double prezioa;
    private double totala;
    private String osagaiaIzena;

    public EskaeraOsagaia() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEskaerakId() { return eskaerakId; }
    public void setEskaerakId(int eskaerakId) { this.eskaerakId = eskaerakId; }

    public int getOsagaiakId() { return osagaiakId; }
    public void setOsagaiakId(int osagaiakId) { this.osagaiakId = osagaiakId; }

    public int getKopurua() { return kopurua; }
    public void setKopurua(int kopurua) { this.kopurua = kopurua; }

    public double getPrezioa() { return prezioa; }
    public void setPrezioa(double prezioa) { this.prezioa = prezioa; }

    public double getTotala() { return totala; }
    public void setTotala(double totala) { this.totala = totala; }

    public String getOsagaiaIzena() { return osagaiaIzena; }
    public void setOsagaiaIzena(String osagaiaIzena) { this.osagaiaIzena = osagaiaIzena; }
}