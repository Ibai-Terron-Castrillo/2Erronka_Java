package Klaseak;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Eskaera {
    private int id;
    private int eskaeraZenbakia;
    private double totala;
    private boolean egoera;
    private String eskaeraPdf;
    private Date data;
    private List<Osagaia> osagaiak;

    public Eskaera() {
        this.osagaiak = new ArrayList<>();
        this.egoera = false;
        this.data = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEskaeraZenbakia() { return eskaeraZenbakia; }
    public void setEskaeraZenbakia(int eskaeraZenbakia) { this.eskaeraZenbakia = eskaeraZenbakia; }

    public double getTotala() { return totala; }
    public void setTotala(double totala) { this.totala = totala; }

    public boolean isEgoera() { return egoera; }
    public void setEgoera(boolean egoera) { this.egoera = egoera; }

    public String getEskaeraPdf() { return eskaeraPdf; }
    public void setEskaeraPdf(String eskaeraPdf) { this.eskaeraPdf = eskaeraPdf; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public List<Osagaia> getOsagaiak() { return osagaiak; }
    public void setOsagaiak(List<Osagaia> osagaiak) { this.osagaiak = osagaiak; }

    public void addOsagaia(Osagaia osagaia) {
        this.osagaiak.add(osagaia);
    }

    public String getEgoeraTextua() {
        return egoera ? "Bukatua" : "Pendiente";
    }
}