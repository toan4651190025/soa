package com.example.SOA_GIUAKY.repository;

import com.example.SOA_GIUAKY.entity.StudentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAccountRepository extends JpaRepository<StudentAccount, Long> {
    Optional<StudentAccount> findByStudentId(Long studentId);
    List<StudentAccount> findAllByOrderByCreatedAtDesc();
}
