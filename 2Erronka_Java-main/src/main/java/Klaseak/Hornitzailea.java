package Klaseak;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Hornitzailea {
    @SerializedName("id")
    private int id;

    @SerializedName("izena")
    private String izena;

    @SerializedName("cif")
    private String cif;

    @SerializedName("helbidea")
    private String helbidea;

    @SerializedName("sektorea")
    private String sektorea;

    @SerializedName("telefonoa")
    private String telefonoa;

    @SerializedName("email")
    private String email;

    @SerializedName("osagaiak")
    private List<Osagaia> osagaiak;

    public Hornitzailea() {
    }

    public Hornitzailea(int id, String izena, String cif, String helbidea,
                        String sektorea, String telefonoa, String email) {
        this.id = id;
        this.izena = izena;
        this.cif = cif;
        this.helbidea = helbidea;
        this.sektorea = sektorea;
        this.telefonoa = telefonoa;
        this.email = email;
        this.osagaiak = new java.util.ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    public String getHelbidea() { return helbidea != null ? helbidea : ""; }
    public void setHelbidea(String helbidea) { this.helbidea = helbidea; }

    public String getSektorea() { return sektorea; }
    public void setSektorea(String sektorea) { this.sektorea = sektorea; }

    public String getTelefonoa() { return telefonoa; }
    public void setTelefonoa(String telefonoa) { this.telefonoa = telefonoa; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Osagaia> getOsagaiak() { return osagaiak; }
    public void setOsagaiak(List<Osagaia> osagaiak) { this.osagaiak = osagaiak; }

    @Override
    public String toString() {
        return izena + " (" + cif + ")";
    }
}