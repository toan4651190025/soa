package com.example.SOA_GIUAKY.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServiceProxyController {
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final Map<String, String> serviceUrls;
    private final String internalKey;

    public ServiceProxyController(@Value("${services.sinhvien.url}") String studentUrl,
                                  @Value("${services.detai.url}") String topicUrl,
                                  @Value("${services.dangky.url}") String registrationUrl,
                                  @Value("${app.internal-key}") String internalKey) {
        serviceUrls = Map.of("/api/sinhvien", studentUrl, "/api/detai", topicUrl, "/api/dangky", registrationUrl);
        this.internalKey = internalKey;
    }

    @RequestMapping(value = {
            "/sinhvien", "/sinhvien/{*path}",
            "/detai", "/detai/{*path}",
            "/dangky", "/dangky/{*path}"
    }, method = {org.springframework.web.bind.annotation.RequestMethod.GET,
            org.springframework.web.bind.annotation.RequestMethod.POST,
            org.springframework.web.bind.annotation.RequestMethod.PUT,
            org.springframework.web.bind.annotation.RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String serviceUrl = serviceUrls.entrySet().stream()
                .filter(entry -> path.equals(entry.getKey()) || path.startsWith(entry.getKey() + "/"))
                .map(Map.Entry::getValue).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dịch vụ"));
        String target = serviceUrl + path + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body);
        HttpRequest outgoing = HttpRequest.newBuilder(URI.create(target))
                .timeout(Duration.ofSeconds(15))
                .header("X-Internal-Service-Key", internalKey)
                .header("Content-Type", request.getContentType() == null ? MediaType.APPLICATION_JSON_VALUE : request.getContentType())
                .method(request.getMethod(), publisher)
                .build();
        try {
            HttpResponse<byte[]> result = httpClient.send(outgoing, HttpResponse.BodyHandlers.ofByteArray());
            ResponseEntity.BodyBuilder response = ResponseEntity.status(result.statusCode());
            result.headers().firstValue("content-type").ifPresent(value -> response.header(HttpHeaders.CONTENT_TYPE, value));
            if (result.statusCode() == HttpStatus.NO_CONTENT.value()) return response.build();
            return response.body(result.body());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ nghiệp vụ hiện không khả dụng", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Yêu cầu tới dịch vụ nghiệp vụ bị gián đoạn", exception);
        }
    }
}
