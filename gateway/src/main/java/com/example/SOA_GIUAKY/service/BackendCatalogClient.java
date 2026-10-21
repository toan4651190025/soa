package com.example.SOA_GIUAKY.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class BackendCatalogClient {
    private final RestClient students;
    private final RestClient topics;
    private final RestClient registrations;

    public BackendCatalogClient(@Value("${services.sinhvien.url}") String studentUrl,
                                @Value("${services.detai.url}") String topicUrl,
                                @Value("${services.dangky.url}") String registrationUrl,
                                @Value("${app.internal-key}") String internalKey,
                                @Value("${spring.profiles.active:}") String activeProfiles) {
        if (Arrays.asList(activeProfiles.split(",")).contains("production") && internalKey.length() < 32) {
            throw new IllegalStateException("INTERNAL_SERVICE_KEY must contain at least 32 characters in production");
        }
        students = client(studentUrl, internalKey);
        topics = client(topicUrl, internalKey);
        registrations = client(registrationUrl, internalKey);
    }

    public List<Student> students() {
        return getList(students, "/api/sinhvien", Student[].class, "Sinh viên");
    }

    public Student student(Long id) {
        return getOne(students, "/api/sinhvien/{id}", id, Student.class, "Sinh viên");
    }

    public List<Topic> topics() {
        return getList(topics, "/api/detai", Topic[].class, "Đề tài");
    }

    public List<Registration> registrations() {
        return getList(registrations, "/api/dangky", Registration[].class, "Đăng ký");
    }

    public Registration createRegistration(Long studentId, Long topicId) {
        try {
            Registration registration = registrations.post().uri("/api/dangky")
                    .body(new RegistrationRequest(studentId, topicId, "DANG_KY", null, null, null))
                    .retrieve().body(Registration.class);
            if (registration == null) throw new IllegalStateException("Registration service returned an empty response");
            return registration;
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(exception.getStatusCode(), "Dịch vụ Đăng ký từ chối yêu cầu", exception);
        } catch (RestClientException exception) {
            throw unavailable("Đăng ký", exception);
        }
    }

    public void deleteRegistration(Long id) {
        try {
            registrations.delete().uri("/api/dangky/{id}", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(exception.getStatusCode(), "Không thể hủy đăng ký", exception);
        } catch (RestClientException exception) {
            throw unavailable("Đăng ký", exception);
        }
    }

    private RestClient client(String baseUrl, String internalKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory)
                .defaultHeader("X-Internal-Service-Key", internalKey).build();
    }

    private <T> List<T> getList(RestClient client, String path, Class<T[]> type, String name) {
        try {
            T[] items = client.get().uri(path).retrieve().body(type);
            if (items == null) throw new IllegalStateException(name + " service returned an empty response");
            return Arrays.asList(items);
        } catch (RestClientException exception) {
            throw unavailable(name, exception);
        }
    }

    private <T> T getOne(RestClient client, String path, Long id, Class<T> type, String name) {
        try {
            T item = client.get().uri(path, id).retrieve().body(type);
            if (item == null) throw new IllegalStateException(name + " service returned an empty response");
            return item;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy " + name.toLowerCase(), exception);
            }
            throw unavailable(name, exception);
        } catch (RestClientException exception) {
            throw unavailable(name, exception);
        }
    }

    private ResponseStatusException unavailable(String name, Exception cause) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ " + name + " hiện không khả dụng", cause);
    }

    public record Student(Long id, String maSinhVien, String hoTen, String email, String nganh, boolean active) {}
    public record Topic(Long id, String maDeTai, String tenDeTai, String giangVienHuongDan,
                        Integer chiTieu, String moTa, boolean active) {}
    public record Registration(Long id, Long sinhVienId, String maSinhVien, String tenSinhVien,
                               Long deTaiId, String maDeTai, String tenDeTai, String giangVienHuongDan,
                               String trangThai, Double diemHuongDan,
                               Double diemPhanBien, Double diemHoiDong, Double diemTongKet, String ketQua) {}
    private record RegistrationRequest(Long sinhVienId, Long deTaiId, String trangThai,
                                       Double diemHuongDan, Double diemPhanBien, Double diemHoiDong) {}
}
