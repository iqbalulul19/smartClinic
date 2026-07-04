package model;

public class RiwayatPrediksi {
    private String tanggal;
    private String namaPasien;
    private String hasilPrediksi;
    private String detailMedis;

    public RiwayatPrediksi(String tanggal, String namaPasien, String hasilPrediksi, String detailMedis) {
        this.tanggal = tanggal;
        this.namaPasien = namaPasien;
        this.hasilPrediksi = hasilPrediksi;
        this.detailMedis = detailMedis;
    }

    // Getter untuk digunakan oleh TableView
    public String getTanggal() { return tanggal; }
    public String getNamaPasien() { return namaPasien; }
    public String getHasilPrediksi() { return hasilPrediksi; }
    public String getDetailMedis() { return detailMedis; }
}