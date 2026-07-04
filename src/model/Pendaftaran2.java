package model;

import java.util.Date;

public class Pendaftaran2 {

    private int idDaftar;
    private Date tanggal;
    private String keluhan;

    // RELASI OBJECT
    private Pasien pasien;
    private Dokter dokter;

    public Pendaftaran2(int idDaftar,
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

    public void daftarPasien() {

        System.out.println(
                pasien.getNama()
                + " berhasil daftar");
    }

    public void tampilAntrian() {

        System.out.println(
                "Antrian pasien : "
                + pasien.getNama());
    }
}
