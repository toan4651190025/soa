package com.example.soa.detai;

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
@RequestMapping("/api/detai")
public class DeTaiController {
    private final DeTaiRepository repository;

    public DeTaiController(DeTaiRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DeTai> findAll() { return repository.findAllByActiveTrue(); }

    @GetMapping("/{id}")
    public DeTai findById(@PathVariable Long id) {
        return repository.findById(id).filter(DeTai::isActive)
                .orElseThrow(() -> notFound("Không tìm thấy đề tài"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeTai create(@RequestBody DeTaiRequest request) {
        validate(request);
        if (repository.existsByMaDeTai(request.maDeTai().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã đề tài đã được sử dụng");
        }
        return repository.save(toEntity(request));
    }

    @PutMapping("/{id}")
    public DeTai update(@PathVariable Long id, @RequestBody DeTaiRequest request) {
        validate(request);
        DeTai topic = repository.findById(id).filter(DeTai::isActive)
                .orElseThrow(() -> notFound("Không tìm thấy đề tài"));
        if (repository.existsByMaDeTaiAndIdNot(request.maDeTai().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã đề tài đã được sử dụng");
        }
        topic.setMaDeTai(request.maDeTai().trim());
        topic.setTenDeTai(request.tenDeTai().trim());
        topic.setGiangVienHuongDan(request.giangVienHuongDan().trim());
        topic.setChiTieu(request.chiTieu());
        topic.setMoTa(clean(request.moTa()));
        return repository.save(topic);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        DeTai topic = repository.findById(id).filter(DeTai::isActive)
                .orElseThrow(() -> notFound("Không tìm thấy đề tài"));
        topic.setActive(false);
        repository.save(topic);
    }

    private DeTai toEntity(DeTaiRequest request) {
        return new DeTai(request.maDeTai().trim(), request.tenDeTai().trim(), request.giangVienHuongDan().trim(),
                request.chiTieu(), clean(request.moTa()));
    }

    private void validate(DeTaiRequest request) {
        if (request == null || blank(request.maDeTai()) || blank(request.tenDeTai())
                || blank(request.giangVienHuongDan()) || request.chiTieu() == null || request.chiTieu() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã, tên, giảng viên và chỉ tiêu (tối thiểu 1) là bắt buộc");
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String clean(String value) { return value == null ? null : value.trim(); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }

    public record DeTaiRequest(String maDeTai, String tenDeTai, String giangVienHuongDan, Integer chiTieu, String moTa) {}
}