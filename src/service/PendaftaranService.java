package service;

import dao.PendaftaranDAO;
import javafx.collections.ObservableList;
import model.Pendaftaran;

public class PendaftaranService {

    // =========================
    // DAO
    // =========================
    private PendaftaranDAO dao = new PendaftaranDAO();

    // =========================
    // GET ALL
    // =========================
    public ObservableList<Pendaftaran> getAll() {

        return dao.getAllPendaftaran();
    }

    // =========================
    // SEARCH
    // =========================
    public ObservableList<Pendaftaran> search(
            String keyword) {

        //return dao.search(keyword);
        return null;
    }

    // =========================
    // DELETE
    // =========================
    public void delete(int id)
            throws Exception {

        if(id <= 0){

            throw new Exception(
                    "ID tidak valid");
        }

        dao.deletePendaftaran(id);
    }

    // =========================
    // SIMPAN
    // =========================
    public void simpan(
            Pendaftaran p,
            boolean editMode)
            throws Exception {

        // =====================
        // VALIDASI BUSINESS
        // =====================

        if (p.getTanggal() == null) {

            throw new Exception(
                    "Tanggal wajib diisi");
        }

        if (p.getPasien() == null) {

            throw new Exception(
                    "Pasien wajib dipilih");
        }

        if (p.getDokter() == null) {

            throw new Exception(
                    "Dokter wajib dipilih");
        }

        if (p.getKeluhan() == null
                || p.getKeluhan()
                .trim()
                .isEmpty()) {

            throw new Exception(
                    "Keluhan wajib diisi");
        }

        // =====================
        // INSERT / UPDATE
        // =====================

        if (editMode) {

            dao.updatePendaftaran(p);

        } else {

            dao.insertPendaftaran(p);
        }
    }
}