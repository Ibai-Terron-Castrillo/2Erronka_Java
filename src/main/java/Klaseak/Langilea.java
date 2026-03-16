package Klaseak;

public class Langilea {
    private int id;
    private String izena;
    private String abizena1;
    private String abizena2;
    private String telefonoa;
    private Lanpostua lanpostua;

    public Langilea(int id, String izena, String abizena1, String abizena2, String telefonoa, Lanpostua lanpostua) {
        this.id = id;
        this.izena = izena;
        this.abizena1 = abizena1;
        this.abizena2 = abizena2;
        this.telefonoa = telefonoa;
        this.lanpostua = lanpostua;
    }

    public Langilea() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getAbizena1() { return abizena1; }
    public void setAbizena1(String abizena1) { this.abizena1 = abizena1; }

    public String getAbizena2() { return abizena2; }
    public void setAbizena2(String abizena2) { this.abizena2 = abizena2; }

    public String getTelefonoa() { return telefonoa; }
    public void setTelefonoa(String telefonoa) { this.telefonoa = telefonoa; }

    public Klaseak.Lanpostua getLanpostua() { return lanpostua; }
    public void setLanpostua(Lanpostua value) { this.lanpostua = value; }

    public String getLanpostuaName() {
        return lanpostua != null ? lanpostua.getIzena() : "";
    }
}
