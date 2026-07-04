package model;

public class Dokter extends Person {

    private int idDokter;
    private String spesialis;
    private String noHP;
    // constructor kosong
    public Dokter(){

    }
    public Dokter(int idDokter,
                  String nama,
                  String spesialis,
                  String noHP) {

        super(nama);

        this.idDokter = idDokter;
        this.spesialis = spesialis;
        this.noHP = noHP;
    }

    public int getIdDokter() {
        return idDokter;
    }

    public String getSpesialis() {
        return spesialis;
    }

    public String getNoHP() {
        return noHP;
    }

    public void setIdDokter(int idDokter) {
        this.idDokter = idDokter;
    }

    public void setSpesialis(String spesialis) {
        this.spesialis = spesialis;
    }

    public void setNoHP(String noHP) {
        this.noHP = noHP;
    }

    public void periksaPasien() {

        System.out.println(
                "Dokter memeriksa pasien");
    }

    public void buatDiagnosa() {

        System.out.println(
                "Dokter membuat diagnosa");
    }

    @Override
    public void tampilInfo() {

        System.out.println("Dokter : " + nama);
        System.out.println("Spesialis : " + spesialis);
    }
    @Override
    public String toString() {

        return nama;
    }    
}