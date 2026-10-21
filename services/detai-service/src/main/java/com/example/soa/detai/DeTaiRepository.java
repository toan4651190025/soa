package com.example.soa.detai;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeTaiRepository extends JpaRepository<DeTai, Long> {
    java.util.List<DeTai> findAllByActiveTrue();
    boolean existsByMaDeTai(String maDeTai);
    boolean existsByMaDeTaiAndIdNot(String maDeTai, Long id);
}