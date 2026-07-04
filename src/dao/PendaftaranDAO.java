package dao;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.sql.*;
// import java.sql.Connection;
// import java.sql.ResultSet;
// import java.sql.Statement;

import model.Pendaftaran;
import model.Pasien;
import model.Dokter;

public class PendaftaranDAO {

        Connection conn = DBConnection.connect();

        public ObservableList<Pendaftaran> getData() {

                ObservableList<Pendaftaran> list =FXCollections.observableArrayList();
                String sql =
                        "SELECT p.*, " +
                        "ps.nama AS nama_pasien, " +
                        "d.nama AS nama_dokter " +
                        "FROM pendaftaran p " +
                        "JOIN pasien ps ON p.id_pasien = ps.id_pasien " +
                        "JOIN dokter d ON p.id_dokter = d.id_dokter";

                try {

                        //Connection conn = Koneksi.getKoneksi();

                        Statement st = conn.createStatement();

                        ResultSet rs = st.executeQuery(sql);

                        while (rs.next()) {

                        Pasien pasien = new Pasien();
                        pasien.setIdPasien(
                                rs.getInt("id_pasien"));
                        pasien.setNama(
                                rs.getString("nama_pasien"));

                        Dokter dokter = new Dokter();
                        dokter.setIdDokter(
                                rs.getInt("id_dokter"));
                        dokter.setNama(
                                rs.getString("nama_dokter"));

                        Pendaftaran p = new Pendaftaran();

                        p.setIdDaftar(
                                rs.getInt("id_daftar"));

                        p.setTanggal(
                                rs.getDate("tanggal"));

                        p.setKeluhan(
                                rs.getString("keluhan"));

                        p.setPasien(pasien);

                        p.setDokter(dokter);

                        list.add(p);
                        }

                } catch (Exception e) {

                        e.printStackTrace();
                }

                return list;
        }
    
    // GET ALL
    public ObservableList<Pendaftaran> getAllPendaftaran(){

        ObservableList<Pendaftaran> list =
                FXCollections.observableArrayList();

        try{

            String sql = """
                    SELECT pendaftaran.*,
                           pasien.nama AS nama_pasien,
                           dokter.nama AS nama_dokter
                    FROM pendaftaran
                    JOIN pasien
                    ON pasien.id_pasien = pendaftaran.id_pasien
                    JOIN dokter
                    ON dokter.id_dokter = pendaftaran.id_dokter
                    """;

            Statement st = conn.createStatement();

            ResultSet rs = st.executeQuery(sql);

            while(rs.next()){

                Pasien pasien = new Pasien();
                pasien.setIdPasien(
                        rs.getInt("id_pasien"));

                pasien.setNama(
                        rs.getString("nama_pasien"));

                Dokter dokter = new Dokter();
                dokter.setIdDokter(
                        rs.getInt("id_dokter"));

                dokter.setNama(
                        rs.getString("nama_dokter"));

                Pendaftaran p = new Pendaftaran(
                        rs.getInt("id_daftar"),
                        rs.getDate("tanggal"),
                        rs.getString("keluhan"),
                        pasien,
                        dokter
                );

                list.add(p);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    // INSERT
    public void insertPendaftaran(Pendaftaran p){

        try{

            String sql = """
                    INSERT INTO pendaftaran
                    (tanggal,keluhan,id_pasien,id_dokter)
                    VALUES(?,?,?,?)
                    """;

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setDate(
                    1,
                    new java.sql.Date(
                            p.getTanggal().getTime()));

            ps.setString(2,p.getKeluhan());

            ps.setInt(
                    3,
                    p.getPasien().getIdPasien());

            ps.setInt(
                    4,
                    p.getDokter().getIdDokter());

            ps.executeUpdate();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // UPDATE
    public void updatePendaftaran(Pendaftaran p){

        try{

            String sql = """
                    UPDATE pendaftaran
                    SET tanggal=?,
                        keluhan=?,
                        id_pasien=?,
                        id_dokter=?
                    WHERE id_daftar=?
                    """;

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setDate(
                    1,
                    new java.sql.Date(
                            p.getTanggal().getTime()));

            ps.setString(2,p.getKeluhan());

            ps.setInt(
                    3,
                    p.getPasien().getIdPasien());

            ps.setInt(
                    4,
                    p.getDokter().getIdDokter());

            ps.setInt(
                    5,
                    p.getIdDaftar());

            ps.executeUpdate();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // DELETE
    public void deletePendaftaran(int id){

        try{

            String sql =
                    "DELETE FROM pendaftaran WHERE id_daftar=?";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1,id);

            ps.executeUpdate();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
   // SEARCH
    public ObservableList<Pasien> search(String keyword){

        ObservableList<Pasien> list = FXCollections.observableArrayList();

        try{

            String sql =
                    "SELECT * FROM pendaftaran WHERE  id_daftar LIKE ?";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                //list.add( new pendaftaran() );
            }

        }catch(Exception e){

            e.printStackTrace();
        }

        return list;
    }

}