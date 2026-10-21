package com.example.soa.dangky;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "DANGKY", uniqueConstraints = @UniqueConstraint(columnNames = {"sinh_vien_id", "de_tai_id"}))
public class DangKy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sinh_vien_id", nullable = false)
    private Long sinhVienId;
    @Column(name = "de_tai_id", nullable = false)
    private Long deTaiId;
    @Column(name = "ma_sinh_vien", length = 30)
    private String maSinhVien;
    @Column(name = "ten_sinh_vien", length = 120)
    private String tenSinhVien;
    @Column(name = "ma_de_tai", length = 30)
    private String maDeTai;
    @Column(name = "ten_de_tai", length = 200)
    private String tenDeTai;
    @Column(name = "giang_vien_huong_dan", length = 120)
    private String giangVienHuongDan;
    @Column(nullable = false, length = 30)
    private String trangThai;
    private Double diemHuongDan;
    private Double diemPhanBien;
    private Double diemHoiDong;

    protected DangKy() {}

    public DangKy(Long sinhVienId, Long deTaiId, String trangThai) {
        this.sinhVienId = sinhVienId;
        this.deTaiId = deTaiId;
        this.trangThai = trangThai;
    }

    public Long getId() { return id; }
    public Long getSinhVienId() { return sinhVienId; }
    public void setSinhVienId(Long value) { sinhVienId = value; }
    public Long getDeTaiId() { return deTaiId; }
    public void setDeTaiId(Long value) { deTaiId = value; }
    public String getMaSinhVien() { return maSinhVien; }
    public void setMaSinhVien(String value) { maSinhVien = value; }
    public String getTenSinhVien() { return tenSinhVien; }
    public void setTenSinhVien(String value) { tenSinhVien = value; }
    public String getMaDeTai() { return maDeTai; }
    public void setMaDeTai(String value) { maDeTai = value; }
    public String getTenDeTai() { return tenDeTai; }
    public void setTenDeTai(String value) { tenDeTai = value; }
    public String getGiangVienHuongDan() { return giangVienHuongDan; }
    public void setGiangVienHuongDan(String value) { giangVienHuongDan = value; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String value) { trangThai = value; }
    public Double getDiemHuongDan() { return diemHuongDan; }
    public void setDiemHuongDan(Double value) { diemHuongDan = value; }
    public Double getDiemPhanBien() { return diemPhanBien; }
    public void setDiemPhanBien(Double value) { diemPhanBien = value; }
    public Double getDiemHoiDong() { return diemHoiDong; }
    public void setDiemHoiDong(Double value) { diemHoiDong = value; }
}