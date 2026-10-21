package com.example.SOA_GIUAKY.service;

import com.example.SOA_GIUAKY.entity.StudentAccount;
import com.example.SOA_GIUAKY.repository.StudentAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentAccountService {
    private final StudentAccountRepository repository;
    private final BackendCatalogClient catalog;
    private final PasswordHasher passwordHasher;

    public StudentAccountService(StudentAccountRepository repository, BackendCatalogClient catalog,
                                 PasswordHasher passwordHasher) {
        this.repository = repository;
        this.catalog = catalog;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        Map<Long, BackendCatalogClient.Student> students = catalog.students().stream()
                .collect(Collectors.toMap(BackendCatalogClient.Student::id, Function.identity()));
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(account -> toResponse(account, students.get(account.getStudentId())))
                .toList();
    }

    public AccountResponse create(CreateAccountRequest request) {
        if (request == null || request.studentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sinh viên là bắt buộc");
        }
        validatePassword(request.password());
        BackendCatalogClient.Student student = catalog.student(request.studentId());
        if (repository.findByStudentId(student.id()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sinh viên đã có tài khoản");
        }
        StudentAccount account = repository.save(new StudentAccount(student.id(), passwordHasher.hash(request.password())));
        return toResponse(account, student);
    }

    public AccountResponse setEnabled(Long id, boolean enabled) {
        StudentAccount account = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        account.setEnabled(enabled);
        return toResponse(repository.save(account), catalog.student(account.getStudentId()));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản");
        }
        repository.deleteById(id);
    }

    public void changePassword(Long studentId, ChangePasswordRequest request) {
        if (request == null || request.currentPassword() == null || request.newPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại và mật khẩu mới là bắt buộc");
        }
        validatePassword(request.newPassword());
        StudentAccount account = repository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không còn hoạt động"));
        if (!account.isEnabled() || !passwordHasher.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu hiện tại không đúng");
        }
        account.setPasswordHash(passwordHasher.hash(request.newPassword()));
        repository.save(account);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 10 || password.length() > 128) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu phải dài từ 10 đến 128 ký tự");
        }
    }

    private AccountResponse toResponse(StudentAccount account, BackendCatalogClient.Student student) {
        return new AccountResponse(account.getId(), account.getStudentId(),
                student == null ? "(sinh viên không còn tồn tại)" : student.maSinhVien(),
                student == null ? "—" : student.hoTen(),
                account.isEnabled(), account.getCreatedAt());
    }

    public record CreateAccountRequest(Long studentId, String password) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
    public record AccountResponse(Long id, Long studentId, String maSinhVien, String hoTen,
                                  boolean enabled, java.time.Instant createdAt) {}
}
