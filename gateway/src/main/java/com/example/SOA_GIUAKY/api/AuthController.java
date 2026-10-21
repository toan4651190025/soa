package com.example.SOA_GIUAKY.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import com.example.SOA_GIUAKY.entity.StudentAccount;
import com.example.SOA_GIUAKY.repository.StudentAccountRepository;
import com.example.SOA_GIUAKY.service.BackendCatalogClient;
import com.example.SOA_GIUAKY.service.PasswordHasher;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String CSRF = "adminCsrfToken";
    private final String username;
    private final String password;
    private final StudentAccountRepository accountRepository;
    private final BackendCatalogClient catalog;
    private final PasswordHasher passwordHasher;

    public AuthController(@Value("${app.admin.username}") String username,
                          @Value("${app.admin.password}") String password,
                          StudentAccountRepository accountRepository, BackendCatalogClient catalog,
                          PasswordHasher passwordHasher) {
        this.username = username;
        this.password = password;
        this.accountRepository = accountRepository;
        this.catalog = catalog;
        this.passwordHasher = passwordHasher;
    }

    @GetMapping("/session")
    public SessionResponse session(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String csrfToken = (String) session.getAttribute(CSRF);
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute(CSRF, csrfToken);
        }
        String role = (String) session.getAttribute("authenticatedRole");
        return new SessionResponse(role != null, csrfToken, role,
                (String) session.getAttribute("authenticatedUser"),
                (String) session.getAttribute("displayName"),
                (Long) session.getAttribute("studentId"));
    }

    @PostMapping("/login")
    public SessionResponse login(@RequestBody LoginRequest login, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        verifyCsrf(session, request);
        if (login == null || login.username() == null || login.password() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        String role;
        String displayName;
        Long studentId = null;
        if (constantTimeEquals(username, login.username())) {
            if (!constantTimeEquals(password, login.password())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng");
            }
            role = "ADMIN";
            displayName = "Quản trị viên";
        } else {
            List<BackendCatalogClient.Student> students = catalog.students();
            BackendCatalogClient.Student student = students.stream()
                    .filter(item -> constantTimeEquals(item.maSinhVien(), login.username()))
                    .findFirst().orElse(null);
            StudentAccount account = student == null ? null : accountRepository.findByStudentId(student.id()).orElse(null);
            boolean passwordMatches = account == null
                    ? passwordHasher.matchesDummy(login.password())
                    : passwordHasher.matches(login.password(), account.getPasswordHash());
            if (student == null || account == null || !account.isEnabled() || !passwordMatches) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng");
            }
            role = "STUDENT";
            studentId = student.id();
            displayName = student.hoTen();
        }
        request.changeSessionId();
        session = request.getSession(false);
        session.setAttribute("authenticatedRole", role);
        session.setAttribute("authenticatedUser", login.username());
        session.setAttribute("displayName", displayName);
        if (studentId != null) session.setAttribute("studentId", studentId);
        return new SessionResponse(true, (String) session.getAttribute(CSRF), role,
                login.username(), displayName, studentId);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        verifyCsrf(session, request);
        session.invalidate();
    }

    private void verifyCsrf(HttpSession session, HttpServletRequest request) {
        Object expected = session == null ? null : session.getAttribute(CSRF);
        String submitted = request.getHeader("X-CSRF-TOKEN");
        if (!(expected instanceof String token) || submitted == null || !constantTimeEquals(token, submitted)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CSRF token không hợp lệ hoặc đã hết hạn");
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return right != null && MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    public record LoginRequest(String username, String password) {}
    public record SessionResponse(boolean authenticated, String csrfToken, String role, String username,
                                  String displayName, Long studentId) {}
}