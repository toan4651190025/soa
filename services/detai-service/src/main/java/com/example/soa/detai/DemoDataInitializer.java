package com.example.soa.detai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!production")
public class DemoDataInitializer {
    @Bean
    CommandLineRunner seedTopics(DeTaiRepository repository) {
        return args -> {
            if (repository.count() != 0) return;
            repository.save(new DeTai("DT001", "Nen tang quan ly do an tot nghiep", "ThS. Pham Thu Trang", 4,
                    "Xay dung REST API va giao dien quan tri do an."));
            repository.save(new DeTai("DT002", "Ung dung nhan dien diem danh", "TS. Nguyen Van Long", 3,
                    "Thu nghiem mo hinh thi giac may tinh trong lop hoc."));
            repository.save(new DeTai("DT003", "He thong thu thap log tap trung", "ThS. Do Hoang Nam", 2,
                    "Phan tich log va canh bao su co he thong."));
        };
    }
}
