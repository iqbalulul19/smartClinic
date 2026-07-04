package model;

import java.util.Date;

public class Pemeriksaan {

    private int idPeriksa;

    private Pendaftaran pendaftaran;

    private Date tanggalPeriksa;

    private String diagnosa;

    private double tekananDarah;
    private double gulaDarah;
    private double suhu;
    private double beratBadan;

    private String catatan;

    private String hasilPrediksi;
    private String tingkatResiko;

    public Pemeriksaan() {
    }

    public int getIdPeriksa() {
        return idPeriksa;
    }

    public void setIdPeriksa(int idPeriksa) {
        this.idPeriksa = idPeriksa;
    }

    public Pendaftaran getPendaftaran() {
        return pendaftaran;
    }

    public void setPendaftaran(Pendaftaran pendaftaran) {
        this.pendaftaran = pendaftaran;
    }

    public Date getTanggalPeriksa() {
        return tanggalPeriksa;
    }

    public void setTanggalPeriksa(Date tanggalPeriksa) {
        this.tanggalPeriksa = tanggalPeriksa;
    }

    public String getDiagnosa() {
        return diagnosa;
    }

    public void setDiagnosa(String diagnosa) {
        this.diagnosa = diagnosa;
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

    public double getSuhu() {
        return suhu;
    }

    public void setSuhu(double suhu) {
        this.suhu = suhu;
    }

    public double getBeratBadan() {
        return beratBadan;
    }

    public void setBeratBadan(double beratBadan) {
        this.beratBadan = beratBadan;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public String getHasilPrediksi() {
        return hasilPrediksi;
    }

    public void setHasilPrediksi(String hasilPrediksi) {
        this.hasilPrediksi = hasilPrediksi;
    }

    public String getTingkatResiko() {
        return tingkatResiko;
    }

    public void setTingkatResiko(String tingkatResiko) {
        this.tingkatResiko = tingkatResiko;
    }

    // helper tableview

    public String getNamaPasien() {
        return pendaftaran != null
                ? pendaftaran.getNamaPasien()
                : "";
    }

    public String getNamaDokter() {
        return pendaftaran != null
                ? pendaftaran.getNamaDokter()
                : "";
    }

    public String getKeluhan() {
        return pendaftaran != null
                ? pendaftaran.getKeluhan()
                : "";
    }
}