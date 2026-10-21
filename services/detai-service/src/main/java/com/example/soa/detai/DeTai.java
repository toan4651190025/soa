package com.example.soa.detai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DETAI")
public class DeTai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String maDeTai;
    @Column(nullable = false, length = 200)
    private String tenDeTai;
    @Column(nullable = false, length = 120)
    private String giangVienHuongDan;
    @Column(nullable = false)
    private Integer chiTieu;
    @Column(length = 2000)
    private String moTa;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    protected DeTai() {}

    public DeTai(String maDeTai, String tenDeTai, String giangVienHuongDan, Integer chiTieu, String moTa) {
        this.maDeTai = maDeTai;
        this.tenDeTai = tenDeTai;
        this.giangVienHuongDan = giangVienHuongDan;
        this.chiTieu = chiTieu;
        this.moTa = moTa;
    }

    public Long getId() { return id; }
    public String getMaDeTai() { return maDeTai; }
    public void setMaDeTai(String value) { maDeTai = value; }
    public String getTenDeTai() { return tenDeTai; }
    public void setTenDeTai(String value) { tenDeTai = value; }
    public String getGiangVienHuongDan() { return giangVienHuongDan; }
    public void setGiangVienHuongDan(String value) { giangVienHuongDan = value; }
    public Integer getChiTieu() { return chiTieu; }
    public void setChiTieu(Integer value) { chiTieu = value; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String value) { moTa = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
}