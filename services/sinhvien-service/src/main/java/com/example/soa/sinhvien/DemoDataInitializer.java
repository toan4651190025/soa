package com.example.soa.sinhvien;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!production")
public class DemoDataInitializer {
    @Bean
    CommandLineRunner seedStudents(SinhVienRepository repository) {
        return args -> {
            if (repository.count() != 0) return;
            repository.save(new SinhVien("SV2021001", "Nguyen Minh Anh", "minhanh@university.edu.vn", "Cong nghe phan mem"));
            repository.save(new SinhVien("SV2021002", "Tran Quoc Bao", "quocbao@university.edu.vn", "He thong thong tin"));
            repository.save(new SinhVien("SV2021003", "Le Thu Ha", "thuha@university.edu.vn", "An toan thong tin"));
        };
    }
}
