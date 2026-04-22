package dto;

public class LangileakDto {
    private int id;
    private String izena;           // izen osoa
    private String erabiltzailea;    // erabiltzaile izena
    private String pasahitza;
    private String aktibo;           // "Bai" / "Ez"
    private String erregistroData;   // ISO formatuan (DateTime)
    private Integer rolaId;
    private boolean txatBaimena;     // txat baimena (true/false)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIzena() {
        return izena;
    }

    public void setIzena(String izena) {
        this.izena = izena;
    }

    public String getErabiltzailea() {
        return erabiltzailea;
    }

    public void setErabiltzailea(String erabiltzailea) {
        this.erabiltzailea = erabiltzailea;
    }

    public String getPasahitza() {
        return pasahitza;
    }

    public void setPasahitza(String pasahitza) {
        this.pasahitza = pasahitza;
    }

    public String getAktibo() {
        return aktibo;
    }

    public void setAktibo(String aktibo) {
        this.aktibo = aktibo;
    }

    public String getErregistroData() {
        return erregistroData;
    }

    public void setErregistroData(String erregistroData) {
        this.erregistroData = erregistroData;
    }

    public Integer getRolaId() {
        return rolaId;
    }

    public void setRolaId(Integer rolaId) {
        this.rolaId = rolaId;
    }

    public boolean isTxatBaimena() {
        return txatBaimena;
    }

    public void setTxatBaimena(boolean txatBaimena) {
        this.txatBaimena = txatBaimena;
    }
}