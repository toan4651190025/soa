package com.example.SOA_GIUAKY.api;

import com.example.SOA_GIUAKY.service.StudentAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final StudentAccountService accounts;

    public AdminAccountController(StudentAccountService accounts) { this.accounts = accounts; }

    @GetMapping
    public List<StudentAccountService.AccountResponse> findAll() { return accounts.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentAccountService.AccountResponse create(
            @RequestBody StudentAccountService.CreateAccountRequest request) {
        return accounts.create(request);
    }

    @PatchMapping("/{id}")
    public StudentAccountService.AccountResponse setEnabled(@PathVariable Long id, @RequestBody EnabledRequest request) {
        return accounts.setEnabled(id, request.enabled());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { accounts.delete(id); }

    public record EnabledRequest(boolean enabled) {}
}
