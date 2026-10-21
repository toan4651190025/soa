package com.example.soa.dangky;

public final class ApiModels {
    private ApiModels() {}

    public record DangKyRequest(Long sinhVienId, Long deTaiId, String trangThai,
                                Double diemHuongDan, Double diemPhanBien, Double diemHoiDong) {}

    public record DangKyResponse(Long id, Long sinhVienId, String maSinhVien, String tenSinhVien,
                                 Long deTaiId, String maDeTai, String tenDeTai, String giangVienHuongDan,
                                 String trangThai, Double diemHuongDan,
                                 Double diemPhanBien, Double diemHoiDong, Double diemTongKet, String ketQua) {}
}
