package com.example.SOA_GIUAKY.api;

import com.example.SOA_GIUAKY.service.BackendCatalogClient;
import com.example.SOA_GIUAKY.service.StudentAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/portal")
public class StudentPortalController {
    private final BackendCatalogClient catalog;
    private final StudentAccountService accounts;

    public StudentPortalController(BackendCatalogClient catalog, StudentAccountService accounts) {
        this.catalog = catalog;
        this.accounts = accounts;
    }

    @GetMapping("/profile")
    public Profile profile(HttpServletRequest request) {
        return new Profile(studentId(request), (String) request.getSession(false).getAttribute("authenticatedUser"),
                (String) request.getSession(false).getAttribute("displayName"));
    }

    @GetMapping("/topics")
    public List<AvailableTopic> topics() {
        List<BackendCatalogClient.Registration> registrations = catalog.registrations();
        return catalog.topics().stream().map(topic -> {
            long registered = registrations.stream().filter(row -> topic.id().equals(row.deTaiId())).count();
            return new AvailableTopic(topic.id(), topic.maDeTai(), topic.tenDeTai(), topic.giangVienHuongDan(),
                    topic.chiTieu(), topic.moTa(), registered, Math.max(0, topic.chiTieu() - registered));
        }).toList();
    }

    @GetMapping("/registrations")
    public List<BackendCatalogClient.Registration> registrations(HttpServletRequest request) {
        Long studentId = studentId(request);
        return catalog.registrations().stream().filter(row -> studentId.equals(row.sinhVienId())).toList();
    }

    @PostMapping("/registrations")
    public BackendCatalogClient.Registration register(@RequestBody RegistrationRequest request,
                                                       HttpServletRequest servletRequest) {
        if (request == null || request.deTaiId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đề tài là bắt buộc");
        }
        return catalog.createRegistration(studentId(servletRequest), request.deTaiId());
    }

    @DeleteMapping("/registrations/{id}")
    public void cancel(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = studentId(request);
        BackendCatalogClient.Registration registration = catalog.registrations().stream()
                .filter(row -> id.equals(row.id()) && studentId.equals(row.sinhVienId()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký của bạn"));
        catalog.deleteRegistration(registration.id());
    }

    @PutMapping("/password")
    public void changePassword(@RequestBody StudentAccountService.ChangePasswordRequest request,
                               HttpServletRequest servletRequest) {
        accounts.changePassword(studentId(servletRequest), request);
    }

    private Long studentId(HttpServletRequest request) {
        Object studentId = request.getSession(false).getAttribute("studentId");
        if (!(studentId instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chức năng này chỉ dành cho sinh viên");
        }
        return id;
    }

    public record Profile(Long studentId, String maSinhVien, String hoTen) {}
    public record RegistrationRequest(Long deTaiId) {}
    public record AvailableTopic(Long id, String maDeTai, String tenDeTai, String giangVienHuongDan,
                                 Integer chiTieu, String moTa, long daDangKy, long conCho) {}
}
