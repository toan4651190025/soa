package com.example.soa.dangky;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class DangKyService {
    private static final List<String> STATUSES = List.of("DANG_KY", "DANG_THUC_HIEN", "DA_HOAN_THANH");
    private final DangKyRepository repository;
    private final ReferenceServices references;

    public DangKyService(DangKyRepository repository, ReferenceServices references) {
        this.repository = repository;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public List<ApiModels.DangKyResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApiModels.DangKyResponse findById(Long id) { return toResponse(requireRegistration(id)); }

    public synchronized ApiModels.DangKyResponse create(ApiModels.DangKyRequest request) {
        validate(request);
        ReferenceServices.StudentReference student = references.requireStudent(request.sinhVienId());
        ReferenceServices.TopicReference topic = references.requireTopic(request.deTaiId());
        if (repository.existsBySinhVienIdAndDeTaiId(student.id(), topic.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sinh viên đã đăng ký đề tài này");
        }
        if (repository.countByDeTaiId(topic.id()) >= topic.chiTieu()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đề tài đã đủ chỉ tiêu");
        }
        DangKy registration = new DangKy(student.id(), topic.id(), status(request.trangThai()));
        applyReferences(registration, student, topic);
        applyScores(registration, request);
        return toResponse(repository.save(registration));
    }

    public synchronized ApiModels.DangKyResponse update(Long id, ApiModels.DangKyRequest request) {
        validate(request);
        DangKy registration = requireRegistration(id);
        ReferenceServices.StudentReference student = references.requireStudent(request.sinhVienId());
        ReferenceServices.TopicReference topic = references.requireTopic(request.deTaiId());
        if (repository.existsBySinhVienIdAndDeTaiId(student.id(), topic.id())
                && !(registration.getSinhVienId().equals(student.id()) && registration.getDeTaiId().equals(topic.id()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sinh viên đã đăng ký đề tài này");
        }
        if (!registration.getDeTaiId().equals(topic.id()) && repository.countByDeTaiId(topic.id()) >= topic.chiTieu()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đề tài đã đủ chỉ tiêu");
        }
        registration.setSinhVienId(student.id());
        registration.setDeTaiId(topic.id());
        applyReferences(registration, student, topic);
        registration.setTrangThai(status(request.trangThai()));
        applyScores(registration, request);
        return toResponse(repository.save(registration));
    }

    public synchronized void delete(Long id) { repository.delete(requireRegistration(id)); }

    @Transactional(readOnly = true)
    private DangKy requireRegistration(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký"));
    }

    private void validate(ApiModels.DangKyRequest request) {
        if (request == null || request.sinhVienId() == null || request.deTaiId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sinhVienId và deTaiId là bắt buộc");
        }
        validateScore(request.diemHuongDan());
        validateScore(request.diemPhanBien());
        validateScore(request.diemHoiDong());
        if (request.trangThai() != null && !request.trangThai().isBlank()
                && !STATUSES.contains(request.trangThai().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái đăng ký không hợp lệ");
        }
    }

    private void validateScore(Double score) {
        if (score != null && (!Double.isFinite(score) || score < 0 || score > 10)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Điểm phải nằm trong khoảng từ 0 đến 10");
        }
    }

    private String status(String value) { return value == null || value.isBlank() ? "DANG_KY" : value.trim().toUpperCase(); }

    private void applyScores(DangKy registration, ApiModels.DangKyRequest request) {
        registration.setDiemHuongDan(request.diemHuongDan());
        registration.setDiemPhanBien(request.diemPhanBien());
        registration.setDiemHoiDong(request.diemHoiDong());
    }

    private void applyReferences(DangKy registration, ReferenceServices.StudentReference student,
                                 ReferenceServices.TopicReference topic) {
        registration.setMaSinhVien(student.maSinhVien());
        registration.setTenSinhVien(student.hoTen());
        registration.setMaDeTai(topic.maDeTai());
        registration.setTenDeTai(topic.tenDeTai());
        registration.setGiangVienHuongDan(topic.giangVienHuongDan());
    }

    private ApiModels.DangKyResponse toResponse(DangKy registration) {
        Double total = null;
        String result = "CHUA_CHAM";
        if (registration.getDiemHuongDan() != null && registration.getDiemPhanBien() != null
                && registration.getDiemHoiDong() != null) {
            total = BigDecimal.valueOf(registration.getDiemHuongDan() * 0.3
                            + registration.getDiemPhanBien() * 0.3 + registration.getDiemHoiDong() * 0.4)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            result = total >= 5 ? "DAT" : "KHONG_DAT";
        }
        return new ApiModels.DangKyResponse(registration.getId(), registration.getSinhVienId(),
                registration.getMaSinhVien(), registration.getTenSinhVien(), registration.getDeTaiId(),
                registration.getMaDeTai(), registration.getTenDeTai(), registration.getGiangVienHuongDan(),
                registration.getTrangThai(), registration.getDiemHuongDan(),
                registration.getDiemPhanBien(), registration.getDiemHoiDong(), total, result);
    }

}
