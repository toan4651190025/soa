package com.example.soa.dangky;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
@Component
public class ReferenceServices {
    private final RestClient studentClient;
    private final RestClient topicClient;

    public ReferenceServices(@Value("${services.sinhvien.url}") String studentUrl,
                             @Value("${services.detai.url}") String topicUrl,
                             @Value("${app.internal-key}") String internalKey) {
        studentClient = client(studentUrl, internalKey);
        topicClient = client(topicUrl, internalKey);
    }

    public StudentReference requireStudent(Long id) {
        try {
            StudentReference student = studentClient.get().uri("/api/sinhvien/{id}", id)
                    .retrieve().body(StudentReference.class);
            if (student == null) throw new IllegalStateException("Student service returned an empty response");
            return student;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên", exception);
            }
            throw unavailable("Sinh viên", exception);
        } catch (RestClientException exception) {
            throw unavailable("Sinh viên", exception);
        }
    }

    public TopicReference requireTopic(Long id) {
        try {
            TopicReference topic = topicClient.get().uri("/api/detai/{id}", id)
                    .retrieve().body(TopicReference.class);
            if (topic == null) throw new IllegalStateException("Topic service returned an empty response");
            return topic;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đề tài", exception);
            }
            throw unavailable("Đề tài", exception);
        } catch (RestClientException exception) {
            throw unavailable("Đề tài", exception);
        }
    }

    private ResponseStatusException unavailable(String service, Exception cause) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Không thể xác minh dữ liệu với dịch vụ " + service, cause);
    }

    private RestClient client(String baseUrl, String internalKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory)
                .defaultHeader("X-Internal-Service-Key", internalKey).build();
    }

    public record StudentReference(Long id, String maSinhVien, String hoTen, String email, String nganh) {}
    public record TopicReference(Long id, String maDeTai, String tenDeTai, String giangVienHuongDan,
                                 Integer chiTieu, String moTa) {}
}