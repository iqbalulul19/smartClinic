package model;

public class LaporanRiwayat {
    private String tanggal;
    private String namaPasien;
    private String hasil;
    
    // Buat Constructor, Getter, dan Setter di sini
    public LaporanRiwayat(String tanggal, String namaPasien, String hasil) {
        this.tanggal = tanggal;
        this.namaPasien = namaPasien;
        this.hasil = hasil;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getNamaPasien() {
        return namaPasien;
    }

    public void setNamaPasien(String namaPasien) {
        this.namaPasien = namaPasien;
    }

    public String getHasil() {
        return hasil;
    }

    public void setHasil(String hasil) {
        this.hasil = hasil;
    }

    @Override
    public String toString() {
        return "LaporanRiwayat{" +
                "tanggal='" + tanggal + '\'' +
                ", namaPasien='" + namaPasien + '\'' +
                ", hasil='" + hasil + '\'' +
                '}';
    }
}