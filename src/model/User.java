package model;

public class User {

    private int idUser;
    private String username;
    private String password;
    private String role;
    private String nama;       // BARU
    private int idDokter;      // BARU

    // UPDATE CONSTRUCTOR JADI 6 PARAMETER
    public User(int idUser, String username, String password, String role, String nama, int idDokter) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.role = role;
        this.nama = nama;
        this.idDokter = idDokter;
    }

    public boolean login(String user, String pass) {
        return username.equals(user) && password.equals(pass);
    }

    public void logouta() {
        System.out.println("Logout berhasil");
    }

    public String getRole() { return role; }
    public int getIdUser() { return idUser; }
    public String getUsername() { return username; }
    public String getNama() { return nama; }       // BARU
    public int getIdDokter() { return idDokter; }  // BARU
}