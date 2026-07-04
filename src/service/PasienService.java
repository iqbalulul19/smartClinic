package service;

import dao.PasienDAO;
import javafx.collections.ObservableList;
import model.Pasien;

public class PasienService {

    private PasienDAO dao =
            new PasienDAO();

    // =========================
    // GET ALL
    // =========================
    public ObservableList<Pasien> getAll() {

        return dao.getAllPasien();
    }

    // =========================
    // SEARCH
    // =========================
    public ObservableList<Pasien> search(
            String keyword) {

        return dao.searchPasien(keyword);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(int id)
            throws Exception {

        if (id <= 0) {

            throw new Exception(
                    "ID pasien tidak valid");
        }

        dao.deletePasien(id);
    }
// =========================
// SIMPAN
// =========================
public void simpan(
        Pasien pasien,
        boolean modeEdit)
        throws Exception {

    // VALIDASI

    if (pasien.getNama() == null
            || pasien.getNama()
            .trim()
            .isEmpty()) {

        throw new Exception(
                "Nama pasien wajib diisi");
    }

    if (pasien.getUmur() <= 0) {

        throw new Exception(
                "Umur tidak valid");
    }

    if (pasien.getGender() == null) {

        throw new Exception(
                "Pilih gender");
    }

    // INSERT / UPDATE

    if (modeEdit) {

        dao.updatePasien(pasien);

    } else {

        dao.insertPasien(pasien);
    }
}
  
}