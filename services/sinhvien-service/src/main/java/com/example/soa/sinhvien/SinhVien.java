package com.example.soa.sinhvien;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SINHVIEN")
public class SinhVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String maSinhVien;
    @Column(nullable = false, length = 120)
    private String hoTen;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false, length = 100)
    private String nganh;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    protected SinhVien() {}

    public SinhVien(String maSinhVien, String hoTen, String email, String nganh) {
        this.maSinhVien = maSinhVien;
        this.hoTen = hoTen;
        this.email = email;
        this.nganh = nganh;
    }

    public Long getId() { return id; }
    public String getMaSinhVien() { return maSinhVien; }
    public void setMaSinhVien(String value) { maSinhVien = value; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String value) { hoTen = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getNganh() { return nganh; }
    public void setNganh(String value) { nganh = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
}