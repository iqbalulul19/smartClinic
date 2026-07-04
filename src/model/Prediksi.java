package model;

import service.MLService;
import java.util.Date;

public class Prediksi implements Predictable {

    private int idPrediksi;
    private String hasilPrediksi;
    private double probabilitas;
    private Date tanggalPrediksi;

    // RELASI OBJECT (Sesuai UML)
    private Pasien pasien;
    
    // DEPENDENCY (Sesuai UML)
    private MLService mlService;

    // Constructor
    public Prediksi(Pasien pasien) {
        this.pasien = pasien;
        this.mlService = new MLService(); // Prediksi otomatis membuat asisten ML-nya
        this.tanggalPrediksi = new Date();
    }

    // Fungsi utama yang dipanggil oleh Controller
    // Menggunakan varargs (String...) agar bisa menerima jumlah parameter yang berbeda-beda
    public void jalankanPrediksiML(String jenisPenyakit, String... parameterMedis) {
        // Objek prediksi menyuruh MLService bekerja
        this.hasilPrediksi = mlService.predict(jenisPenyakit, parameterMedis);
        
        // Karena Python kita saat ini baru mengembalikan String hasil,
        // kita set probabilitas default (bisa dikembangkan nanti)
        this.probabilitas = 0.0; 
    }

    // Method wajib dari interface Predictable
    @Override
    public void prosesPrediksi() {
        // Biarkan kosong jika interface mewajibkan method tanpa parameter,
        // karena kita menggunakan jalankanPrediksiML() di atas.
    }

    // Method sesuai UML
    public void tampilHasil() {
        System.out.println("--- LOG PREDIKSI ---");
        System.out.println("Nama Pasien    : " + pasien.getNama());
        System.out.println("Tanggal        : " + tanggalPrediksi.toString());
        System.out.println("Hasil Prediksi : " + hasilPrediksi);
    }

    // Getter untuk diambil oleh Controller
    public String getHasilPrediksi() {
        return hasilPrediksi;
    }

    public Date getTanggalPrediksi() {
        return tanggalPrediksi;
    }
}