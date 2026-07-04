package model;

import java.util.Date;

public class Pendaftaran {

    private int idDaftar;
    private Date tanggal;
    private String keluhan;

    private Pasien pasien;
    private Dokter dokter;
    private int noAntrian;

    public Pendaftaran() {
    }

    public Pendaftaran(int idDaftar,
                       Date tanggal,
                       String keluhan,
                       Pasien pasien,
                       Dokter dokter) {

        this.idDaftar = idDaftar;
        this.tanggal = tanggal;
        this.keluhan = keluhan;
        this.pasien = pasien;
        this.dokter = dokter;
    }

    public int getIdDaftar() {
        return idDaftar;
    }

    public void setIdDaftar(int idDaftar) {
        this.idDaftar = idDaftar;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public String getKeluhan() {
        return keluhan;
    }

    public void setKeluhan(String keluhan) {
        this.keluhan = keluhan;
    }

    public Pasien getPasien() {
        return pasien;
    }

    public void setPasien(Pasien pasien) {
        this.pasien = pasien;
    }

    public Dokter getDokter() {
        return dokter;
    }

    public void setDokter(Dokter dokter) {
        this.dokter = dokter;
    }

    // untuk tableview
    public String getNamaPasien() {
        return pasien != null ? pasien.getNama() : "";
    }

    public String getNamaDokter() {
        return dokter != null ? dokter.getNama() : "";
    }

    public int getNoAntrian() {
     return noAntrian;
    }
 
    public void setNoAntrian(int noAntrian) {
        this.noAntrian = noAntrian;
    }
}