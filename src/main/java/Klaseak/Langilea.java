package Klaseak;

import java.time.LocalDateTime;

public class Langilea {
    private int id;
    private String izena;               // izen osoa
    private String erabiltzailea;       // username
    private String pasahitza;
    private String aktibo;               // "Bai" / "Ez"
    private LocalDateTime erregistroData;
    private int rolaId;                  // atzerriko gakoa
    private Rolak rola;                  // erakusteko
    private boolean txat_baimena;          // txat baimena (true/false)

    public Langilea() {}

    public Langilea(int id, String izena, String erabiltzailea, String pasahitza,
                    String aktibo, LocalDateTime erregistroData, int rolaId, boolean txat_baimena) {
        this.id = id;
        this.izena = izena;
        this.erabiltzailea = erabiltzailea;
        this.pasahitza = pasahitza;
        this.aktibo = aktibo;
        this.erregistroData = erregistroData;
        this.rolaId = rolaId;
        this.txat_baimena = txat_baimena;
    }

    // Getters / Setters

    public boolean getTxat_baimena() {
        return txat_baimena;
    }

    public void setTxat_baimena(boolean txat_baimena) {
        this.txat_baimena = txat_baimena;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getErabiltzailea() { return erabiltzailea; }
    public void setErabiltzailea(String erabiltzailea) { this.erabiltzailea = erabiltzailea; }

    public String getPasahitza() { return pasahitza; }
    public void setPasahitza(String pasahitza) { this.pasahitza = pasahitza; }

    public String getAktibo() { return aktibo; }
    public void setAktibo(String aktibo) { this.aktibo = aktibo; }

    public LocalDateTime getErregistroData() { return erregistroData; }
    public void setErregistroData(LocalDateTime erregistroData) { this.erregistroData = erregistroData; }

    public int getRolaId() { return rolaId; }
    public void setRolaId(int rolaId) { this.rolaId = rolaId; }

    public Rolak getRola() { return rola; }
    public void setRola(Rolak rola) { this.rola = rola; }

    // Taulan erakusteko
    public String getRolaIzena() {
        return rola != null ? rola.getIzena() : "";
    }
}