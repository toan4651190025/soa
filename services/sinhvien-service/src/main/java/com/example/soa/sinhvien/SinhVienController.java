package com.example.soa.sinhvien;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/sinhvien")
public class SinhVienController {
    private final SinhVienRepository repository;

    public SinhVienController(SinhVienRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SinhVien> findAll() {
        return repository.findAllByActiveTrue();
    }

    @GetMapping("/{id}")
    public SinhVien findById(@PathVariable Long id) {
        return repository.findById(id).filter(SinhVien::isActive)
                .orElseThrow(() -> notFound("Không tìm thấy sinh viên"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SinhVien create(@RequestBody SinhVienRequest request) {
        validate(request);
        if (repository.existsByMaSinhVien(request.maSinhVien().trim()) || repository.existsByEmail(request.email().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã sinh viên hoặc email đã được sử dụng");
        }
        return repository.save(new SinhVien(request.maSinhVien().trim(), request.hoTen().trim(),
                request.email().trim(), request.nganh().trim()));
    }

    @PutMapping("/{id}")
    public SinhVien update(@PathVariable Long id, @RequestBody SinhVienRequest request) {
        validate(request);
        SinhVien student = repository.findById(id).filter(SinhVien::isActive)
                .orElseThrow(() -> notFound("Không tìm thấy sinh viên"));
        if (repository.existsByMaSinhVienAndIdNot(request.maSinhVien().trim(), id)
                || repository.existsByEmailAndIdNot(request.email().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã sinh viên hoặc email đã được sử dụng");
        }
        student.setMaSinhVien(request.maSinhVien().trim());
        student.setHoTen(request.hoTen().trim());
        student.setEmail(request.email().trim());
        student.setNganh(request.nganh().trim());
        return repository.save(student);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        SinhVien student = repository.findById(id).filter(SinhVien::isActive)
                .orElseThrow(() -> notFound("Không tìm thấy sinh viên"));
        student.setActive(false);
        repository.save(student);
    }

    private void validate(SinhVienRequest request) {
        if (request == null || blank(request.maSinhVien()) || blank(request.hoTen())
                || blank(request.email()) || blank(request.nganh())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã sinh viên, họ tên, email và ngành là bắt buộc");
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }

    public record SinhVienRequest(String maSinhVien, String hoTen, String email, String nganh) {}
}