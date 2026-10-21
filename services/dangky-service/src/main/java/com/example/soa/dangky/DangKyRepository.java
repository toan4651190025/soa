package com.example.soa.dangky;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DangKyRepository extends JpaRepository<DangKy, Long> {
    boolean existsBySinhVienIdAndDeTaiId(Long sinhVienId, Long deTaiId);
    boolean existsBySinhVienId(Long sinhVienId);
    boolean existsByDeTaiId(Long deTaiId);
    long countByDeTaiId(Long deTaiId);
}