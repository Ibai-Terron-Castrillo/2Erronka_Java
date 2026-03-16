package Klaseak;

public class Lanpostua {
    private int id;
    private String izena;

    public Lanpostua(int id, String izena) {
        this.id = id;
        this.izena = izena;
    }

    public Lanpostua() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIzena() {
        return izena;
    }

    @Override
    public String toString() {
        return izena;
    }


    public void setIzena(String izena) {
        this.izena = izena;
    }
}
