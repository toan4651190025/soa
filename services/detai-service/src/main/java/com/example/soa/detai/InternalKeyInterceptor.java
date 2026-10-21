package com.example.soa.detai;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Component
public class InternalKeyInterceptor implements HandlerInterceptor {
    private final String internalKey;

    public InternalKeyInterceptor(@Value("${app.internal-key}") String internalKey,
                                  @Value("${spring.profiles.active:}") String activeProfiles) {
        this.internalKey = internalKey;
        if (Arrays.asList(activeProfiles.split(",")).contains("production")
                && (internalKey.length() < 32 || internalKey.contains("replace-with"))) {
            throw new IllegalStateException("INTERNAL_SERVICE_KEY must contain at least 32 characters in production");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String provided = request.getHeader("X-Internal-Service-Key");
        if (provided != null && MessageDigest.isEqual(internalKey.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8))) return true;
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Internal service authentication required\"}");
        return false;
    }
}
