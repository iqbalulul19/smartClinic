package model;

public abstract class Person {

    // UBAH private → protected
    protected String nama;
    // constructor kosong
    public Person(){

    }
    public Person(String nama) {

        this.nama = nama;
    }

    public String getNama() {

        return nama;
    }

    public void setNama(String nama) {

        this.nama = nama;
    }

    public abstract void tampilInfo();
}