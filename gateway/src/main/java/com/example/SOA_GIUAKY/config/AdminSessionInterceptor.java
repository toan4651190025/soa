package com.example.SOA_GIUAKY.config;

import com.example.SOA_GIUAKY.entity.StudentAccount;
import com.example.SOA_GIUAKY.repository.StudentAccountRepository;
import com.example.SOA_GIUAKY.service.BackendCatalogClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AdminSessionInterceptor implements HandlerInterceptor {
    private final StudentAccountRepository accountRepository;
    private final BackendCatalogClient catalog;

    public AdminSessionInterceptor(StudentAccountRepository accountRepository, BackendCatalogClient catalog) {
        this.accountRepository = accountRepository;
        this.catalog = catalog;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        String role = session == null ? null : (String) session.getAttribute("authenticatedRole");
        if (role == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
            return false;
        }
        String path = request.getRequestURI();
        boolean portalPath = path.startsWith("/api/portal/");
        boolean student = "STUDENT".equals(role);
        if ((portalPath && !student) || (!portalPath && !"ADMIN".equals(role))) {
            writeError(response, HttpStatus.FORBIDDEN, "Tài khoản không có quyền thực hiện thao tác này");
            return false;
        }
        if (portalPath) {
            Object studentId = session.getAttribute("studentId");
            StudentAccount account = studentId instanceof Long id
                    ? accountRepository.findByStudentId(id).orElse(null) : null;
            if (account == null || !account.isEnabled()) {
                session.invalidate();
                writeError(response, HttpStatus.UNAUTHORIZED, "Tài khoản sinh viên đã bị khóa hoặc không còn tồn tại");
                return false;
            }
            try {
                catalog.student((Long) studentId);
            } catch (ResponseStatusException exception) {
                if (exception.getStatusCode().value() != HttpStatus.NOT_FOUND.value()) throw exception;
                session.invalidate();
                writeError(response, HttpStatus.UNAUTHORIZED, "Hồ sơ sinh viên không còn hoạt động");
                return false;
            }
        }
        if (!HttpMethod.GET.matches(request.getMethod()) && !HttpMethod.HEAD.matches(request.getMethod())
                && !HttpMethod.OPTIONS.matches(request.getMethod())) {
            Object expected = session.getAttribute("adminCsrfToken");
            String actual = request.getHeader("X-CSRF-TOKEN");
            if (!(expected instanceof String token) || actual == null
                    || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))) {
                writeError(response, HttpStatus.FORBIDDEN, "CSRF token không hợp lệ hoặc đã hết hạn");
                return false;
            }
        }
        return true;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
