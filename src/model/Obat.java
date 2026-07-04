package model;

public class Obat {

    private int idObat;
    private String namaObat;
    private int stok;
    private double harga;
    private String aturan_pakai;
    private String kode_kfa;

    public Obat() {
    }

    public Obat(int idObat,
                String namaObat,
                int stok,
                double harga,
                String aturan_pakai,
                String kode_kfa) {

        this.idObat = idObat;
        this.namaObat = namaObat;
        this.stok = stok;
        this.harga = harga;
        this.aturan_pakai = aturan_pakai;
        this.kode_kfa = kode_kfa;
    }

    public int getIdObat() {
        return idObat;
    }

    public void setIdObat(int idObat) {
        this.idObat = idObat;
    }

    public String getNamaObat() {
        return namaObat;
    }

    public void setNamaObat(String namaObat) {
        this.namaObat = namaObat;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public String getAturanPakai() {
        return aturan_pakai;
    }

    public void setAturanPakai(String aturan_pakai) {
        this.aturan_pakai = aturan_pakai;
    }

    public String getKodeKfa() {
        return kode_kfa;
    }

    public void setKodeKfa(String kode_kfa) {
        this.kode_kfa = kode_kfa;
    }

    @Override
    public String toString() {
        return namaObat;
    }
}