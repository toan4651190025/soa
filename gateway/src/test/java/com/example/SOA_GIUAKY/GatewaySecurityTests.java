package com.example.SOA_GIUAKY;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.admin.username=admin",
        "app.admin.password=admin123",
        "app.internal-key=test-internal-key"
})
class GatewaySecurityTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requiresAdminSessionForBusinessApi() throws Exception {
        mockMvc.perform(get("/api/sinhvien")).andExpect(status().isUnauthorized());

        AdminSession session = login();
        mockMvc.perform(get("/api/auth/session").session(session.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void requiresCsrfForLoginAndBusinessWritesAndLogout() throws Exception {
        MvcResult sessionResult = mockMvc.perform(get("/api/auth/session")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) sessionResult.getRequest().getSession(false);
        String token = objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("csrfToken").asText();

        mockMvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());

        AdminSession authenticated = login();
        mockMvc.perform(post("/api/sinhvien").session(authenticated.session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maSinhVien":"SV","hoTen":"Test","email":"test@example.com","nganh":"CNTT"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout").session(authenticated.session())
                        .header("X-CSRF-TOKEN", authenticated.csrfToken()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/sinhvien").session(authenticated.session()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void enforcesRoleBoundariesForStudentPortalAndAdminApis() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("authenticatedRole", "ADMIN");
        mockMvc.perform(get("/api/portal/profile").session(adminSession))
                .andExpect(status().isForbidden());

        MockHttpSession studentSession = new MockHttpSession();
        studentSession.setAttribute("authenticatedRole", "STUDENT");
        studentSession.setAttribute("studentId", 999L);
        mockMvc.perform(get("/api/sinhvien").session(studentSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/accounts").session(studentSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/portal/profile").session(studentSession))
                .andExpect(status().isUnauthorized());
    }

    private AdminSession login() throws Exception {
        MvcResult sessionResult = mockMvc.perform(get("/api/auth/session")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) sessionResult.getRequest().getSession(false);
        String token = objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("csrfToken").asText();
        mockMvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
        return new AdminSession(session, token);
    }

    private record AdminSession(MockHttpSession session, String csrfToken) {}
}
