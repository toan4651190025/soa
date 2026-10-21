package com.example.soa.sinhvien;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SinhVienRepository extends JpaRepository<SinhVien, Long> {
    java.util.List<SinhVien> findAllByActiveTrue();
    boolean existsByMaSinhVien(String maSinhVien);
    boolean existsByMaSinhVienAndIdNot(String maSinhVien, Long id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}