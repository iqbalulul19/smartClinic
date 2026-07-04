package model;

import java.util.ArrayList;

public class Pasien extends Person {

    private int idPasien;
    private int umur;
    private String gender;
    private String alamat;
    private String noHP;
    
    private double tekananDarah;
    private double gulaDarah;

    // RELASI OBJECT
    private ArrayList<RekamMedis> daftarRekam;
    private ArrayList<Prediksi> daftarPrediksi;

    // CONSTRUCTOR KOSONG
    public Pasien() {

        super("");

        daftarRekam = new ArrayList<>();
        daftarPrediksi = new ArrayList<>();
    }

    // CONSTRUCTOR UTAMA
    public Pasien(int idPasien,
                  String nama,
                  int umur,
                  String gender,
                  String alamat,
                  String noHP,
                  double tekananDarah,
                  double gulaDarah) {

        super(nama);

        this.idPasien = idPasien;
        this.umur = umur;
        this.gender = gender;
        this.alamat = alamat;
        this.noHP = noHP;

        this.tekananDarah = tekananDarah;
        this.gulaDarah = gulaDarah;

        daftarRekam = new ArrayList<>();
        daftarPrediksi = new ArrayList<>();
    }

    // GETTER & SETTER

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public int getUmur() {
        return umur;
    }

    public void setUmur(int umur) {
        this.umur = umur;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoHp() {
        return noHP;
    }

    public void setNoHp(String noHP) {
        this.noHP = noHP;
    }

    public double getTekananDarah() {
        return tekananDarah;
    }

    public void setTekananDarah(double tekananDarah) {
        this.tekananDarah = tekananDarah;
    }

    public double getGulaDarah() {
        return gulaDarah;
    }

    public void setGulaDarah(double gulaDarah) {
        this.gulaDarah = gulaDarah;
    }

    // RELASI OBJECT

    public void tambahRekamMedis(RekamMedis rm) {

        daftarRekam.add(rm);
    }

    public void tambahPrediksi(Prediksi p) {

        daftarPrediksi.add(p);
    }

    public ArrayList<RekamMedis> getDaftarRekam() {

        return daftarRekam;
    }

    public ArrayList<Prediksi> getDaftarPrediksi() {

        return daftarPrediksi;
    }

    // POLYMORPHISM
    @Override
    public void tampilInfo() {

        System.out.println(
                "ID Pasien : " + idPasien);

        System.out.println(
                "Nama      : " + nama);

        System.out.println(
                "Umur      : " + umur);

        System.out.println(
                "Gender    : " + gender);

        System.out.println(
                "Tekanan   : " + tekananDarah);

        System.out.println(
                "Gula      : " + gulaDarah);
    }
    @Override
    public String toString() {

        return nama;
    }
}