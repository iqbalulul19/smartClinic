package model;

public class RiwayatPasien {
    private String tanggal;
    private String namaDokter;
    private String keluhan;
    private String diagnosa;
    private String resep;

    public RiwayatPasien(String tanggal, String namaDokter, String keluhan, String diagnosa, String resep) {
        this.tanggal = tanggal;
        this.namaDokter = namaDokter;
        this.keluhan = keluhan;
        this.diagnosa = diagnosa;
        this.resep = resep;
    }

    public String getTanggal() { return tanggal; }
    public String getNamaDokter() { return namaDokter; }
    public String getKeluhan() { return keluhan; }
    public String getDiagnosa() { return diagnosa; }
    public String getResep() { return resep; }
}